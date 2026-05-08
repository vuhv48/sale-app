package com.klb.app.web.controller;

import com.klb.app.application.batch.DemoGreetingTrigger;
import com.klb.app.application.batch.StudentCsvImportTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class StudentBatchController {

	private final StudentCsvImportTrigger studentCsvImportTrigger;
	private final DemoGreetingTrigger demoGreetingTrigger;

	/**
	 * Chạy job import sinh viên mẫu từ {@code classpath:batch/sample-students.csv}.
	 * Bản ghi trùng {@code student_code} sẽ bị bỏ qua.
	 */
	@PostMapping("/jobs/student-csv-import")
	public ResponseEntity<Map<String, Object>> runStudentCsvImport() {
		var r = studentCsvImportTrigger.run();
		return ResponseEntity.ok(Map.of(
				"exitStatus", r.exitStatus(),
				"status", r.status(),
				"executionId", r.executionId()));
	}

	/**
	 * Chạy job demo đọc {@code classpath:batch/demo-greeting.csv} và in lời chào ra log/stdout.
	 */
	@PostMapping("/jobs/demo-greeting")
	public ResponseEntity<Map<String, Object>> runDemoGreeting() {
		var r = demoGreetingTrigger.run();
		return ResponseEntity.ok(Map.of(
				"jobName", r.jobName(),
				"jobInstanceId", r.jobInstanceId(),
				"jobExecutionId", r.jobExecutionId(),
				"status", r.status(),
				"exitStatus", r.exitStatus()));
	}
}
