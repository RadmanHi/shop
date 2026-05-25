package com.radman.shop.cart.api.model.response;

import java.math.BigDecimal;

public record CheckoutItemDto(
        String productId,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}