package com.radman.shop.common.exception;


import com.radman.shop.common.ResultStatus;

public class ExpenseNotFoundException extends BusinessException {

    public ExpenseNotFoundException(String message) {
        super(message);
    }

    @Override
    public ResultStatus getResultStatus() {
        return ResultStatus.EXPENSE_NOT_FOUND;
    }

}
