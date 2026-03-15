package com.zenz.neopay.api.route.auth.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    @Email(message = "Invalid email")
    private String email;

    @NotBlank
    private String password;
}
