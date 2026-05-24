package com.radman.shop.cart.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CheckoutState {
    IDLE(0),
    CHECKOUT_IN_PROGRESS(1);

    private final int value;
}