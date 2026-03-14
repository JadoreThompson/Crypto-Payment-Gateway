package com.zenz.crypto_payment_gateway.entity;

import com.zenz.crypto_payment_gateway.enums.InvoiceStatus;
import com.zenz.crypto_payment_gateway.model.InvoiceLine;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "invoices")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID invoiceId;

    @Column(name="merchant_id" , nullable = false, updatable = false)
    private UUID merchantId;

    @Column(name="customer_id" , nullable = false, updatable = false)
    private UUID customerId;

    private long amountDue;

    private long amountPaid;

    private String currency;

    private int attempts;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<InvoiceLine> lines;

    private String metadata;

    private InvoiceStatus status;

    private long createdAt;

    // Operations

    @PrePersist
    public void prePersist() {
        createdAt = System.currentTimeMillis();
    }
}
