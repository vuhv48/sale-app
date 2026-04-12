package com.klb.app.domain.security;

import java.util.Optional;
import java.util.UUID;

/**
 * Port tải user cho Spring Security; triển khai ở {@code persistence}.
 */
public interface LoadUserForSecurityPort {

	Optional<UserSecuritySnapshot> loadByUsername(String username);

	Optional<UserSecuritySnapshot> loadByUserId(UUID userId);
}
