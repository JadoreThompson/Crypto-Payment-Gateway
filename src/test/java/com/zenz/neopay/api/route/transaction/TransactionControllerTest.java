package com.zenz.neopay.api.route.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenz.neopay.api.GlobalExceptionHandler;
import com.zenz.neopay.api.config.JWTAuthenticationFilter;
import com.zenz.neopay.api.config.SecurityConfig;
import com.zenz.neopay.api.error.ResourceNotFound;
import com.zenz.neopay.api.route.transaction.response.TransactionResponse;
import com.zenz.neopay.entity.Merchant;
import com.zenz.neopay.entity.Transaction;
import com.zenz.neopay.entity.User;
import com.zenz.neopay.enums.TransactionStatus;
import com.zenz.neopay.repository.UserRepository;
import com.zenz.neopay.service.JWTService;
import com.zenz.neopay.service.MerchantService;
import com.zenz.neopay.service.TransactionService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@WebMvcTest(TransactionController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private MerchantService merchantService;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private ObjectMapper objectMapper;
    private User testUser;
    private Merchant testMerchant;
    private Transaction testTransaction;
    private String testToken;
    private UUID testMerchantId;
    private UUID testInvoiceId;
    private UUID testTransactionId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test-token";
        testMerchantId = UUID.randomUUID();
        testInvoiceId = UUID.randomUUID();
        testTransactionId = UUID.randomUUID();

        testUser = new User();
        testUser.setUserId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");

        testMerchant = new Merchant();
        testMerchant.setMerchantId(testMerchantId);
        testMerchant.setName("Test Merchant");
        testMerchant.setCreatedAt(System.currentTimeMillis());

        testTransaction = new Transaction();
        testTransaction.setTransactionId(testTransactionId);
        testTransaction.setMerchantId(testMerchantId);
        testTransaction.setInvoiceId(testInvoiceId);
        testTransaction.setAmountExpected(1000000L);
        testTransaction.setAmountReceived(1000000L);
        testTransaction.setCurrency("USDT");
        testTransaction.setChain("ETH");
        testTransaction.setTxnAddress("0x1234567890abcdef1234567890abcdef12345678");
        testTransaction.setSenderWalletAddress("0xabcdef1234567890abcdef1234567890abcdef12");
        testTransaction.setRecipientWalletAddress("0x9876543210fedcba9876543210fedcba98765432");
        testTransaction.setStatus(TransactionStatus.SUCCESS);
        testTransaction.setCreatedAt(System.currentTimeMillis());
        testTransaction.setCompletedAt(System.currentTimeMillis());
    }

    /**
     * Helper method to set up authentication mocks
     */
    private void setupAuthentication() {
        Mockito.when(jwtService.isTokenValid(testToken)).thenReturn(true);
        Mockito.when(jwtService.extractUserId(testToken)).thenReturn(Optional.of(testUser.getUserId().toString()));
        Mockito.when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
    }

    /**
     * Helper method to create a TransactionResponse object.
     */
    private TransactionResponse createTransactionResponse(Transaction transaction) {
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

    // GET TRANSACTIONS BY INVOICE ID ENDPOINT TESTS

    @Test
    @DisplayName("Get Transactions: Should return transactions for valid invoice ID")
    void getTransactions_withValidInvoiceId_shouldReturnTransactions() throws Exception {
        setupAuthentication();

        List<Transaction> transactions = Arrays.asList(testTransaction);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(transactionService.getTransactionsByInvoiceId(testInvoiceId))
                .thenReturn(transactions);
        Mockito.when(transactionService.toResponse(testTransaction))
                .thenReturn(createTransactionResponse(testTransaction));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .param("invoiceId", testInvoiceId.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].transactionId").value(testTransactionId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].invoiceId").value(testInvoiceId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].amountExpected").value(1000000L))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].currency").value("USDT"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].chain").value("ETH"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").value("SUCCESS"));

        Mockito.verify(merchantService).getMerchantByIdAndUserId(testMerchantId, testUser.getUserId());
        Mockito.verify(transactionService).getTransactionsByInvoiceId(testInvoiceId);
    }

    @Test
    @DisplayName("Get Transactions: Should return multiple transactions for invoice")
    void getTransactions_withMultipleTransactions_shouldReturnAll() throws Exception {
        setupAuthentication();

        Transaction transaction2 = new Transaction();
        transaction2.setTransactionId(UUID.randomUUID());
        transaction2.setMerchantId(testMerchantId);
        transaction2.setInvoiceId(testInvoiceId);
        transaction2.setAmountExpected(500000L);
        transaction2.setAmountReceived(500000L);
        transaction2.setCurrency("USDT");
        transaction2.setChain("ETH");
        transaction2.setTxnAddress("0xtransaction2address123456789012345678");
        transaction2.setSenderWalletAddress("0xsender2address1234567890123456789012");
        transaction2.setRecipientWalletAddress("0xrecipient2address1234567890123456789");
        transaction2.setStatus(TransactionStatus.PENDING);
        transaction2.setCreatedAt(System.currentTimeMillis());

        List<Transaction> transactions = Arrays.asList(testTransaction, transaction2);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(transactionService.getTransactionsByInvoiceId(testInvoiceId))
                .thenReturn(transactions);
        Mockito.when(transactionService.toResponse(testTransaction))
                .thenReturn(createTransactionResponse(testTransaction));
        Mockito.when(transactionService.toResponse(transaction2))
                .thenReturn(createTransactionResponse(transaction2));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .param("invoiceId", testInvoiceId.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].transactionId").value(testTransactionId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].status").value("PENDING"));
    }

    @Test
    @DisplayName("Get Transactions: Should return empty list when no transactions for invoice")
    void getTransactions_whenNoTransactions_shouldReturnEmptyList() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(transactionService.getTransactionsByInvoiceId(testInvoiceId))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .param("invoiceId", testInvoiceId.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Get Transactions: Should return 401 when not authenticated")
    void getTransactions_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/")
                .param("invoiceId", testInvoiceId.toString()))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(transactionService, Mockito.never()).getTransactionsByInvoiceId(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Transactions: Should return 401 with invalid JWT token")
    void getTransactions_withInvalidJwt_shouldReturnUnauthorized() throws Exception {
        String invalidToken = "invalid-token";
        Mockito.when(jwtService.isTokenValid(invalidToken)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, invalidToken))
                .param("invoiceId", testInvoiceId.toString()))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(transactionService, Mockito.never()).getTransactionsByInvoiceId(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Transactions: Should return 404 when merchant not found for user")
    void getTransactions_whenMerchantNotFoundForUser_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .param("invoiceId", testInvoiceId.toString()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(transactionService, Mockito.never()).getTransactionsByInvoiceId(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Transactions: Should return 400 when invoiceId parameter is missing")
    void getTransactions_withoutInvoiceId_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(transactionService, Mockito.never()).getTransactionsByInvoiceId(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Transactions: Should return 400 when invoiceId is invalid UUID format")
    void getTransactions_withInvalidInvoiceIdFormat_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .param("invoiceId", "invalid-uuid"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(transactionService, Mockito.never()).getTransactionsByInvoiceId(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Transactions: Should return 400 for invalid merchant UUID format")
    void getTransactions_withInvalidMerchantUuidFormat_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/invalid-uuid/transactions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .param("invoiceId", testInvoiceId.toString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Get Transactions: Should return JSON content type")
    void getTransactions_shouldReturnJsonContentType() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(transactionService.getTransactionsByInvoiceId(testInvoiceId))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .param("invoiceId", testInvoiceId.toString()))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    // GET TRANSACTION BY ID ENDPOINT TESTS

    @Test
    @DisplayName("Get Transaction: Should return transaction when authenticated and transaction exists")
    void getTransaction_whenAuthenticatedAndExists_shouldReturnTransaction() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(transactionService.getTransactionById(testTransactionId))
                .thenReturn(testTransaction);
        Mockito.when(transactionService.toResponse(testTransaction))
                .thenReturn(createTransactionResponse(testTransaction));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/" + testTransactionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.transactionId").value(testTransactionId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.invoiceId").value(testInvoiceId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.amountExpected").value(1000000L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.amountReceived").value(1000000L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value("USDT"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.chain").value("ETH"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("SUCCESS"));

        Mockito.verify(merchantService).getMerchantByIdAndUserId(testMerchantId, testUser.getUserId());
        Mockito.verify(transactionService).getTransactionById(testTransactionId);
    }

    @Test
    @DisplayName("Get Transaction: Should return correct transaction status")
    void getTransaction_shouldReturnCorrectStatus() throws Exception {
        setupAuthentication();

        testTransaction.setStatus(TransactionStatus.PENDING);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(transactionService.getTransactionById(testTransactionId))
                .thenReturn(testTransaction);
        Mockito.when(transactionService.toResponse(testTransaction))
                .thenReturn(createTransactionResponse(testTransaction));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/" + testTransactionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Get Transaction: Should return 404 when transaction not found")
    void getTransaction_whenNotFound_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID randomTransactionId = UUID.randomUUID();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(transactionService.getTransactionById(randomTransactionId))
                .thenThrow(new ResourceNotFound("Failed to find transaction with id " + randomTransactionId));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/" + randomTransactionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Get Transaction: Should return 401 when not authenticated")
    void getTransaction_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/" + testTransactionId + "/"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(transactionService, Mockito.never()).getTransactionById(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Transaction: Should return 401 with invalid JWT token")
    void getTransaction_withInvalidJwt_shouldReturnUnauthorized() throws Exception {
        String invalidToken = "invalid-token";
        Mockito.when(jwtService.isTokenValid(invalidToken)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/" + testTransactionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, invalidToken)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(transactionService, Mockito.never()).getTransactionById(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Transaction: Should return 404 when merchant not found for user")
    void getTransaction_whenMerchantNotFoundForUser_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/" + testTransactionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(transactionService, Mockito.never()).getTransactionById(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Transaction: Should return 400 for invalid transaction UUID format")
    void getTransaction_withInvalidTransactionUuidFormat_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/invalid-uuid/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Get Transaction: Should return 400 for invalid merchant UUID format")
    void getTransaction_withInvalidMerchantUuidFormat_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/invalid-uuid/transactions/" + testTransactionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Get Transaction: Should return JSON content type")
    void getTransaction_shouldReturnJsonContentType() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(transactionService.getTransactionById(testTransactionId))
                .thenReturn(testTransaction);
        Mockito.when(transactionService.toResponse(testTransaction))
                .thenReturn(createTransactionResponse(testTransaction));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/" + testTransactionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    // SECURITY TESTS

    @Test
    @DisplayName("Security: Should not expose sensitive data in error responses")
    void endpoints_shouldNotExposeSensitiveDataInErrors() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(transactionService.getTransactionById(testTransactionId))
                .thenThrow(new RuntimeException("Database connection string: jdbc:postgresql://..."));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/" + testTransactionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().is5xxServerError());
    }

    @Test
    @DisplayName("Security: Should not allow access to another user's merchant transactions")
    void getTransactions_forOtherUsersMerchant_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID otherUserMerchantId = UUID.randomUUID();

        Mockito.when(merchantService.getMerchantByIdAndUserId(otherUserMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + otherUserMerchantId + "/transactions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .param("invoiceId", testInvoiceId.toString()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(transactionService, Mockito.never()).getTransactionsByInvoiceId(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Security: Should handle SQL injection attempts in UUID safely")
    void getTransaction_withSqlInjectionInUuid_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        // UUIDs are strongly typed, SQL injection would fail validation
        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/'; DROP TABLE transactions;--/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(transactionService, Mockito.never()).getTransactionById(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Security: Should handle XSS attempts safely")
    void getTransaction_withXssAttempt_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        // UUID fields are strongly typed, XSS would fail validation
        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/<script>alert('xss')</script>/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        Mockito.verify(transactionService, Mockito.never()).getTransactionById(ArgumentMatchers.any());
    }

    // EDGE CASES TESTS

    @Test
    @DisplayName("Edge Case: Should handle transaction with all transaction statuses")
    void getTransaction_shouldHandleAllStatuses() throws Exception {
        setupAuthentication();

        for (TransactionStatus status : TransactionStatus.values()) {
            testTransaction.setStatus(status);

            Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                    .thenReturn(testMerchant);
            Mockito.when(transactionService.getTransactionById(testTransactionId))
                    .thenReturn(testTransaction);
            Mockito.when(transactionService.toResponse(testTransaction))
                    .thenReturn(createTransactionResponse(testTransaction));

            mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/" + testTransactionId + "/")
                    .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(status.name()));
        }
    }

    @Test
    @DisplayName("Edge Case: Should handle transaction with zero amount received")
    void getTransaction_withZeroAmountReceived_shouldSucceed() throws Exception {
        setupAuthentication();

        testTransaction.setAmountReceived(0L);
        testTransaction.setStatus(TransactionStatus.PENDING);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(transactionService.getTransactionById(testTransactionId))
                .thenReturn(testTransaction);
        Mockito.when(transactionService.toResponse(testTransaction))
                .thenReturn(createTransactionResponse(testTransaction));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/" + testTransactionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.amountReceived").value(0L));
    }

    @Test
    @DisplayName("Edge Case: Should handle transaction with large amounts")
    void getTransaction_withLargeAmounts_shouldSucceed() throws Exception {
        setupAuthentication();

        testTransaction.setAmountExpected(Long.MAX_VALUE);
        testTransaction.setAmountReceived(Long.MAX_VALUE);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(transactionService.getTransactionById(testTransactionId))
                .thenReturn(testTransaction);
        Mockito.when(transactionService.toResponse(testTransaction))
                .thenReturn(createTransactionResponse(testTransaction));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/" + testTransactionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.amountExpected").value(Long.MAX_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.amountReceived").value(Long.MAX_VALUE));
    }

    @Test
    @DisplayName("Edge Case: Should return correct timestamps in response")
    void getTransaction_shouldReturnCorrectTimestamps() throws Exception {
        setupAuthentication();

        long testCreatedAt = 1700000000000L;
        long testCompletedAt = 1700000001000L;
        testTransaction.setCreatedAt(testCreatedAt);
        testTransaction.setCompletedAt(testCompletedAt);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(transactionService.getTransactionById(testTransactionId))
                .thenReturn(testTransaction);
        Mockito.when(transactionService.toResponse(testTransaction))
                .thenReturn(createTransactionResponse(testTransaction));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/" + testTransactionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").value(testCreatedAt))
                .andExpect(MockMvcResultMatchers.jsonPath("$.completedAt").value(testCompletedAt));
    }

    @Test
    @DisplayName("Edge Case: Should handle transaction with zero completedAt (not completed)")
    void getTransaction_withZeroCompletedAt_shouldSucceed() throws Exception {
        setupAuthentication();

        testTransaction.setCompletedAt(0L);
        testTransaction.setStatus(TransactionStatus.PENDING);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(transactionService.getTransactionById(testTransactionId))
                .thenReturn(testTransaction);
        Mockito.when(transactionService.toResponse(testTransaction))
                .thenReturn(createTransactionResponse(testTransaction));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/" + testTransactionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.completedAt").value(0L));
    }

    @Test
    @DisplayName("Edge Case: Should handle expired JWT token")
    void endpoints_withExpiredJwt_shouldReturnUnauthorized() throws Exception {
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.expired";
        Mockito.when(jwtService.isTokenValid(expiredToken)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, expiredToken))
                .param("invoiceId", testInvoiceId.toString()))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("Edge Case: Should handle user not found in database after JWT validation")
    void endpoints_whenUserNotFoundAfterJwtValidation_shouldReturnUnauthorized() throws Exception {
        Mockito.when(jwtService.isTokenValid(testToken)).thenReturn(true);
        Mockito.when(jwtService.extractUserId(testToken)).thenReturn(Optional.of(testUser.getUserId().toString()));
        Mockito.when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .param("invoiceId", testInvoiceId.toString()))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("Edge Case: Should handle different currency types")
    void getTransaction_withDifferentCurrencies_shouldSucceed() throws Exception {
        setupAuthentication();

        String[] currencies = {"USDT", "USDC", "BTC", "ETH", "DAI"};

        for (String currency : currencies) {
            testTransaction.setCurrency(currency);

            Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                    .thenReturn(testMerchant);
            Mockito.when(transactionService.getTransactionById(testTransactionId))
                    .thenReturn(testTransaction);
            Mockito.when(transactionService.toResponse(testTransaction))
                    .thenReturn(createTransactionResponse(testTransaction));

            mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/" + testTransactionId + "/")
                    .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value(currency));
        }
    }

    @Test
    @DisplayName("Edge Case: Should handle different blockchain chains")
    void getTransaction_withDifferentChains_shouldSucceed() throws Exception {
        setupAuthentication();

        String[] chains = {"ETH", "BTC", "MATIC", "BSC", "SOL"};

        for (String chain : chains) {
            testTransaction.setChain(chain);

            Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                    .thenReturn(testMerchant);
            Mockito.when(transactionService.getTransactionById(testTransactionId))
                    .thenReturn(testTransaction);
            Mockito.when(transactionService.toResponse(testTransaction))
                    .thenReturn(createTransactionResponse(testTransaction));

            mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/" + testTransactionId + "/")
                    .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.chain").value(chain));
        }
    }

    @Test
    @DisplayName("Edge Case: Should handle empty invoiceId parameter")
    void getTransactions_withEmptyInvoiceId_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .param("invoiceId", ""))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(transactionService, Mockito.never()).getTransactionsByInvoiceId(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Edge Case: Should handle partial amount received")
    void getTransaction_withPartialAmountReceived_shouldSucceed() throws Exception {
        setupAuthentication();

        testTransaction.setAmountExpected(1000000L);
        testTransaction.setAmountReceived(500000L);
        testTransaction.setStatus(TransactionStatus.PENDING);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(transactionService.getTransactionById(testTransactionId))
                .thenReturn(testTransaction);
        Mockito.when(transactionService.toResponse(testTransaction))
                .thenReturn(createTransactionResponse(testTransaction));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/" + testTransactionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.amountExpected").value(1000000L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.amountReceived").value(500000L));
    }

    @Test
    @DisplayName("Edge Case: Should handle overpayment scenario")
    void getTransaction_withOverpayment_shouldSucceed() throws Exception {
        setupAuthentication();

        testTransaction.setAmountExpected(1000000L);
        testTransaction.setAmountReceived(1500000L);
        testTransaction.setStatus(TransactionStatus.SUCCESS);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(transactionService.getTransactionById(testTransactionId))
                .thenReturn(testTransaction);
        Mockito.when(transactionService.toResponse(testTransaction))
                .thenReturn(createTransactionResponse(testTransaction));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/" + testTransactionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.amountExpected").value(1000000L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.amountReceived").value(1500000L));
    }

    @Test
    @DisplayName("Edge Case: Should handle failed transaction status")
    void getTransaction_withFailedStatus_shouldSucceed() throws Exception {
        setupAuthentication();

        testTransaction.setStatus(TransactionStatus.FAILED);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(transactionService.getTransactionById(testTransactionId))
                .thenReturn(testTransaction);
        Mockito.when(transactionService.toResponse(testTransaction))
                .thenReturn(createTransactionResponse(testTransaction));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/" + testTransactionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("FAILED"));
    }

    @Test
    @DisplayName("Edge Case: Should return all wallet addresses in response")
    void getTransaction_shouldReturnAllWalletAddresses() throws Exception {
        setupAuthentication();

        String senderAddress = "0xsender1234567890abcdef1234567890abcdef";
        String recipientAddress = "0xrecipient1234567890abcdef1234567890ab";
        String txnAddress = "0xtxn1234567890abcdef1234567890abcdef12";

        testTransaction.setSenderWalletAddress(senderAddress);
        testTransaction.setRecipientWalletAddress(recipientAddress);
        testTransaction.setTxnAddress(txnAddress);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(transactionService.getTransactionById(testTransactionId))
                .thenReturn(testTransaction);
        Mockito.when(transactionService.toResponse(testTransaction))
                .thenReturn(createTransactionResponse(testTransaction));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/transactions/" + testTransactionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.senderWalletAddress").value(senderAddress))
                .andExpect(MockMvcResultMatchers.jsonPath("$.recipientWalletAddress").value(recipientAddress))
                .andExpect(MockMvcResultMatchers.jsonPath("$.txnAddress").value(txnAddress));
    }
}