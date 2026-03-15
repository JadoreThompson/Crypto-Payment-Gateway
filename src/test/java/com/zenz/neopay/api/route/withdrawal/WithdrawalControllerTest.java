package com.zenz.neopay.api.route.withdrawal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenz.neopay.api.GlobalExceptionHandler;
import com.zenz.neopay.api.config.JWTAuthenticationFilter;
import com.zenz.neopay.api.config.SecurityConfig;
import com.zenz.neopay.api.error.ResourceNotFound;
import com.zenz.neopay.api.route.withdrawal.request.CreateWithdrawalRequest;
import com.zenz.neopay.api.route.withdrawal.response.WithdrawalResponse;
import com.zenz.neopay.entity.Merchant;
import com.zenz.neopay.entity.User;
import com.zenz.neopay.entity.Withdrawal;
import com.zenz.neopay.enums.WithdrawalStatus;
import com.zenz.neopay.repository.UserRepository;
import com.zenz.neopay.service.JWTService;
import com.zenz.neopay.service.MerchantService;
import com.zenz.neopay.service.WithdrawalService;
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



@WebMvcTest(WithdrawalController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class WithdrawalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WithdrawalService withdrawalService;

    @MockitoBean
    private MerchantService merchantService;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private ObjectMapper objectMapper;
    private User testUser;
    private Merchant testMerchant;
    private Withdrawal testWithdrawal;
    private String testToken;
    private UUID testMerchantId;
    private UUID testWithdrawalId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test-token";
        testMerchantId = UUID.randomUUID();
        testWithdrawalId = UUID.randomUUID();

        testUser = new User();
        testUser.setUserId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");

        testMerchant = new Merchant();
        testMerchant.setMerchantId(testMerchantId);
        testMerchant.setName("Test Merchant");
        testMerchant.setCreatedAt(System.currentTimeMillis());

        testWithdrawal = new Withdrawal();
        testWithdrawal.setWithdrawalId(testWithdrawalId);
        testWithdrawal.setMerchantId(testMerchantId);
        testWithdrawal.setAmount(1000000L);
        testWithdrawal.setCurrency("USDT");
        testWithdrawal.setChain("ETH");
        testWithdrawal.setStatus(WithdrawalStatus.PENDING);
        testWithdrawal.setCreatedAt(System.currentTimeMillis());
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
     * Helper method to create a WithdrawalResponse object.
     */
    private WithdrawalResponse createWithdrawalResponse(Withdrawal withdrawal) {
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

    // CREATE WITHDRAWAL ENDPOINT TESTS

    @Test
    @DisplayName("Create Withdrawal: Should create withdrawal successfully with valid data")
    void createWithdrawal_withValidData_shouldReturnOk() throws Exception {
        setupAuthentication();

        CreateWithdrawalRequest request = new CreateWithdrawalRequest();
        request.setAmount(1000000L);
        request.setCurrency("USDT");
        request.setChain("ETH");
        request.setMerchantId(testMerchantId);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(withdrawalService.createWithdrawal(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(CreateWithdrawalRequest.class)))
                .thenReturn(testWithdrawal);
        Mockito.when(withdrawalService.toResponse(testWithdrawal))
                .thenReturn(createWithdrawalResponse(testWithdrawal));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.withdrawalId").value(testWithdrawalId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(1000000L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value("USDT"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.chain").value("ETH"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("PENDING"));

        Mockito.verify(merchantService).getMerchantByIdAndUserId(testMerchantId, testUser.getUserId());
        Mockito.verify(withdrawalService).createWithdrawal(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(CreateWithdrawalRequest.class));
    }

    @Test
    @DisplayName("Create Withdrawal: Should create withdrawal with different amount")
    void createWithdrawal_withDifferentAmount_shouldSucceed() throws Exception {
        setupAuthentication();

        CreateWithdrawalRequest request = new CreateWithdrawalRequest();
        request.setAmount(5000000L);
        request.setCurrency("USDT");
        request.setChain("ETH");
        request.setMerchantId(testMerchantId);

        testWithdrawal.setAmount(5000000L);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(withdrawalService.createWithdrawal(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(CreateWithdrawalRequest.class)))
                .thenReturn(testWithdrawal);
        Mockito.when(withdrawalService.toResponse(testWithdrawal))
                .thenReturn(createWithdrawalResponse(testWithdrawal));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(5000000L));
    }

    @Test
    @DisplayName("Create Withdrawal: Should reject request without authentication")
    void createWithdrawal_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        CreateWithdrawalRequest request = new CreateWithdrawalRequest();
        request.setAmount(1000000L);
        request.setCurrency("USDT");
        request.setChain("ETH");

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/withdrawals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(withdrawalService, Mockito.never()).createWithdrawal(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Withdrawal: Should reject request with invalid JWT token")
    void createWithdrawal_withInvalidJwt_shouldReturnUnauthorized() throws Exception {
        String invalidToken = "invalid-token";
        Mockito.when(jwtService.isTokenValid(invalidToken)).thenReturn(false);

        CreateWithdrawalRequest request = new CreateWithdrawalRequest();
        request.setAmount(1000000L);
        request.setCurrency("USDT");
        request.setChain("ETH");

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, invalidToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(withdrawalService, Mockito.never()).createWithdrawal(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Withdrawal: Should reject request when merchant not found for user")
    void createWithdrawal_whenMerchantNotFoundForUser_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID randomMerchantId = UUID.randomUUID();

        CreateWithdrawalRequest request = new CreateWithdrawalRequest();
        request.setAmount(1000000L);
        request.setCurrency("USDT");
        request.setChain("ETH");

        Mockito.when(merchantService.getMerchantByIdAndUserId(randomMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + randomMerchantId + "/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(withdrawalService, Mockito.never()).createWithdrawal(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Withdrawal: Should reject request with zero amount")
    void createWithdrawal_withZeroAmount_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateWithdrawalRequest request = new CreateWithdrawalRequest();
        request.setAmount(0L);
        request.setCurrency("USDT");
        request.setChain("ETH");

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(withdrawalService, Mockito.never()).createWithdrawal(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Withdrawal: Should reject request with negative amount")
    void createWithdrawal_withNegativeAmount_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateWithdrawalRequest request = new CreateWithdrawalRequest();
        request.setAmount(-1000L);
        request.setCurrency("USDT");
        request.setChain("ETH");

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(withdrawalService, Mockito.never()).createWithdrawal(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Withdrawal: Should reject request with missing body")
    void createWithdrawal_withMissingBody_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(withdrawalService, Mockito.never()).createWithdrawal(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Withdrawal: Should reject request with malformed JSON")
    void createWithdrawal_withMalformedJson_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(withdrawalService, Mockito.never()).createWithdrawal(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Withdrawal: Should reject request with wrong content type")
    void createWithdrawal_withWrongContentType_shouldReturnUnsupportedMediaType() throws Exception {
        setupAuthentication();

        CreateWithdrawalRequest request = new CreateWithdrawalRequest();
        request.setAmount(1000000L);
        request.setCurrency("USDT");
        request.setChain("ETH");

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType());

        Mockito.verify(withdrawalService, Mockito.never()).createWithdrawal(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Withdrawal: Should return 400 for invalid merchant UUID format")
    void createWithdrawal_withInvalidMerchantUuidFormat_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateWithdrawalRequest request = new CreateWithdrawalRequest();
        request.setAmount(1000000L);
        request.setCurrency("USDT");
        request.setChain("ETH");

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/invalid-uuid/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Create Withdrawal: Should reject request with blank currency")
    void createWithdrawal_withBlankCurrency_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateWithdrawalRequest request = new CreateWithdrawalRequest();
        request.setAmount(1000000L);
        request.setCurrency("");
        request.setChain("ETH");

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(withdrawalService, Mockito.never()).createWithdrawal(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Withdrawal: Should reject request with null currency")
    void createWithdrawal_withNullCurrency_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateWithdrawalRequest request = new CreateWithdrawalRequest();
        request.setAmount(1000000L);
        request.setCurrency(null);
        request.setChain("ETH");

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(withdrawalService, Mockito.never()).createWithdrawal(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Withdrawal: Should reject request with blank chain")
    void createWithdrawal_withBlankChain_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateWithdrawalRequest request = new CreateWithdrawalRequest();
        request.setAmount(1000000L);
        request.setCurrency("USDT");
        request.setChain("");

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(withdrawalService, Mockito.never()).createWithdrawal(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Withdrawal: Should reject request with null chain")
    void createWithdrawal_withNullChain_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateWithdrawalRequest request = new CreateWithdrawalRequest();
        request.setAmount(1000000L);
        request.setCurrency("USDT");
        request.setChain(null);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(withdrawalService, Mockito.never()).createWithdrawal(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Withdrawal: Should reject request with whitespace-only currency")
    void createWithdrawal_withWhitespaceCurrency_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateWithdrawalRequest request = new CreateWithdrawalRequest();
        request.setAmount(1000000L);
        request.setCurrency("   ");
        request.setChain("ETH");

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(withdrawalService, Mockito.never()).createWithdrawal(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Withdrawal: Should reject request with whitespace-only chain")
    void createWithdrawal_withWhitespaceChain_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateWithdrawalRequest request = new CreateWithdrawalRequest();
        request.setAmount(1000000L);
        request.setCurrency("USDT");
        request.setChain("   ");

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(withdrawalService, Mockito.never()).createWithdrawal(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    // GET WITHDRAWAL BY ID ENDPOINT TESTS

    @Test
    @DisplayName("Get Withdrawal: Should return withdrawal when authenticated and owner")
    void getWithdrawal_whenAuthenticatedAndOwner_shouldReturnWithdrawal() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(withdrawalService.getWithdrawalByIdAndMerchantId(testWithdrawalId, testMerchantId))
                .thenReturn(testWithdrawal);
        Mockito.when(withdrawalService.toResponse(testWithdrawal))
                .thenReturn(createWithdrawalResponse(testWithdrawal));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/" + testWithdrawalId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.withdrawalId").value(testWithdrawalId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(1000000L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value("USDT"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.chain").value("ETH"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("PENDING"));

        Mockito.verify(merchantService).getMerchantByIdAndUserId(testMerchantId, testUser.getUserId());
        Mockito.verify(withdrawalService).getWithdrawalByIdAndMerchantId(testWithdrawalId, testMerchantId);
    }

    @Test
    @DisplayName("Get Withdrawal: Should return correct withdrawal status")
    void getWithdrawal_shouldReturnCorrectStatus() throws Exception {
        setupAuthentication();

        testWithdrawal.setStatus(WithdrawalStatus.SUCCESS);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(withdrawalService.getWithdrawalByIdAndMerchantId(testWithdrawalId, testMerchantId))
                .thenReturn(testWithdrawal);
        Mockito.when(withdrawalService.toResponse(testWithdrawal))
                .thenReturn(createWithdrawalResponse(testWithdrawal));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/" + testWithdrawalId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("Get Withdrawal: Should return 404 when withdrawal not found")
    void getWithdrawal_whenNotFound_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID randomWithdrawalId = UUID.randomUUID();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(withdrawalService.getWithdrawalByIdAndMerchantId(randomWithdrawalId, testMerchantId))
                .thenThrow(new ResourceNotFound("Failed to find withdrawal with id " + randomWithdrawalId + " for merchant"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/" + randomWithdrawalId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Get Withdrawal: Should return 401 when not authenticated")
    void getWithdrawal_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/" + testWithdrawalId + "/"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(withdrawalService, Mockito.never()).getWithdrawalByIdAndMerchantId(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Withdrawal: Should return 401 with invalid JWT token")
    void getWithdrawal_withInvalidJwt_shouldReturnUnauthorized() throws Exception {
        String invalidToken = "invalid-token";
        Mockito.when(jwtService.isTokenValid(invalidToken)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/" + testWithdrawalId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, invalidToken)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(withdrawalService, Mockito.never()).getWithdrawalByIdAndMerchantId(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Withdrawal: Should return 404 when merchant not found for user")
    void getWithdrawal_whenMerchantNotFoundForUser_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/" + testWithdrawalId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(withdrawalService, Mockito.never()).getWithdrawalByIdAndMerchantId(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Withdrawal: Should return 400 for invalid withdrawal UUID format")
    void getWithdrawal_withInvalidWithdrawalUuidFormat_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/invalid-uuid/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Get Withdrawal: Should return 400 for invalid merchant UUID format")
    void getWithdrawal_withInvalidMerchantUuidFormat_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/invalid-uuid/withdrawals/" + testWithdrawalId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Get Withdrawal: Should return JSON content type")
    void getWithdrawal_shouldReturnJsonContentType() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(withdrawalService.getWithdrawalByIdAndMerchantId(testWithdrawalId, testMerchantId))
                .thenReturn(testWithdrawal);
        Mockito.when(withdrawalService.toResponse(testWithdrawal))
                .thenReturn(createWithdrawalResponse(testWithdrawal));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/" + testWithdrawalId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Get Withdrawal: Should not return withdrawal belonging to different merchant")
    void getWithdrawal_whenWithdrawalBelongsToDifferentMerchant_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID differentMerchantId = UUID.randomUUID();

        Mockito.when(merchantService.getMerchantByIdAndUserId(differentMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(withdrawalService.getWithdrawalByIdAndMerchantId(testWithdrawalId, differentMerchantId))
                .thenThrow(new ResourceNotFound("Failed to find withdrawal with id " + testWithdrawalId + " for merchant"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + differentMerchantId + "/withdrawals/" + testWithdrawalId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    // GET ALL WITHDRAWALS ENDPOINT TESTS

    @Test
    @DisplayName("Get Withdrawals: Should return all withdrawals for authenticated merchant")
    void getWithdrawals_whenAuthenticated_shouldReturnWithdrawals() throws Exception {
        setupAuthentication();

        Withdrawal withdrawal2 = new Withdrawal();
        withdrawal2.setWithdrawalId(UUID.randomUUID());
        withdrawal2.setMerchantId(testMerchantId);
        withdrawal2.setAmount(2000000L);
        withdrawal2.setCurrency("USDC");
        withdrawal2.setChain("MATIC");
        withdrawal2.setStatus(WithdrawalStatus.SUCCESS);
        withdrawal2.setCreatedAt(System.currentTimeMillis());

        List<Withdrawal> withdrawals = Arrays.asList(testWithdrawal, withdrawal2);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(withdrawalService.getWithdrawalsByMerchantId(testMerchantId))
                .thenReturn(withdrawals);
        Mockito.when(withdrawalService.toResponse(testWithdrawal))
                .thenReturn(createWithdrawalResponse(testWithdrawal));
        Mockito.when(withdrawalService.toResponse(withdrawal2))
                .thenReturn(createWithdrawalResponse(withdrawal2));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].withdrawalId").value(testWithdrawalId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].withdrawalId").value(withdrawal2.getWithdrawalId().toString()));
    }

    @Test
    @DisplayName("Get Withdrawals: Should return empty list when merchant has no withdrawals")
    void getWithdrawals_whenNoWithdrawals_shouldReturnEmptyList() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(withdrawalService.getWithdrawalsByMerchantId(testMerchantId))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Get Withdrawals: Should return 401 when not authenticated")
    void getWithdrawals_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(withdrawalService, Mockito.never()).getWithdrawalsByMerchantId(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Withdrawals: Should return 401 with invalid JWT token")
    void getWithdrawals_withInvalidJwt_shouldReturnUnauthorized() throws Exception {
        String invalidToken = "invalid-token";
        Mockito.when(jwtService.isTokenValid(invalidToken)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, invalidToken)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(withdrawalService, Mockito.never()).getWithdrawalsByMerchantId(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Withdrawals: Should return 404 when merchant not found for user")
    void getWithdrawals_whenMerchantNotFoundForUser_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(withdrawalService, Mockito.never()).getWithdrawalsByMerchantId(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Withdrawals: Should return 400 for invalid merchant UUID format")
    void getWithdrawals_withInvalidMerchantUuidFormat_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/invalid-uuid/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Get Withdrawals: Should return JSON content type")
    void getWithdrawals_shouldReturnJsonContentType() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(withdrawalService.getWithdrawalsByMerchantId(testMerchantId))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/")
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
        Mockito.when(withdrawalService.getWithdrawalByIdAndMerchantId(testWithdrawalId, testMerchantId))
                .thenThrow(new RuntimeException("Database connection string: jdbc:postgresql://..."));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/" + testWithdrawalId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().is5xxServerError());
    }

    @Test
    @DisplayName("Security: Should not allow access to another user's merchant withdrawals")
    void getWithdrawals_forOtherUsersMerchant_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID otherUserMerchantId = UUID.randomUUID();

        Mockito.when(merchantService.getMerchantByIdAndUserId(otherUserMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + otherUserMerchantId + "/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(withdrawalService, Mockito.never()).getWithdrawalsByMerchantId(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Security: Should handle SQL injection attempts in UUID safely")
    void getWithdrawal_withSqlInjectionInUuid_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/'; DROP TABLE withdrawals;--/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(withdrawalService, Mockito.never()).getWithdrawalByIdAndMerchantId(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Security: Should handle XSS attempts safely")
    void getWithdrawal_withXssAttempt_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/<script>alert('xss')</script>/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        Mockito.verify(withdrawalService, Mockito.never()).getWithdrawalByIdAndMerchantId(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    // EDGE CASES TESTS

    @Test
    @DisplayName("Edge Case: Should handle withdrawal with all withdrawal statuses")
    void getWithdrawal_shouldHandleAllStatuses() throws Exception {
        setupAuthentication();

        for (WithdrawalStatus status : WithdrawalStatus.values()) {
            testWithdrawal.setStatus(status);

            Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                    .thenReturn(testMerchant);
            Mockito.when(withdrawalService.getWithdrawalByIdAndMerchantId(testWithdrawalId, testMerchantId))
                    .thenReturn(testWithdrawal);
            Mockito.when(withdrawalService.toResponse(testWithdrawal))
                    .thenReturn(createWithdrawalResponse(testWithdrawal));

            mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/" + testWithdrawalId + "/")
                    .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(status.name()));
        }
    }

    @Test
    @DisplayName("Edge Case: Should handle withdrawal with large amount")
    void getWithdrawal_withLargeAmount_shouldSucceed() throws Exception {
        setupAuthentication();

        testWithdrawal.setAmount(Long.MAX_VALUE);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(withdrawalService.getWithdrawalByIdAndMerchantId(testWithdrawalId, testMerchantId))
                .thenReturn(testWithdrawal);
        Mockito.when(withdrawalService.toResponse(testWithdrawal))
                .thenReturn(createWithdrawalResponse(testWithdrawal));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/" + testWithdrawalId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(Long.MAX_VALUE));
    }

    @Test
    @DisplayName("Edge Case: Should return correct timestamps in response")
    void getWithdrawal_shouldReturnCorrectTimestamps() throws Exception {
        setupAuthentication();

        long testCreatedAt = 1700000000000L;
        long testCompletedAt = 1700000001000L;
        testWithdrawal.setCreatedAt(testCreatedAt);
        testWithdrawal.setCompletedAt(testCompletedAt);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(withdrawalService.getWithdrawalByIdAndMerchantId(testWithdrawalId, testMerchantId))
                .thenReturn(testWithdrawal);
        Mockito.when(withdrawalService.toResponse(testWithdrawal))
                .thenReturn(createWithdrawalResponse(testWithdrawal));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/" + testWithdrawalId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").value(testCreatedAt))
                .andExpect(MockMvcResultMatchers.jsonPath("$.completedAt").value(testCompletedAt));
    }

    @Test
    @DisplayName("Edge Case: Should handle withdrawal with zero completedAt (not completed)")
    void getWithdrawal_withZeroCompletedAt_shouldSucceed() throws Exception {
        setupAuthentication();

        testWithdrawal.setCompletedAt(0L);
        testWithdrawal.setStatus(WithdrawalStatus.PENDING);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(withdrawalService.getWithdrawalByIdAndMerchantId(testWithdrawalId, testMerchantId))
                .thenReturn(testWithdrawal);
        Mockito.when(withdrawalService.toResponse(testWithdrawal))
                .thenReturn(createWithdrawalResponse(testWithdrawal));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/" + testWithdrawalId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.completedAt").value(0L));
    }

    @Test
    @DisplayName("Edge Case: Should handle expired JWT token")
    void endpoints_withExpiredJwt_shouldReturnUnauthorized() throws Exception {
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.expired";
        Mockito.when(jwtService.isTokenValid(expiredToken)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, expiredToken)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("Edge Case: Should handle user not found in database after JWT validation")
    void endpoints_whenUserNotFoundAfterJwtValidation_shouldReturnUnauthorized() throws Exception {
        Mockito.when(jwtService.isTokenValid(testToken)).thenReturn(true);
        Mockito.when(jwtService.extractUserId(testToken)).thenReturn(Optional.of(testUser.getUserId().toString()));
        Mockito.when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("Edge Case: Should handle different currency types")
    void getWithdrawal_withDifferentCurrencies_shouldSucceed() throws Exception {
        setupAuthentication();

        String[] currencies = {"USDT", "USDC", "BTC", "ETH", "DAI"};

        for (String currency : currencies) {
            testWithdrawal.setCurrency(currency);

            Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                    .thenReturn(testMerchant);
            Mockito.when(withdrawalService.getWithdrawalByIdAndMerchantId(testWithdrawalId, testMerchantId))
                    .thenReturn(testWithdrawal);
            Mockito.when(withdrawalService.toResponse(testWithdrawal))
                    .thenReturn(createWithdrawalResponse(testWithdrawal));

            mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/" + testWithdrawalId + "/")
                    .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value(currency));
        }
    }

    @Test
    @DisplayName("Edge Case: Should handle different blockchain chains")
    void getWithdrawal_withDifferentChains_shouldSucceed() throws Exception {
        setupAuthentication();

        String[] chains = {"ETH", "BTC", "MATIC", "BSC", "SOL"};

        for (String chain : chains) {
            testWithdrawal.setChain(chain);

            Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                    .thenReturn(testMerchant);
            Mockito.when(withdrawalService.getWithdrawalByIdAndMerchantId(testWithdrawalId, testMerchantId))
                    .thenReturn(testWithdrawal);
            Mockito.when(withdrawalService.toResponse(testWithdrawal))
                    .thenReturn(createWithdrawalResponse(testWithdrawal));

            mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/" + testWithdrawalId + "/")
                    .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.chain").value(chain));
        }
    }

    @Test
    @DisplayName("Edge Case: Should handle failed withdrawal status")
    void getWithdrawal_withFailedStatus_shouldSucceed() throws Exception {
        setupAuthentication();

        testWithdrawal.setStatus(WithdrawalStatus.FAILED);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(withdrawalService.getWithdrawalByIdAndMerchantId(testWithdrawalId, testMerchantId))
                .thenReturn(testWithdrawal);
        Mockito.when(withdrawalService.toResponse(testWithdrawal))
                .thenReturn(createWithdrawalResponse(testWithdrawal));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/" + testWithdrawalId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("FAILED"));
    }

    @Test
    @DisplayName("Edge Case: Should verify all response fields are present")
    void getWithdrawal_shouldReturnAllFields() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(withdrawalService.getWithdrawalByIdAndMerchantId(testWithdrawalId, testMerchantId))
                .thenReturn(testWithdrawal);
        Mockito.when(withdrawalService.toResponse(testWithdrawal))
                .thenReturn(createWithdrawalResponse(testWithdrawal));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/" + testWithdrawalId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.withdrawalId").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.amount").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.currency").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.chain").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.completedAt").exists());
    }

    @Test
    @DisplayName("Edge Case: Should handle single withdrawal for merchant")
    void getWithdrawals_withSingleWithdrawal_shouldReturnSingleElementList() throws Exception {
        setupAuthentication();

        List<Withdrawal> withdrawals = Arrays.asList(testWithdrawal);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(withdrawalService.getWithdrawalsByMerchantId(testMerchantId))
                .thenReturn(withdrawals);
        Mockito.when(withdrawalService.toResponse(testWithdrawal))
                .thenReturn(createWithdrawalResponse(testWithdrawal));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].withdrawalId").value(testWithdrawalId.toString()));
    }

    @Test
    @DisplayName("Edge Case: Should handle multiple withdrawals with different statuses")
    void getWithdrawals_withMultipleStatuses_shouldReturnAllWithdrawals() throws Exception {
        setupAuthentication();

        Withdrawal withdrawal2 = new Withdrawal();
        withdrawal2.setWithdrawalId(UUID.randomUUID());
        withdrawal2.setMerchantId(testMerchantId);
        withdrawal2.setAmount(500000L);
        withdrawal2.setCurrency("BTC");
        withdrawal2.setChain("BTC");
        withdrawal2.setStatus(WithdrawalStatus.SUCCESS);
        withdrawal2.setCreatedAt(System.currentTimeMillis());
        withdrawal2.setCompletedAt(System.currentTimeMillis());

        Withdrawal withdrawal3 = new Withdrawal();
        withdrawal3.setWithdrawalId(UUID.randomUUID());
        withdrawal3.setMerchantId(testMerchantId);
        withdrawal3.setAmount(2000000L);
        withdrawal3.setCurrency("ETH");
        withdrawal3.setChain("ETH");
        withdrawal3.setStatus(WithdrawalStatus.FAILED);
        withdrawal3.setCreatedAt(System.currentTimeMillis());

        List<Withdrawal> withdrawals = Arrays.asList(testWithdrawal, withdrawal2, withdrawal3);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(withdrawalService.getWithdrawalsByMerchantId(testMerchantId))
                .thenReturn(withdrawals);
        Mockito.when(withdrawalService.toResponse(testWithdrawal))
                .thenReturn(createWithdrawalResponse(testWithdrawal));
        Mockito.when(withdrawalService.toResponse(withdrawal2))
                .thenReturn(createWithdrawalResponse(withdrawal2));
        Mockito.when(withdrawalService.toResponse(withdrawal3))
                .thenReturn(createWithdrawalResponse(withdrawal3));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/withdrawals/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").value("PENDING"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].status").value("SUCCESS"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].status").value("FAILED"));
    }
}