package com.hrms.backend.identity.serviceImpl;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hrms.backend.common.exception.AuthenticationFailedException;
import com.hrms.backend.identity.dto.LoginRequest;
import com.hrms.backend.identity.dto.LoginResponse;
import com.hrms.backend.identity.entity.JwtToken;
import com.hrms.backend.identity.entity.RolePermission;
import com.hrms.backend.identity.entity.User;
import com.hrms.backend.identity.entity.UserRole;
import com.hrms.backend.identity.enums.RoleName;
import com.hrms.backend.identity.enums.UserStatus;
import com.hrms.backend.identity.repository.JwtTokenRepository;
import com.hrms.backend.identity.repository.RolePermissionRepository;
import com.hrms.backend.identity.repository.UserRepository;
import com.hrms.backend.identity.repository.UserRoleRepository;
import com.hrms.backend.identity.security.JwtProperties;
import com.hrms.backend.identity.security.JwtService;
import com.hrms.backend.identity.service.AuthenticationService;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

	private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

	private static final String TOKEN_TYPE = "Bearer";

	private final UserRepository userRepository;
	private final UserRoleRepository userRoleRepository;
	private final RolePermissionRepository rolePermissionRepository;
	private final JwtTokenRepository jwtTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final JwtProperties jwtProperties;
	private final Clock clock;

	public AuthenticationServiceImpl(
			UserRepository userRepository,
			UserRoleRepository userRoleRepository,
			RolePermissionRepository rolePermissionRepository,
			JwtTokenRepository jwtTokenRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			JwtProperties jwtProperties,
			Clock clock) {
		this.userRepository = userRepository;
		this.userRoleRepository = userRoleRepository;
		this.rolePermissionRepository = rolePermissionRepository;
		this.jwtTokenRepository = jwtTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.jwtProperties = jwtProperties;
		this.clock = clock;
	}

	@Override
	@Transactional
	public LoginResponse login(LoginRequest request) {
		User user = userRepository.findByEmailIgnoreCase(request.getEmail().trim())
				.orElseThrow(AuthenticationFailedException::new);
		if (user.getStatus() != UserStatus.ACTIVE) {
			log.warn("Authentication failed");
			throw new AuthenticationFailedException();
		}
		if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			log.warn("Authentication failed");
			throw new AuthenticationFailedException();
		}

		List<UserRole> userRoles = userRoleRepository.findByUser_UserId(user.getUserId());
		List<String> roles = roleNames(userRoles);
		List<String> permissions = permissionCodes(userRoles);
		Instant issuedAt = clock.instant();
		Instant expiresAt = issuedAt.plus(expiration());
		String accessToken = jwtService.generateToken(
				user.getUserId(),
				user.getEmail(),
				roles,
				permissions,
				issuedAt,
				expiresAt);
		jwtTokenRepository.save(new JwtToken(user.getUserId(), accessToken, issuedAt, expiresAt));
		user.setLastLoginAt(issuedAt);
		userRepository.save(user);
		log.info("User logged in: userId={}", user.getUserId());
		return new LoginResponse(accessToken, TOKEN_TYPE, expiresAt);
	}

	private Duration expiration() {
		Duration expiration = jwtProperties.getExpiration();
		if (expiration == null || expiration.isZero() || expiration.isNegative()) {
			throw new IllegalStateException("JWT expiration is not configured.");
		}
		return expiration;
	}

	private List<String> roleNames(List<UserRole> userRoles) {
		return userRoles.stream()
				.map(userRole -> userRole.getRole().getRoleName())
				.map(RoleName::name)
				.distinct()
				.toList();
	}

	private List<String> permissionCodes(List<UserRole> userRoles) {
		Set<String> codes = new LinkedHashSet<>();
		for (UserRole userRole : userRoles) {
			Integer roleId = userRole.getRole().getRoleId();
			for (RolePermission rolePermission : rolePermissionRepository.findByRole_RoleIdWithPermission(roleId)) {
				codes.add(rolePermission.getPermission().getPermissionCode());
			}
		}
		return List.copyOf(codes);
	}
}
