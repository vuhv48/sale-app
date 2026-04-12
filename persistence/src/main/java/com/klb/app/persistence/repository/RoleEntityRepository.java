package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleEntityRepository extends JpaRepository<RoleEntity, Long> {

	Optional<RoleEntity> findByCode(String code);
}
