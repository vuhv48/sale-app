package com.klb.app.common.api;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Mã lỗi API thống nhất (code + HTTP status + message mặc định).
 * Dùng trong {@link com.klb.app.common.exception.DomainException} và {@code RestExceptionHandler}.
 */
public enum ErrorStatus {

	// —— Xác thực / phân quyền ——
	AUTH_FAILED(401, "AUTH_FAILED", "Đăng nhập thất bại"),
	UNAUTHORIZED(401, "UNAUTHORIZED", "Chưa xác thực"),
	REFRESH_TOKEN_INVALID(401, "REFRESH_TOKEN_INVALID", "Refresh token không hợp lệ hoặc đã hết hạn"),
	FORBIDDEN(403, "FORBIDDEN", "Không đủ quyền"),
	TOO_MANY_REQUESTS(429, "TOO_MANY_REQUESTS", "Bạn thao tác quá nhanh, vui lòng thử lại sau"),

	// —— Dữ liệu đầu vào ——
	VALIDATION_ERROR(400, "VALIDATION_ERROR", "Dữ liệu không hợp lệ"),
	CONSTRAINT_VIOLATION(400, "CONSTRAINT_VIOLATION", "Vi phạm ràng buộc dữ liệu"),
	INVALID_ARGUMENT(400, "INVALID_ARGUMENT", "Tham số không hợp lệ"),
	INVALID_STUDENT_CODE(400, "INVALID_STUDENT_CODE", "Mã sinh viên không hợp lệ"),
	PASSWORD_MISMATCH(400, "PASSWORD_MISMATCH", "Mật khẩu hiện tại không đúng"),
	USER_NOT_FOUND(404, "USER_NOT_FOUND", "Không tìm thấy người dùng"),
	ACCOUNT_DISABLED(403, "ACCOUNT_DISABLED", "Tài khoản đã bị khóa"),

	// —— Nghiệp vụ / trạng thái ——
	USERNAME_TAKEN(409, "USERNAME_TAKEN", "Tên đăng nhập đã được sử dụng"),
	STUDENT_DUPLICATE_CODE(409, "STUDENT_DUPLICATE_CODE", "Mã sinh viên đã tồn tại"),
	DATA_INTEGRITY(409, "DATA_INTEGRITY", "Dữ liệu xung đột ràng buộc (trùng khóa, FK, …)"),
	ILLEGAL_STATE(409, "ILLEGAL_STATE", "Trạng thái không hợp lệ"),

	// —— Hệ thống ——
	DATA_ACCESS_ERROR(500, "DATA_ACCESS_ERROR", "Không thực hiện được thao tác dữ liệu"),
	BATCH_JOB_FAILED(500, "BATCH_JOB_FAILED", "Job xử lý thất bại"),
	INTERNAL_ERROR(500, "INTERNAL_ERROR", "Lỗi hệ thống không mong đợi");

	private static final Map<String, ErrorStatus> BY_CODE = Arrays.stream(values())
			.collect(Collectors.toUnmodifiableMap(ErrorStatus::code, Function.identity()));

	private final int httpStatus;
	private final String code;
	private final String defaultMessage;

	ErrorStatus(int httpStatus, String code, String defaultMessage) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.defaultMessage = defaultMessage;
	}

	public int httpStatus() {
		return httpStatus;
	}

	public String code() {
		return code;
	}

	public String defaultMessage() {
		return defaultMessage;
	}

	/** Tra theo mã string (khớp JSON {@code error.code}) — dùng khi bắt {@link com.klb.app.common.exception.DomainException}. */
	public static Optional<ErrorStatus> resolve(String code) {
		if (code == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(BY_CODE.get(code));
	}
}
