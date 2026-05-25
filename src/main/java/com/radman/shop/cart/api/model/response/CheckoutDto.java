package com.radman.shop.cart.api.model.response;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CheckoutDto(
        String userId,
        Instant expiresAt,
        BigDecimal totalAmount,
        List<CheckoutItemDto> items
) {
}