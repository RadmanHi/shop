package com.radman.shop.common;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class GeneralResponse extends ResponseService {

	public GeneralResponse(ResultStatus resultStatus) {
		this.setResult(resultStatus);
	}

	public GeneralResponse(Result result) {
		this.setResult(result);
	}

	public static GeneralResponse success() {
		return of(ResultStatus.SUCCESS);
	}

	public static GeneralResponse of(ResultStatus resultStatus) {
		return new GeneralResponse(resultStatus);
	}

	public String toString() {
		return "GeneralResponse(super=" + super.toString() + ")";
	}

}
