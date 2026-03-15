package com.zenz.neopay.model;

import com.zenz.neopay.enums.PricingInterval;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Embeddable
public class Recurring {
    @NotNull
    @Enumerated(EnumType.STRING)
    private PricingInterval intervalType;

    @NotNull
    @Min(1)
    private int intervalCount;
}
