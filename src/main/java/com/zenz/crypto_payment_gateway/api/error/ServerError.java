package com.zenz.crypto_payment_gateway.api.error;

public class ServerError extends RuntimeException {
    public ServerError(String message) {
        super(message);
    }
}
