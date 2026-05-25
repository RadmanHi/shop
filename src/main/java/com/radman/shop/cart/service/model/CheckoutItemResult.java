package com.radman.shop.cart.service.model;

import java.math.BigDecimal;

public record CheckoutItemResult(String productId, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {}