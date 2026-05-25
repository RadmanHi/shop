package com.radman.shop.cart.api.model.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateItemQuantityRequest {

    @Min(1)
    private Integer quantity;
}