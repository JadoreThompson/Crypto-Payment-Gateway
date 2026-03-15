package com.zenz.neopay.enums;

public enum PricingType {
    ONE_TIME,
    RECURRING;

    @Override
    public String toString() {
        return name();
    }
}
