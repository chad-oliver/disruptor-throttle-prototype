package com.xyz.distruptor.handler;

import com.xyz.ASCII;
import com.xyz.distruptor.EventQueue;
import com.xyz.distruptor.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class PostTransactionHandler extends AbstractEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(PostTransactionHandler.class);
    private EventQueue callback;

    public PostTransactionHandler(EventQueue tp) {
        this.callback =  tp;
    }
    
    @Override
    public void onEvent(TransactionEvent event, long sequence, boolean endOfBatch) throws Exception {
        logger.debug(ASCII.ANSI_RED + "POSTED TRANSACTION [QUEUE PUT] " + event.getTransaction().toString() + ASCII.ANSI_RESET);
        callback.addEvent(event);
        logger.debug(ASCII.ANSI_YELLOW + "POSTED TRANSACTION [QUEUE PUT-DONE] " + event.getTransaction().toString() + ASCII.ANSI_RESET);
    }
}