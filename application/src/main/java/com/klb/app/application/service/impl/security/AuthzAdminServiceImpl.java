package com.klb.app.application.service.impl.security;

import com.klb.app.application.service.security.AuthzAdminService;
import com.klb.app.application.service.security.AuthzResourceDto;
import com.klb.app.common.api.ErrorStatus;
import com.klb.app.common.exception.DomainException;
import com.klb.app.persistence.entity.AuthzResourceEntity;
import com.klb.app.persistence.entity.AuthzRoleResourceEntity;
import com.klb.app.persistence.repository.AuthzResourceRepository;
import com.klb.app.persistence.repository.AuthzRoleResourceRepository;
import com.klb.app.persistence.repository.RoleEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthzAdminServiceImpl implements AuthzAdminService {

	private final AuthzResourceRepository resourceRepository;
	private final AuthzRoleResourceRepository roleResourceRepository;
	private final RoleEntityRepository roleRepository;

	@Override
	@Transactional(readOnly = true)
	public List<AuthzResourceDto> listResources() {
		return resourceRepository.findAll().stream()
				.filter(r -> !r.isDeleted())
				.map(this::toResourceDto)
				.toList();
	}

	@Override
	@Transactional
	public AuthzResourceDto createResource(
			String resourceCode,
			String resourceGroup,
			String actionCode,
			String name,
			String urlPattern,
			String httpMethod) {
		String normCode = required(resourceCode, "resourceCode").toUpperCase(Locale.ROOT);
		String normGroup = required(resourceGroup, "resourceGroup").toUpperCase(Locale.ROOT);
		String normAction = required(actionCode, "actionCode").toUpperCase(Locale.ROOT);
		String normName = required(name, "name");
		String normPath = required(urlPattern, "urlPattern");
		String method = httpMethod == null || httpMethod.isBlank() ? null : httpMethod.trim().toUpperCase(Locale.ROOT);
		if (resourceRepository.findByResourceCodeAndIsDeletedFalse(normCode).isPresent()) {
			throw new DomainException(ErrorStatus.DATA_INTEGRITY, "Resource code already exists: " + normCode);
		}
		AuthzResourceEntity resource = new AuthzResourceEntity();
		resource.setResourceCode(normCode);
		resource.setResourceGroup(normGroup);
		resource.setActionCode(normAction);
		resource.setName(normName);
		resource.setUrlPattern(normPath);
		resource.setHttpMethod(method);
		resource.setEnabled(true);
		return toResourceDto(resourceRepository.save(resource));
	}

	@Override
	@Transactional
	public void assignRoleResource(String roleCode, String resourceCode) {
		var role = roleRepository.findByCodeAndIsDeletedFalseAndEnabledTrue(required(roleCode, "roleCode").toUpperCase(Locale.ROOT))
				.orElseThrow(() -> new DomainException(ErrorStatus.INVALID_ARGUMENT, "Role not found: " + roleCode));
		var resource = resourceRepository.findByResourceCodeAndIsDeletedFalse(required(resourceCode, "resourceCode").toUpperCase(Locale.ROOT))
				.orElseThrow(() -> new DomainException(ErrorStatus.INVALID_ARGUMENT, "Resource not found: " + resourceCode));
		AuthzRoleResourceEntity entity = new AuthzRoleResourceEntity();
		entity.setRole(role);
		entity.setResource(resource);
		roleResourceRepository.save(entity);
	}

	@Override
	@Transactional
	public void unassignRoleResource(String roleCode, String resourceCode) {
		var role = roleRepository.findByCodeAndIsDeletedFalseAndEnabledTrue(required(roleCode, "roleCode").toUpperCase(Locale.ROOT))
				.orElseThrow(() -> new DomainException(ErrorStatus.INVALID_ARGUMENT, "Role not found: " + roleCode));
		var resource = resourceRepository.findByResourceCodeAndIsDeletedFalse(required(resourceCode, "resourceCode").toUpperCase(Locale.ROOT))
				.orElseThrow(() -> new DomainException(ErrorStatus.INVALID_ARGUMENT, "Resource not found: " + resourceCode));
		AuthzRoleResourceEntity entity = roleResourceRepository.findById(new com.klb.app.persistence.entity.AuthzRoleResourceId(role.getId(), resource.getId()))
				.orElse(null);
		if (entity != null) {
			roleResourceRepository.delete(entity);
		}
	}

	private AuthzResourceDto toResourceDto(AuthzResourceEntity entity) {
		return new AuthzResourceDto(
				entity.getResourceCode(),
				entity.getResourceGroup(),
				entity.getActionCode(),
				entity.getName(),
				entity.getUrlPattern(),
				entity.getHttpMethod(),
				entity.isEnabled());
	}

	private static String required(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new DomainException(ErrorStatus.VALIDATION_ERROR, field + " is required");
		}
		return value.trim();
	}
}
