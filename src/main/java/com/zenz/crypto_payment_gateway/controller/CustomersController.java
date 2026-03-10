package com.zenz.crypto_payment_gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
public class CustomersController {

    @GetMapping(path="/")
    public String getCustomers() {return "list of customers";}
}
