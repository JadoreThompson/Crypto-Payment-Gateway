package com.zenz.crypto_payment_gateway.api.route.merchant.response;

import lombok.Data;

@Data
public class MerchantResponse {
    private String merchantId;
    private String name;
    private String description;
    private long createdAt;
}
