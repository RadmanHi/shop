package com.radman.shop.common.exception;

import com.radman.shop.common.ResultStatus;

public class InvalidQuantityException extends BusinessException {

    public InvalidQuantityException(Integer quantity) {
        super("product.invalid.quantity");
    }

    @Override
    public ResultStatus getResultStatus() {
        return ResultStatus.INVALID_QUANTITY;
    }
}