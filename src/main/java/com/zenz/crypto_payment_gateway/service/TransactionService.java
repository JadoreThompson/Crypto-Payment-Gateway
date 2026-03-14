package com.zenz.crypto_payment_gateway.service;

import com.zenz.crypto_payment_gateway.api.error.ResourceNotFound;
import com.zenz.crypto_payment_gateway.api.route.transaction.response.TransactionResponse;
import com.zenz.crypto_payment_gateway.entity.Transaction;
import com.zenz.crypto_payment_gateway.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public Transaction getTransactionById(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFound(
                        String.format("Failed to find transaction with id %s", transactionId)
                ));
    }

    public Transaction getTransactionByIdAndInvoiceId(UUID transactionId, UUID invoiceId) {
        Transaction transaction = transactionRepository.findByTransactionIdAndInvoiceId(transactionId, invoiceId);
        if (transaction == null) {
            throw new ResourceNotFound(
                    String.format("Failed to find transaction with id %s for invoice id %s", transactionId, invoiceId)
            );
        }
        return transaction;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getTransactionsByInvoiceId(UUID invoiceId) {
        return transactionRepository.findByInvoiceId(invoiceId);
    }

    public List<Transaction> getTransactionsByMerchantId(UUID merchantId) {
        return transactionRepository.findByMerchantId(merchantId);
    }

    public Transaction getTransactionByIdAndMerchantId(UUID transactionId, UUID merchantId) {
        Transaction transaction = transactionRepository.findByTransactionIdAndMerchantId(transactionId, merchantId);
        if (transaction == null) {
            throw new ResourceNotFound(
                    String.format("Failed to find transaction with id %s for merchant", transactionId)
            );
        }
        return transaction;
    }

    public Transaction getTransactionByIdAndInvoiceIdAndMerchantId(UUID transactionId, UUID invoiceId, UUID merchantId) {
        Transaction transaction = transactionRepository.findByTransactionIdAndInvoiceIdAndMerchantId(transactionId, invoiceId, merchantId);
        if (transaction == null) {
            throw new ResourceNotFound(
                    String.format("Failed to find transaction with id %s for invoice id %s and merchant", transactionId, invoiceId)
            );
        }
        return transaction;
    }

    public TransactionResponse toResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setTransactionId(transaction.getTransactionId());
        response.setInvoiceId(transaction.getInvoiceId());
        response.setAmountExpected(transaction.getAmountExpected());
        response.setAmountReceived(transaction.getAmountReceived());
        response.setCurrency(transaction.getCurrency());
        response.setChain(transaction.getChain());
        response.setTxnAddress(transaction.getTxnAddress());
        response.setSenderWalletAddress(transaction.getSenderWalletAddress());
        response.setRecipientWalletAddress(transaction.getRecipientWalletAddress());
        response.setStatus(transaction.getStatus());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setCompletedAt(transaction.getCompletedAt());
        
        return response;
    }
}