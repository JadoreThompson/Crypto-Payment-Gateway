package com.zenz.neopay.model;

import com.zenz.neopay.entity.Price;

import java.util.UUID;


public class SubscriptionItem extends InvoiceLine {

    private UUID subscriptionItemId;

    private UUID subscriptionId;

    private Price price;

    private int quantity;
}
