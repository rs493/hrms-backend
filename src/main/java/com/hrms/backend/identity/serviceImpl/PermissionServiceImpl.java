package com.hrms.backend.identity.serviceImpl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.hrms.backend.common.exception.InvalidRequestException;
import com.hrms.backend.common.exception.ResourceConflictException;
import com.hrms.backend.common.exception.ResourceNotFoundException;
import com.hrms.backend.identity.bootstrap.InitialPermissionCatalog;
import com.hrms.backend.identity.entity.Permission;
import com.hrms.backend.identity.entity.Role;
import com.hrms.backend.identity.entity.RolePermission;
import com.hrms.backend.identity.repository.PermissionRepository;
import com.hrms.backend.identity.repository.RolePermissionRepository;
import com.hrms.backend.identity.service.PermissionService;
import com.hrms.backend.identity.service.RoleService;

@Service
public class PermissionServiceImpl implements PermissionService {

	private final PermissionRepository permissionRepository;
	private final RolePermissionRepository rolePermissionRepository;
	private final RoleService roleService;

	public PermissionServiceImpl(
			PermissionRepository permissionRepository,
			RolePermissionRepository rolePermissionRepository,
			RoleService roleService) {
		this.permissionRepository = permissionRepository;
		this.rolePermissionRepository = rolePermissionRepository;
		this.roleService = roleService;
	}

	@Override
	@Transactional
	public Permission createPermission(String permissionCode) {
		String normalizedCode = normalizePermissionCode(permissionCode);
		if (permissionRepository.existsByPermissionCode(normalizedCode)) {
			throw new ResourceConflictException("A permission with this code already exists.");
		}
		Permission permission = new Permission(normalizedCode);
		permission.setUpdatedAt(Instant.now());
		return permissionRepository.save(permission);
	}

	@Override
	@Transactional(readOnly = true)
	public Permission getPermissionById(Integer permissionId) {
		return requirePermissionById(permissionId);
	}

	@Override
	@Transactional(readOnly = true)
	public Permission getPermissionByCode(String permissionCode) {
		String normalizedCode = normalizePermissionCode(permissionCode);
		return permissionRepository.findByPermissionCode(normalizedCode)
				.orElseThrow(() -> new ResourceNotFoundException("Permission was not found."));
	}

	@Override
	@Transactional(readOnly = true)
	public List<Permission> getAllPermissions() {
		return permissionRepository.findAllByOrderByPermissionCodeAsc();
	}

	@Override
	@Transactional
	public void deletePermission(Integer permissionId) {
		Permission permission = requirePermissionById(permissionId);
		if (InitialPermissionCatalog.PERMISSION_CODES.contains(permission.getPermissionCode())) {
			throw new ResourceConflictException("System-defined permissions cannot be deleted.");
		}
		if (!rolePermissionRepository.findByPermission_PermissionId(permissionId).isEmpty()) {
			throw new ResourceConflictException(
					"Permission cannot be deleted while it is assigned to one or more roles.");
		}
		permissionRepository.delete(permission);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Permission> getPermissionsForRole(Integer roleId) {
		roleService.getRoleById(roleId);
		List<Permission> permissions = new ArrayList<>();
		for (RolePermission assignment : rolePermissionRepository.findByRole_RoleIdWithPermission(roleId)) {
			permissions.add(assignment.getPermission());
		}
		return permissions;
	}

	@Override
	@Transactional
	public void assignPermissionToRole(Integer roleId, Integer permissionId) {
		Role role = roleService.getRoleById(roleId);
		Permission permission = requirePermissionById(permissionId);
		if (rolePermissionRepository.existsByRole_RoleIdAndPermission_PermissionId(roleId, permissionId)) {
			throw new ResourceConflictException("The role already has this permission.");
		}
		rolePermissionRepository.save(new RolePermission(role, permission));
	}

	@Override
	@Transactional
	public void removePermissionFromRole(Integer roleId, Integer permissionId) {
		roleService.getRoleById(roleId);
		requirePermissionById(permissionId);
		RolePermission assignment = rolePermissionRepository
				.findByRole_RoleIdAndPermission_PermissionId(roleId, permissionId)
				.orElseThrow(() -> new ResourceNotFoundException("Permission assignment was not found."));
		rolePermissionRepository.delete(assignment);
	}

	@Override
	@Transactional
	public void updateRolePermissions(Integer roleId, Set<Integer> permissionIds) {
		if (permissionIds == null) {
			throw new InvalidRequestException("Permission ids are required.");
		}
		Role role = roleService.getRoleById(roleId);
		Set<Integer> requestedIds = new LinkedHashSet<>(permissionIds);
		Set<Permission> requestedPermissions = new LinkedHashSet<>();
		for (Integer permissionId : requestedIds) {
			if (permissionId == null) {
				throw new InvalidRequestException("Permission id is required.");
			}
			requestedPermissions.add(requirePermissionById(permissionId));
		}

		List<RolePermission> currentAssignments = rolePermissionRepository.findByRole_RoleId(roleId);
		Set<Integer> currentPermissionIds = new HashSet<>();
		for (RolePermission assignment : currentAssignments) {
			Integer currentPermissionId = assignment.getPermission().getPermissionId();
			currentPermissionIds.add(currentPermissionId);
			if (!requestedIds.contains(currentPermissionId)) {
				rolePermissionRepository.delete(assignment);
			}
		}

		for (Permission permission : requestedPermissions) {
			if (!currentPermissionIds.contains(permission.getPermissionId())) {
				rolePermissionRepository.save(new RolePermission(role, permission));
			}
		}
	}

	@Override
	@Transactional(readOnly = true)
	public boolean hasPermission(Integer roleId, Integer permissionId) {
		roleService.getRoleById(roleId);
		requirePermissionById(permissionId);
		return rolePermissionRepository.existsByRole_RoleIdAndPermission_PermissionId(roleId, permissionId);
	}

	private Permission requirePermissionById(Integer permissionId) {
		return permissionRepository.findById(permissionId)
				.orElseThrow(() -> new ResourceNotFoundException("Permission was not found."));
	}

	private String normalizePermissionCode(String permissionCode) {
		if (!StringUtils.hasText(permissionCode)) {
			throw new InvalidRequestException("Permission code is required.");
		}
		return permissionCode.trim().toUpperCase(Locale.ROOT);
	}
}
