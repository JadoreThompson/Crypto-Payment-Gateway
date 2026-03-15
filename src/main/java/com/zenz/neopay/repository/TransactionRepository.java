package com.zenz.neopay.repository;

import com.zenz.neopay.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByInvoiceId(UUID invoiceId);

    Transaction findByTransactionIdAndInvoiceId(UUID id, UUID invoiceId);

    List<Transaction> findByMerchantId(UUID merchantId);

    Transaction findByTransactionIdAndMerchantId(UUID id, UUID merchantId);

    Transaction findByTransactionIdAndInvoiceIdAndMerchantId(UUID id, UUID invoiceId, UUID merchantId);
}
