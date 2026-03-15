package com.zenz.neopay.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.zenz.neopay.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    User findByEmail(String email);
}
