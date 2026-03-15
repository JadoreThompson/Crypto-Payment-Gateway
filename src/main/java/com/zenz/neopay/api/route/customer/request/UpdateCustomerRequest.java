package com.zenz.neopay.api.route.customer.request;

import lombok.Data;

@Data
public class UpdateCustomerRequest {
    private String nickname;
    private String email;
}