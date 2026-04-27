package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.DocumentTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DocumentTypeRepository extends JpaRepository<DocumentTypeEntity, UUID> {

	@Query("select d from DocumentTypeEntity d where d.code = :code and d.isDeleted = false")
	Optional<DocumentTypeEntity> findActiveByCode(@Param("code") String code);
}

