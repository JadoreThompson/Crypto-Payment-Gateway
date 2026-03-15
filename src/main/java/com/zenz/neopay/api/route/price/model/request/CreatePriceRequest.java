package com.zenz.neopay.api.route.price.model.request;

import com.zenz.neopay.api.route.price.model.validation.ValidateIsRecurring;
import com.zenz.neopay.enums.PricingType;
import com.zenz.neopay.model.Recurring;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
@ValidateIsRecurring
public class CreatePriceRequest {
    @Min(value = 1)
    private long amount;

    @NotNull
    private PricingType pricingType;

    @NotNull
    private String currency;

    @NotNull
    private UUID productId;

    private Recurring recurring;
}