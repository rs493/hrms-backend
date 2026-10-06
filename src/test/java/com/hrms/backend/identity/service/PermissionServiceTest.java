package com.hrms.backend.identity.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.hrms.backend.common.exception.InvalidRequestException;
import com.hrms.backend.common.exception.ResourceConflictException;
import com.hrms.backend.common.exception.ResourceNotFoundException;
import com.hrms.backend.identity.bootstrap.InitialPermissionCatalog;
import com.hrms.backend.identity.entity.Permission;
import com.hrms.backend.identity.entity.Role;
import com.hrms.backend.identity.entity.RolePermission;
import com.hrms.backend.identity.entity.RolePermissionId;
import com.hrms.backend.identity.enums.RoleName;
import com.hrms.backend.identity.repository.PermissionRepository;
import com.hrms.backend.identity.repository.RolePermissionRepository;
import com.hrms.backend.identity.service.PermissionService;
import com.hrms.backend.identity.service.RoleService;
import com.hrms.backend.identity.serviceImpl.PermissionServiceImpl;

class PermissionServiceTest {

	private final PermissionRepository permissionRepository = org.mockito.Mockito.mock(PermissionRepository.class);
	private final RolePermissionRepository rolePermissionRepository =
			org.mockito.Mockito.mock(RolePermissionRepository.class);
	private final RoleService roleService = org.mockito.Mockito.mock(RoleService.class);

	private PermissionService permissionService;

	@BeforeEach
	void setUp() {
		permissionService = new PermissionServiceImpl(permissionRepository, rolePermissionRepository, roleService);
	}

	@Test
	void createPermission_normalizesAndSaves() {
		when(permissionRepository.existsByPermissionCode("EMPLOYEE_CREATE")).thenReturn(false);
		when(permissionRepository.save(any(Permission.class))).thenAnswer(invocation -> {
			Permission permission = invocation.getArgument(0);
			permission.setPermissionId(11);
			return permission;
		});

		Permission created = permissionService.createPermission(" employee_create ");

		assertEquals(11, created.getPermissionId());
		assertEquals("EMPLOYEE_CREATE", created.getPermissionCode());
		ArgumentCaptor<Permission> captor = ArgumentCaptor.forClass(Permission.class);
		verify(permissionRepository).save(captor.capture());
		assertEquals("EMPLOYEE_CREATE", captor.getValue().getPermissionCode());
	}

	@Test
	void createPermission_rejectsBlankCode() {
		assertThrows(InvalidRequestException.class, () -> permissionService.createPermission("   "));
		verify(permissionRepository, never()).save(any());
	}

	@Test
	void createPermission_rejectsDuplicate() {
		when(permissionRepository.existsByPermissionCode("ROLE_MANAGE")).thenReturn(true);

		assertThrows(ResourceConflictException.class, () -> permissionService.createPermission("role_manage"));
		verify(permissionRepository, never()).save(any());
	}

	@Test
	void getPermissionById_returnsPermission() {
		Permission permission = permission(5, "TEAM_VIEW");
		when(permissionRepository.findById(5)).thenReturn(Optional.of(permission));

		assertEquals(permission, permissionService.getPermissionById(5));
	}

	@Test
	void getPermissionById_missingThrows() {
		when(permissionRepository.findById(99)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> permissionService.getPermissionById(99));
	}

	@Test
	void getPermissionByCode_normalizesAndReturns() {
		Permission permission = permission(7, "LEAVE_APPLY");
		when(permissionRepository.findByPermissionCode("LEAVE_APPLY")).thenReturn(Optional.of(permission));

		assertEquals(permission, permissionService.getPermissionByCode(" leave_apply "));
	}

	@Test
	void getAllPermissions_returnsOrderedList() {
		List<Permission> permissions = List.of(permission(1, "ATTENDANCE_MARK"), permission(2, "ROLE_MANAGE"));
		when(permissionRepository.findAllByOrderByPermissionCodeAsc()).thenReturn(permissions);

		assertEquals(permissions, permissionService.getAllPermissions());
	}

	@Test
	void deletePermission_rejectsSystemDefined() {
		String systemCode = InitialPermissionCatalog.PERMISSION_CODES.getFirst();
		Permission permission = permission(1, systemCode);
		when(permissionRepository.findById(1)).thenReturn(Optional.of(permission));

		assertThrows(ResourceConflictException.class, () -> permissionService.deletePermission(1));
		verify(permissionRepository, never()).delete(any());
	}

	@Test
	void deletePermission_rejectsAssignedPermission() {
		Permission permission = permission(20, "REPORT_EXPORT");
		when(permissionRepository.findById(20)).thenReturn(Optional.of(permission));
		when(rolePermissionRepository.findByPermission_PermissionId(20))
				.thenReturn(List.of(new RolePermission(role(3), permission)));

		assertThrows(ResourceConflictException.class, () -> permissionService.deletePermission(20));
		verify(permissionRepository, never()).delete(any());
	}

