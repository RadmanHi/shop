package com.radman.shop.cart.service.model;

import com.radman.shop.cart.model.CheckoutState;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CheckoutResult(
        String userId,
        CheckoutState checkoutState,
        Instant expiresAt,
        BigDecimal totalAmount,
        List<CheckoutItemResult> items
) {}