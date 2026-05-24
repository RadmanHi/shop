package com.radman.shop.product.service.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Setter
@Getter
@ToString
public class ProductResult {

    private String id;

    private String name;

    private BigDecimal price;

    private Integer totalQuantity;

    private Integer reservedQuantity;

    private Long creationDate;
}