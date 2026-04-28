package com.klb.app.web.controller;

import com.klb.app.application.service.chat.InternalChatService;
import com.klb.app.common.chat.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

	private final InternalChatService internalChatService;

	@GetMapping("/rooms/{roomCode}/messages")
	public Page<ChatMessageDto> listMessages(
			@PathVariable String roomCode,
			@PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
	) {
		return internalChatService.listMessages(roomCode, pageable);
	}
}
