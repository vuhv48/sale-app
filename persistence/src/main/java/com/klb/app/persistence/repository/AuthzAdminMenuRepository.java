package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.AuthzAdminMenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthzAdminMenuRepository extends JpaRepository<AuthzAdminMenuEntity, UUID> {

	Optional<AuthzAdminMenuEntity> findByMenuCodeAndIsDeletedFalse(String menuCode);
}
