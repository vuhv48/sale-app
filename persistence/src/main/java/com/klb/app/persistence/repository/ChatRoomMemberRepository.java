package com.klb.app.persistence.repository;

import com.klb.app.common.chat.ChatRoomDto;
import com.klb.app.persistence.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, ChatRoomMember.ChatRoomMemberId> {

	@Query(
			"""
					select case when count(m) > 0 then true else false end
					from ChatRoomMember m
					where m.chatRoom.id = :roomId and m.user.id = :userId
					  and m.isDeleted = false and m.chatRoom.isDeleted = false and m.user.isDeleted = false
					"""
	)
	boolean existsActiveMembership(@Param("roomId") UUID roomId, @Param("userId") UUID userId);

	@Query(
			"""
					select new com.klb.app.common.chat.ChatRoomDto(r.id, r.code, r.name, r.roomType, r.createdAt)
					from ChatRoomMember m
					join m.chatRoom r
					where m.user.id = :userId and m.isDeleted = false and r.isDeleted = false
					order by r.createdAt desc
					"""
	)
	List<ChatRoomDto> findActiveRoomsByUserId(@Param("userId") UUID userId);
}
