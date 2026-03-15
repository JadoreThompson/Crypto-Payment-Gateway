package com.zenz.neopay.repository;

import com.zenz.neopay.entity.Withdrawal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WithdrawalRepository extends JpaRepository<Withdrawal, UUID> {

    List<Withdrawal> findByMerchantId(UUID merchantId);

    Withdrawal findByWalletIdAndMerchantId(UUID id, UUID merchantId);
}