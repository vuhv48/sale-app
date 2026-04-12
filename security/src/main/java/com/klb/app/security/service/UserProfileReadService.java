package com.klb.app.security.service;

import com.klb.app.common.dto.UserSummaryResponse;
import com.klb.app.security.user.AppUserDetails;

/**
 * Đọc hồ sơ user hiện tại cho API (không phụ thuộc persistence trong web).
 */
public interface UserProfileReadService {

	UserSummaryResponse summarize(AppUserDetails user);
}
