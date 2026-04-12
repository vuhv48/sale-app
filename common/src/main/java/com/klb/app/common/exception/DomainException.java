package com.klb.app.common.exception;

import com.klb.app.common.api.ErrorStatus;

public class DomainException extends RuntimeException {

	private final String code;

	public DomainException(String code, String message) {
		super(message);
		this.code = code;
	}

	public DomainException(ErrorStatus status, String message) {
		super(message);
		this.code = status.code();
	}

	public String getCode() {
		return code;
	}
}
