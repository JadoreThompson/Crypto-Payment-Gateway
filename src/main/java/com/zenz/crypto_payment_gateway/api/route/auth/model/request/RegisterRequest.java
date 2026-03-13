package com.zenz.crypto_payment_gateway.api.route.auth.model.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @AssertTrue(message = "Password length must be greater than or equal to 8 characters")
    private boolean checkPasswordLength() {
        return password.length() >= 8;
    }

    @AssertTrue(message = "Password must have at least 2 upper case characters")
    private boolean checkPasswordUpperCase() {
        final int minUpperCase = 2;
        int count = 0;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                count++;
                if (count >= minUpperCase) break;
            }
        }

        return count >= minUpperCase;
    }
}
