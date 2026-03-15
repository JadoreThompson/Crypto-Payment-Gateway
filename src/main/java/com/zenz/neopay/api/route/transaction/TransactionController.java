package com.zenz.neopay.api.route.transaction;

import com.zenz.neopay.api.route.transaction.response.TransactionResponse;
import com.zenz.neopay.entity.Transaction;
import com.zenz.neopay.entity.User;
import com.zenz.neopay.service.MerchantService;
import com.zenz.neopay.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/merchants/{merchantId}/transactions/")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final MerchantService merchantService;

    @GetMapping("/")
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId,
            @RequestParam(value = "invoiceId") UUID invoiceId
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        List<Transaction> transactions = transactionService.getTransactionsByInvoiceId(invoiceId);
        return ResponseEntity.ok(transactions.stream().map(transactionService::toResponse).toList());
    }

    @GetMapping("/{transactionId}/")
    public ResponseEntity<TransactionResponse> getTransaction(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId,
            @PathVariable UUID transactionId
    ) {
        System.out.println("Merchant Id: " + merchantId);
        System.out.println("Transaction Id: " + transactionId);

        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        Transaction transaction = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(transactionService.toResponse(transaction));
    }
}
