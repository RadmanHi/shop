package com.radman.shop.common.exception;

import com.radman.shop.common.ResultStatus;

public class CartItemNotFoundException extends BusinessException {

    public CartItemNotFoundException(String productId) {
        super("Cart item not found for productId: " + productId);
    }

    @Override
    public ResultStatus getResultStatus() {
        return ResultStatus.CART_ITEM_NOT_FOUND;
    }
}