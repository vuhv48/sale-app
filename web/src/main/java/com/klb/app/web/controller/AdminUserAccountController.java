package com.klb.app.web.controller;

import com.klb.app.application.service.auth.AuthAccountService;
import com.klb.app.application.service.auth.AdminUserSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserAccountController {

	private final AuthAccountService authAccountService;

	@PostMapping("/{username}/lock")
	public ResponseEntity<Void> lock(@PathVariable String username) {
		authAccountService.setUserEnabledByUsername(username, false);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{username}/unlock")
	public ResponseEntity<Void> unlock(@PathVariable String username) {
		authAccountService.setUserEnabledByUsername(username, true);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/search")
	public List<AdminUserSearchResponse> searchByName(@RequestParam("name") String name) {
		return authAccountService.searchUsersByName(name);
	}
}
