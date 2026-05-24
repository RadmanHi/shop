package com.radman.shop.product.service.model;

import java.util.List;

public record ProductsResult(
        List<ProductResult> products,
        Integer page,
        Integer size,
        Long totalElements,
        Integer totalPages
) {
}