package com.zenz.crypto_payment_gateway.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name="customers")
public class Customer {
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    private UUID customerId;

    @Column(name="merchant_id" , nullable = false, updatable = false)
    private UUID merchantId;

    @Column(nullable = false)
    private String nickname;

    private String email;

    private byte[] metadata;

    @Column(nullable = false, updatable = false)
    private long createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = System.currentTimeMillis();
    }
}
