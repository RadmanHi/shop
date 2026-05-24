package com.radman.shop.product.service.model;

import java.math.BigDecimal;

public record ProductResult(
        String id,
        String name,
        BigDecimal price,
        Integer totalQuantity,
        Integer reservedQuantity,
        Long creationDate
) {}