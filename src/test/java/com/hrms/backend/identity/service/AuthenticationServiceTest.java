package com.hrms.backend.identity.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hrms.backend.common.exception.AuthenticationFailedException;
import com.hrms.backend.identity.dto.LoginRequest;
import com.hrms.backend.identity.dto.LoginResponse;
import com.hrms.backend.identity.entity.JwtToken;
import com.hrms.backend.identity.entity.Permission;
import com.hrms.backend.identity.entity.Role;
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
import com.hrms.backend.identity.serviceImpl.AuthenticationServiceImpl;

class AuthenticationServiceTest {

	private static final Instant NOW = Instant.parse("2026-09-28T12:00:00Z");

	private static final String EMAIL = "admin@hrms.com";

	private static final String PASSWORD = "Admin@123";

	private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
	private final UserRoleRepository userRoleRepository = org.mockito.Mockito.mock(UserRoleRepository.class);
	private final RolePermissionRepository rolePermissionRepository = org.mockito.Mockito.mock(RolePermissionRepository.class);
	private final JwtTokenRepository jwtTokenRepository = org.mockito.Mockito.mock(JwtTokenRepository.class);
	private final PasswordEncoder passwordEncoder = spy(new BCryptPasswordEncoder());
	private final JwtProperties jwtProperties = new JwtProperties();
	private final JwtService jwtService = new JwtService(jwtProperties);
	private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

	private AuthenticationService authenticationService;
	private String passwordHash;

	@BeforeEach
	void setUp() {
		jwtProperties.setSecret("0123456789abcdef0123456789abcdef");
		jwtProperties.setExpiration(Duration.ofHours(1));
		passwordHash = passwordEncoder.encode(PASSWORD);
		authenticationService = new AuthenticationServiceImpl(
				userRepository,
				userRoleRepository,
				rolePermissionRepository,
				jwtTokenRepository,
				passwordEncoder,
				jwtService,
				jwtProperties,
				clock);
	}

	@Test
	void successfulLoginPersistsTokenAndUpdatesLastLoginAt() {
		User user = activeUser();
		when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(user));
		when(userRoleRepository.findByUser_UserId(7)).thenReturn(List.of(userRole(user)));
		when(rolePermissionRepository.findByRole_RoleIdWithPermission(3)).thenReturn(List.of(rolePermission()));
		when(jwtTokenRepository.save(any(JwtToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(userRepository.save(user)).thenReturn(user);

		LoginResponse response = authenticationService.login(new LoginRequest(EMAIL, PASSWORD));

		assertEquals("Bearer", response.getTokenType());
		assertEquals(NOW.plus(Duration.ofHours(1)), response.getExpiresAt());
		assertEquals(NOW, user.getLastLoginAt());

		ArgumentCaptor<JwtToken> tokenCaptor = ArgumentCaptor.forClass(JwtToken.class);
		verify(jwtTokenRepository).save(tokenCaptor.capture());
		JwtToken saved = tokenCaptor.getValue();
		assertEquals(7, saved.getUserId());
		assertEquals(response.getAccessToken(), saved.getToken());
		assertEquals(NOW, saved.getCreatedAt());
		assertEquals(NOW.plus(Duration.ofHours(1)), saved.getExpiresAt());
		verify(userRepository).save(user);
	}

	@Test
	void unknownUserDoesNotPersistToken() {
		when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.empty());

		assertThrows(AuthenticationFailedException.class,
				() -> authenticationService.login(new LoginRequest(EMAIL, PASSWORD)));

		verify(passwordEncoder, never()).matches(anyString(), anyString());
		verify(jwtTokenRepository, never()).save(any());
		verify(userRepository, never()).save(any());
	}

	@Test
	void incorrectPasswordDoesNotPersistTokenOrUpdateLastLoginAt() {
		User user = activeUser();
		when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(user));

		assertThrows(AuthenticationFailedException.class,
				() -> authenticationService.login(new LoginRequest(EMAIL, "wrong-password")));

		assertEquals(null, user.getLastLoginAt());
		verify(jwtTokenRepository, never()).save(any());
		verify(userRepository, never()).save(any());
		verify(userRoleRepository, never()).findByUser_UserId(any());
	}

	@ParameterizedTest
	@EnumSource(value = UserStatus.class, names = { "INVITED", "INACTIVE", "LOCKED" })
	void nonActiveUserDoesNotPersistToken(UserStatus status) {
		User user = activeUser();
		user.setStatus(status);
		when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(user));

		AuthenticationFailedException exception = assertThrows(AuthenticationFailedException.class,
				() -> authenticationService.login(new LoginRequest(EMAIL, PASSWORD)));

		assertEquals("Invalid email or password.", exception.getMessage());
		assertEquals(null, user.getLastLoginAt());
		verify(passwordEncoder, never()).matches(anyString(), anyString());
		verify(jwtTokenRepository, never()).save(any());
		verify(userRepository, never()).save(any());
	}

	private User activeUser() {
		User user = new User(EMAIL, passwordHash, UserStatus.ACTIVE);
		user.setUserId(7);
		return user;
	}

	private UserRole userRole(User user) {
		Role role = new Role(RoleName.SUPER_ADMIN);
		role.setRoleId(3);
		return new UserRole(user, role);
	}

	private RolePermission rolePermission() {
		Role role = new Role(RoleName.SUPER_ADMIN);
		role.setRoleId(3);
		Permission permission = new Permission("ROLE_MANAGE");
		permission.setPermissionId(10);
		return new RolePermission(role, permission);
	}
}
