package com.klb.app.persistence.repository;

import com.klb.app.common.chat.ChatMessageDto;
import com.klb.app.persistence.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

	@Query(
			"""
					select new com.klb.app.common.chat.ChatMessageDto(
						m.id, r.code, s.id, m.senderUsername, m.body, m.createdAt)
					from ChatMessage m
					join m.chatRoom r
					join m.sender s
					where r.id = :roomId and m.isDeleted = false and r.isDeleted = false
					"""
	)
	Page<ChatMessageDto> pageDtosByRoomId(@Param("roomId") UUID roomId, Pageable pageable);
}
