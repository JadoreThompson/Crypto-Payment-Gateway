package com.zenz.neopay.api.model.response;

import lombok.Data;


@Data
public class SimpleErrorDetail implements   ErrorDetail {
    private final String message;
}
