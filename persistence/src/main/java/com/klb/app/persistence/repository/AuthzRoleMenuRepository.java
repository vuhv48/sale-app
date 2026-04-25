package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.AuthzRoleMenuEntity;
import com.klb.app.persistence.entity.AuthzRoleMenuId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthzRoleMenuRepository extends JpaRepository<AuthzRoleMenuEntity, AuthzRoleMenuId> {
}
