package com.zenz.neopay.service;

import com.zenz.neopay.api.error.ResourceNotFound;
import com.zenz.neopay.api.route.merchant.request.CreateMerchantRequest;
import com.zenz.neopay.api.route.merchant.request.UpdateMerchantRequest;
import com.zenz.neopay.api.route.merchant.response.MerchantResponse;
import com.zenz.neopay.entity.Merchant;
import com.zenz.neopay.entity.User;
import com.zenz.neopay.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantService {
    
    private final MerchantRepository merchantRepository;
    
    public Merchant createMerchant(User user, CreateMerchantRequest request) {
        Merchant merchant = new Merchant();
        merchant.setUserId(user.getUserId());
        merchant.setName(request.getName());
        merchant.setDescription(request.getDescription());

        return merchantRepository.save(merchant);
    }
    
    public Merchant getMerchantById(UUID merchantId) {
        return merchantRepository.findById(merchantId).orElse(null);
    }
    
    public Merchant getMerchantByIdAndUserId(UUID merchantId, UUID userId) {
        Merchant merchant = merchantRepository.findByMerchantIdAndUserId(merchantId, userId).orElse(null);
        if (merchant == null) {
            throw new ResourceNotFound(
                    String.format("Failed to find merchant with id %s for user", merchantId)
            );
        }
        return merchant;
    }
    
//    public List<Merchant> getMerchantsByUser(User user) {
//        return merchantRepository.findByUser(user);
//    }

    public List<Merchant> getMerchantsByUserId(UUID userId) {
        return merchantRepository.findByUserId(userId);
    }
    
    public Merchant updateMerchant(UUID merchantId, UpdateMerchantRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId).orElse(null);
        
        if (merchant == null) {
            return null;
        }
        
        if (request.getName() != null) {
            merchant.setName(request.getName());
        }
        if (request.getDescription() != null) {
            merchant.setDescription(request.getDescription());
        }
        
        return merchantRepository.save(merchant);
    }
    
    public MerchantResponse toResponse(Merchant merchant) {
        MerchantResponse response = new MerchantResponse();
        response.setMerchantId(merchant.getMerchantId());
        response.setName(merchant.getName());
        response.setDescription(merchant.getDescription());
        response.setCreatedAt(merchant.getCreatedAt());
        return response;
    }
}