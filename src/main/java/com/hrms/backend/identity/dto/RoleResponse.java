package com.hrms.backend.identity.dto;

import java.time.Instant;

import com.hrms.backend.identity.enums.RoleName;

public class RoleResponse {

	private Integer roleId;

	private RoleName roleName;

	private Instant createdAt;

	private Instant updatedAt;

	public RoleResponse() {
	}

	public RoleResponse(Integer roleId, RoleName roleName, Instant createdAt, Instant updatedAt) {
		this.roleId = roleId;
		this.roleName = roleName;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public RoleName getRoleName() {
		return roleName;
	}

	public void setRoleName(RoleName roleName) {
		this.roleName = roleName;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
