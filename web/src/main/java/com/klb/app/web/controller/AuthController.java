package com.klb.app.web.controller;

import com.klb.app.application.service.auth.AccessRefreshResult;
import com.klb.app.application.service.auth.AuthAccountService;
import com.klb.app.application.service.auth.LoginRateLimitService;
import com.klb.app.common.api.ErrorStatus;
import com.klb.app.common.exception.DomainException;
import com.klb.app.security.user.AppUserDetails;
import com.klb.app.web.dto.ChangePasswordRequest;
import com.klb.app.web.dto.LoginRequest;
import com.klb.app.web.dto.LoginResponse;
import com.klb.app.web.dto.RefreshTokenRequest;
import com.klb.app.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final AuthAccountService authAccountService;
	private final LoginRateLimitService loginRateLimitService;

	@PostMapping("/register")
	public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest body) {
		var tokens = authAccountService.register(body.username(), body.password(), body.email());
		return ResponseEntity.status(HttpStatus.CREATED).body(toLoginResponse(tokens));
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest body, HttpServletRequest request) {
		String clientIp = resolveClientIp(request);
		if (!loginRateLimitService.isAllowed(clientIp)) {
			throw new DomainException(
					ErrorStatus.TOO_MANY_REQUESTS,
					"Bạn đã vượt quá 5 lần đăng nhập trong 1 phút từ IP: " + clientIp
			);
		}
		var auth = authenticationManager.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated(body.username(), body.password()));
		var principal = (AppUserDetails) auth.getPrincipal();
		var tokens = authAccountService.issueTokens(principal);
		loginRateLimitService.clear(clientIp);
		authAccountService.recordAdminLoginSuccess(
				principal.getId(),
				principal.getUsername(),
				request.getRemoteAddr(),
				request.getHeader("User-Agent"));
		return ResponseEntity.ok(toLoginResponse(tokens));
	}

	@PostMapping("/refresh")
	public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest body) {
		var tokens = authAccountService.rotateWithRefreshToken(body.refreshToken());
		return ResponseEntity.ok(toLoginResponse(tokens));
	}

	@PostMapping("/change-password")
	public ResponseEntity<Void> changePassword(
			@AuthenticationPrincipal AppUserDetails principal,
			@Valid @RequestBody ChangePasswordRequest body
	) {
		authAccountService.changePassword(principal.getId(), body.currentPassword(), body.newPassword());
		return ResponseEntity.noContent().build();
	}

	private static LoginResponse toLoginResponse(AccessRefreshResult r) {
		return new LoginResponse(
				r.accessToken(),
				r.refreshToken(),
				"Bearer",
				r.accessExpiresInSeconds(),
				r.refreshExpiresInSeconds());
	}

	private static String resolveClientIp(HttpServletRequest request) {
		String forwardedFor = request.getHeader("X-Forwarded-For");
		if (forwardedFor != null && !forwardedFor.isBlank()) {
			String[] parts = forwardedFor.split(",");
			if (parts.length > 0 && !parts[0].isBlank()) {
				return parts[0].trim();
			}
		}
		String realIp = request.getHeader("X-Real-IP");
		if (realIp != null && !realIp.isBlank()) {
			return realIp.trim();
		}
		return request.getRemoteAddr();
	}
}
