package com.radman.shop.cart.api.model.response;

import com.radman.shop.cart.model.CheckoutState;

import java.util.List;

public record CartDto(
        String id,
        String userId,
        CheckoutState checkoutState,
        Long checkoutExpiresAt,
        Long createdAt,
        List<CartItemDto> items
) {
}