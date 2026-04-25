package com.klb.app.application.service.notice;

import java.util.UUID;

/**
 * Ghi notice vao bang {@code notices}. Khong gui Kafka.
 */
public interface RecordNoticeService {

	UUID record(String noticeType, String payloadJson);
}
