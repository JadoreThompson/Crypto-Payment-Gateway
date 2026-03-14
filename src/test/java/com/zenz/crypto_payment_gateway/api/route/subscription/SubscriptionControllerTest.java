package com.zenz.crypto_payment_gateway.api.route.subscription;

import com.zenz.crypto_payment_gateway.api.config.SecurityConfig;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.web.WebAppConfiguration;

@WebMvcTest(SubscriptionController.class)
@Import(SecurityConfig.class)
public class SubscriptionControllerTest {
}
