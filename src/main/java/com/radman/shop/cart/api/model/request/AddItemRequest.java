package com.radman.shop.cart.api.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddItemRequest {

    @NotBlank
    private String productId;

    @Min(1)
    private Integer quantity;
}
