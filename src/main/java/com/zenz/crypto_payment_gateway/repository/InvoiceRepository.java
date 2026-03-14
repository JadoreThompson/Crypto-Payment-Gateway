package com.zenz.crypto_payment_gateway.repository;

import com.zenz.crypto_payment_gateway.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findByCustomerId(String customerId);

    List<Invoice> findByMerchantId(UUID merchantId);

    Invoice findByInvoiceIdAndMerchantId(UUID id, UUID merchantId);
}