package com.hrms.backend.identity.service;

import java.util.List;
import java.util.Set;

import com.hrms.backend.identity.entity.Permission;

public interface PermissionService {

	Permission createPermission(String permissionCode);

	Permission getPermissionById(Integer permissionId);

	Permission getPermissionByCode(String permissionCode);

	List<Permission> getAllPermissions();

	void deletePermission(Integer permissionId);

	List<Permission> getPermissionsForRole(Integer roleId);

	void assignPermissionToRole(Integer roleId, Integer permissionId);

	void removePermissionFromRole(Integer roleId, Integer permissionId);

	void updateRolePermissions(Integer roleId, Set<Integer> permissionIds);

	boolean hasPermission(Integer roleId, Integer permissionId);
}
