package com.hrms.backend.identity.entity;

import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "permissions")
public class Permission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "permission_id")
	private Integer permissionId;

	@Column(name = "permission_code", nullable = false, unique = true, length = 100)
	private String permissionCode;

	@Column(name = "created_at", nullable = false, insertable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at")
	private Instant updatedAt;

	public Permission() {
	}

	public Permission(String permissionCode) {
		this.permissionCode = permissionCode;
	}

	public Integer getPermissionId() {
		return permissionId;
	}

	public void setPermissionId(Integer permissionId) {
		this.permissionId = permissionId;
	}

	public String getPermissionCode() {
		return permissionCode;
	}

	public void setPermissionCode(String permissionCode) {
		this.permissionCode = permissionCode;
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
		if (!(other instanceof Permission permission)) {
			return false;
		}
		return permissionId != null && permissionId.equals(permission.permissionId);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(permissionId);
	}

	@Override
	public String toString() {
		return "Permission{permissionId=" + permissionId + ", permissionCode='" + permissionCode + "'}";
	}
}
