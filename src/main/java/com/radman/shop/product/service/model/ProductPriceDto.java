package com.radman.shop.product.service.model;

import java.math.BigDecimal;

public record ProductPriceDto(String productId, BigDecimal price) {}