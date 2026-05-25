package com.radman.shop.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI shopOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Shop API")
                        .version("1.0")
                        .description("""
                                E-commerce backend APIs.
                                
                                IMPORTANT:
                                - All Cart APIs require X-User-Id header
                                - Use 'user-1' for local/dev (seeded in DB)
                                """));
    }
}