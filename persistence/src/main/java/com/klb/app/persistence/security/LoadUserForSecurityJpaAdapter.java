package com.klb.app.persistence.security;

import com.klb.app.common.security.LoadUserForSecurityPort;
import com.klb.app.common.security.UserSecuritySnapshot;
import com.klb.app.persistence.repository.UserAccountRepository;
import com.klb.app.persistence.repository.UserSecurityCredentialsProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoadUserForSecurityJpaAdapter implements LoadUserForSecurityPort {

	private final UserAccountRepository users;

	@Override
	@Transactional(readOnly = true)
	public Optional<UserSecuritySnapshot> loadByUsername(String username) {
		return users.findActiveCredentialsByUsername(username).map(this::toSnapshot);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<UserSecuritySnapshot> loadByUserId(UUID userId) {
		return users.findActiveCredentialsById(userId).map(this::toSnapshot);
	}

	private UserSecuritySnapshot toSnapshot(UserSecurityCredentialsProjection c) {
		UUID id = c.getId();
		var roleCodes = new LinkedHashSet<>(users.findActiveRoleCodesByUserId(id));
		var perms = new LinkedHashSet<>(users.findActivePermissionCodesViaRolesByUserId(id));
		for (String p : users.findActiveDirectGrantedPermissionCodesByUserId(id)) {
			perms.add(p);
		}
		for (String p : users.findActiveDirectDeniedPermissionCodesByUserId(id)) {
			perms.remove(p);
		}
		return new UserSecuritySnapshot(
				id,
				c.getUsername(),
				c.getPasswordHash(),
				c.isEnabled(),
				c.getDataScope(),
				roleCodes,
				perms);
	}
}
