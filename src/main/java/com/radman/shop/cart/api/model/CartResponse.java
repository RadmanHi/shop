package com.radman.shop.cart.api.model;

import com.radman.shop.common.ResponseService;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse extends ResponseService {
    private CartDto cart;
}