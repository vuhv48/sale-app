package com.klb.app.application.service.student;

import org.springframework.data.domain.Pageable;

/**
 * Use case sinh viên cho API (đọc / tạo có kiểm soát). Import bulk xem {@link StudentImportService}.
 */
public interface StudentService {

	StudentPageResponse listPage(Pageable pageable);

	StudentResponse create(String studentCode, String fullName);
}
