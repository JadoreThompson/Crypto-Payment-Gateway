package com.zenz.crypto_payment_gateway.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class InvoiceLine {
    private long amount;

    private String description;
}
