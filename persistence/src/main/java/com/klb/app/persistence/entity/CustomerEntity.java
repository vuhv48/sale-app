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

@Getter
@Setter
@Entity
@Table(name = "customers")
public class CustomerEntity extends BaseAuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "customer_code", nullable = false, unique = true, length = 32)
	private String customerCode;

	@Column(nullable = false, length = 255)
	private String name;

	@Column(length = 32)
	private String phone;

	@Column(length = 255)
	private String email;

	@Column(name = "tax_code", length = 64)
	private String taxCode;

	@Column(name = "address_line", length = 500)
	private String addressLine;

	@Column(name = "is_active", nullable = false)
	private boolean active = true;
}
