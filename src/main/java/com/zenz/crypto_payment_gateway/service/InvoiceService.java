package com.zenz.crypto_payment_gateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenz.crypto_payment_gateway.api.error.ResourceNotFound;
import com.zenz.crypto_payment_gateway.api.error.ServerError;
import com.zenz.crypto_payment_gateway.api.route.invoice.request.CreateInvoiceRequest;
import com.zenz.crypto_payment_gateway.api.route.invoice.response.InvoiceResponse;
import com.zenz.crypto_payment_gateway.entity.Customer;
import com.zenz.crypto_payment_gateway.entity.Invoice;
import com.zenz.crypto_payment_gateway.enums.InvoiceStatus;
import com.zenz.crypto_payment_gateway.repository.CustomerRepository;
import com.zenz.crypto_payment_gateway.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Invoice createInvoice(UUID merchantId, CreateInvoiceRequest request) {
        Customer customer = customerRepository.findByIdAndMerchantId(
                UUID.fromString(request.getCustomerId()), 
                merchantId
        );
        
        if (customer == null) {
            throw new ResourceNotFound(
                    String.format("Failed to find customer with id %s for merchant", request.getCustomerId())
            );
        }

        Invoice invoice = new Invoice();
        invoice.setCustomerId(customer.getCustomerId());
        invoice.setCustomer(customer);
        invoice.setAmountDue(request.getAmountDue());
        invoice.setCurrency(request.getCurrency());
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setAmountPaid(0);
        invoice.setAttempts(0);
        
        // Convert metadata map to JSON string
        if (request.getMetadata() != null) {
            try {
                invoice.setMetadata(objectMapper.writeValueAsString(request.getMetadata()));
            } catch (JsonProcessingException e) {
                invoice.setMetadata(null);
            }
        }
        
        return invoiceRepository.save(invoice);
    }

    public Invoice getInvoiceById(UUID invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFound(
                        String.format("Failed to find invoice with id %s", invoiceId)
                ));
    }

    public Invoice getInvoiceByIdAndMerchantId(UUID invoiceId, UUID merchantId) {
        Invoice invoice = invoiceRepository.findByIdAndMerchantId(invoiceId, merchantId);
        if (invoice == null) {
            throw new ResourceNotFound(
                    String.format("Failed to find invoice with id %s for merchant", invoiceId)
            );
        }
        return invoice;
    }

    public List<Invoice> getInvoicesByMerchantId(UUID merchantId) {
        return invoiceRepository.findByMerchantId(merchantId);
    }

    public List<Invoice> getInvoicesByCustomerId(UUID customerId) {
        return invoiceRepository.findByCustomerId(customerId.toString());
    }

    public void deleteInvoice(UUID invoiceId) {
        Invoice invoice = getInvoiceById(invoiceId);
        invoiceRepository.delete(invoice);
    }

    public InvoiceResponse toResponse(Invoice invoice) {
        InvoiceResponse response = new InvoiceResponse();
        response.setInvoiceId(invoice.getInvoiceId());
        response.setCustomerId(invoice.getCustomerId());
        response.setAmountDue(invoice.getAmountDue());
        response.setAmountPaid(invoice.getAmountPaid());
        response.setCurrency(invoice.getCurrency());
        response.setAttempts(invoice.getAttempts());
        response.setLines(invoice.getLines());
        if (invoice.getMetadata() != null) {
            try {
                response.setMetadata(objectMapper.readValue(invoice.getMetadata(), new TypeReference<Map<String, ?>>() {}));
            } catch (JsonProcessingException e) {
                throw new ServerError("Failed to deserialize invoice metadata");
            }
        }
        response.setStatus(invoice.getStatus());
        response.setCreatedAt(invoice.getCreatedAt());
        
        return response;
    }
}