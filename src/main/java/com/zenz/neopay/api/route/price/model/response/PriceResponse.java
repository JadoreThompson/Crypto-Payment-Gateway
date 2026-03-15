package com.zenz.neopay.api.route.price.model.response;

import com.zenz.neopay.enums.PricingType;
import com.zenz.neopay.model.Recurring;
import lombok.Data;

import java.util.UUID;

@Data
public class PriceResponse {
    private UUID priceId;
    private long amount;
    private PricingType pricingType;
    private String currency;
    private UUID productId;
    private Recurring recurring;
    private String metadata;
}
