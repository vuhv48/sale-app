package com.klb.app.security.service.impl;

import com.klb.app.common.dto.UserSummaryResponse;
import com.klb.app.security.service.UserProfileReadService;
import com.klb.app.security.user.AppUserDetails;
import org.springframework.stereotype.Service;

@Service
public class UserProfileReadServiceImpl implements UserProfileReadService {

	@Override
	public UserSummaryResponse summarize(AppUserDetails user) {
		var s = user.snapshot();
		return new UserSummaryResponse(
				s.id(),
				s.username(),
				s.dataScope(),
				s.roleCodes(),
				s.effectivePermissionCodes());
	}
}
