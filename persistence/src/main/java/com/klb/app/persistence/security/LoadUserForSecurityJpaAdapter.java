package com.klb.app.persistence.security;

import com.klb.app.domain.security.LoadUserForSecurityPort;
import com.klb.app.domain.security.UserSecuritySnapshot;
import com.klb.app.persistence.entity.PermissionEntity;
import com.klb.app.persistence.entity.RoleEntity;
import com.klb.app.persistence.entity.UserAccount;
import com.klb.app.persistence.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoadUserForSecurityJpaAdapter implements LoadUserForSecurityPort {

	private final UserAccountRepository users;

	@Override
	@Transactional(readOnly = true)
	public Optional<UserSecuritySnapshot> loadByUsername(String username) {
		return users.findByUsername(username).map(LoadUserForSecurityJpaAdapter::toSnapshot);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<UserSecuritySnapshot> loadByUserId(Long userId) {
		return users.findDetailedById(userId).map(LoadUserForSecurityJpaAdapter::toSnapshot);
	}

	private static UserSecuritySnapshot toSnapshot(UserAccount a) {
		Set<String> roleCodes = a.getRoles().stream()
				.map(RoleEntity::getCode)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		Set<String> perms = new LinkedHashSet<>();
		for (RoleEntity r : a.getRoles()) {
			for (PermissionEntity p : r.getPermissions()) {
				perms.add(p.getCode());
			}
		}
		for (PermissionEntity p : a.getDirectPermissions()) {
			perms.add(p.getCode());
		}
		return new UserSecuritySnapshot(
				a.getId(),
				a.getUsername(),
				a.getPasswordHash(),
				a.isEnabled(),
				a.getDataScope(),
				roleCodes,
				perms);
	}
}
