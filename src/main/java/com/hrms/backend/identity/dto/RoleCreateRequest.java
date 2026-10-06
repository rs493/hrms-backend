package com.hrms.backend.identity.dto;

import com.hrms.backend.identity.enums.RoleName;

import jakarta.validation.constraints.NotNull;

public class RoleCreateRequest {

	@NotNull(message = "Role name is required.")
	private RoleName roleName;

	public RoleCreateRequest() {
	}

	public RoleCreateRequest(RoleName roleName) {
		this.roleName = roleName;
	}

	public RoleName getRoleName() {
		return roleName;
	}

	public void setRoleName(RoleName roleName) {
		this.roleName = roleName;
	}
}
