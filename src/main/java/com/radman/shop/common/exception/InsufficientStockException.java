package com.radman.shop.common.exception;

import com.radman.shop.common.ResultStatus;

public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(String productId, int requested, int available) {
        super("product.insufficient.stock");
    }

    @Override
    public ResultStatus getResultStatus() {
        return ResultStatus.INSUFFICIENT_STOCK;
    }
}