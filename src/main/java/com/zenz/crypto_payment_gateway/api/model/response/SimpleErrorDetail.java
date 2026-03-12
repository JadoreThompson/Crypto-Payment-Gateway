package com.zenz.crypto_payment_gateway.api.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;


@Data
public class SimpleErrorDetail implements   ErrorDetail {
    private final String message;
}
