package com.klb.app.application.service.impl.chat;

import com.klb.app.application.service.chat.InternalChatService;
import com.klb.app.common.chat.ChatMessageDto;
import com.klb.app.common.chat.ChatRoomDto;
import com.klb.app.common.api.ErrorStatus;
import com.klb.app.common.exception.DomainException;
import com.klb.app.persistence.entity.ChatMessage;
import com.klb.app.persistence.entity.ChatRoom;
import com.klb.app.persistence.entity.ChatRoomMember;
import com.klb.app.persistence.repository.ChatMessageRepository;
import com.klb.app.persistence.repository.ChatRoomMemberRepository;
import com.klb.app.persistence.repository.ChatRoomRepository;
import com.klb.app.persistence.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InternalChatServiceImpl implements InternalChatService {

	private static final int MAX_BODY_LENGTH = 4000;
	private static final int MAX_ROOM_CODE_LENGTH = 64;

	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomMemberRepository chatRoomMemberRepository;
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
		var sender = userAccountRepository.findActiveById(senderId)
				.orElseThrow(() -> new DomainException(ErrorStatus.USER_NOT_FOUND, "Người gửi không tồn tại"));
		if (!chatRoomMemberRepository.existsActiveMembership(room.getId(), senderId)) {
			throw new DomainException(ErrorStatus.FORBIDDEN, "Bạn không thuộc phòng chat này");
		}
		if (!StringUtils.hasText(senderUsername)) {
			senderUsername = sender.getUsername();
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
		if (userAccountRepository.findActiveById(requesterId).isEmpty()) {
			throw new DomainException(ErrorStatus.USER_NOT_FOUND, "Không tìm thấy người dùng");
		}
		if (!chatRoomMemberRepository.existsActiveMembership(room.getId(), requesterId)) {
			throw new DomainException(ErrorStatus.FORBIDDEN, "Bạn không thuộc phòng chat này");
		}
		return chatMessageRepository.pageDtosByRoomId(room.getId(), pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ChatRoomDto> listMyRooms(UUID userId) {
		if (userAccountRepository.findActiveById(userId).isEmpty()) {
			throw new DomainException(ErrorStatus.USER_NOT_FOUND, "Không tìm thấy người dùng");
		}
		return chatRoomMemberRepository.findActiveRoomsByUserId(userId);
	}

	@Override
	@Transactional
	public ChatRoomDto openDirectRoom(UUID requesterId, UUID peerUserId) {
		if (requesterId.equals(peerUserId)) {
			throw new DomainException(ErrorStatus.INVALID_ARGUMENT, "Không thể mở chat trực tiếp với chính mình");
		}
		var requester = userAccountRepository.findActiveById(requesterId)
				.orElseThrow(() -> new DomainException(ErrorStatus.USER_NOT_FOUND, "Không tìm thấy người dùng"));
		var peer = userAccountRepository.findActiveById(peerUserId)
				.orElseThrow(() -> new DomainException(ErrorStatus.USER_NOT_FOUND, "Không tìm thấy người dùng đối thoại"));
		String directKey = makeDirectKey(requesterId, peerUserId);
		var existing = chatRoomRepository.findActiveDirectByKey(directKey);
		if (existing.isPresent()) {
			return toRoomDto(existing.get());
		}
		var room = new ChatRoom();
		room.setCode("dm_" + UUID.randomUUID());
		room.setName("Direct: " + requester.getUsername() + " - " + peer.getUsername());
		room.setRoomType("DIRECT");
		room.setDirectKey(directKey);
		room.setCreatedBy(requester.getUsername());
		room.setUpdatedBy(requester.getUsername());
		chatRoomRepository.save(room);
		addMembership(room, requesterId, requester.getUsername());
		addMembership(room, peerUserId, requester.getUsername());
		return toRoomDto(room);
	}

	@Override
	@Transactional
	public ChatRoomDto createGroupRoom(UUID ownerId, String code, String name, List<UUID> memberUserIds) {
		var owner = userAccountRepository.findActiveById(ownerId)
				.orElseThrow(() -> new DomainException(ErrorStatus.USER_NOT_FOUND, "Không tìm thấy người tạo phòng"));
		if (!StringUtils.hasText(code) || !StringUtils.hasText(name)) {
			throw new DomainException(ErrorStatus.INVALID_ARGUMENT, "code và name là bắt buộc");
		}
		String normalizedCode = code.trim();
		if (normalizedCode.length() > MAX_ROOM_CODE_LENGTH) {
			throw new DomainException(ErrorStatus.INVALID_ARGUMENT, "room code tối đa " + MAX_ROOM_CODE_LENGTH + " ký tự");
		}
		if (chatRoomRepository.findActiveByCode(normalizedCode).isPresent()) {
			throw new DomainException(ErrorStatus.DATA_INTEGRITY, "Mã phòng đã tồn tại: " + normalizedCode);
		}
		var room = new ChatRoom();
		room.setCode(normalizedCode);
		room.setName(name.trim());
		room.setRoomType("GROUP");
		room.setCreatedBy(owner.getUsername());
		room.setUpdatedBy(owner.getUsername());
		chatRoomRepository.save(room);

		LinkedHashSet<UUID> members = new LinkedHashSet<>();
		members.add(ownerId);
		if (memberUserIds != null) {
			members.addAll(memberUserIds);
		}
		List<UUID> missingUsers = new ArrayList<>();
		for (UUID uid : members) {
			if (uid == null || userAccountRepository.findActiveById(uid).isEmpty()) {
				missingUsers.add(uid);
			}
		}
		if (!missingUsers.isEmpty()) {
			throw new DomainException(ErrorStatus.INVALID_ARGUMENT, "Danh sách thành viên không hợp lệ");
		}
		for (UUID uid : members) {
			addMembership(room, uid, owner.getUsername());
		}
		return toRoomDto(room);
	}

	private void addMembership(ChatRoom room, UUID userId, String actor) {
		if (chatRoomMemberRepository.existsActiveMembership(room.getId(), userId)) {
			return;
		}
		var member = new ChatRoomMember();
		member.setChatRoom(room);
		member.setUser(userAccountRepository.getReferenceById(userId));
		member.setCreatedBy(actor);
		member.setUpdatedBy(actor);
		chatRoomMemberRepository.save(member);
	}

	private static ChatRoomDto toRoomDto(ChatRoom room) {
		return new ChatRoomDto(room.getId(), room.getCode(), room.getName(), room.getRoomType(), room.getCreatedAt());
	}

	private static String makeDirectKey(UUID a, UUID b) {
		String s1 = a.toString();
		String s2 = b.toString();
		return s1.compareTo(s2) <= 0 ? s1 + ":" + s2 : s2 + ":" + s1;
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
