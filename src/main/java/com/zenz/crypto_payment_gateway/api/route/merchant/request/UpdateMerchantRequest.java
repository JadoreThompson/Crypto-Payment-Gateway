package com.zenz.crypto_payment_gateway.api.route.merchant.request;

import lombok.Data;

@Data
public class UpdateMerchantRequest {
    private String name;
    private String description;
}
