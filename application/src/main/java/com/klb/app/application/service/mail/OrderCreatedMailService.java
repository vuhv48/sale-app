package com.klb.app.application.service.mail;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Sau khi tạo đơn hàng: ghi {@code mail_queue} + outbox Kafka (nếu bật) để gửi mail, không chặn HTTP.
 */
public interface OrderCreatedMailService {

	void enqueueOrderCreatedEmail(
			UUID orderId,
			String orderNo,
			String customerName,
			String customerEmail,
			BigDecimal totalAmount
	);
}
