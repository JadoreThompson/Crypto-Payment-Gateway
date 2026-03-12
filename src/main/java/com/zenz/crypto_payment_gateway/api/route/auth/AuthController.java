package com.zenz.crypto_payment_gateway.api.route.user;

import com.zenz.crypto_payment_gateway.api.route.user.model.request.LoginRequest;
import com.zenz.crypto_payment_gateway.api.route.user.model.request.RegisterRequest;
import com.zenz.crypto_payment_gateway.entity.User;
import com.zenz.crypto_payment_gateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;

    @PostMapping("/register/")
    public void register(@RequestBody RegisterRequest body) {
        User user = new User();
        user.setEmail(body.getEmail());
        user.setPassword(body.getPassword());
        userRepository.save(user);
    }

    @PostMapping("/login/")
    public void login(@RequestBody LoginRequest body) {}

    @GetMapping("/me/")
    public void getMe() {}

    @PostMapping("/logout/")
    public void logout() {}
}
