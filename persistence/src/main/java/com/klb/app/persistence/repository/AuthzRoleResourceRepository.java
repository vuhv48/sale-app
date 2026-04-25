package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.AuthzRoleResourceEntity;
import com.klb.app.persistence.entity.AuthzRoleResourceId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthzRoleResourceRepository extends JpaRepository<AuthzRoleResourceEntity, AuthzRoleResourceId> {
}
