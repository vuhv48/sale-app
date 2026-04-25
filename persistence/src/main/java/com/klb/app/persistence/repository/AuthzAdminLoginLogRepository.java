package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.AuthzAdminLoginLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthzAdminLoginLogRepository extends JpaRepository<AuthzAdminLoginLogEntity, Long> {
}
