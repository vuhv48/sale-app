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

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "admin_menus", schema = "authz")
public class AuthzAdminMenuEntity extends BaseAuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private AuthzAdminMenuEntity parent;

	@Column(name = "menu_code", nullable = false, unique = true, length = 64)
	private String menuCode;

	@Column(nullable = false, length = 128)
	private String title;

	@Column(name = "route_path", length = 255)
	private String routePath;

	@Column(length = 128)
	private String icon;

	@Column(nullable = false)
	private boolean hidden = false;

	@Column(name = "sort_order", nullable = false)
	private Integer sortOrder = 0;

	@Column(name = "is_enabled", nullable = false)
	private boolean enabled = true;
}
