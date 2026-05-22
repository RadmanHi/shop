package com.radman.shop.common.exception;


import com.radman.shop.common.ResultStatus;

public class GroupNotFoundException extends BusinessException {

	public GroupNotFoundException(String message) {
		super(message);
	}

	@Override
	public ResultStatus getResultStatus() {
		return ResultStatus.GROUP_NOT_FOUND;
	}

}
