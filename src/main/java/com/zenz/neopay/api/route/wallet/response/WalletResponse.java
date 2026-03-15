package com.zenz.neopay.api.route.wallet.response;

import lombok.Data;

import java.util.UUID;

@Data
public class WalletResponse {
    private UUID walletId;
    private long balance;
    private String currency;
    private String walletAddress;
}