package com.zenz.crypto_payment_gateway.service.blockchain.watcher;


import com.zenz.crypto_payment_gateway.service.blockchain.event.TransactionCreatedEvent;
import com.zenz.crypto_payment_gateway.service.blockchain.event.TransactionExecutedEvent;

public interface WatcherService {

    void start();

    void stop();

    void handleTransactionCreated(TransactionCreatedEvent event);

    void handleTransactionExecuted(TransactionExecutedEvent event);
}
