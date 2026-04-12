package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

	Optional<RefreshTokenEntity> findByTokenHashAndRevokedIsFalse(String tokenHash);

	@Modifying
	@Query("update RefreshTokenEntity r set r.revoked = true where r.userId = :userId and r.revoked = false")
	int revokeAllActiveForUser(@Param("userId") Long userId);
}
