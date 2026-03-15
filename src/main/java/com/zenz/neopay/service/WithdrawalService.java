package com.zenz.neopay.service;

import com.zenz.neopay.api.error.ResourceNotFound;
import com.zenz.neopay.api.route.withdrawal.request.CreateWithdrawalRequest;
import com.zenz.neopay.api.route.withdrawal.response.WithdrawalResponse;
import com.zenz.neopay.entity.Withdrawal;
import com.zenz.neopay.enums.WithdrawalStatus;
import com.zenz.neopay.repository.WithdrawalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WithdrawalService {

    private final WithdrawalRepository withdrawalRepository;

    public Withdrawal createWithdrawal(UUID merchantId, CreateWithdrawalRequest request) {
        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setMerchantId(merchantId);
        withdrawal.setAmount(request.getAmount());
        withdrawal.setCurrency(request.getCurrency());
        withdrawal.setChain(request.getChain());
        withdrawal.setStatus(WithdrawalStatus.PENDING);
        
        return withdrawalRepository.save(withdrawal);
    }

    public Withdrawal getWithdrawalById(UUID withdrawalId) {
        return withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new ResourceNotFound(
                        String.format("Failed to find withdrawal with id %s", withdrawalId)
                ));
    }

    public Withdrawal getWithdrawalByIdAndMerchantId(UUID withdrawalId, UUID merchantId) {
        Withdrawal withdrawal = withdrawalRepository.findByWalletIdAndMerchantId(withdrawalId, merchantId);
        if (withdrawal == null) {
            throw new ResourceNotFound(
                    String.format("Failed to find withdrawal with id %s for merchant", withdrawalId)
            );
        }
        return withdrawal;
    }

    public List<Withdrawal> getWithdrawalsByMerchantId(UUID merchantId) {
        return withdrawalRepository.findByMerchantId(merchantId);
    }

    public WithdrawalResponse toResponse(Withdrawal withdrawal) {
        WithdrawalResponse response = new WithdrawalResponse();
        response.setWithdrawalId(withdrawal.getWithdrawalId());
        response.setAmount(withdrawal.getAmount());
        response.setCurrency(withdrawal.getCurrency());
        response.setChain(withdrawal.getChain());
        response.setStatus(withdrawal.getStatus());
        response.setCreatedAt(withdrawal.getCreatedAt());
        response.setCompletedAt(withdrawal.getCompletedAt());
        
        return response;
    }
}