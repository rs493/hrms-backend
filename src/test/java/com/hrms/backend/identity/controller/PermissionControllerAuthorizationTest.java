package com.hrms.backend.identity.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.hrms.backend.identity.dto.PermissionCreateRequest;
import com.hrms.backend.identity.dto.UpdateRolePermissionsRequest;
import com.hrms.backend.identity.entity.Permission;
import com.hrms.backend.identity.service.AuthorizationService;
import com.hrms.backend.identity.service.PermissionService;

class PermissionControllerAuthorizationTest {

	private final PermissionService permissionService = org.mockito.Mockito.mock(PermissionService.class);
	private final AuthorizationService authorizationService = org.mockito.Mockito.mock(AuthorizationService.class);

	private PermissionController permissionController;

	@BeforeEach
	void setUp() {
		permissionController = new PermissionController(permissionService, authorizationService);
	}

	@Test
	void createPermission_requiresRoleManage() {
		doThrow(new AccessDeniedException("Required permission is missing: ROLE_MANAGE"))
				.when(authorizationService)
				.requirePermission("ROLE_MANAGE");

		assertThrows(
				AccessDeniedException.class,
				() -> permissionController.createPermission(new PermissionCreateRequest("REPORT_EXPORT")));

		verify(permissionService, never()).createPermission(anyString());
	}

	@Test
	void listPermissions_allowsWhenAuthorized() {
		when(permissionService.getAllPermissions()).thenReturn(List.of(permission(1, "ROLE_MANAGE")));

		permissionController.getAllPermissions();

		verify(authorizationService).requirePermission("ROLE_MANAGE");
		verify(permissionService).getAllPermissions();
	}

	@Test
	void updateRolePermissions_requiresRoleManage() {
		doThrow(new AccessDeniedException("Required permission is missing: ROLE_MANAGE"))
				.when(authorizationService)
				.requirePermission("ROLE_MANAGE");

		assertThrows(
				AccessDeniedException.class,
				() -> permissionController.updateRolePermissions(3, new UpdateRolePermissionsRequest(Set.of(1, 2))));

		verify(permissionService, never()).updateRolePermissions(anyInt(), any());
	}

	@Test
	void assignPermission_allowsWhenAuthorized() {
		permissionController.assignPermissionToRole(3, 8);

		verify(authorizationService).requirePermission("ROLE_MANAGE");
		verify(permissionService).assignPermissionToRole(eq(3), eq(8));
	}

	private static Permission permission(Integer id, String code) {
		Permission permission = new Permission(code);
		permission.setPermissionId(id);
		return permission;
	}
}
