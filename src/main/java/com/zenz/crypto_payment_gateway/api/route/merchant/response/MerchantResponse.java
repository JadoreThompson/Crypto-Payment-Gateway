package com.zenz.crypto_payment_gateway.api.route.merchant.response;

import lombok.Data;

import java.util.UUID;

@Data
public class MerchantResponse {
    private UUID merchantId;
    private String name;
    private String description;
    private long createdAt;
}
