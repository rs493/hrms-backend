package com.hrms.backend.identity.serviceImpl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hrms.backend.common.exception.InvalidRequestException;
import com.hrms.backend.common.exception.ResourceConflictException;
import com.hrms.backend.common.exception.ResourceNotFoundException;
import com.hrms.backend.identity.entity.Role;
import com.hrms.backend.identity.entity.User;
import com.hrms.backend.identity.entity.UserRole;
import com.hrms.backend.identity.enums.RoleName;
import com.hrms.backend.identity.repository.RolePermissionRepository;
import com.hrms.backend.identity.repository.RoleRepository;
import com.hrms.backend.identity.repository.UserRepository;
import com.hrms.backend.identity.repository.UserRoleRepository;
import com.hrms.backend.identity.service.RoleService;

@Service
public class RoleServiceImpl implements RoleService {

	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final UserRoleRepository userRoleRepository;
	private final RolePermissionRepository rolePermissionRepository;

	public RoleServiceImpl(
			RoleRepository roleRepository,
			UserRepository userRepository,
			UserRoleRepository userRoleRepository,
			RolePermissionRepository rolePermissionRepository) {
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
		this.userRoleRepository = userRoleRepository;
		this.rolePermissionRepository = rolePermissionRepository;
	}

	@Override
	@Transactional
	public Role createRole(RoleName roleName) {
		requireRoleName(roleName);
		if (roleRepository.existsByRoleName(roleName)) {
			throw new ResourceConflictException("A role with this name already exists.");
		}
		Role role = new Role(roleName);
		role.setUpdatedAt(Instant.now());
		return roleRepository.save(role);
	}

	@Override
	@Transactional(readOnly = true)
	public Role getRoleById(Integer roleId) {
		return requireRoleById(roleId);
	}

	@Override
	@Transactional(readOnly = true)
	public Role getRoleByName(RoleName roleName) {
		requireRoleName(roleName);
		return roleRepository.findByRoleName(roleName)
				.orElseThrow(() -> new ResourceNotFoundException("Role was not found."));
	}

	@Override
	@Transactional(readOnly = true)
	public List<Role> getAllRoles() {
		return roleRepository.findAll();
	}

	@Override
	@Transactional
	public Role updateRole(Integer roleId, RoleName roleName) {
		requireRoleName(roleName);
		Role role = requireRoleById(roleId);
		if (role.getRoleName() == RoleName.SUPER_ADMIN && roleName != RoleName.SUPER_ADMIN) {
			throw new InvalidRequestException("The SUPER_ADMIN role cannot be renamed.");
		}
		roleRepository.findByRoleName(roleName).ifPresent(existing -> {
			if (!existing.getRoleId().equals(roleId)) {
				throw new ResourceConflictException("A role with this name already exists.");
			}
		});
		role.setRoleName(roleName);
		role.setUpdatedAt(Instant.now());
		return roleRepository.save(role);
	}

	@Override
	@Transactional
	public void deleteRole(Integer roleId) {
		Role role = requireRoleById(roleId);
		if (role.getRoleName() == RoleName.SUPER_ADMIN) {
			throw new InvalidRequestException("The SUPER_ADMIN role cannot be deleted.");
		}
		if (!userRoleRepository.findByRole_RoleId(roleId).isEmpty()) {
			throw new ResourceConflictException(
					"Role cannot be deleted while it is assigned to one or more users.");
		}
		if (!rolePermissionRepository.findByRole_RoleId(roleId).isEmpty()) {
			throw new ResourceConflictException(
					"Role cannot be deleted while it still has permission assignments.");
		}
		roleRepository.delete(role);
	}

	@Override
	@Transactional
	public void assignRoleToUser(Integer userId, RoleName roleName) {
		User user = requireUser(userId);
		Role role = getRoleByName(roleName);
		if (userRoleRepository.existsByUser_UserIdAndRole_RoleId(user.getUserId(), role.getRoleId())) {
			throw new ResourceConflictException("The user already has this role.");
		}
		userRoleRepository.save(new UserRole(user, role));
	}

	@Override
	@Transactional
	public void removeRoleFromUser(Integer userId, RoleName roleName) {
		User user = requireUser(userId);
		Role role = getRoleByName(roleName);
		UserRole assignment = userRoleRepository.findByUser_UserId(user.getUserId()).stream()
				.filter(userRole -> userRole.getRole().getRoleId().equals(role.getRoleId()))
				.findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Role assignment was not found."));
		userRoleRepository.delete(assignment);
	}

	@Override
	@Transactional(readOnly = true)
	public List<RoleName> getRolesForUser(Integer userId) {
		requireUser(userId);
		List<RoleName> roles = new ArrayList<>();
		for (UserRole userRole : userRoleRepository.findByUser_UserId(userId)) {
			roles.add(userRole.getRole().getRoleName());
		}
		return roles;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean hasRole(Integer userId, RoleName roleName) {
		requireUser(userId);
		requireRoleName(roleName);
		return roleRepository.findByRoleName(roleName)
				.map(role -> userRoleRepository.existsByUser_UserIdAndRole_RoleId(userId, role.getRoleId()))
				.orElse(false);
	}

	@Override
	@Transactional
	public void updateUserRoles(Integer userId, Set<RoleName> roleNames) {
		if (roleNames == null) {
			throw new InvalidRequestException("Roles are required.");
		}
		User user = requireUser(userId);
		Set<RoleName> requestedNames = new LinkedHashSet<>(roleNames);
		Set<Role> requestedRoles = new LinkedHashSet<>();
		for (RoleName roleName : requestedNames) {
			requireRoleName(roleName);
			requestedRoles.add(getRoleByName(roleName));
		}

		List<UserRole> currentAssignments = userRoleRepository.findByUser_UserId(userId);
		Set<Integer> requestedRoleIds = new HashSet<>();
		for (Role role : requestedRoles) {
			requestedRoleIds.add(role.getRoleId());
		}

		Set<Integer> currentRoleIds = new HashSet<>();
		for (UserRole assignment : currentAssignments) {
			Integer currentRoleId = assignment.getRole().getRoleId();
			currentRoleIds.add(currentRoleId);
			if (!requestedRoleIds.contains(currentRoleId)) {
				userRoleRepository.delete(assignment);
			}
		}

		for (Role role : requestedRoles) {
			if (!currentRoleIds.contains(role.getRoleId())) {
				userRoleRepository.save(new UserRole(user, role));
			}
		}
	}

	private User requireUser(Integer userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User was not found."));
	}

	private Role requireRoleById(Integer roleId) {
		return roleRepository.findById(roleId)
				.orElseThrow(() -> new ResourceNotFoundException("Role was not found."));
	}

	private void requireRoleName(RoleName roleName) {
		if (roleName == null) {
			throw new InvalidRequestException("Role name is required.");
		}
	}
}
