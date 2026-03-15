package com.zenz.crypto_payment_gateway.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Embeddable
public class InvoiceLine {
    @Min(1)
    private long amount;

    private String description;
}
