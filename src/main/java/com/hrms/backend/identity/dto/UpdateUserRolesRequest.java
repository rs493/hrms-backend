package com.hrms.backend.identity.dto;

import java.util.LinkedHashSet;
import java.util.Set;

import com.hrms.backend.identity.enums.RoleName;

import jakarta.validation.constraints.NotNull;

public class UpdateUserRolesRequest {

	@NotNull(message = "Roles are required.")
	private Set<RoleName> roles = new LinkedHashSet<>();

	public UpdateUserRolesRequest() {
	}

	public UpdateUserRolesRequest(Set<RoleName> roles) {
		this.roles = roles;
	}

	public Set<RoleName> getRoles() {
		return roles;
	}

	public void setRoles(Set<RoleName> roles) {
		this.roles = roles;
	}
}
