package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

	boolean existsByStudentCode(String studentCode);

	List<Student> findAllByOrderByStudentCodeAsc();
}
