package com.klb.app.persistence.repository;

import com.klb.app.persistence.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

	@Query("select r from ChatRoom r where r.code = :code and r.isDeleted = false")
	Optional<ChatRoom> findActiveByCode(@Param("code") String code);
}
