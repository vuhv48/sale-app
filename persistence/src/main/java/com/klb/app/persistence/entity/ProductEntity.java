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
@Table(name = "products")
public class ProductEntity extends BaseAuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "product_code", nullable = false, unique = true, length = 32)
	private String productCode;

	@Column(nullable = false, length = 255)
	private String name;

	@Column(columnDefinition = "text")
	private String description;

	@Column(name = "is_active", nullable = false)
	private boolean active = true;
}
