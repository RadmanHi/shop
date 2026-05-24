package com.radman.shop.cart.service.model;

import java.math.BigDecimal;

public record CartItemResult(String id, String productId, Integer quantity, BigDecimal checkoutPriceSnapshot) {

}