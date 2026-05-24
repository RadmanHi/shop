package com.radman.shop.common.exception;

import com.radman.shop.common.ResultStatus;

public class CartNotFoundException extends BusinessException {

    public CartNotFoundException(String userId) {
        super("Cart not found for userId: " + userId);
    }

    @Override
    public ResultStatus getResultStatus() {
        return ResultStatus.CART_NOT_FOUND;
    }
}