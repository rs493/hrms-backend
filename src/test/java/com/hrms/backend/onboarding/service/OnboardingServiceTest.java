package com.hrms.backend.onboarding.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;

import com.hrms.backend.common.exception.InvalidRequestException;
import com.hrms.backend.common.exception.ResourceConflictException;
import com.hrms.backend.common.exception.ResourceNotFoundException;
import com.hrms.backend.employee.entity.Employee;
import com.hrms.backend.employee.enums.EmployeeStatus;
import com.hrms.backend.employee.enums.EmploymentType;
import com.hrms.backend.employee.service.EmployeeManagementService;
import com.hrms.backend.identity.entity.Role;
import com.hrms.backend.identity.entity.User;
import com.hrms.backend.identity.enums.RoleName;
import com.hrms.backend.identity.enums.UserStatus;
import com.hrms.backend.identity.service.AuthorizationService;
import com.hrms.backend.identity.service.RoleService;
import com.hrms.backend.identity.service.UserManagementService;
import com.hrms.backend.onboarding.config.OnboardingProperties;
import com.hrms.backend.onboarding.dto.ActivationRequest;
import com.hrms.backend.onboarding.dto.ActivationResponse;
import com.hrms.backend.onboarding.dto.OnboardingRequest;
import com.hrms.backend.onboarding.dto.OnboardingResponse;
import com.hrms.backend.onboarding.entity.UserActivationToken;
import com.hrms.backend.onboarding.repository.UserActivationTokenRepository;
import com.hrms.backend.onboarding.serviceImpl.OnboardingServiceImpl;

class OnboardingServiceTest {

	private final EmployeeManagementService employeeManagementService =
			org.mockito.Mockito.mock(EmployeeManagementService.class);
	private final UserManagementService userManagementService =
			org.mockito.Mockito.mock(UserManagementService.class);
	private final RoleService roleService = org.mockito.Mockito.mock(RoleService.class);
	private final AuthorizationService authorizationService =
			org.mockito.Mockito.mock(AuthorizationService.class);
	private final OnboardingMailService onboardingMailService =
			org.mockito.Mockito.mock(OnboardingMailService.class);
	private final UserActivationTokenRepository userActivationTokenRepository =
			org.mockito.Mockito.mock(UserActivationTokenRepository.class);
	private final OnboardingProperties onboardingProperties = new OnboardingProperties();

	private OnboardingService onboardingService;

	@BeforeEach
	void setUp() {
		onboardingProperties.setActivationBaseUrl("http://localhost:3000/activate");
		onboardingProperties.setActivationTokenExpirationMinutes(60);
		onboardingService = new OnboardingServiceImpl(
				employeeManagementService,
				userManagementService,
				roleService,
				authorizationService,
				onboardingMailService,
				onboardingProperties,
				userActivationTokenRepository);
	}

	@Test
	void onboardEmployee_createsInvitedEmployeeAndUserAndAssignsRole() {
		OnboardingRequest request = request(RoleName.EMPLOYEE);
		Employee employee = employee(10, "EMP001");
		User user = user(20, "raja@hrms.com", employee);
		stubSuccessfulOnboarding(RoleName.EMPLOYEE, "EMPLOYEE_CREATE", employee, user);

		OnboardingResponse response = onboardingService.onboard(request);

		assertEquals(10, response.getEmployeeId());
		assertEquals(20, response.getUserId());
		assertEquals("EMP001", response.getEmployeeCode());
		assertEquals("raja@hrms.com", response.getEmail());
		assertEquals(EmployeeStatus.INVITED, response.getEmployeeStatus());
		assertEquals(UserStatus.INVITED, response.getUserStatus());
		assertEquals(RoleName.EMPLOYEE, response.getRole());
		verify(roleService).assignRoleToUser(20, RoleName.EMPLOYEE);
		verify(userActivationTokenRepository).save(any(UserActivationToken.class));
		verify(authorizationService).requirePermission("EMPLOYEE_CREATE");
		verify(userManagementService).createInvitedAccount("raja@hrms.com", employee);
	}

	@Test
	void onboardAdmin_requiresAdminCreatePermission() {
		OnboardingRequest request = request(RoleName.ADMIN);
		Employee employee = employee(1, "ADM001");
		User user = user(2, "admin@hrms.com", employee);
		stubSuccessfulOnboarding(RoleName.ADMIN, "ADMIN_CREATE", employee, user);

		OnboardingResponse response = onboardingService.onboard(request);

		assertEquals(RoleName.ADMIN, response.getRole());
		assertEquals(EmployeeStatus.INVITED, response.getEmployeeStatus());
		assertEquals(UserStatus.INVITED, response.getUserStatus());
		verify(authorizationService).requirePermission("ADMIN_CREATE");
		verify(roleService).assignRoleToUser(2, RoleName.ADMIN);
	}

