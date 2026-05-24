package com.radman.shop.product.api.model.response;

import java.math.BigDecimal;

public record ProductDto(String id, String name, BigDecimal price, Integer availableQuantity) {}
