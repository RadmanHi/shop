package com.radman.shop.product.service.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class ProductsResult {

    private List<ProductResult> products;

    private Integer page;

    private Integer size;

    private Long totalElements;

    private Integer totalPages;

}
