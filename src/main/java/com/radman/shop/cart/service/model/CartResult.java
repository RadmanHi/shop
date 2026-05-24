package com.radman.shop.cart.service.model;

import com.radman.shop.cart.model.CheckoutState;

import java.util.List;

public record CartResult(
        String id,
        String userId,
        CheckoutState checkoutState,
        Long checkoutExpiresAt,
        Long createdAt,
        List<CartItemResult> items
) {
}