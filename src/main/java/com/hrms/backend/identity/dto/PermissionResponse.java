package com.hrms.backend.identity.dto;

import java.time.Instant;

public class PermissionResponse {

	private Integer permissionId;

	private String permissionCode;

	private Instant createdAt;

	private Instant updatedAt;

	public PermissionResponse() {
	}

	public PermissionResponse(
			Integer permissionId,
			String permissionCode,
			Instant createdAt,
			Instant updatedAt) {
		this.permissionId = permissionId;
		this.permissionCode = permissionCode;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
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
