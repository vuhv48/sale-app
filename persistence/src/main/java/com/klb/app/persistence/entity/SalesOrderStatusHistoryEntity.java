package com.klb.app.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "order_status_history")
public class SalesOrderStatusHistoryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private SalesOrderEntity order;

	@Enumerated(EnumType.STRING)
	@Column(name = "from_status", length = 32)
	private SalesOrderStatus fromStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "to_status", nullable = false, length = 32)
	private SalesOrderStatus toStatus;

	@Column(name = "changed_reason", length = 500)
	private String changedReason;

	@Column(name = "changed_by", length = 255)
	private String changedBy;

	@Column(name = "changed_at", nullable = false)
	private Instant changedAt = Instant.now();
}
