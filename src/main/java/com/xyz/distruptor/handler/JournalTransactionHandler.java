package com.xyz.distruptor.handler;

import com.xyz.distruptor.event.TransactionEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JournalTransactionHandler extends AbstractEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(JournalTransactionHandler.class);
    private FileWriter journal;
    
    public JournalTransactionHandler(File journalFile) {
        try {
            this.journal = new FileWriter(journalFile, true);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    public void closeJournal() throws IOException {
        if (journal!=null) {
            journal.flush();
            journal.close();
        }
    }

    @Override
    public void onEvent(TransactionEvent event, long sequence, boolean endOfBatch) throws Exception {
        journal.write(event.asJournalEntry());
        journal.flush();
        logger.debug("JOURNALED TRANSACTION -> {}", event.getTransaction().toString());
    }
}
