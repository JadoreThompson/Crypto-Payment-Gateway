package com.zenz.crypto_payment_gateway.api.route.auth.model.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
