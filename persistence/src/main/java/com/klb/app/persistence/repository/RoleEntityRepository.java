package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleEntityRepository extends JpaRepository<RoleEntity, UUID> {

	Optional<RoleEntity> findByCodeAndIsDeletedFalseAndEnabledTrue(String code);
}
