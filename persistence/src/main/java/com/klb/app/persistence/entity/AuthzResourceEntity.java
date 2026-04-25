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
@Table(name = "resources", schema = "authz")
public class AuthzResourceEntity extends BaseAuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "resource_code", nullable = false, unique = true, length = 128)
	private String resourceCode;

	@Column(name = "resource_group", nullable = false, length = 64)
	private String resourceGroup;

	@Column(name = "action_code", nullable = false, length = 32)
	private String actionCode;

	@Column(nullable = false, length = 255)
	private String name;

	@Column(name = "url_pattern", nullable = false, length = 255)
	private String urlPattern;

	@Column(name = "http_method", length = 16)
	private String httpMethod;

	@Column(name = "is_enabled", nullable = false)
	private boolean enabled = true;
}
