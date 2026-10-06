package com.hrms.backend.onboarding.serviceImpl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import com.hrms.backend.common.exception.InvalidRequestException;
import com.hrms.backend.employee.entity.Employee;
import com.hrms.backend.employee.service.EmployeeManagementService;
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
import com.hrms.backend.onboarding.service.OnboardingMailService;
import com.hrms.backend.onboarding.service.OnboardingService;

@Service
public class OnboardingServiceImpl implements OnboardingService {

	private static final Logger log = LoggerFactory.getLogger(OnboardingServiceImpl.class);

	private static final int ACTIVATION_TOKEN_BYTES = 32;

	private static final String INVALID_ACTIVATION_TOKEN_MESSAGE =
			"Activation token is invalid or has expired.";

	private final EmployeeManagementService employeeManagementService;
	private final UserManagementService userManagementService;
	private final RoleService roleService;
	private final AuthorizationService authorizationService;
	private final OnboardingMailService onboardingMailService;
	private final OnboardingProperties onboardingProperties;
	private final UserActivationTokenRepository userActivationTokenRepository;
	private final SecureRandom secureRandom = new SecureRandom();

	public OnboardingServiceImpl(
			EmployeeManagementService employeeManagementService,
			UserManagementService userManagementService,
			RoleService roleService,
			AuthorizationService authorizationService,
			OnboardingMailService onboardingMailService,
			OnboardingProperties onboardingProperties,
			UserActivationTokenRepository userActivationTokenRepository) {
		this.employeeManagementService = employeeManagementService;
		this.userManagementService = userManagementService;
		this.roleService = roleService;
		this.authorizationService = authorizationService;
		this.onboardingMailService = onboardingMailService;
		this.onboardingProperties = onboardingProperties;
		this.userActivationTokenRepository = userActivationTokenRepository;
	}

	@Override
	@Transactional
	public OnboardingResponse onboard(OnboardingRequest request) {
		if (request == null) {
			throw new InvalidRequestException("Onboarding request is required.");
		}
		RoleName role = requireOnboardableRole(request.getRole());
		authorizeOnboarding(role);
		roleService.getRoleByName(role);

		Employee employee = employeeManagementService.createEmployee(
				request.getEmployeeCode(),
				request.getFirstName(),
				request.getLastName(),
				request.getPhone(),
				request.getDepartmentId(),
				request.getDesignationId(),
				request.getLocationId(),
				request.getShiftId(),
				request.getManagerId(),
				request.getEmploymentType(),
				request.getJoiningDate());

		User user = userManagementService.createInvitedAccount(request.getEmail(), employee);
		roleService.assignRoleToUser(user.getUserId(), role);

		String rawActivationToken = generateActivationToken();
		persistActivationToken(user, rawActivationToken);

		String activationLink = buildActivationLink(rawActivationToken);
		scheduleOnboardingEmail(user.getEmail(), activationLink);

		log.info(
				"Onboarding completed: employeeId={} userId={} role={}",
				employee.getEmployeeId(),
				user.getUserId(),
				role);

		return new OnboardingResponse(
				employee.getEmployeeId(),
				user.getUserId(),
				employee.getEmployeeCode(),
				user.getEmail(),
				employee.getStatus(),
				user.getStatus(),
				role);
	}

