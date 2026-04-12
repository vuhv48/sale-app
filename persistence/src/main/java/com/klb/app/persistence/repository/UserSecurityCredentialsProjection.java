package com.klb.app.persistence.repository;

import java.util.UUID;

/**
 * Kết quả native query {@code users} (chỉ bản ghi {@code is_deleted = false}).
 */
public interface UserSecurityCredentialsProjection {

	UUID getId();

	String getUsername();

	String getPasswordHash();

	boolean isEnabled();

	String getDataScope();
}
