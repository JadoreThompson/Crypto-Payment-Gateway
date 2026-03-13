package com.zenz.crypto_payment_gateway.repository;

import com.zenz.crypto_payment_gateway.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByInvoiceId(UUID invoiceId);

    Transaction findByIdAndInvoiceId(UUID id, UUID invoiceId);

    List<Transaction> findByMerchantId(UUID merchantId);

    Transaction findByIdAndMerchantId(UUID id, UUID merchantId);

    Transaction findByIdAndInvoiceIdAndMerchantId(UUID id, UUID invoiceId, UUID merchantId);
}
