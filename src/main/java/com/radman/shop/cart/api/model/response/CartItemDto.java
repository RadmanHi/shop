package com.radman.shop.cart.api.model.response;

import java.math.BigDecimal;

public record CartItemDto(String id, String productId, Integer quantity, BigDecimal checkoutPriceSnapshot) {
}