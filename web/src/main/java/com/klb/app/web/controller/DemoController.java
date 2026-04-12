package com.klb.app.web.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Example endpoints showing permission-based and role-based checks. Replace with real features later.
 */
@RestController
@RequestMapping("/api/demo")
public class DemoController {

	@GetMapping("/admin")
	@PreAuthorize("hasAuthority('ADMIN_ACCESS')")
	public Map<String, String> adminOnly() {
		return Map.of("message", "admin permission required");
	}

	@GetMapping("/user-role")
	@PreAuthorize("hasRole('USER')")
	public Map<String, String> userRole() {
		return Map.of("message", "ROLE_USER required");
	}
}
