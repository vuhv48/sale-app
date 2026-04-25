package com.klb.app.common.security;

import java.util.Optional;
import java.util.UUID;

public interface LoadUserForSecurityPort {

	Optional<UserSecuritySnapshot> loadByUsername(String username);

	Optional<UserSecuritySnapshot> loadByUserId(UUID userId);
}
