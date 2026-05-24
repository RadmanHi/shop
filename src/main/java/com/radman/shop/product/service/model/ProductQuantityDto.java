package com.radman.shop.product.service.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProductQuantityDto {
    private String productId;
    private Integer quantity;
}