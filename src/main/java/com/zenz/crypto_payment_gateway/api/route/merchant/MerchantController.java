package com.zenz.crypto_payment_gateway.api.route.merchant;

import com.zenz.crypto_payment_gateway.api.route.merchant.request.CreateMerchantRequest;
import com.zenz.crypto_payment_gateway.api.route.merchant.request.UpdateMerchantRequest;
import com.zenz.crypto_payment_gateway.api.route.merchant.response.MerchantResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/merchant/")
public class MerchantController {
    @PostMapping("/")
    public void createMerchant(@RequestBody CreateMerchantRequest body) {}

    @GetMapping("/{merchantId}/")
    public MerchantResponse getMerchant(@PathVariable String merchantId) {return null;}

    @GetMapping("/")
    public List<MerchantResponse> getMerchants() {return null;}

    @PutMapping("/{merchantId}/")
    public MerchantResponse updateMerchant(@RequestBody UpdateMerchantRequest body, @PathVariable String merchantId)
    {
        return null;
    }

}
