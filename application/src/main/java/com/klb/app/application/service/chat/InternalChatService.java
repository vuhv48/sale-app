package com.klb.app.application.service.chat;

import com.klb.app.common.chat.ChatMessageDto;
import com.klb.app.common.chat.ChatRoomDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface InternalChatService {

	ChatMessageDto appendMessage(String roomCode, UUID senderId, String senderUsername, String body);

	Page<ChatMessageDto> listMessages(String roomCode, UUID requesterId, Pageable pageable);

	List<ChatRoomDto> listMyRooms(UUID userId);

	ChatRoomDto openDirectRoom(UUID requesterId, UUID peerUserId);

	ChatRoomDto createGroupRoom(UUID ownerId, String code, String name, List<UUID> memberUserIds);
}
