package com.zenz.crypto_payment_gateway.api.route.withdrawal.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateWithdrawalRequest {
    @Min(1)
    private long amount;

    @NotBlank
    private String currency;

    @NotBlank
    private String chain;

    private UUID merchantId;
}