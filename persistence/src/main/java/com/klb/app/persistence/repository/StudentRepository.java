package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {

	@Query("select case when count(s) > 0 then true else false end from Student s where s.studentCode = :studentCode and s.isDeleted = false")
	boolean existsByStudentCode(@Param("studentCode") String studentCode);

	@Query("select s from Student s where s.isDeleted = false order by s.studentCode asc")
	Page<Student> findAllActiveOrderByStudentCodeAsc(Pageable pageable);
}
