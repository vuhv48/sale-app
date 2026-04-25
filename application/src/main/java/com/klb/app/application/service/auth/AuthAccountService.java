package com.klb.app.application.service.auth;

import com.klb.app.security.user.AppUserDetails;

import java.util.UUID;

/**
 * Use case xác thực: đăng ký, token, refresh, đổi mật khẩu, khóa/mở tài khoản.
 */
public interface AuthAccountService {

	AccessRefreshResult register(String username, String rawPassword, String contactEmail);

	AccessRefreshResult issueTokens(AppUserDetails principal);

	AccessRefreshResult rotateWithRefreshToken(String rawRefreshToken);

	void changePassword(UUID userId, String currentPassword, String newPassword);

	void setUserEnabledByUsername(String username, boolean enabled);

	void recordAdminLoginSuccess(UUID userId, String username, String ipAddress, String userAgent);
}
