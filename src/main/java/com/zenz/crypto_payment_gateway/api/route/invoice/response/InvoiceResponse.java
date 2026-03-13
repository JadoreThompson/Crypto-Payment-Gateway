package com.zenz.crypto_payment_gateway.api.route.invoice.response;

import com.zenz.crypto_payment_gateway.enums.InvoiceStatus;
import com.zenz.crypto_payment_gateway.model.InvoiceLine;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class InvoiceResponse {
    private UUID invoiceId;
    private UUID customerId;
    private long amountDue;
    private long amountPaid;
    private String currency;
    private int attempts;
    private List<InvoiceLine> lines;
    private Map<String, ?> metadata;
    private InvoiceStatus status;
    private long createdAt;
}