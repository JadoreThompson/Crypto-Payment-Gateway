package com.zenz.neopay.api.route.price;

import com.zenz.neopay.api.route.price.model.request.CreatePriceRequest;
import com.zenz.neopay.api.route.price.model.request.UpdatePriceRequest;
import com.zenz.neopay.api.route.price.model.response.PriceResponse;
import com.zenz.neopay.entity.Price;
import com.zenz.neopay.entity.User;
import com.zenz.neopay.service.MerchantService;
import com.zenz.neopay.service.PriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/merchants/{merchantId}/prices/")
@RequiredArgsConstructor
public class PriceController {

    private final PriceService priceService;
    private final MerchantService merchantService;

    @PostMapping("/")
    public ResponseEntity<PriceResponse> createPrice(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId,
            @Valid @RequestBody CreatePriceRequest body
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        Price price = priceService.createPrice(merchantId, body);
        return ResponseEntity.ok(priceService.toResponse(price));
    }

    @GetMapping("/{priceId}/")
    public ResponseEntity<PriceResponse> getPrice(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId,
            @PathVariable UUID priceId
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        Price price = priceService.getPriceById(priceId);
        return ResponseEntity.ok(priceService.toResponse(price));
    }

    @GetMapping("/")
    public ResponseEntity<List<PriceResponse>> getPrices(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        List<Price> prices = priceService.getPricesByMerchantId(merchantId);
        List<PriceResponse> responses = prices.stream()
                .map(priceService::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{priceId}/")
    public ResponseEntity<PriceResponse> updatePrice(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId,
            @PathVariable UUID priceId,
            @RequestBody UpdatePriceRequest body
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        Price price = priceService.updatePrice(priceId, body);
        return ResponseEntity.ok(priceService.toResponse(price));
    }
}