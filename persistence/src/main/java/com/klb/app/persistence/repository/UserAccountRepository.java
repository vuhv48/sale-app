package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.UserAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

	@EntityGraph(attributePaths = {"roles", "roles.permissions", "directPermissions"})
	Optional<UserAccount> findByUsername(String username);

	@EntityGraph(attributePaths = {"roles", "roles.permissions", "directPermissions"})
	@Query("select u from UserAccount u where u.id = :id")
	Optional<UserAccount> findDetailedById(@Param("id") Long id);

	boolean existsByUsername(String username);
}
