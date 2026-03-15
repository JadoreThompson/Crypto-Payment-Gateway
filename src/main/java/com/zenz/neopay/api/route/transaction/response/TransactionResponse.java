package com.zenz.neopay.api.route.transaction.response;

import com.zenz.neopay.enums.TransactionStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class TransactionResponse {
    private UUID transactionId;
    private UUID invoiceId;
    private long amountExpected;
    private long amountReceived;
    private String currency;
    private String chain;
    private String senderWalletAddress;
    private String recipientWalletAddress;
    private String txnAddress;
    private TransactionStatus status;
    private long createdAt;
    private long completedAt;
}