package com.zenz.neopay.api.route.merchant.response;

import lombok.Data;

import java.util.UUID;

@Data
public class MerchantResponse {
    private UUID merchantId;
    private String name;
    private String description;
    private long createdAt;
}
