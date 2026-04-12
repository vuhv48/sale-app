package com.klb.app.application.service.impl.student;

import com.klb.app.application.service.student.StudentPageResponse;
import com.klb.app.application.service.student.StudentResponse;
import com.klb.app.application.service.student.StudentService;
import com.klb.app.common.api.ErrorStatus;
import com.klb.app.common.exception.DomainException;
import com.klb.app.domain.student.StudentCode;
import com.klb.app.persistence.entity.Student;
import com.klb.app.persistence.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

	private final StudentRepository studentRepository;

	@Override
	@Transactional(readOnly = true)
	public StudentPageResponse listPage(Pageable pageable) {
		Page<Student> page = studentRepository.findAllActiveOrderByStudentCodeAsc(pageable);
		return new StudentPageResponse(
				page.getContent().stream().map(StudentServiceImpl::toResponse).toList(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.isFirst(),
				page.isLast());
	}

	@Override
	@Transactional
	public StudentResponse create(String studentCode, String fullName) {
		StudentCode code = StudentCode.parse(studentCode);
		if (studentRepository.existsByStudentCode(code.value())) {
			throw new DomainException(ErrorStatus.STUDENT_DUPLICATE_CODE, "Student code already exists: " + code.value());
		}
		Student s = new Student();
		s.setStudentCode(code.value());
		s.setFullName(fullName.trim());
		studentRepository.save(s);
		return toResponse(s);
	}

	private static StudentResponse toResponse(Student s) {
		return new StudentResponse(s.getId(), s.getStudentCode(), s.getFullName(), s.getCreatedAt());
	}
}
