package com.zenz.neopay.api.route.product.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class ProductResponse {
    private UUID productId;
    private UUID merchantId;
    private String name;
    private String description;
    private String image;
    private long createdAt;
}