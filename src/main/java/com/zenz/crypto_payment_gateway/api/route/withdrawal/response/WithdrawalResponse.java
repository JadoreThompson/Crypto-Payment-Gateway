package com.zenz.crypto_payment_gateway.api.route.withdrawal.response;

import com.zenz.crypto_payment_gateway.enums.WithdrawalStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class WithdrawalResponse {
    private UUID withdrawalId;
    private long amount;
    private String currency;
    private String chain;
    private WithdrawalStatus status;
    private long createdAt;
    private long completedAt;
}