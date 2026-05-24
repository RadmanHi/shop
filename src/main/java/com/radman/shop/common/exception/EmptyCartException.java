package com.radman.shop.common.exception;

import com.radman.shop.common.ResultStatus;

public class EmptyCartException extends BusinessException {

    public EmptyCartException(String userId) {
        super("Cart is empty for userId: " + userId);
    }

    @Override
    public ResultStatus getResultStatus() {
        return ResultStatus.EMPTY_CART;
    }
}