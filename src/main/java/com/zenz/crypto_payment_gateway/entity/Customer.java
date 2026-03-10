package com.zenz.crypto_payment_gateway.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="customers")
public class Customer {
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    private String customerId;

    @Column(nullable = false)
    private String nickname;

    private String email;

    private byte[] metadata;

    @Column(nullable = false, updatable = false)
    private long createdAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    public void prePersist() {
        this.createdAt = System.currentTimeMillis();
    }
}
