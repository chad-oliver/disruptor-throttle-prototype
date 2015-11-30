package com.xyz.distruptor;

import com.xyz.ASCII;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.xyz.accountstore.model.Transaction;
import java.security.SecureRandom;
import java.util.Date;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    /**
     * THIS ARE ALL THE CONSTANTS USED BY THIS DEMO
     */
    public static final int NUM_XYZS_TO_CREATE = 1000;  // how many XYZs should this demo 'create'
    public static final int BLOCK_QUEUE_SIZE = 10; // what is the size of the ArrayBlockingQueue
    public static final int THREAD_POOL_SIZE = 5; // how many threads should be used by the Disruptor
    public static final int RING_BUFFER_SIZE = 4; // what is the size of the ring buffer
    public static final int MAX_PAUSE_BETWEEN_EXTERNAL_REQUEST = 10; // fake pause when reading generated XYZs

    /**
     * The mainline of the demo
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        App app = new App();
        app.multiThreadUpdatesTest();
    }
    
    public void multiThreadUpdatesTest() throws InterruptedException {
        TransactionProcessor processor = new TransactionProcessor();
        processor.init();

        FakeProcessorWorker fakeProcessorWorker = new FakeProcessorWorker(processor);

        Thread t1 = new Thread(fakeProcessorWorker);
        t1.start();

        // Thread t2 = new Thread(new FakeProcessorWorker(processor));
        //  t2.start();

        // we want to fake a request from outside of the LMAX Disruptor
        Thread t3 = new Thread(new FakeExternalRequest(processor, fakeProcessorWorker));
        t3.start();

        // Wait for the transactions to filter through, of course you would
        // usually have the transaction processor lifecycle managed by a 
        // container or in some other more sophisticated way...
        do {
            try {
                Thread.sleep(2000);
            } catch (Exception ignored) {
            }
        } while (fakeProcessorWorker.isAlive());

        processor.destroy();

        logger.debug(ASCII.ANSI_BLUE + "[EXECUTION DONE]" + ASCII.ANSI_RESET);
    }

    /**
     * Lets fake putting real work on the ring buffer....
     */
    class FakeProcessorWorker implements Runnable {
        private TransactionProcessor tp;
        private boolean alive = true;

        public FakeProcessorWorker(TransactionProcessor tp) {
            this.tp = tp;
        }
        
        public void run() {
            for (int i = 0; i < NUM_XYZS_TO_CREATE; i++) {
                Transaction tx = new Transaction(new Date(), Integer.toString(i));

                logger.debug(ASCII.ANSI_GREEN + "[PROCESSOR LOAD] " + tx.toString() + ASCII.ANSI_RESET);
                tp.postTransaction(tx);
            }

            alive = false;
        }

        public boolean isAlive() {
            return alive;
        }
    }

    /**
     *  This is the hook into the system, which can be used to stop & start the creation of TPANs
     */
    class FakeExternalRequest implements Runnable {
        private TransactionProcessor tp;
        private FakeProcessorWorker fakeProcessorWorker;

        public FakeExternalRequest(TransactionProcessor tp, FakeProcessorWorker fakeProcessorWorker) {
            this.tp = tp;
            this.fakeProcessorWorker = fakeProcessorWorker;
        }

        public void run() {
            do {
                try {
                    // we need a fake pause
                    Thread.sleep(new SecureRandom().nextInt(MAX_PAUSE_BETWEEN_EXTERNAL_REQUEST));
                    // get data from the ArrayBlockingQueue -- I NEED A generated TPAN
                    this.tp.getEvent();
                } catch(InterruptedException e) {
                }
            } while(true);
        }
    }
}
