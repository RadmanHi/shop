package com.radman.shop.cart.api.model.response;

import com.radman.shop.common.ResponseService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CheckoutResponse extends ResponseService {

    private CheckoutDto checkout;
}