package com.zenz.neopay.service.blockchain.event;

import java.util.UUID;

public record TransactionExecutedEvent(
        String transactionKey,
        UUID transactionId,
        String sender,
        String recipient,
        String token,
        long amount
) {
}