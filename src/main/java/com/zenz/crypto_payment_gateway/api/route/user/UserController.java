package com.zenz.crypto_payment_gateway.api.route.user;

import com.zenz.crypto_payment_gateway.api.route.user.model.request.LoginRequest;
import com.zenz.crypto_payment_gateway.api.route.user.model.request.RegisterRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/")
public class UserController {
    @PostMapping("/register/")
    public void register(@RequestBody RegisterRequest body) {}

    @PostMapping("/login/")
    public void login(@RequestBody LoginRequest body) {}

    @GetMapping("/me/")
    public void getMe() {}

    @PostMapping("/logout/")
    public void logout() {}
}
