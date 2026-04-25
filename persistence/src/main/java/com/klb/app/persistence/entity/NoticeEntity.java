package com.klb.app.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Ban ghi thong bao / su kien luu PostgreSQL. Chi persist — khong tu publish Kafka.
 */
@Getter
@Setter
@Entity
@Table(name = "notices")
public class NoticeEntity extends BaseAuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "notice_type", nullable = false, length = 128)
	private String noticeType;

	/** Noi dung, thuong la JSON string. */
	@Column(nullable = false, columnDefinition = "text")
	private String payload;
}
