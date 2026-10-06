package com.hrms.backend.identity.entity;

import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "role_permissions")
public class RolePermission {

	@EmbeddedId
	private RolePermissionId id = new RolePermissionId();

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("roleId")
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("permissionId")
	@JoinColumn(name = "permission_id", nullable = false)
	private Permission permission;

	@Column(name = "created_at", nullable = false, insertable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false, insertable = false)
	private Instant updatedAt;

	public RolePermission() {
	}

	public RolePermission(Role role, Permission permission) {
		this.role = role;
		this.permission = permission;
	}

	public RolePermissionId getId() {
		return id;
	}

	public void setId(RolePermissionId id) {
		this.id = id;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Permission getPermission() {
		return permission;
	}

	public void setPermission(Permission permission) {
		this.permission = permission;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof RolePermission rolePermission)) {
			return false;
		}
		return id != null && id.equals(rolePermission.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@Override
	public String toString() {
		return "RolePermission{id=" + id + "}";
	}
}
