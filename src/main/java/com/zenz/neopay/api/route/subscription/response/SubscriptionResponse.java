package com.zenz.neopay.api.route.subscription.response;

import com.zenz.neopay.enums.SubscriptionStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class SubscriptionResponse {
    private UUID subscriptionId;
    private int quantity;
    private SubscriptionStatus status;
    private UUID customerId;
    private UUID productId;
    private UUID priceId;
    private long startedAt;
    private long createdAt;
}