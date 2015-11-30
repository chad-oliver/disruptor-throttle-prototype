package com.xyz.distruptor;

import com.xyz.ASCII;
import com.xyz.accountstore.model.Transaction;
import com.xyz.distruptor.event.TransactionEvent;
import com.xyz.distruptor.handler.GenericExceptionHandler;
import com.xyz.distruptor.handler.JournalTransactionHandler;
import com.xyz.distruptor.handler.PostTransactionHandler;
import com.xyz.distruptor.handler.ReplicateTransactionHandler;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionProcessor implements EventQueue {
    private static final Logger logger = LoggerFactory.getLogger(TransactionProcessor.class);

    private final ExecutorService EXECUTOR = Executors.newFixedThreadPool(App.THREAD_POOL_SIZE);
    private Disruptor disruptor;
    private RingBuffer ringBuffer;
    private ArrayBlockingQueue<TransactionEvent> blockingQueue = new ArrayBlockingQueue<TransactionEvent>(App.BLOCK_QUEUE_SIZE);

    JournalTransactionHandler journal;
    ReplicateTransactionHandler replicate;
    PostTransactionHandler post;

    public TransactionProcessor() {

    }
    
    public void postTransaction(Transaction transaction) {
        disruptor.publishEvent(new TransactionEventPublisher(transaction));
    }

    public int getBlockingQueueSize() {
        return this.blockingQueue.size();
    }

    public void init() {
        disruptor = new Disruptor<TransactionEvent>(
                TransactionEvent.EVENT_FACTORY, 
                App.RING_BUFFER_SIZE,
                EXECUTOR,
                ProducerType.SINGLE,
                new YieldingWaitStrategy());
        
        // Pretend that we have real journaling, just to demo it...
        File journalDir = new File("target/test");
        journalDir.mkdirs();
        File journalFile = new File(journalDir, "test-journal.txt");
        
        // In this example start fresh each time - though a real implementation
        // might roll over the journal or the like.
        if (journalFile.exists()) {
            journalFile.delete(); 
        }

        journal = new JournalTransactionHandler(journalFile);
        replicate = new ReplicateTransactionHandler();
        // this magic starts here.  passing a reference to this class, which holds an instance of an
        // ArrayBlockingQueue
        post = new PostTransactionHandler(this);

        // This is where the magic happens 
        disruptor.handleEventsWith(journal, replicate).then(post);
        
        // We don't do any fancy exception handling in this demo
        ExceptionHandler exh = new GenericExceptionHandler();
        disruptor.handleExceptionsFor(journal).with(exh);
        disruptor.handleExceptionsFor(replicate).with(exh);
        disruptor.handleExceptionsFor(post).with(exh);

        ringBuffer = disruptor.start();
    }

    public TransactionEvent getEvent() {
        TransactionEvent te = null;

        try {
            logger.debug(ASCII.ANSI_CYAN + " [BLOCKING QUEUE] size=" + this.blockingQueue.size() + ASCII.ANSI_RESET);
            te =  this.blockingQueue.take();
        } catch(InterruptedException e) {
        }

        return te;
    }

    public void addEvent(TransactionEvent transactionEvent) {
        try {
            this.blockingQueue.put(transactionEvent);
        } catch(InterruptedException e) {
        }
    }

    public void destroy() {
        try {
            journal.closeJournal();
        } catch (Exception ignored) {}
        
        try {
            disruptor.shutdown();
        } catch (Exception ignored) {}
        
        EXECUTOR.shutdownNow();
    }

    /**
     *
     */
    class TransactionEventPublisher implements EventTranslator<TransactionEvent> {
        private Transaction transaction;
        
        public TransactionEventPublisher(Transaction transaction) {
            this.transaction = transaction;
        }

        public void translateTo(TransactionEvent event, long sequence) {
            event.setTransaction(transaction);
            event.setBufferSeq(sequence); // We don't really use this, just demonstrating its availability
        }
    }
}
