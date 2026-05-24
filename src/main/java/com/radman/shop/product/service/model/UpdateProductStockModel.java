package com.radman.shop.product.service.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class UpdateProductStockModel {

    private List<ProductQuantityDto> products;
}