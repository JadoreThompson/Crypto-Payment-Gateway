package com.zenz.crypto_payment_gateway.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.zenz.crypto_payment_gateway.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    User findByEmail(String email);
}
