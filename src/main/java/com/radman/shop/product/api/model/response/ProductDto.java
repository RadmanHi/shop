package com.radman.shop.product.api.model.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Setter
@Getter
@ToString
public class ProductDto {

    private String id;

    private String name;

    private BigDecimal price;

    private Integer availableQuantity;

}
