package com.klb.app.web.controller;

import com.klb.app.application.batch.StudentCsvImportTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class StudentBatchController {

	private final StudentCsvImportTrigger studentCsvImportTrigger;

	/**
	 * Chạy job import sinh viên mẫu từ {@code classpath:batch/sample-students.csv}.
	 * Bản ghi trùng {@code student_code} sẽ bị bỏ qua.
	 */
	@PostMapping("/jobs/student-csv-import")
	@PreAuthorize("hasAuthority('ADMIN_ACCESS')")
	public ResponseEntity<Map<String, Object>> runStudentCsvImport() {
		var r = studentCsvImportTrigger.run();
		return ResponseEntity.ok(Map.of(
				"exitStatus", r.exitStatus(),
				"status", r.status(),
				"executionId", r.executionId()));
	}
}
