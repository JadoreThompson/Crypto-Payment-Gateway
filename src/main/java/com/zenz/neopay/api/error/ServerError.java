package com.zenz.neopay.api.error;

public class ServerError extends RuntimeException {
    public ServerError(String message) {
        super(message);
    }
}
