package com.zenz.crypto_payment_gateway.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "products")
public class Product {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID productId;

    @Column(name="merchant_id", nullable = false, updatable = false)
    private UUID merchantId;

    @Column(nullable = false)
    private String name;

    private String description;

    private String image;

    @Column(nullable = false)
    private String walletAddress;

    @Column(nullable = false, updatable = false)
    private long createdAt;

    // Operations
    @PrePersist
    public void prePersist() {
        createdAt = System.currentTimeMillis();
    }
}