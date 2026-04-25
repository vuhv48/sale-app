package com.klb.app.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@IdClass(AuthzRoleMenuId.class)
@Table(name = "role_menus", schema = "authz")
public class AuthzRoleMenuEntity extends BaseAuditableEntity {

	@Id
	@ManyToOne
	@JoinColumn(name = "role_id", nullable = false)
	private RoleEntity role;

	@Id
	@ManyToOne
	@JoinColumn(name = "menu_id", nullable = false)
	private AuthzAdminMenuEntity menu;
}