	@Test
	void deletePermission_deletesUnassignedCustomPermission() {
		Permission permission = permission(21, "REPORT_EXPORT");
		when(permissionRepository.findById(21)).thenReturn(Optional.of(permission));
		when(rolePermissionRepository.findByPermission_PermissionId(21)).thenReturn(List.of());

		permissionService.deletePermission(21);

		verify(permissionRepository).delete(permission);
	}

	@Test
	void assignPermissionToRole_savesAssignment() {
		Role role = role(3);
		Permission permission = permission(8, "EMPLOYEE_CREATE");
		when(roleService.getRoleById(3)).thenReturn(role);
		when(permissionRepository.findById(8)).thenReturn(Optional.of(permission));
		when(rolePermissionRepository.existsByRole_RoleIdAndPermission_PermissionId(3, 8)).thenReturn(false);

		permissionService.assignPermissionToRole(3, 8);

		ArgumentCaptor<RolePermission> captor = ArgumentCaptor.forClass(RolePermission.class);
		verify(rolePermissionRepository).save(captor.capture());
		assertEquals(role, captor.getValue().getRole());
		assertEquals(permission, captor.getValue().getPermission());
	}

	@Test
	void assignPermissionToRole_rejectsDuplicate() {
		when(roleService.getRoleById(3)).thenReturn(role(3));
		when(permissionRepository.findById(8)).thenReturn(Optional.of(permission(8, "EMPLOYEE_CREATE")));
		when(rolePermissionRepository.existsByRole_RoleIdAndPermission_PermissionId(3, 8)).thenReturn(true);

		assertThrows(ResourceConflictException.class, () -> permissionService.assignPermissionToRole(3, 8));
		verify(rolePermissionRepository, never()).save(any());
	}

	@Test
	void assignPermissionToRole_missingRoleThrows() {
		when(roleService.getRoleById(999)).thenThrow(new ResourceNotFoundException("Role was not found."));

		assertThrows(ResourceNotFoundException.class, () -> permissionService.assignPermissionToRole(999, 1));
	}

	@Test
	void assignPermissionToRole_missingPermissionThrows() {
		when(roleService.getRoleById(3)).thenReturn(role(3));
		when(permissionRepository.findById(999)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> permissionService.assignPermissionToRole(3, 999));
	}

	@Test
	void removePermissionFromRole_deletesAssignment() {
		RolePermission assignment = new RolePermission(role(3), permission(8, "EMPLOYEE_CREATE"));
		when(roleService.getRoleById(3)).thenReturn(role(3));
		when(permissionRepository.findById(8)).thenReturn(Optional.of(permission(8, "EMPLOYEE_CREATE")));
		when(rolePermissionRepository.findByRole_RoleIdAndPermission_PermissionId(3, 8))
				.thenReturn(Optional.of(assignment));

		permissionService.removePermissionFromRole(3, 8);

		verify(rolePermissionRepository).delete(assignment);
	}

