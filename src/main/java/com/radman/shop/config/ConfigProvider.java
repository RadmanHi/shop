package com.radman.shop.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@RequiredArgsConstructor
@Configuration
public class ConfigProvider {
    private final Environment env;

    public boolean isStaleCheckoutExpiryEnabled() {
        return env.getRequiredProperty("scheduled.expire.stale.checkout.enabled", Boolean.class);
    }

}
