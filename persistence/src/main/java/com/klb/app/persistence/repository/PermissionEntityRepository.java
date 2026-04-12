package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PermissionEntityRepository extends JpaRepository<PermissionEntity, UUID> {

	Optional<PermissionEntity> findByCodeAndIsDeletedFalseAndEnabledTrue(String code);
}
