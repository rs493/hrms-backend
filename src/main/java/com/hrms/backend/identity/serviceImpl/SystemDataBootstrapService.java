package com.hrms.backend.identity.serviceImpl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.hrms.backend.identity.bootstrap.InitialPermissionCatalog;
import com.hrms.backend.identity.bootstrap.SuperAdminBootstrapProperties;
import com.hrms.backend.identity.entity.Permission;
import com.hrms.backend.identity.entity.Role;
import com.hrms.backend.identity.entity.RolePermission;
import com.hrms.backend.identity.entity.User;
import com.hrms.backend.identity.entity.UserRole;
import com.hrms.backend.identity.enums.RoleName;
import com.hrms.backend.identity.enums.UserStatus;
import com.hrms.backend.identity.repository.PermissionRepository;
import com.hrms.backend.identity.repository.RolePermissionRepository;
import com.hrms.backend.identity.repository.RoleRepository;
import com.hrms.backend.identity.repository.UserRepository;
import com.hrms.backend.identity.repository.UserRoleRepository;

@Service
public class SystemDataBootstrapService {

	private static final Logger log = LoggerFactory.getLogger(SystemDataBootstrapService.class);

	private final PermissionRepository permissionRepository;
	private final RoleRepository roleRepository;
	private final RolePermissionRepository rolePermissionRepository;
	private final UserRepository userRepository;
	private final UserRoleRepository userRoleRepository;
	private final PasswordEncoder passwordEncoder;
	private final SuperAdminBootstrapProperties superAdminProperties;

	public SystemDataBootstrapService(
			PermissionRepository permissionRepository,
			RoleRepository roleRepository,
			RolePermissionRepository rolePermissionRepository,
			UserRepository userRepository,
			UserRoleRepository userRoleRepository,
			PasswordEncoder passwordEncoder,
			SuperAdminBootstrapProperties superAdminProperties) {
		this.permissionRepository = permissionRepository;
		this.roleRepository = roleRepository;
		this.rolePermissionRepository = rolePermissionRepository;
		this.userRepository = userRepository;
		this.userRoleRepository = userRoleRepository;
		this.passwordEncoder = passwordEncoder;
		this.superAdminProperties = superAdminProperties;
	}

	@Transactional
	public void bootstrap() {
		try {
			List<Permission> permissions = seedPermissions();
			Role superAdminRole = ensureSuperAdminRole();
			assignPermissionsToSuperAdmin(superAdminRole, permissions);
			User superAdminUser = ensureSuperAdminUser();
			assignSuperAdminRole(superAdminUser, superAdminRole);
			log.info("System data bootstrap completed");
		} catch (IllegalStateException exception) {
			throw exception;
		} catch (RuntimeException exception) {
			throw new IllegalStateException(
					"System data bootstrap failed. Required initial data was not created.",
					exception);
		}
	}

	private List<Permission> seedPermissions() {
		List<Permission> permissions = new ArrayList<>();
		for (String permissionCode : InitialPermissionCatalog.PERMISSION_CODES) {
			permissionRepository.findByPermissionCode(permissionCode).ifPresentOrElse(existing -> {
				log.info("Permission already exists: {}", permissionCode);
				permissions.add(existing);
			}, () -> {
				Permission created = permissionRepository.save(new Permission(permissionCode));
				log.info("Permission created: {}", permissionCode);
				permissions.add(created);
			});
		}
		return permissions;
	}

	private Role ensureSuperAdminRole() {
		return roleRepository.findByRoleName(RoleName.SUPER_ADMIN)
				.map(role -> {
					log.info("SUPER_ADMIN role already exists");
					return role;
				})
				.orElseGet(() -> {
					Role role = roleRepository.save(new Role(RoleName.SUPER_ADMIN));
					log.info("SUPER_ADMIN role created");
					return role;
				});
	}

	private void assignPermissionsToSuperAdmin(Role superAdminRole, List<Permission> permissions) {
		Integer roleId = superAdminRole.getRoleId();
		if (roleId == null) {
			throw new IllegalStateException("SUPER_ADMIN role was not persisted.");
		}
		for (Permission permission : permissions) {
			Integer permissionId = permission.getPermissionId();
			if (permissionId == null) {
				throw new IllegalStateException(
						"Permission was not persisted: " + permission.getPermissionCode());
			}
			if (rolePermissionRepository.existsByRole_RoleIdAndPermission_PermissionId(roleId, permissionId)) {
				log.info("Role-permission assignment already exists: role={} permission={}",
						RoleName.SUPER_ADMIN, permission.getPermissionCode());
			} else {
				rolePermissionRepository.save(new RolePermission(superAdminRole, permission));
				log.info("Role-permission assignment created: role={} permission={}",
						RoleName.SUPER_ADMIN, permission.getPermissionCode());
			}
		}
	}

	private User ensureSuperAdminUser() {
		String email = superAdminProperties.getEmail();
		if (!StringUtils.hasText(email)) {
			throw new IllegalStateException(
					"Cannot bootstrap the initial Super Admin because no email is configured.");
		}
		return userRepository.findByEmailIgnoreCase(email)
				.map(user -> {
					log.info("Initial Super Admin already exists: {}", user.getEmail());
					return user;
				})
				.orElseGet(() -> createSuperAdminUser(email));
	}

	private User createSuperAdminUser(String email) {
		if (!StringUtils.hasText(superAdminProperties.getPassword())) {
			throw new IllegalStateException(
					"Cannot create the initial Super Admin because no password is configured. Set HRMS_BOOTSTRAP_SUPER_ADMIN_PASSWORD.");
		}
		String passwordHash = passwordEncoder.encode(superAdminProperties.getPassword());
		User user = userRepository.save(new User(email, passwordHash, UserStatus.ACTIVE));
		log.info("Initial Super Admin created: {}", user.getEmail());
		return user;
	}

	private void assignSuperAdminRole(User user, Role superAdminRole) {
		if (user.getUserId() == null || superAdminRole.getRoleId() == null) {
			throw new IllegalStateException("Initial Super Admin user or role was not persisted.");
		}
		if (userRoleRepository.existsByUser_UserIdAndRole_RoleId(user.getUserId(), superAdminRole.getRoleId())) {
			log.info("User-role assignment already exists: email={} role={}",
					user.getEmail(), RoleName.SUPER_ADMIN);
			return;
		}
		userRoleRepository.save(new UserRole(user, superAdminRole));
		log.info("User-role assignment created: email={} role={}", user.getEmail(), RoleName.SUPER_ADMIN);
	}
}
