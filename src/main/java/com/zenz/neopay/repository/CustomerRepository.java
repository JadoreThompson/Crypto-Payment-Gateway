package com.zenz.neopay.repository;

import com.zenz.neopay.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    List<Customer> findByMerchantId(UUID merchantId);

    Customer findByCustomerIdAndMerchantId(UUID id, UUID merchantId);
}