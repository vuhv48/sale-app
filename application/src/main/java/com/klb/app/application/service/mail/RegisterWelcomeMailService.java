package com.klb.app.application.service.mail;

import java.util.UUID;

/**
 * Sau dang ky: ghi {@code mail_queue} + outbox Kafka (neu bat Kafka) de gui mail chao.
 */
public interface RegisterWelcomeMailService {

	/**
	 * @param contactEmail email tuy chon tu request; neu trong thi co the dung username neu giong dinh dang email
	 */
	void enqueueWelcomeEmail(UUID userId, String username, String contactEmail);
}
