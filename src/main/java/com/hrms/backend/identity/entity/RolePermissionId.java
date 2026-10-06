package com.hrms.backend.identity.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class RolePermissionId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "role_id")
	private Integer roleId;

	@Column(name = "permission_id")
	private Integer permissionId;

	public RolePermissionId() {
	}

	public RolePermissionId(Integer roleId, Integer permissionId) {
		this.roleId = roleId;
		this.permissionId = permissionId;
	}

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public Integer getPermissionId() {
		return permissionId;
	}

	public void setPermissionId(Integer permissionId) {
		this.permissionId = permissionId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof RolePermissionId that)) {
			return false;
		}
		return Objects.equals(roleId, that.roleId) && Objects.equals(permissionId, that.permissionId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(roleId, permissionId);
	}

	@Override
	public String toString() {
		return "RolePermissionId{roleId=" + roleId + ", permissionId=" + permissionId + "}";
	}
}
