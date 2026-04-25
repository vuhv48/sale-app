package com.klb.app.security.user;

import com.klb.app.common.security.LoadUserForSecurityPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

	private final LoadUserForSecurityPort loadUserForSecurity;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return loadUserForSecurity.loadByUsername(username)
				.map(AppUserDetails::new)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
	}
}
