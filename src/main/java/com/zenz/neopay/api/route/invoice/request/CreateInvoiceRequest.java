package com.zenz.neopay.api.route.invoice.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class CreateInvoiceRequest {
    @NotNull
    @Min(1)
    private long amountDue;

    @NotBlank
    private String currency;

    @NotNull
    private UUID customerId;

    private Map<String, ?> metadata;
}