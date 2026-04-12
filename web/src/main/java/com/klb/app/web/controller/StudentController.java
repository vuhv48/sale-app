package com.klb.app.web.controller;

import com.klb.app.application.student.StudentResponse;
import com.klb.app.application.student.StudentService;
import com.klb.app.web.dto.CreateStudentRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

	private final StudentService studentService;

	@GetMapping
	@PreAuthorize("hasAuthority('STUDENT_READ')")
	public List<StudentResponse> list() {
		return studentService.listAll();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAuthority('STUDENT_CREATE')")
	public StudentResponse create(@Valid @RequestBody CreateStudentRequest body) {
		return studentService.create(body.studentCode(), body.fullName());
	}
}
