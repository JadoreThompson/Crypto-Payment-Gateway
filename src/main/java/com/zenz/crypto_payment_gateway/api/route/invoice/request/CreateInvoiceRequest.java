package com.zenz.crypto_payment_gateway.api.route.invoice.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class CreateInvoiceRequest {
    @NotNull
    private long amountDue;

    @NotNull
    private String currency;

    @NotNull
    private String customerId;

    private Map<String, ?> metadata;
}