package com.radman.shop.cart.api.model.response;

import com.radman.shop.common.ResponseService;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse extends ResponseService {
    private CartDto cart;
}