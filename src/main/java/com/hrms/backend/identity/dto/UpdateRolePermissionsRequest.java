package com.hrms.backend.identity.dto;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.validation.constraints.NotNull;

public class UpdateRolePermissionsRequest {

	@NotNull(message = "Permission ids are required.")
	private Set<Integer> permissionIds = new LinkedHashSet<>();

	public UpdateRolePermissionsRequest() {
	}

	public UpdateRolePermissionsRequest(Set<Integer> permissionIds) {
		this.permissionIds = permissionIds;
	}

	public Set<Integer> getPermissionIds() {
		return permissionIds;
	}

	public void setPermissionIds(Set<Integer> permissionIds) {
		this.permissionIds = permissionIds;
	}
}