	@Test
	void onboardHr_requiresHrCreatePermission() {
		OnboardingRequest request = request(RoleName.HR);
		Employee employee = employee(11, "HR001");
		User user = user(21, "hr@hrms.com", employee);
		stubSuccessfulOnboarding(RoleName.HR, "HR_CREATE", employee, user);

		OnboardingResponse response = onboardingService.onboard(request);

		assertEquals(RoleName.HR, response.getRole());
		verify(authorizationService).requirePermission("HR_CREATE");
		verify(roleService).assignRoleToUser(21, RoleName.HR);
	}

	@Test
	void onboardManager_requiresManagerCreatePermission() {
		OnboardingRequest request = request(RoleName.MANAGER);
		Employee employee = employee(12, "MGR001");
		User user = user(22, "mgr@hrms.com", employee);
		stubSuccessfulOnboarding(RoleName.MANAGER, "MANAGER_CREATE", employee, user);

		OnboardingResponse response = onboardingService.onboard(request);

		assertEquals(RoleName.MANAGER, response.getRole());
		verify(authorizationService).requirePermission("MANAGER_CREATE");
		verify(roleService).assignRoleToUser(22, RoleName.MANAGER);
	}

	@Test
	void onboard_rejectsSuperAdminRole() {
		OnboardingRequest request = request(RoleName.SUPER_ADMIN);

		assertThrows(InvalidRequestException.class, () -> onboardingService.onboard(request));
		verify(authorizationService, never()).requirePermission(any());
		verify(employeeManagementService, never()).createEmployee(
				any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
	}

	@Test
	void onboard_rejectsNullRequest() {
		assertThrows(InvalidRequestException.class, () -> onboardingService.onboard(null));
	}

	@Test
	void onboard_rejectsMissingRole() {
		OnboardingRequest request = request(null);

		assertThrows(InvalidRequestException.class, () -> onboardingService.onboard(request));
		verify(employeeManagementService, never()).createEmployee(
				any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
	}

	@Test
	void onboardAdmin_deniedWithoutPermission() {
		OnboardingRequest request = request(RoleName.ADMIN);
		doThrow(new AccessDeniedException("Required permission is missing: ADMIN_CREATE"))
				.when(authorizationService)
				.requirePermission("ADMIN_CREATE");

		assertThrows(AccessDeniedException.class, () -> onboardingService.onboard(request));
		verify(employeeManagementService, never()).createEmployee(
				any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
	}

	@Test
	void onboard_rejectsMissingRequestedRole() {
		OnboardingRequest request = request(RoleName.EMPLOYEE);
		org.mockito.Mockito.doNothing().when(authorizationService).requirePermission("EMPLOYEE_CREATE");
		when(roleService.getRoleByName(RoleName.EMPLOYEE))
				.thenThrow(new ResourceNotFoundException("Role was not found."));

		assertThrows(ResourceNotFoundException.class, () -> onboardingService.onboard(request));
		verify(userManagementService, never()).createInvitedAccount(any(), any());
	}

	@Test
	void onboard_rejectsDuplicateEmail() {
		OnboardingRequest request = request(RoleName.EMPLOYEE);
		Employee employee = employee(10, "EMP001");
		org.mockito.Mockito.doNothing().when(authorizationService).requirePermission("EMPLOYEE_CREATE");
		when(roleService.getRoleByName(RoleName.EMPLOYEE)).thenReturn(new Role(RoleName.EMPLOYEE));
		when(employeeManagementService.createEmployee(
				any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
				.thenReturn(employee);
		when(userManagementService.createInvitedAccount(eq("raja@hrms.com"), eq(employee)))
				.thenThrow(new ResourceConflictException("A user with this email already exists."));

		assertThrows(ResourceConflictException.class, () -> onboardingService.onboard(request));
		verify(roleService, never()).assignRoleToUser(any(), any());
		verify(userActivationTokenRepository, never()).save(any());
	}

	@Test
	void onboard_rejectsDuplicateEmployeeCode() {
		OnboardingRequest request = request(RoleName.EMPLOYEE);
		org.mockito.Mockito.doNothing().when(authorizationService).requirePermission("EMPLOYEE_CREATE");
		when(roleService.getRoleByName(RoleName.EMPLOYEE)).thenReturn(new Role(RoleName.EMPLOYEE));
		when(employeeManagementService.createEmployee(
				any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
				.thenThrow(new ResourceConflictException(
						"An employee with this employee code already exists."));

		assertThrows(ResourceConflictException.class, () -> onboardingService.onboard(request));
		verify(userManagementService, never()).createInvitedAccount(any(), any());
	}

	@Test
	void onboard_rejectsInvalidManagerId() {
		OnboardingRequest request = request(RoleName.EMPLOYEE);
		request.setManagerId(999);
		org.mockito.Mockito.doNothing().when(authorizationService).requirePermission("EMPLOYEE_CREATE");
		when(roleService.getRoleByName(RoleName.EMPLOYEE)).thenReturn(new Role(RoleName.EMPLOYEE));
		when(employeeManagementService.createEmployee(
				any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
				.thenThrow(new ResourceNotFoundException("Manager was not found."));

		assertThrows(ResourceNotFoundException.class, () -> onboardingService.onboard(request));
		verify(userManagementService, never()).createInvitedAccount(any(), any());
	}

	@Test
	void onboard_rollsBackWhenUserCreationFails() {
		OnboardingRequest request = request(RoleName.EMPLOYEE);
		Employee employee = employee(10, "EMP001");
		org.mockito.Mockito.doNothing().when(authorizationService).requirePermission("EMPLOYEE_CREATE");
		when(roleService.getRoleByName(RoleName.EMPLOYEE)).thenReturn(new Role(RoleName.EMPLOYEE));
		when(employeeManagementService.createEmployee(
				any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
				.thenReturn(employee);
		when(userManagementService.createInvitedAccount(eq("raja@hrms.com"), eq(employee)))
				.thenThrow(new RuntimeException("user creation failed"));

		assertThrows(RuntimeException.class, () -> onboardingService.onboard(request));
		verify(roleService, never()).assignRoleToUser(any(), any());
		verify(userActivationTokenRepository, never()).save(any());
		verify(onboardingMailService, never()).sendOnboardingEmail(any(), any());
	}

	@Test
	void onboard_rollsBackWhenRoleAssignmentFails() {
		OnboardingRequest request = request(RoleName.EMPLOYEE);
		Employee employee = employee(10, "EMP001");
		User user = user(20, "raja@hrms.com", employee);
		org.mockito.Mockito.doNothing().when(authorizationService).requirePermission("EMPLOYEE_CREATE");
		when(roleService.getRoleByName(RoleName.EMPLOYEE)).thenReturn(new Role(RoleName.EMPLOYEE));
		when(employeeManagementService.createEmployee(
				any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
				.thenReturn(employee);
		when(userManagementService.createInvitedAccount(eq("raja@hrms.com"), eq(employee)))
				.thenReturn(user);
		doThrow(new RuntimeException("role assignment failed"))
				.when(roleService)
				.assignRoleToUser(20, RoleName.EMPLOYEE);

		assertThrows(RuntimeException.class, () -> onboardingService.onboard(request));
		verify(userActivationTokenRepository, never()).save(any());
		verify(onboardingMailService, never()).sendOnboardingEmail(any(), any());
	}

	@Test
	void onboard_rollsBackWhenActivationTokenCreationFails() {
		OnboardingRequest request = request(RoleName.EMPLOYEE);
		Employee employee = employee(10, "EMP001");
		User user = user(20, "raja@hrms.com", employee);
		org.mockito.Mockito.doNothing().when(authorizationService).requirePermission("EMPLOYEE_CREATE");
		when(roleService.getRoleByName(RoleName.EMPLOYEE)).thenReturn(new Role(RoleName.EMPLOYEE));
		when(employeeManagementService.createEmployee(
				any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
				.thenReturn(employee);
		when(userManagementService.createInvitedAccount(eq("raja@hrms.com"), eq(employee)))
				.thenReturn(user);
		when(userActivationTokenRepository.save(any(UserActivationToken.class)))
				.thenThrow(new RuntimeException("token save failed"));

		assertThrows(RuntimeException.class, () -> onboardingService.onboard(request));
		verify(onboardingMailService, never()).sendOnboardingEmail(any(), any());
	}

	@Test
	void onboard_storesHashedTokenAndEmailsActivationLinkOnly() {
		OnboardingRequest request = request(RoleName.EMPLOYEE);
		Employee employee = employee(10, "EMP001");
		User user = user(20, "raja@hrms.com", employee);
		stubSuccessfulOnboarding(RoleName.EMPLOYEE, "EMPLOYEE_CREATE", employee, user);

		OnboardingResponse response = onboardingService.onboard(request);

		ArgumentCaptor<UserActivationToken> tokenCaptor = ArgumentCaptor.forClass(UserActivationToken.class);
		verify(userActivationTokenRepository).save(tokenCaptor.capture());
		UserActivationToken savedToken = tokenCaptor.getValue();
		assertEquals(64, savedToken.getTokenHash().length());
		assertNull(savedToken.getUsedAt());
		assertNotNull(savedToken.getExpiresAt());

		ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
		verify(onboardingMailService).sendOnboardingEmail(eq("raja@hrms.com"), linkCaptor.capture());

		String activationLink = linkCaptor.getValue();
		String rawToken = extractToken(activationLink);
		assertTrue(activationLink.startsWith("http://localhost:3000/activate?token="));
		assertFalse(activationLink.contains("email="));
		assertFalse(activationLink.contains("password="));
		assertFalse(response.toString().contains(rawToken));
		assertFalse(savedToken.getTokenHash().equals(rawToken));
		assertEquals(sha256(rawToken), savedToken.getTokenHash());
	}

	@Test
	void activate_succeedsForValidToken() {
		Employee employee = employee(10, "EMP001");
		User invitedUser = user(20, "raja@hrms.com", employee);
		String rawToken = "secure-activation-token";
		UserActivationToken activationToken = new UserActivationToken(
				invitedUser,
				sha256(rawToken),
				Instant.now().plusSeconds(3600),
				Instant.now());
		when(userActivationTokenRepository.findByTokenHash(sha256(rawToken)))
				.thenReturn(Optional.of(activationToken));

		Employee activeEmployee = employee(10, "EMP001");
		activeEmployee.setStatus(EmployeeStatus.ACTIVE);
		User activeUser = user(20, "raja@hrms.com", activeEmployee);
		activeUser.setStatus(UserStatus.ACTIVE);
		when(userManagementService.getUserById(20)).thenReturn(activeUser);
		when(employeeManagementService.getEmployeeById(10)).thenReturn(activeEmployee);

		ActivationRequest request = activationRequest(rawToken, "NewSecurePassword123!", "NewSecurePassword123!");
		ActivationResponse response = onboardingService.activate(request);

		assertEquals(10, response.getEmployeeId());
		assertEquals(20, response.getUserId());
		assertEquals(EmployeeStatus.ACTIVE, response.getEmployeeStatus());
		assertEquals(UserStatus.ACTIVE, response.getUserStatus());
		verify(userManagementService).changePassword(20, "NewSecurePassword123!");
		verify(userManagementService).activateUser(20);
		verify(employeeManagementService).markInvitedEmployeeActive(10);
		assertNotNull(activationToken.getUsedAt());
		verify(userActivationTokenRepository).save(activationToken);
	}

	@Test
	void activate_rejectsInvalidToken() {
		when(userActivationTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

		ActivationRequest request = activationRequest("missing", "NewSecurePassword123!", "NewSecurePassword123!");
		assertThrows(InvalidRequestException.class, () -> onboardingService.activate(request));
		verify(userManagementService, never()).changePassword(any(), any());
	}

	@Test
	void activate_rejectsExpiredToken() {
		Employee employee = employee(10, "EMP001");
		User invitedUser = user(20, "raja@hrms.com", employee);
		String rawToken = "expired-token";
		UserActivationToken activationToken = new UserActivationToken(
				invitedUser,
				sha256(rawToken),
				Instant.now().minusSeconds(60),
				Instant.now().minusSeconds(3600));
		when(userActivationTokenRepository.findByTokenHash(sha256(rawToken)))
				.thenReturn(Optional.of(activationToken));

		ActivationRequest request = activationRequest(rawToken, "NewSecurePassword123!", "NewSecurePassword123!");
		assertThrows(InvalidRequestException.class, () -> onboardingService.activate(request));
		verify(userManagementService, never()).changePassword(any(), any());
	}

	@Test
	void activate_rejectsAlreadyUsedToken() {
		Employee employee = employee(10, "EMP001");
		User invitedUser = user(20, "raja@hrms.com", employee);
		String rawToken = "used-token";
		UserActivationToken activationToken = new UserActivationToken(
				invitedUser,
				sha256(rawToken),
				Instant.now().plusSeconds(3600),
				Instant.now().minusSeconds(60));
		activationToken.setUsedAt(Instant.now().minusSeconds(30));
		when(userActivationTokenRepository.findByTokenHash(sha256(rawToken)))
				.thenReturn(Optional.of(activationToken));

		ActivationRequest request = activationRequest(rawToken, "NewSecurePassword123!", "NewSecurePassword123!");
		assertThrows(InvalidRequestException.class, () -> onboardingService.activate(request));
		verify(userManagementService, never()).changePassword(any(), any());
	}

	@Test
	void activate_rejectsPasswordMismatch() {
		ActivationRequest request = activationRequest("token", "NewSecurePassword123!", "DifferentPassword123!");
		assertThrows(InvalidRequestException.class, () -> onboardingService.activate(request));
		verify(userActivationTokenRepository, never()).findByTokenHash(any());
	}

	@Test
	void activate_rejectsMissingPassword() {
		ActivationRequest request = activationRequest("token", null, null);
		assertThrows(InvalidRequestException.class, () -> onboardingService.activate(request));
	}

	@Test
	void activate_rejectsWeakPassword() {
		Employee employee = employee(10, "EMP001");
		User invitedUser = user(20, "raja@hrms.com", employee);
		String rawToken = "valid-token";
		UserActivationToken activationToken = new UserActivationToken(
				invitedUser,
				sha256(rawToken),
				Instant.now().plusSeconds(3600),
				Instant.now());
		when(userActivationTokenRepository.findByTokenHash(sha256(rawToken)))
				.thenReturn(Optional.of(activationToken));
		doThrow(new InvalidRequestException("Password must be between 8 and 255 characters."))
				.when(userManagementService)
				.changePassword(20, "short");

		ActivationRequest request = activationRequest(rawToken, "short", "short");
		assertThrows(InvalidRequestException.class, () -> onboardingService.activate(request));
		verify(userManagementService, never()).activateUser(any());
		verify(employeeManagementService, never()).markInvitedEmployeeActive(any());
		assertNull(activationToken.getUsedAt());
	}

	@Test
	void activate_rejectsNonInvitedUser() {
		Employee employee = employee(10, "EMP001");
		User activeUser = user(20, "raja@hrms.com", employee);
		activeUser.setStatus(UserStatus.ACTIVE);
		String rawToken = "token-for-active-user";
		UserActivationToken activationToken = new UserActivationToken(
				activeUser,
				sha256(rawToken),
				Instant.now().plusSeconds(3600),
				Instant.now());
		when(userActivationTokenRepository.findByTokenHash(sha256(rawToken)))
				.thenReturn(Optional.of(activationToken));

		ActivationRequest request = activationRequest(rawToken, "NewSecurePassword123!", "NewSecurePassword123!");
		assertThrows(InvalidRequestException.class, () -> onboardingService.activate(request));
		verify(userManagementService, never()).changePassword(any(), any());
	}

	private void stubSuccessfulOnboarding(
			RoleName role,
			String permission,
			Employee employee,
			User user) {
		org.mockito.Mockito.doNothing().when(authorizationService).requirePermission(permission);
		when(roleService.getRoleByName(role)).thenReturn(new Role(role));
		when(employeeManagementService.createEmployee(
				any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
				.thenReturn(employee);
		when(userManagementService.createInvitedAccount(any(), eq(employee))).thenReturn(user);
		when(userActivationTokenRepository.save(any(UserActivationToken.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
	}

	private static OnboardingRequest request(RoleName role) {
		OnboardingRequest request = new OnboardingRequest();
		request.setEmployeeCode("EMP001");
		request.setFirstName("Raja");
		request.setLastName("Kumar");
		request.setPhone("9999999999");
		request.setEmploymentType(EmploymentType.FULL_TIME);
		request.setJoiningDate(LocalDate.of(2026, 1, 1));
		request.setEmail("raja@hrms.com");
		request.setRole(role);
		return request;
	}

	private static ActivationRequest activationRequest(
			String token,
			String newPassword,
			String confirmPassword) {
		ActivationRequest request = new ActivationRequest();
		request.setToken(token);
		request.setNewPassword(newPassword);
		request.setConfirmPassword(confirmPassword);
		return request;
	}

	private static Employee employee(Integer id, String code) {
		Employee employee = new Employee(
				code,
				"Raja",
				"Kumar",
				"9999999999",
				EmploymentType.FULL_TIME,
				LocalDate.of(2026, 1, 1),
				EmployeeStatus.INVITED);
		employee.setEmployeeId(id);
		return employee;
	}

	private static User user(Integer id, String email, Employee employee) {
		User user = new User(email, "hash", UserStatus.INVITED);
		user.setUserId(id);
		user.setEmployee(employee);
		return user;
	}

	private static String sha256(String rawToken) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception exception) {
			throw new IllegalStateException(exception);
		}
	}

	private static String extractToken(String activationLink) {
		int index = activationLink.indexOf("token=");
		return activationLink.substring(index + "token=".length());
	}
}
