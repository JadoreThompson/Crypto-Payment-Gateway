package com.zenz.neopay.api.route.customer.response;

import lombok.Data;

import java.util.UUID;

@Data
public class CustomerResponse {
    private UUID customerId;
    private String nickname;
    private String email;
    private String merchantId;
    private long createdAt;
}