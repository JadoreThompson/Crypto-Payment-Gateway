package com.zenz.crypto_payment_gateway.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "merchants")
public class Merchant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID merchantId;

    @Column(name="user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, updatable = false)
    private long createdAt;

    // Operations

    @PrePersist
    public void prePersist() {
        this.createdAt = System.currentTimeMillis();
    }
}