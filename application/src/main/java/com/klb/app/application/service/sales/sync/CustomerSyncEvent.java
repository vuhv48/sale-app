package com.klb.app.application.service.sales.sync;

import java.time.Instant;

/**
 * 1 message = 1 customer (hoac 1 thay doi customer) de partition theo customerCode/customerId.
 * Listener se consume theo batch (List<String> JSON) va bulk-upsert xuong DB.
 */
public record CustomerSyncEvent(
		String eventId,
		String customerCode,
		String name,
		String phone,
		String email,
		String taxCode,
		String addressLine,
		Boolean active,
		Instant eventTime
) {}

