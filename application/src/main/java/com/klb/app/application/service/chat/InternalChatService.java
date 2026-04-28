package com.klb.app.application.service.chat;

import com.klb.app.common.chat.ChatMessageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface InternalChatService {

	ChatMessageDto appendMessage(String roomCode, UUID senderId, String senderUsername, String body);

	Page<ChatMessageDto> listMessages(String roomCode, Pageable pageable);
}
