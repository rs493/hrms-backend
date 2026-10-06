package com.hrms.backend.identity.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class UserRoleId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "user_id")
	private Integer userId;

	@Column(name = "role_id")
	private Integer roleId;

	public UserRoleId() {
	}

	public UserRoleId(Integer userId, Integer roleId) {
		this.userId = userId;
		this.roleId = roleId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof UserRoleId that)) {
			return false;
		}
		return Objects.equals(userId, that.userId) && Objects.equals(roleId, that.roleId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, roleId);
	}

	@Override
	public String toString() {
		return "UserRoleId{userId=" + userId + ", roleId=" + roleId + "}";
	}
}
