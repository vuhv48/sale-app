package com.klb.app.web.controller;

import com.klb.app.application.service.student.StudentPageResponse;
import com.klb.app.application.service.student.StudentResponse;
import com.klb.app.application.service.student.StudentService;
import com.klb.app.web.dto.CreateStudentRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

	private final StudentService studentService;

	@GetMapping
	public StudentPageResponse list(
			@PageableDefault(size = 20, sort = "studentCode") Pageable pageable
	) {
		return studentService.listPage(pageable);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public StudentResponse create(@Valid @RequestBody CreateStudentRequest body) {
		return studentService.create(body.studentCode(), body.fullName());
	}
}
