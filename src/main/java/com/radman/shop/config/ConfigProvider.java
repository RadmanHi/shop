package com.radman.shop.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.time.Duration;

@RequiredArgsConstructor
@Configuration
public class ConfigProvider {
    private final Environment env;

    public boolean isStaleCheckoutExpiryEnabled() {
        return env.getRequiredProperty("scheduled.expire.stale.checkout.enabled", Boolean.class);
    }

    public Duration getCheckoutTimeout() {
        return Duration.ofMinutes(
                env.getRequiredProperty("shop.cart.checkout.timeout.minutes", Long.class)
        );
    }
}
