package com.zenz.crypto_payment_gateway.repository;

import com.zenz.crypto_payment_gateway.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PriceRepository extends JpaRepository<Price, UUID> {

    public List<Price> findByMerchantId(UUID merchantId);

    public List<Price> findByMerchantIdAndProductId(UUID merchantId, UUID productId);

    public List<Price> findByProductId(UUID productId);

    public Price findByPriceIdAndProductId(UUID id, UUID productId);
}
