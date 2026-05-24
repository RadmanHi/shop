package com.radman.shop.product.api.model.response;

import com.radman.shop.common.PagedResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class GetAllProductsResponse extends PagedResponse {

    private List<ProductDto> products;

}