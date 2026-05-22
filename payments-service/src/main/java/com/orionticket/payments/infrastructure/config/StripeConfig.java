package com.orionticket.payments.infrastructure.config;

import com.stripe.StripeClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Bean
    public StripeClient stripeClient(@Value("${stripe.api-key}") String apiKey) {
        if (apiKey != null && apiKey.startsWith("sk_live")) {
            throw new IllegalArgumentException("Stripe API key must be a test mode key (sk_test_...)");
        }
        return new StripeClient(apiKey);
    }
}
