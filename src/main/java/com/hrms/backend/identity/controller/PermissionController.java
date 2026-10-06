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

import com.hrms.backend.identity.dto.PermissionCreateRequest;
import com.hrms.backend.identity.dto.PermissionResponse;
import com.hrms.backend.identity.dto.UpdateRolePermissionsRequest;
import com.hrms.backend.identity.entity.Permission;
import com.hrms.backend.identity.service.AuthorizationService;
import com.hrms.backend.identity.service.PermissionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class PermissionController {

	private static final String ROLE_MANAGE_PERMISSION = "ROLE_MANAGE";

	private final PermissionService permissionService;
	private final AuthorizationService authorizationService;

	public PermissionController(
			PermissionService permissionService,
			AuthorizationService authorizationService) {
		this.permissionService = permissionService;
		this.authorizationService = authorizationService;
	}

	@PostMapping("/permissions")
	@ResponseStatus(HttpStatus.CREATED)
	public PermissionResponse createPermission(@Valid @RequestBody PermissionCreateRequest request) {
		requireRoleManage();
		return toResponse(permissionService.createPermission(request.getPermissionCode()));
	}

	@GetMapping("/permissions")
	public List<PermissionResponse> getAllPermissions() {
		requireRoleManage();
		return permissionService.getAllPermissions().stream().map(this::toResponse).toList();
	}

	@GetMapping("/permissions/code/{permissionCode}")
	public PermissionResponse getPermissionByCode(@PathVariable String permissionCode) {
		requireRoleManage();
		return toResponse(permissionService.getPermissionByCode(permissionCode));
	}

	@GetMapping("/permissions/{permissionId}")
	public PermissionResponse getPermissionById(@PathVariable Integer permissionId) {
		requireRoleManage();
		return toResponse(permissionService.getPermissionById(permissionId));
	}

	@DeleteMapping("/permissions/{permissionId}")
	public ResponseEntity<Void> deletePermission(@PathVariable Integer permissionId) {
		requireRoleManage();
		permissionService.deletePermission(permissionId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/roles/{roleId}/permissions")
	public List<PermissionResponse> getPermissionsForRole(@PathVariable Integer roleId) {
		requireRoleManage();
		return permissionService.getPermissionsForRole(roleId).stream().map(this::toResponse).toList();
	}

	@PostMapping("/roles/{roleId}/permissions/{permissionId}")
	public ResponseEntity<Void> assignPermissionToRole(
			@PathVariable Integer roleId,
			@PathVariable Integer permissionId) {
		requireRoleManage();
		permissionService.assignPermissionToRole(roleId, permissionId);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
	public ResponseEntity<Void> removePermissionFromRole(
			@PathVariable Integer roleId,
			@PathVariable Integer permissionId) {
		requireRoleManage();
		permissionService.removePermissionFromRole(roleId, permissionId);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/roles/{roleId}/permissions")
	public ResponseEntity<Void> updateRolePermissions(
			@PathVariable Integer roleId,
			@Valid @RequestBody UpdateRolePermissionsRequest request) {
		requireRoleManage();
		permissionService.updateRolePermissions(roleId, request.getPermissionIds());
		return ResponseEntity.noContent().build();
	}

	private void requireRoleManage() {
		authorizationService.requirePermission(ROLE_MANAGE_PERMISSION);
	}

	private PermissionResponse toResponse(Permission permission) {
		return new PermissionResponse(
				permission.getPermissionId(),
				permission.getPermissionCode(),
				permission.getCreatedAt(),
				permission.getUpdatedAt());
	}
}
