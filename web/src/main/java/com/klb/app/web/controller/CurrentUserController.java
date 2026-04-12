package com.klb.app.web.controller;

import com.klb.app.common.dto.UserSummaryResponse;
import com.klb.app.security.profile.UserProfileReadService;
import com.klb.app.security.user.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/current")
@RequiredArgsConstructor
public class CurrentUserController {

	private final UserProfileReadService userProfileReadService;

	@GetMapping
	public UserSummaryResponse currentUser(@AuthenticationPrincipal AppUserDetails user) {
		return userProfileReadService.summarize(user);
	}
}
