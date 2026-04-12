package com.klb.app.web.controller;

import com.klb.app.application.service.auth.AuthAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserAccountController {

	private final AuthAccountService authAccountService;

	@PostMapping("/{username}/lock")
	@PreAuthorize("hasAuthority('ADMIN_ACCESS')")
	public ResponseEntity<Void> lock(@PathVariable String username) {
		authAccountService.setUserEnabledByUsername(username, false);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{username}/unlock")
	@PreAuthorize("hasAuthority('ADMIN_ACCESS')")
	public ResponseEntity<Void> unlock(@PathVariable String username) {
		authAccountService.setUserEnabledByUsername(username, true);
		return ResponseEntity.noContent().build();
	}
}
