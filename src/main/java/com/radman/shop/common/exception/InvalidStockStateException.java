package com.radman.shop.common.exception;

import com.radman.shop.common.ResultStatus;

public class InvalidStockStateException extends BusinessException {

    public InvalidStockStateException(String productId) {
        super("product.invalid.stock.state");
    }

    @Override
    public ResultStatus getResultStatus() {
        return ResultStatus.INVALID_STOCK_STATE;
    }
}