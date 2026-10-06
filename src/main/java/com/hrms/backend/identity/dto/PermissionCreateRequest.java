package com.hrms.backend.identity.dto;

import jakarta.validation.constraints.NotBlank;

public class PermissionCreateRequest {

	@NotBlank(message = "Permission code is required.")
	private String permissionCode;

	public PermissionCreateRequest() {
	}

	public PermissionCreateRequest(String permissionCode) {
		this.permissionCode = permissionCode;
	}

	public String getPermissionCode() {
		return permissionCode;
	}

	public void setPermissionCode(String permissionCode) {
		this.permissionCode = permissionCode;
	}
}
