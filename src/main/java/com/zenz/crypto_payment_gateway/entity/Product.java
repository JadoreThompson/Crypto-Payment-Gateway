package com.zenz.crypto_payment_gateway.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Table(name = "products")
public class Product {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private String walletAddress;

    private String image;

    @Column(nullable = false, updatable = false)
    private long createdAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    // Operations
    @PrePersist
    public void prePersist() {
        createdAt = System.currentTimeMillis();
    }
}
