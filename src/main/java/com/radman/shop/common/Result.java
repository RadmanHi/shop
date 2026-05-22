package com.radman.shop.common;

import java.io.Serializable;

public class Result implements Serializable {

	private ResultStatus title;

	private int status;

	private String message;

	private ResultLevel level;

	public Result(ResultStatus title) {
		this.title = title;
		this.status = title.getStatus();
		this.message = title.getDescription();
		this.level = this.getResultLevel(title);
	}

	public Result(ResultStatus title, ResultLevel level) {
		this.title = title;
		this.status = title.getStatus();
		this.message = title.getDescription();
		this.level = level;
	}

	public Result() {
	}

	public ResultLevel getResultLevel(ResultStatus resultStatus) {
		if (resultStatus == ResultStatus.SUCCESS) {
			return ResultLevel.INFO;
		} else {
			return resultStatus == ResultStatus.FAILURE ? ResultLevel.BLOCKER : ResultLevel.WARN;
		}
	}

	public ResultStatus getTitle() {
		return this.title;
	}

	public void setTitle(final ResultStatus title) {
		this.title = title;
	}

	public int getStatus() {
		return this.status;
	}

	public void setStatus(final int status) {
		this.status = status;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	public ResultLevel getLevel() {
		return this.level;
	}

	public void setLevel(final ResultLevel level) {
		this.level = level;
	}

	public String toString() {
		ResultStatus var10000 = this.getTitle();
		return "Result(title=" + var10000 + ", status=" + this.getStatus() + ", message=" + this.getMessage()
				+ ", level=" + this.getLevel() + ")";
	}

}
