package com.zenz.neopay.api.route.product;

import com.zenz.neopay.api.route.product.request.CreateProductRequest;
import com.zenz.neopay.api.route.product.request.UpdateProductRequest;
import com.zenz.neopay.entity.Product;
import com.zenz.neopay.entity.User;
import com.zenz.neopay.service.MerchantService;
import com.zenz.neopay.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/merchants/{merchantId}/product/")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService  productService;
    private final MerchantService merchantService;

    @PostMapping("/")
    public ResponseEntity<?> createProduct(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId,
            @Valid @RequestBody CreateProductRequest body
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        Product product = productService.createProduct(body, merchantId);
        return ResponseEntity.ok(productService.toResponse(product));
    }

    @GetMapping("/{productId}/")
    public ResponseEntity<?> getProduct(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId,
            @PathVariable UUID productId
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        Product product = productService.getProductsByIdAndMerchantId(productId, merchantId);
        return ResponseEntity.ok(productService.toResponse(product));
    }

    @GetMapping("/")
    public ResponseEntity<?> getProducts(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        List<Product> products = productService.getProductsByMerchantId(merchantId);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{productId}/")
    public ResponseEntity<Product> updateProduct(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId,
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateProductRequest body
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        Product product = productService.getProductsByIdAndMerchantId(productId, merchantId);
        product = productService.updateProduct(product, body);
        return ResponseEntity.ok(product);
    }
}
