package com.zenz.crypto_payment_gateway.api.error;

public class ResourceNotFound extends RuntimeException {
    public ResourceNotFound(String message) {
        super(message);
    }
}
