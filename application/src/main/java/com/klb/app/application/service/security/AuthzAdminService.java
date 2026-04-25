package com.klb.app.application.service.security;

import java.util.List;

public interface AuthzAdminService {

	List<AuthzResourceDto> listResources();

	AuthzResourceDto createResource(
			String resourceCode,
			String resourceGroup,
			String actionCode,
			String name,
			String urlPattern,
			String httpMethod);

	void assignRoleResource(String roleCode, String resourceCode);

	void unassignRoleResource(String roleCode, String resourceCode);
}
