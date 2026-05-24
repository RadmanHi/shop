package com.radman.shop.common.exception;

import com.radman.shop.common.ResultStatus;

public class CartAlreadyInCheckoutException extends BusinessException {

    public CartAlreadyInCheckoutException(String userId) {
        super("Cart already in checkout for userId: " + userId);
    }

    @Override
    public ResultStatus getResultStatus() {
        return ResultStatus.CART_ALREADY_IN_CHECKOUT;
    }
}