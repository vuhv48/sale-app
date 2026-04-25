package com.klb.app.application.service.student;

import com.klb.app.common.api.ErrorStatus;
import com.klb.app.common.exception.DomainException;

import java.util.Optional;

public final class StudentCode {

	public static final int MAX_LEN = 64;

	private final String value;

	private StudentCode(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	public static StudentCode parse(String raw) {
		if (raw == null || raw.trim().isEmpty()) {
			throw new DomainException(ErrorStatus.INVALID_STUDENT_CODE, "Ma sinh vien khong duoc de trong");
		}
		String t = raw.trim();
		if (t.length() > MAX_LEN) {
			throw new DomainException(ErrorStatus.INVALID_STUDENT_CODE, "Ma sinh vien vuot qua " + MAX_LEN + " ky tu");
		}
		return new StudentCode(t);
	}

	public static Optional<StudentCode> tryParseForImport(String raw) {
		if (raw == null) {
			return Optional.empty();
		}
		String t = raw.trim();
		if (t.isEmpty() || t.length() > MAX_LEN) {
			return Optional.empty();
		}
		return Optional.of(new StudentCode(t));
	}
}
