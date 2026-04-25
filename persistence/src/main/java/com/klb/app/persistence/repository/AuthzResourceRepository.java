package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.AuthzResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthzResourceRepository extends JpaRepository<AuthzResourceEntity, UUID> {

	interface ResourceRuleProjection {

		String getResourceCode();

		String getUrlPattern();

		String getHttpMethod();
	}

	@Query(
			value = """
					select ar.resource_code as resourceCode,
					       ar.url_pattern as urlPattern,
					       ar.http_method as httpMethod
					from authz.resources ar
					where ar.is_deleted = false
					  and ar.is_enabled = true
					order by ar.resource_code
					""",
			nativeQuery = true)
	List<ResourceRuleProjection> findAllActiveResourceRules();

	@Query(
			value = """
					select distinct ar.resource_code
					from authz.user_roles ur
					inner join authz.roles r on r.id = ur.role_id and r.is_deleted = false and r.enabled = true
					inner join authz.role_resources arr on arr.role_id = r.id and arr.is_deleted = false
					inner join authz.resources ar on ar.id = arr.resource_id and ar.is_deleted = false and ar.is_enabled = true
					where ur.user_id = :userId
					  and ur.is_deleted = false
					""",
			nativeQuery = true)
	List<String> findActiveResourceCodesByUserId(@Param("userId") UUID userId);

	Optional<AuthzResourceEntity> findByResourceCodeAndIsDeletedFalse(String resourceCode);
}
