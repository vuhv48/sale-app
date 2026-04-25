package com.klb.app.web.controller;

import com.klb.app.application.service.security.AuthzAdminService;
import com.klb.app.application.service.security.AuthzResourceDto;
import com.klb.app.web.dto.CreateAuthzResourceRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/authz")
@RequiredArgsConstructor
public class AdminAuthorizationController {

	private final AuthzAdminService authzAdminService;

	@GetMapping("/resources")
	public List<AuthzResourceDto> listResources() {
		return authzAdminService.listResources();
	}

	@PostMapping("/resources")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthzResourceDto createResource(@Valid @RequestBody CreateAuthzResourceRequest body) {
		return authzAdminService.createResource(
				body.resourceCode(),
				body.resourceGroup(),
				body.actionCode(),
				body.name(),
				body.urlPattern(),
				body.httpMethod());
	}

	@PostMapping("/roles/{roleCode}/resources/{resourceCode}")
	public ResponseEntity<Void> assignRoleResource(@PathVariable String roleCode, @PathVariable String resourceCode) {
		authzAdminService.assignRoleResource(roleCode, resourceCode);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/roles/{roleCode}/resources/{resourceCode}")
	public ResponseEntity<Void> unassignRoleResource(@PathVariable String roleCode, @PathVariable String resourceCode) {
		authzAdminService.unassignRoleResource(roleCode, resourceCode);
		return ResponseEntity.noContent().build();
	}
}