	@Override
	@Transactional
	public ActivationResponse activate(ActivationRequest request) {
		if (request == null) {
			throw new InvalidRequestException("Activation request is required.");
		}
		if (!StringUtils.hasText(request.getToken())) {
			throw new InvalidRequestException(INVALID_ACTIVATION_TOKEN_MESSAGE);
		}
		if (!StringUtils.hasText(request.getNewPassword())) {
			throw new InvalidRequestException("New password is required.");
		}
		if (!StringUtils.hasText(request.getConfirmPassword())) {
			throw new InvalidRequestException("Confirm password is required.");
		}
		if (!request.getNewPassword().equals(request.getConfirmPassword())) {
			throw new InvalidRequestException("New password and confirm password must match.");
		}

		String tokenHash = hashToken(request.getToken().trim());
		UserActivationToken activationToken = userActivationTokenRepository.findByTokenHash(tokenHash)
				.orElseThrow(() -> new InvalidRequestException(INVALID_ACTIVATION_TOKEN_MESSAGE));

		Instant now = Instant.now();
		if (activationToken.isUsed() || activationToken.isExpired(now)) {
			throw new InvalidRequestException(INVALID_ACTIVATION_TOKEN_MESSAGE);
		}

		User user = activationToken.getUser();
		if (user == null || user.getStatus() != UserStatus.INVITED) {
			throw new InvalidRequestException(INVALID_ACTIVATION_TOKEN_MESSAGE);
		}
		Employee employee = user.getEmployee();
		if (employee == null || employee.getEmployeeId() == null) {
			throw new InvalidRequestException(INVALID_ACTIVATION_TOKEN_MESSAGE);
		}

		userManagementService.changePassword(user.getUserId(), request.getNewPassword());
		userManagementService.activateUser(user.getUserId());
		employeeManagementService.markInvitedEmployeeActive(employee.getEmployeeId());

		activationToken.setUsedAt(now);
		userActivationTokenRepository.save(activationToken);

		User activatedUser = userManagementService.getUserById(user.getUserId());
		Employee activatedEmployee = employeeManagementService.getEmployeeById(employee.getEmployeeId());

		log.info(
				"Onboarding activation completed: employeeId={} userId={}",
				activatedEmployee.getEmployeeId(),
				activatedUser.getUserId());

		return new ActivationResponse(
				activatedEmployee.getEmployeeId(),
				activatedUser.getUserId(),
				activatedUser.getEmail(),
				activatedEmployee.getStatus(),
				activatedUser.getStatus());
	}

	private RoleName requireOnboardableRole(RoleName role) {
		if (role == null) {
			throw new InvalidRequestException("Role is required.");
		}
		return switch (role) {
			case ADMIN, HR, MANAGER, EMPLOYEE -> role;
			case SUPER_ADMIN -> throw new InvalidRequestException(
					"SUPER_ADMIN cannot be created through onboarding.");
		};
	}

	private void authorizeOnboarding(RoleName role) {
		switch (role) {
			case ADMIN -> authorizationService.requirePermission("ADMIN_CREATE");
			case HR -> authorizationService.requirePermission("HR_CREATE");
			case MANAGER -> authorizationService.requirePermission("MANAGER_CREATE");
			case EMPLOYEE -> authorizationService.requirePermission("EMPLOYEE_CREATE");
			case SUPER_ADMIN -> throw new InvalidRequestException(
					"SUPER_ADMIN cannot be created through onboarding.");
		}
	}

	private String generateActivationToken() {
		byte[] bytes = new byte[ACTIVATION_TOKEN_BYTES];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private void persistActivationToken(User user, String rawToken) {
		int expirationMinutes = onboardingProperties.getActivationTokenExpirationMinutes();
		if (expirationMinutes <= 0) {
			throw new InvalidRequestException("Activation token expiration must be a positive number.");
		}
		Instant createdAt = Instant.now();
		Instant expiresAt = createdAt.plus(expirationMinutes, ChronoUnit.MINUTES);
		UserActivationToken activationToken = new UserActivationToken(
				user,
				hashToken(rawToken),
				expiresAt,
				createdAt);
		userActivationTokenRepository.save(activationToken);
	}

	private String hashToken(String rawToken) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashed);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available.", exception);
		}
	}

	private String buildActivationLink(String rawToken) {
		String encodedToken = URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
		String baseUrl = onboardingProperties.getActivationBaseUrl();
		String separator = baseUrl.contains("?") ? "&" : "?";
		return baseUrl + separator + "token=" + encodedToken;
	}

	private void scheduleOnboardingEmail(String email, String activationLink) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			sendOnboardingEmailSafely(email, activationLink);
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				sendOnboardingEmailSafely(email, activationLink);
			}
		});
	}

	private void sendOnboardingEmailSafely(String email, String activationLink) {
		try {
			onboardingMailService.sendOnboardingEmail(email, activationLink);
		} catch (RuntimeException exception) {
			log.error("Failed to send onboarding email to {}", email, exception);
		}
	}
}
