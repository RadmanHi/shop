package com.radman.shop.common.exception;

import com.radman.shop.common.ResultStatus;

public class ProductNotFoundException extends BusinessException {

    public ProductNotFoundException(String productId) {
        super("product.not.found");
    }

    @Override
    public ResultStatus getResultStatus() {
        return ResultStatus.PRODUCT_NOT_FOUND;
    }
}