package com.radman.shop.product.api.model.response;

import com.radman.shop.common.ResponseService;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ProductResponse extends ResponseService {

	private ProductDto product;

}
