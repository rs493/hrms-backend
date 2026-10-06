package com.hrms.backend.identity.service;

import java.util.List;
import java.util.Set;

import com.hrms.backend.identity.entity.Role;
import com.hrms.backend.identity.enums.RoleName;

public interface RoleService {

	Role createRole(RoleName roleName);

	Role getRoleById(Integer roleId);

	Role getRoleByName(RoleName roleName);

	List<Role> getAllRoles();

	Role updateRole(Integer roleId, RoleName roleName);

	void deleteRole(Integer roleId);

	void assignRoleToUser(Integer userId, RoleName roleName);

	void removeRoleFromUser(Integer userId, RoleName roleName);

	List<RoleName> getRolesForUser(Integer userId);

	boolean hasRole(Integer userId, RoleName roleName);

	void updateUserRoles(Integer userId, Set<RoleName> roleNames);
}
