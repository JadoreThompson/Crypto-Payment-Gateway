package com.zenz.neopay.repository;

import com.zenz.neopay.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByMerchantId(UUID merchantId);

    Product findByProductIdAndMerchantId(UUID id, UUID merchantId);
}
