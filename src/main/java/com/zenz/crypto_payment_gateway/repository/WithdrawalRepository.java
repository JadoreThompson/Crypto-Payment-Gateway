package com.zenz.crypto_payment_gateway.repository;

import com.zenz.crypto_payment_gateway.entity.Withdrawal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WithdrawalRepository extends JpaRepository<Withdrawal, UUID> {

    List<Withdrawal> findByMerchantId(UUID merchantId);

    Withdrawal findByIdAndMerchantId(UUID id, UUID merchantId);
}