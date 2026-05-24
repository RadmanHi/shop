package com.radman.shop.cart.api.model;

import java.math.BigDecimal;

public record CartItemDto(String id, String productId, Integer quantity, BigDecimal checkoutPriceSnapshot) {
}