	@Test
	void removePermissionFromRole_missingAssignmentThrows() {
		when(roleService.getRoleById(3)).thenReturn(role(3));
		when(permissionRepository.findById(8)).thenReturn(Optional.of(permission(8, "EMPLOYEE_CREATE")));
		when(rolePermissionRepository.findByRole_RoleIdAndPermission_PermissionId(3, 8))
				.thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> permissionService.removePermissionFromRole(3, 8));
	}

	@Test
	void getPermissionsForRole_returnsPermissions() {
		Permission permission = permission(8, "EMPLOYEE_CREATE");
		when(roleService.getRoleById(3)).thenReturn(role(3));
		when(rolePermissionRepository.findByRole_RoleIdWithPermission(3))
				.thenReturn(List.of(new RolePermission(role(3), permission)));

		List<Permission> permissions = permissionService.getPermissionsForRole(3);

		assertEquals(List.of(permission), permissions);
	}

	@Test
	void getPermissionsForRole_missingRoleThrows() {
		when(roleService.getRoleById(999)).thenThrow(new ResourceNotFoundException("Role was not found."));

		assertThrows(ResourceNotFoundException.class, () -> permissionService.getPermissionsForRole(999));
	}

	@Test
	void hasPermission_returnsTrueWhenAssigned() {
		when(roleService.getRoleById(3)).thenReturn(role(3));
		when(permissionRepository.findById(8)).thenReturn(Optional.of(permission(8, "EMPLOYEE_CREATE")));
		when(rolePermissionRepository.existsByRole_RoleIdAndPermission_PermissionId(3, 8)).thenReturn(true);

		assertTrue(permissionService.hasPermission(3, 8));
	}

	@Test
	void hasPermission_returnsFalseWhenNotAssigned() {
		when(roleService.getRoleById(3)).thenReturn(role(3));
		when(permissionRepository.findById(8)).thenReturn(Optional.of(permission(8, "EMPLOYEE_CREATE")));
		when(rolePermissionRepository.existsByRole_RoleIdAndPermission_PermissionId(3, 8)).thenReturn(false);

		assertFalse(permissionService.hasPermission(3, 8));
	}

	@Test
	void updateRolePermissions_syncsAddRemoveRetain() {
		Role role = role(3);
		Permission a = permission(1, "A");
		Permission b = permission(2, "B");
		Permission c = permission(3, "C");
		Permission d = permission(4, "D");
		RolePermission assignmentA = rolePermission(role, a);
		RolePermission assignmentB = rolePermission(role, b);
		RolePermission assignmentC = rolePermission(role, c);

		when(roleService.getRoleById(3)).thenReturn(role);
		when(permissionRepository.findById(2)).thenReturn(Optional.of(b));
		when(permissionRepository.findById(3)).thenReturn(Optional.of(c));
		when(permissionRepository.findById(4)).thenReturn(Optional.of(d));
		when(rolePermissionRepository.findByRole_RoleId(3))
				.thenReturn(List.of(assignmentA, assignmentB, assignmentC));

		permissionService.updateRolePermissions(3, Set.of(2, 3, 4));

		ArgumentCaptor<RolePermission> deleted = ArgumentCaptor.forClass(RolePermission.class);
		verify(rolePermissionRepository).delete(deleted.capture());
		assertEquals(a, deleted.getValue().getPermission());
		ArgumentCaptor<RolePermission> saved = ArgumentCaptor.forClass(RolePermission.class);
		verify(rolePermissionRepository).save(saved.capture());
		assertEquals(d, saved.getValue().getPermission());
	}

	@Test
	void updateRolePermissions_emptySetClearsAll() {
		Role role = role(3);
		Permission a = permission(1, "A");
		Permission b = permission(2, "B");
		RolePermission assignmentA = rolePermission(role, a);
		RolePermission assignmentB = rolePermission(role, b);

		when(roleService.getRoleById(3)).thenReturn(role);
		when(rolePermissionRepository.findByRole_RoleId(3)).thenReturn(List.of(assignmentA, assignmentB));

		permissionService.updateRolePermissions(3, Set.of());

		ArgumentCaptor<RolePermission> deleted = ArgumentCaptor.forClass(RolePermission.class);
		verify(rolePermissionRepository, org.mockito.Mockito.times(2)).delete(deleted.capture());
		assertEquals(Set.of(a, b), Set.copyOf(deleted.getAllValues().stream().map(RolePermission::getPermission).toList()));
		verify(rolePermissionRepository, never()).save(any());
	}

	@Test
	void updateRolePermissions_rejectsNull() {
		assertThrows(InvalidRequestException.class, () -> permissionService.updateRolePermissions(3, null));
		verify(rolePermissionRepository, never()).delete(any());
		verify(rolePermissionRepository, never()).save(any());
	}

	@Test
	void updateRolePermissions_validatesAllBeforeModifying() {
		Role role = role(3);
		Permission a = permission(1, "A");
		Permission b = permission(2, "B");
		Permission c = permission(3, "C");

		when(roleService.getRoleById(3)).thenReturn(role);
		when(permissionRepository.findById(1)).thenReturn(Optional.of(a));
		when(permissionRepository.findById(2)).thenReturn(Optional.of(b));
		when(permissionRepository.findById(999)).thenReturn(Optional.empty());
		when(rolePermissionRepository.findByRole_RoleId(3))
				.thenReturn(List.of(new RolePermission(role, a), new RolePermission(role, b), new RolePermission(role, c)));

		assertThrows(
				ResourceNotFoundException.class,
				() -> permissionService.updateRolePermissions(3, Set.of(1, 2, 999)));

		verify(rolePermissionRepository, never()).delete(any());
		verify(rolePermissionRepository, never()).save(any());
	}

	private static Permission permission(Integer id, String code) {
		Permission permission = new Permission(code);
		permission.setPermissionId(id);
		return permission;
	}

	private static Role role(Integer id) {
		Role role = new Role(RoleName.ADMIN);
		role.setRoleId(id);
		return role;
	}

	private static RolePermission rolePermission(Role role, Permission permission) {
		RolePermission assignment = new RolePermission(role, permission);
		assignment.setId(new RolePermissionId(role.getRoleId(), permission.getPermissionId()));
		return assignment;
	}
}
