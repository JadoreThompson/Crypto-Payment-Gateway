package com.zenz.crypto_payment_gateway.api.route.user.model.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;

    private String password;
}
