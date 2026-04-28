package com.klb.app.web.controller;

import com.klb.app.application.service.chat.InternalChatService;
import com.klb.app.common.chat.ChatMessageDto;
import com.klb.app.common.chat.ChatRoomDto;
import com.klb.app.security.user.AppUserDetails;
import com.klb.app.web.dto.ChatCreateDirectRoomRequest;
import com.klb.app.web.dto.ChatCreateGroupRoomRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

	private final InternalChatService internalChatService;

	@GetMapping("/rooms/mine")
	public List<ChatRoomDto> listMyRooms(@AuthenticationPrincipal AppUserDetails currentUser) {
		return internalChatService.listMyRooms(currentUser.getId());
	}

	@PostMapping("/direct-rooms")
	public ChatRoomDto openDirectRoom(
			@AuthenticationPrincipal AppUserDetails currentUser,
			@Valid @RequestBody ChatCreateDirectRoomRequest body
	) {
		return internalChatService.openDirectRoom(currentUser.getId(), body.peerUserId());
	}

	@PostMapping("/group-rooms")
	public ChatRoomDto createGroupRoom(
			@AuthenticationPrincipal AppUserDetails currentUser,
			@Valid @RequestBody ChatCreateGroupRoomRequest body
	) {
		return internalChatService.createGroupRoom(
				currentUser.getId(),
				body.code(),
				body.name(),
				body.memberUserIds()
		);
	}

	@GetMapping("/rooms/{roomCode}/messages")
	public Page<ChatMessageDto> listMessages(
			@AuthenticationPrincipal AppUserDetails currentUser,
			@PathVariable String roomCode,
			@PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
	) {
		return internalChatService.listMessages(roomCode, currentUser.getId(), pageable);
	}
}
