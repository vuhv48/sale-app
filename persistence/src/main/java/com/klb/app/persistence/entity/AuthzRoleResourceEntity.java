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
@IdClass(AuthzRoleResourceId.class)
@Table(name = "role_resources", schema = "authz")
public class AuthzRoleResourceEntity extends BaseAuditableEntity {

	@Id
	@ManyToOne
	@JoinColumn(name = "role_id", nullable = false)
	private RoleEntity role;

	@Id
	@ManyToOne
	@JoinColumn(name = "resource_id", nullable = false)
	private AuthzResourceEntity resource;

}
