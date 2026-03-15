package com.zenz.neopay.repository;

import com.zenz.neopay.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    List<Wallet> findByMerchantId(UUID merchantId);

    Wallet findByWalletIdAndMerchantId(UUID id, UUID merchantId);
}