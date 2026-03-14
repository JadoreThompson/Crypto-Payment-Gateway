package com.zenz.crypto_payment_gateway.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "wallets")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID walletId;

    @Column(name="merchant_id", nullable = false, updatable = false)
    private UUID merchantId;

    @Column(nullable = false)
    private long balance;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String walletAddress;
}
