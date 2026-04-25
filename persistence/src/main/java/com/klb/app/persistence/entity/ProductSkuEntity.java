package com.klb.app.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "product_skus")
public class ProductSkuEntity extends BaseAuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private ProductEntity product;

	@Column(name = "sku_code", nullable = false, unique = true, length = 64)
	private String skuCode;

	@Column(name = "sku_name", nullable = false, length = 255)
	private String skuName;

	@Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
	private BigDecimal unitPrice;

	@Column(name = "is_active", nullable = false)
	private boolean active = true;
}
