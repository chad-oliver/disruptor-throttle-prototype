package com.xyz.distruptor;

import com.xyz.distruptor.event.TransactionEvent;

/**
 * Created by coliver on 11/19/15.
 */
public interface EventQueue {
    TransactionEvent getEvent();

    void addEvent(TransactionEvent transactionEvent);
}
