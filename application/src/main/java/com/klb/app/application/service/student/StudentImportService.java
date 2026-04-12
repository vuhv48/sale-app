package com.klb.app.application.service.student;

import java.util.Optional;

/**
 * Import sinh viên từ nguồn bulk (CSV, …): contract chỉ dùng {@link ImportedStudentRef}, không lộ entity JPA.
 */
public interface StudentImportService {

	/**
	 * Chèn bản ghi mới nếu mã hợp lệ và chưa tồn tại.
	 *
	 * @return rỗng nếu bỏ qua dòng (mã lỗi / trùng mã)
	 */
	Optional<ImportedStudentRef> importIfAbsent(String studentCode, String fullName);
}
