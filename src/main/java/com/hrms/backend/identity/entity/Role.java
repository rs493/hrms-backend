package com.hrms.backend.identity.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.hrms.backend.identity.enums.RoleName;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
public class Role {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "role_id")
	private Integer roleId;

	@Enumerated(EnumType.STRING)
	@Column(name = "role_name", nullable = false, unique = true, length = 50)
	private RoleName roleName;

	@Column(name = "created_at", nullable = false, insertable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at")
	private Instant updatedAt;

	@OneToMany(mappedBy = "role")
	private Set<UserRole> userRoles = new HashSet<>();

	public Role() {
	}

	public Role(RoleName roleName) {
		this.roleName = roleName;
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

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Set<UserRole> getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(Set<UserRole> userRoles) {
		this.userRoles = userRoles;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Role role)) {
			return false;
		}
		return roleId != null && roleId.equals(role.roleId);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(roleId);
	}

	@Override
	public String toString() {
		return "Role{roleId=" + roleId + ", roleName=" + roleName + "}";
	}
}
