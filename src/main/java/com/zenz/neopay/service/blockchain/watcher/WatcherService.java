package com.zenz.neopay.service.blockchain.watcher;


import com.zenz.neopay.service.blockchain.event.TransactionCreatedEvent;
import com.zenz.neopay.service.blockchain.event.TransactionExecutedEvent;

public interface WatcherService {

    void start();

    void stop();

    void handleTransactionCreated(TransactionCreatedEvent event);

    void handleTransactionExecuted(TransactionExecutedEvent event);
}
