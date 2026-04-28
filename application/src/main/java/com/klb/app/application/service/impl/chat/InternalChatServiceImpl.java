package com.klb.app.application.service.impl.chat;

import com.klb.app.application.service.chat.InternalChatService;
import com.klb.app.common.chat.ChatMessageDto;
import com.klb.app.common.api.ErrorStatus;
import com.klb.app.common.exception.DomainException;
import com.klb.app.persistence.entity.ChatMessage;
import com.klb.app.persistence.repository.ChatMessageRepository;
import com.klb.app.persistence.repository.ChatRoomRepository;
import com.klb.app.persistence.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InternalChatServiceImpl implements InternalChatService {

	private static final int MAX_BODY_LENGTH = 4000;

	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final UserAccountRepository userAccountRepository;

	@Override
	@Transactional
	public ChatMessageDto appendMessage(String roomCode, UUID senderId, String senderUsername, String body) {
		if (!StringUtils.hasText(roomCode)) {
			throw new DomainException(ErrorStatus.INVALID_ARGUMENT, "roomCode không được để trống");
		}
		if (!StringUtils.hasText(body)) {
			throw new DomainException(ErrorStatus.INVALID_ARGUMENT, "Nội dung tin nhắn không được để trống");
		}
		if (body.length() > MAX_BODY_LENGTH) {
			throw new DomainException(ErrorStatus.INVALID_ARGUMENT, "Tin nhắn tối đa " + MAX_BODY_LENGTH + " ký tự");
		}
		var room = chatRoomRepository.findActiveByCode(roomCode.trim())
				.orElseThrow(() -> new DomainException(ErrorStatus.INVALID_ARGUMENT, "Phòng chat không tồn tại: " + roomCode));
		if (userAccountRepository.findActiveById(senderId).isEmpty()) {
			throw new DomainException(ErrorStatus.USER_NOT_FOUND, "Người gửi không tồn tại");
		}
		var msg = new ChatMessage();
		msg.setChatRoom(room);
		msg.setSender(userAccountRepository.getReferenceById(senderId));
		msg.setSenderUsername(senderUsername);
		msg.setBody(body.trim());
		msg.setCreatedBy(senderUsername);
		msg.setUpdatedBy(senderUsername);
		chatMessageRepository.save(msg);
		return toDto(msg, room.getCode());
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ChatMessageDto> listMessages(String roomCode, Pageable pageable) {
		var room = chatRoomRepository.findActiveByCode(roomCode.trim())
				.orElseThrow(() -> new DomainException(ErrorStatus.INVALID_ARGUMENT, "Phòng chat không tồn tại: " + roomCode));
		return chatMessageRepository.pageDtosByRoomId(room.getId(), pageable);
	}

	private static ChatMessageDto toDto(ChatMessage m, String roomCode) {
		return new ChatMessageDto(
				m.getId(),
				roomCode,
				m.getSender().getId(),
				m.getSenderUsername(),
				m.getBody(),
				m.getCreatedAt()
		);
	}
}
