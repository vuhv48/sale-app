package com.klb.app.security.user;

import com.klb.app.domain.security.UserSecuritySnapshot;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public record AppUserDetails(UserSecuritySnapshot snapshot) implements UserDetails {

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Set<GrantedAuthority> out = new LinkedHashSet<>();
		for (String role : snapshot.roleCodes()) {
			out.add(new SimpleGrantedAuthority("ROLE_" + role));
		}
		for (String p : snapshot.effectivePermissionCodes()) {
			out.add(new SimpleGrantedAuthority(p));
		}
		return out;
	}

	@Override
	public String getPassword() {
		return snapshot.passwordHash();
	}

	@Override
	public String getUsername() {
		return snapshot.username();
	}

	@Override
	public boolean isEnabled() {
		return snapshot.enabled();
	}

	public Long getId() {
		return snapshot.id();
	}
}
