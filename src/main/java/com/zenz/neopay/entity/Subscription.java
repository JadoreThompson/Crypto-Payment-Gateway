package com.zenz.neopay.entity;

import com.zenz.neopay.enums.SubscriptionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "subscriptions")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID subscriptionId;

    @Column(name="merchant_id" , nullable = false, updatable = false)
    private UUID merchantId;

    @Column(name="customer_id", nullable = false, updatable = false)
    private UUID customerId;

    @Column(name="product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name="price_id",nullable = false, updatable = false)
    private UUID priceId;

    @Positive
    private int quantity;

    private SubscriptionStatus status;

    @Column(updatable = false)
    private long startedAt;

    @Column(nullable = false, updatable = false)
    private long createdAt;

    // Operations

    @PrePersist
    public void prePersist() {
        createdAt = System.currentTimeMillis();
    }
}