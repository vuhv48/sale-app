package com.klb.app.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "chat_room_members")
@IdClass(ChatRoomMember.ChatRoomMemberId.class)
public class ChatRoomMember extends BaseAuditableEntity {

	@Id
	@ManyToOne(optional = false)
	@JoinColumn(name = "room_id", nullable = false)
	private ChatRoom chatRoom;

	@Id
	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private UserAccount user;

	public static class ChatRoomMemberId implements Serializable {
		private UUID chatRoom;
		private UUID user;

		public ChatRoomMemberId() {
		}

		public ChatRoomMemberId(UUID chatRoom, UUID user) {
			this.chatRoom = chatRoom;
			this.user = user;
		}

		public UUID getChatRoom() {
			return chatRoom;
		}

		public void setChatRoom(UUID chatRoom) {
			this.chatRoom = chatRoom;
		}

		public UUID getUser() {
			return user;
		}

		public void setUser(UUID user) {
			this.user = user;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof ChatRoomMemberId that)) {
				return false;
			}
			return Objects.equals(chatRoom, that.chatRoom) && Objects.equals(user, that.user);
		}

		@Override
		public int hashCode() {
			return Objects.hash(chatRoom, user);
		}
	}
}
