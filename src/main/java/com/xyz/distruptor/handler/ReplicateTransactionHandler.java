package com.xyz.distruptor.handler;

import com.xyz.distruptor.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;

/**
 * In the real world, this handler would replicate the transaction event to in-memory 
 * date stores running on one or more other nodes as part of a redundancy strategy.
 */
public class ReplicateTransactionHandler extends AbstractEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(ReplicateTransactionHandler.class);

    @Override
    public void onEvent(TransactionEvent event, long sequence, boolean endOfBatch) throws Exception {

        try {
            Thread.sleep(new SecureRandom().nextInt(100));
        } catch(InterruptedException e) { }

        logger.debug("REPLICATE -> {}", event.getTransaction().toString());
    }
}
