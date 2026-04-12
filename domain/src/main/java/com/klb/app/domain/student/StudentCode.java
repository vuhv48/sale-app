package com.klb.app.domain.student;

import com.klb.app.common.api.ErrorStatus;
import com.klb.app.common.exception.DomainException;

import java.util.Optional;

/**
 * Mã sinh viên đã chuẩn hoá; ràng buộc độ dài khớp cột DB.
 */
public final class StudentCode {

	public static final int MAX_LEN = 64;

	private final String value;

	private StudentCode(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	/** API tạo tay: mã bắt buộc, không rỗng sau trim, không vượt quá độ dài. */
	public static StudentCode parse(String raw) {
		if (raw == null || raw.trim().isEmpty()) {
			throw new DomainException(ErrorStatus.INVALID_STUDENT_CODE, "Mã sinh viên không được để trống");
		}
		String t = raw.trim();
		if (t.length() > MAX_LEN) {
			throw new DomainException(ErrorStatus.INVALID_STUDENT_CODE, "Mã sinh viên vượt quá " + MAX_LEN + " ký tự");
		}
		return new StudentCode(t);
	}

	/** Import batch: dòng rỗng / quá dài → bỏ qua, không ném lỗi. */
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
