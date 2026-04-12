package com.klb.app.application.service.student;

import java.util.List;

/**
 * Một trang danh sách sinh viên (API, không phụ thuộc Spring Data ở JSON).
 */
public record StudentPageResponse(
		List<StudentResponse> content,
		int page,
		int size,
		long totalElements,
		int totalPages,
		boolean first,
		boolean last
) {
}
