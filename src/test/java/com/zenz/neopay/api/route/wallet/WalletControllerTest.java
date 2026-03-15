package com.zenz.neopay.api.route.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenz.neopay.api.config.JWTAuthenticationFilter;
import com.zenz.neopay.api.config.SecurityConfig;
import com.zenz.neopay.api.error.ResourceNotFound;
import com.zenz.neopay.api.route.wallet.response.WalletResponse;
import com.zenz.neopay.entity.Merchant;
import com.zenz.neopay.entity.User;
import com.zenz.neopay.entity.Wallet;
import com.zenz.neopay.repository.UserRepository;
import com.zenz.neopay.service.JWTService;
import com.zenz.neopay.service.MerchantService;
import com.zenz.neopay.service.WalletService;
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

@WebMvcTest(WalletController.class)
@Import(SecurityConfig.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WalletService walletService;

    @MockitoBean
    private MerchantService merchantService;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private ObjectMapper objectMapper;
    private User testUser;
    private Merchant testMerchant;
    private Wallet testWallet;
    private String testToken;
    private UUID testMerchantId;
    private UUID testWalletId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test-token";
        testMerchantId = UUID.randomUUID();
        testWalletId = UUID.randomUUID();

        testUser = new User();
        testUser.setUserId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");

        testMerchant = new Merchant();
        testMerchant.setMerchantId(testMerchantId);
        testMerchant.setName("Test Merchant");
        testMerchant.setCreatedAt(System.currentTimeMillis());

        testWallet = new Wallet();
        testWallet.setWalletId(testWalletId);
        testWallet.setMerchantId(testMerchantId);
        testWallet.setBalance(1000000L);
        testWallet.setCurrency("USDT");
        testWallet.setWalletAddress("0x1234567890abcdef1234567890abcdef12345678");
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
     * Helper method to create a WalletResponse object.
     */
    private WalletResponse createWalletResponse(Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setWalletId(wallet.getWalletId());
        response.setBalance(wallet.getBalance());
        response.setCurrency(wallet.getCurrency());
        response.setWalletAddress(wallet.getWalletAddress());
        return response;
    }

    // GET WALLET BY ID ENDPOINT TESTS

    @Test
    @DisplayName("Get Wallet: Should return wallet when authenticated and owner")
    void getWallet_whenAuthenticatedAndOwner_shouldReturnWallet() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(walletService.getWalletByIdAndMerchantId(testWalletId, testMerchantId))
                .thenReturn(testWallet);
        Mockito.when(walletService.toResponse(testWallet))
                .thenReturn(createWalletResponse(testWallet));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/" + testWalletId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.walletId").value(testWalletId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance").value(1000000L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value("USDT"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.walletAddress").value("0x1234567890abcdef1234567890abcdef12345678"));

        Mockito.verify(merchantService).getMerchantByIdAndUserId(testMerchantId, testUser.getUserId());
        Mockito.verify(walletService).getWalletByIdAndMerchantId(testWalletId, testMerchantId);
    }

    @Test
    @DisplayName("Get Wallet: Should return correct balance")
    void getWallet_shouldReturnCorrectBalance() throws Exception {
        setupAuthentication();

        testWallet.setBalance(5000000L);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(walletService.getWalletByIdAndMerchantId(testWalletId, testMerchantId))
                .thenReturn(testWallet);
        Mockito.when(walletService.toResponse(testWallet))
                .thenReturn(createWalletResponse(testWallet));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/" + testWalletId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance").value(5000000L));
    }

    @Test
    @DisplayName("Get Wallet: Should return 404 when wallet not found")
    void getWallet_whenNotFound_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID randomWalletId = UUID.randomUUID();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(walletService.getWalletByIdAndMerchantId(randomWalletId, testMerchantId))
                .thenThrow(new ResourceNotFound("Failed to find wallet with id " + randomWalletId + " for merchant"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/" + randomWalletId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Get Wallet: Should return 401 when not authenticated")
    void getWallet_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/" + testWalletId + "/"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(walletService, Mockito.never()).getWalletByIdAndMerchantId(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Wallet: Should return 401 with invalid JWT token")
    void getWallet_withInvalidJwt_shouldReturnUnauthorized() throws Exception {
        String invalidToken = "invalid-token";
        Mockito.when(jwtService.isTokenValid(invalidToken)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/" + testWalletId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, invalidToken)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(walletService, Mockito.never()).getWalletByIdAndMerchantId(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Wallet: Should return 404 when merchant not found for user")
    void getWallet_whenMerchantNotFoundForUser_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/" + testWalletId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(walletService, Mockito.never()).getWalletByIdAndMerchantId(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Wallet: Should return 400 for invalid wallet UUID format")
    void getWallet_withInvalidWalletUuidFormat_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/invalid-uuid/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Get Wallet: Should return 400 for invalid merchant UUID format")
    void getWallet_withInvalidMerchantUuidFormat_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/invalid-uuid/wallets/" + testWalletId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Get Wallet: Should return JSON content type")
    void getWallet_shouldReturnJsonContentType() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(walletService.getWalletByIdAndMerchantId(testWalletId, testMerchantId))
                .thenReturn(testWallet);
        Mockito.when(walletService.toResponse(testWallet))
                .thenReturn(createWalletResponse(testWallet));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/" + testWalletId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Get Wallet: Should not return wallet belonging to different merchant")
    void getWallet_whenWalletBelongsToDifferentMerchant_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID differentMerchantId = UUID.randomUUID();

        Mockito.when(merchantService.getMerchantByIdAndUserId(differentMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(walletService.getWalletByIdAndMerchantId(testWalletId, differentMerchantId))
                .thenThrow(new ResourceNotFound("Failed to find wallet with id " + testWalletId + " for merchant"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + differentMerchantId + "/wallets/" + testWalletId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    // GET ALL WALLETS ENDPOINT TESTS

    @Test
    @DisplayName("Get Wallets: Should return all wallets for authenticated merchant")
    void getWallets_whenAuthenticated_shouldReturnWallets() throws Exception {
        setupAuthentication();

        Wallet wallet2 = new Wallet();
        wallet2.setWalletId(UUID.randomUUID());
        wallet2.setMerchantId(testMerchantId);
        wallet2.setBalance(2000000L);
        wallet2.setCurrency("USDC");
        wallet2.setWalletAddress("0xabcdef1234567890abcdef1234567890abcdef12");

        List<Wallet> wallets = Arrays.asList(testWallet, wallet2);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(walletService.getWalletsByMerchantId(testMerchantId))
                .thenReturn(wallets);
        Mockito.when(walletService.toResponse(testWallet))
                .thenReturn(createWalletResponse(testWallet));
        Mockito.when(walletService.toResponse(wallet2))
                .thenReturn(createWalletResponse(wallet2));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].walletId").value(testWalletId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].walletId").value(wallet2.getWalletId().toString()));
    }

    @Test
    @DisplayName("Get Wallets: Should return empty list when merchant has no wallets")
    void getWallets_whenNoWallets_shouldReturnEmptyList() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(walletService.getWalletsByMerchantId(testMerchantId))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Get Wallets: Should return 401 when not authenticated")
    void getWallets_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(walletService, Mockito.never()).getWalletsByMerchantId(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Wallets: Should return 401 with invalid JWT token")
    void getWallets_withInvalidJwt_shouldReturnUnauthorized() throws Exception {
        String invalidToken = "invalid-token";
        Mockito.when(jwtService.isTokenValid(invalidToken)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, invalidToken)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(walletService, Mockito.never()).getWalletsByMerchantId(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Wallets: Should return 404 when merchant not found for user")
    void getWallets_whenMerchantNotFoundForUser_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(walletService, Mockito.never()).getWalletsByMerchantId(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Wallets: Should return 400 for invalid merchant UUID format")
    void getWallets_withInvalidMerchantUuidFormat_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/invalid-uuid/wallets/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Get Wallets: Should return JSON content type")
    void getWallets_shouldReturnJsonContentType() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(walletService.getWalletsByMerchantId(testMerchantId))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/")
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
        Mockito.when(walletService.getWalletByIdAndMerchantId(testWalletId, testMerchantId))
                .thenThrow(new RuntimeException("Database connection string: jdbc:postgresql://..."));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/" + testWalletId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().is5xxServerError());
    }

    @Test
    @DisplayName("Security: Should not allow access to another user's merchant wallets")
    void getWallets_forOtherUsersMerchant_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID otherUserMerchantId = UUID.randomUUID();

        Mockito.when(merchantService.getMerchantByIdAndUserId(otherUserMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + otherUserMerchantId + "/wallets/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(walletService, Mockito.never()).getWalletsByMerchantId(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Security: Should handle SQL injection attempts in UUID safely")
    void getWallet_withSqlInjectionInUuid_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        // UUIDs are strongly typed, SQL injection would fail validation
        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/'; DROP TABLE wallets;--/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(walletService, Mockito.never()).getWalletByIdAndMerchantId(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Security: Should handle XSS attempts safely")
    void getWallet_withXssAttempt_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        // UUID fields are strongly typed, XSS would fail validation
        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/<script>alert('xss')</script>/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        Mockito.verify(walletService, Mockito.never()).getWalletByIdAndMerchantId(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    // EDGE CASES TESTS

    @Test
    @DisplayName("Edge Case: Should handle wallet with zero balance")
    void getWallet_withZeroBalance_shouldSucceed() throws Exception {
        setupAuthentication();

        testWallet.setBalance(0L);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(walletService.getWalletByIdAndMerchantId(testWalletId, testMerchantId))
                .thenReturn(testWallet);
        Mockito.when(walletService.toResponse(testWallet))
                .thenReturn(createWalletResponse(testWallet));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/" + testWalletId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance").value(0L));
    }

    @Test
    @DisplayName("Edge Case: Should handle wallet with large balance")
    void getWallet_withLargeBalance_shouldSucceed() throws Exception {
        setupAuthentication();

        testWallet.setBalance(Long.MAX_VALUE);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(walletService.getWalletByIdAndMerchantId(testWalletId, testMerchantId))
                .thenReturn(testWallet);
        Mockito.when(walletService.toResponse(testWallet))
                .thenReturn(createWalletResponse(testWallet));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/" + testWalletId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance").value(Long.MAX_VALUE));
    }

    @Test
    @DisplayName("Edge Case: Should handle different currency types")
    void getWallet_withDifferentCurrencies_shouldSucceed() throws Exception {
        setupAuthentication();

        String[] currencies = {"USDT", "USDC", "BTC", "ETH", "DAI", "MATIC"};

        for (String currency : currencies) {
            testWallet.setCurrency(currency);

            Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                    .thenReturn(testMerchant);
            Mockito.when(walletService.getWalletByIdAndMerchantId(testWalletId, testMerchantId))
                    .thenReturn(testWallet);
            Mockito.when(walletService.toResponse(testWallet))
                    .thenReturn(createWalletResponse(testWallet));

            mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/" + testWalletId + "/")
                    .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value(currency));
        }
    }

    @Test
    @DisplayName("Edge Case: Should handle expired JWT token")
    void endpoints_withExpiredJwt_shouldReturnUnauthorized() throws Exception {
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.expired";
        Mockito.when(jwtService.isTokenValid(expiredToken)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, expiredToken)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("Edge Case: Should handle user not found in database after JWT validation")
    void endpoints_whenUserNotFoundAfterJwtValidation_shouldReturnUnauthorized() throws Exception {
        Mockito.when(jwtService.isTokenValid(testToken)).thenReturn(true);
        Mockito.when(jwtService.extractUserId(testToken)).thenReturn(Optional.of(testUser.getUserId().toString()));
        Mockito.when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("Edge Case: Should return correct wallet address in response")
    void getWallet_shouldReturnCorrectWalletAddress() throws Exception {
        setupAuthentication();

        String walletAddress = "0xabcdef1234567890abcdef1234567890abcdef12";
        testWallet.setWalletAddress(walletAddress);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(walletService.getWalletByIdAndMerchantId(testWalletId, testMerchantId))
                .thenReturn(testWallet);
        Mockito.when(walletService.toResponse(testWallet))
                .thenReturn(createWalletResponse(testWallet));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/" + testWalletId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.walletAddress").value(walletAddress));
    }

    @Test
    @DisplayName("Edge Case: Should handle single wallet for merchant")
    void getWallets_withSingleWallet_shouldReturnSingleElementList() throws Exception {
        setupAuthentication();

        List<Wallet> wallets = Arrays.asList(testWallet);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(walletService.getWalletsByMerchantId(testMerchantId))
                .thenReturn(wallets);
        Mockito.when(walletService.toResponse(testWallet))
                .thenReturn(createWalletResponse(testWallet));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].walletId").value(testWalletId.toString()));
    }

    @Test
    @DisplayName("Edge Case: Should handle multiple wallets with different currencies")
    void getWallets_withMultipleCurrencies_shouldReturnAllWallets() throws Exception {
        setupAuthentication();

        Wallet wallet2 = new Wallet();
        wallet2.setWalletId(UUID.randomUUID());
        wallet2.setMerchantId(testMerchantId);
        wallet2.setBalance(500000L);
        wallet2.setCurrency("BTC");
        wallet2.setWalletAddress("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh");

        Wallet wallet3 = new Wallet();
        wallet3.setWalletId(UUID.randomUUID());
        wallet3.setMerchantId(testMerchantId);
        wallet3.setBalance(2000000L);
        wallet3.setCurrency("ETH");
        wallet3.setWalletAddress("0x9876543210fedcba9876543210fedcba98765432");

        List<Wallet> wallets = Arrays.asList(testWallet, wallet2, wallet3);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(walletService.getWalletsByMerchantId(testMerchantId))
                .thenReturn(wallets);
        Mockito.when(walletService.toResponse(testWallet))
                .thenReturn(createWalletResponse(testWallet));
        Mockito.when(walletService.toResponse(wallet2))
                .thenReturn(createWalletResponse(wallet2));
        Mockito.when(walletService.toResponse(wallet3))
                .thenReturn(createWalletResponse(wallet3));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].currency").value("USDT"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].currency").value("BTC"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].currency").value("ETH"));
    }

    @Test
    @DisplayName("Edge Case: Should handle wallet with lowercase wallet address")
    void getWallet_withLowercaseWalletAddress_shouldSucceed() throws Exception {
        setupAuthentication();

        String walletAddress = "0xabcdef1234567890abcdef1234567890abcdef12";
        testWallet.setWalletAddress(walletAddress);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(walletService.getWalletByIdAndMerchantId(testWalletId, testMerchantId))
                .thenReturn(testWallet);
        Mockito.when(walletService.toResponse(testWallet))
                .thenReturn(createWalletResponse(testWallet));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/" + testWalletId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.walletAddress").value(walletAddress));
    }

    @Test
    @DisplayName("Edge Case: Should handle wallet with mixed case wallet address")
    void getWallet_withMixedCaseWalletAddress_shouldSucceed() throws Exception {
        setupAuthentication();

        String walletAddress = "0xAbCdEf1234567890aBcDeF1234567890AbCdEf12";
        testWallet.setWalletAddress(walletAddress);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(walletService.getWalletByIdAndMerchantId(testWalletId, testMerchantId))
                .thenReturn(testWallet);
        Mockito.when(walletService.toResponse(testWallet))
                .thenReturn(createWalletResponse(testWallet));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/" + testWalletId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.walletAddress").value(walletAddress));
    }

    @Test
    @DisplayName("Edge Case: Should handle Bitcoin wallet address format")
    void getWallet_withBitcoinAddressFormat_shouldSucceed() throws Exception {
        setupAuthentication();

        String btcAddress = "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh";
        testWallet.setWalletAddress(btcAddress);
        testWallet.setCurrency("BTC");

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(walletService.getWalletByIdAndMerchantId(testWalletId, testMerchantId))
                .thenReturn(testWallet);
        Mockito.when(walletService.toResponse(testWallet))
                .thenReturn(createWalletResponse(testWallet));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/" + testWalletId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.walletAddress").value(btcAddress))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value("BTC"));
    }

    @Test
    @DisplayName("Edge Case: Should verify all response fields are present")
    void getWallet_shouldReturnAllFields() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(walletService.getWalletByIdAndMerchantId(testWalletId, testMerchantId))
                .thenReturn(testWallet);
        Mockito.when(walletService.toResponse(testWallet))
                .thenReturn(createWalletResponse(testWallet));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/wallets/" + testWalletId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.walletId").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.currency").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.walletAddress").exists());
    }
}