package com.klb.app.application.service.impl.notice;

import com.klb.app.application.service.notice.RecordNoticeService;
import com.klb.app.persistence.entity.NoticeEntity;
import com.klb.app.persistence.repository.NoticeEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordNoticeServiceImpl implements RecordNoticeService {

	private final NoticeEntityRepository noticeEntityRepository;

	@Override
	@Transactional
	public UUID record(String noticeType, String payloadJson) {
		Assert.hasText(noticeType, "noticeType");
		Assert.hasText(payloadJson, "payloadJson");
		NoticeEntity row = new NoticeEntity();
		row.setNoticeType(noticeType.trim());
		row.setPayload(payloadJson);
		return noticeEntityRepository.save(row).getId();
	}
}
