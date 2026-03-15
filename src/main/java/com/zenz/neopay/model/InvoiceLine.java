package com.zenz.neopay.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Embeddable
public class InvoiceLine {
    @Min(1)
    protected long amount;

    protected String description;
}
