package com.hrms.backend.identity.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hrms.backend.identity.dto.RoleCreateRequest;
import com.hrms.backend.identity.dto.RoleResponse;
import com.hrms.backend.identity.dto.RoleUpdateRequest;
import com.hrms.backend.identity.dto.UpdateUserRolesRequest;
import com.hrms.backend.identity.entity.Role;
import com.hrms.backend.identity.enums.RoleName;
import com.hrms.backend.identity.service.AuthorizationService;
import com.hrms.backend.identity.service.RoleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class RoleController {

	private static final String ROLE_MANAGE_PERMISSION = "ROLE_MANAGE";

	private final RoleService roleService;
	private final AuthorizationService authorizationService;

	public RoleController(RoleService roleService, AuthorizationService authorizationService) {
		this.roleService = roleService;
		this.authorizationService = authorizationService;
	}

	@PostMapping("/roles")
	@ResponseStatus(HttpStatus.CREATED)
	public RoleResponse createRole(@Valid @RequestBody RoleCreateRequest request) {
		requireRoleManage();
		return toResponse(roleService.createRole(request.getRoleName()));
	}

	@GetMapping("/roles")
	public List<RoleResponse> getAllRoles() {
		requireRoleManage();
		return roleService.getAllRoles().stream().map(this::toResponse).toList();
	}

	@GetMapping("/roles/by-name/{roleName}")
	public RoleResponse getRoleByName(@PathVariable RoleName roleName) {
		requireRoleManage();
		return toResponse(roleService.getRoleByName(roleName));
	}

	@GetMapping("/roles/{roleId}")
	public RoleResponse getRoleById(@PathVariable Integer roleId) {
		requireRoleManage();
		return toResponse(roleService.getRoleById(roleId));
	}

	@PutMapping("/roles/{roleId}")
	public RoleResponse updateRole(
			@PathVariable Integer roleId,
			@Valid @RequestBody RoleUpdateRequest request) {
		requireRoleManage();
		return toResponse(roleService.updateRole(roleId, request.getRoleName()));
	}

	@DeleteMapping("/roles/{roleId}")
	public ResponseEntity<Void> deleteRole(@PathVariable Integer roleId) {
		requireRoleManage();
		roleService.deleteRole(roleId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/users/{userId}/roles")
	public List<RoleName> getRolesForUser(@PathVariable Integer userId) {
		requireRoleManage();
		return roleService.getRolesForUser(userId);
	}

	@PostMapping("/users/{userId}/roles/{roleName}")
	public ResponseEntity<Void> assignRoleToUser(
			@PathVariable Integer userId,
			@PathVariable RoleName roleName) {
		requireRoleManage();
		roleService.assignRoleToUser(userId, roleName);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/users/{userId}/roles/{roleName}")
	public ResponseEntity<Void> removeRoleFromUser(
			@PathVariable Integer userId,
			@PathVariable RoleName roleName) {
		requireRoleManage();
		roleService.removeRoleFromUser(userId, roleName);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/users/{userId}/roles")
	public ResponseEntity<Void> updateUserRoles(
			@PathVariable Integer userId,
			@Valid @RequestBody UpdateUserRolesRequest request) {
		requireRoleManage();
		roleService.updateUserRoles(userId, request.getRoles());
		return ResponseEntity.noContent().build();
	}

	private void requireRoleManage() {
		authorizationService.requirePermission(ROLE_MANAGE_PERMISSION);
	}

	private RoleResponse toResponse(Role role) {
		return new RoleResponse(
				role.getRoleId(),
				role.getRoleName(),
				role.getCreatedAt(),
				role.getUpdatedAt());
	}
}
