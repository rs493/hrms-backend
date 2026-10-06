package com.hrms.backend.identity.serviceImpl;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.hrms.backend.common.exception.InvalidRequestException;
import com.hrms.backend.common.exception.ResourceConflictException;
import com.hrms.backend.common.exception.ResourceNotFoundException;
import com.hrms.backend.employee.entity.Employee;
import com.hrms.backend.identity.entity.User;
import com.hrms.backend.identity.enums.UserStatus;
import com.hrms.backend.identity.repository.UserRepository;
import com.hrms.backend.identity.service.UserManagementService;

@Service
public class UserManagementServiceImpl implements UserManagementService {

	private static final int EMAIL_MAX_LENGTH = 150;

	private static final int PASSWORD_MIN_LENGTH = 8;

	private static final int PASSWORD_MAX_LENGTH = 255;

	private static final int INTERNAL_SECRET_BYTES = 32;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final SecureRandom secureRandom = new SecureRandom();

	public UserManagementServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public User createAccount(String email, String rawPassword, Employee employee) {
		String normalizedEmail = normalizeEmail(email);
		ensureEmailAvailable(normalizedEmail);
		validatePassword(rawPassword);
		return persistUser(normalizedEmail, passwordEncoder.encode(rawPassword), employee, UserStatus.ACTIVE);
	}

	@Override
	@Transactional
	public User createInvitedAccount(String email, Employee employee) {
		String normalizedEmail = normalizeEmail(email);
		ensureEmailAvailable(normalizedEmail);
		String initialSecret = generateInternalSecret();
		String passwordHash = passwordEncoder.encode(initialSecret);
		return persistUser(normalizedEmail, passwordHash, employee, UserStatus.INVITED);
	}

	private User persistUser(
			String normalizedEmail,
			String passwordHash,
			Employee employee,
			UserStatus status) {
		User user = new User(normalizedEmail, passwordHash, status);
		user.setEmployee(employee);
		user.setUpdatedAt(Instant.now());
		return userRepository.save(user);
	}

	private void ensureEmailAvailable(String normalizedEmail) {
		if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
			throw new ResourceConflictException("A user with this email already exists.");
		}
	}

	private String generateInternalSecret() {
		byte[] bytes = new byte[INTERNAL_SECRET_BYTES];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	@Override
	@Transactional(readOnly = true)
	public User getUserById(Integer userId) {
		return requireUser(userId);
	}

	@Override
	@Transactional(readOnly = true)
	public User getUserByEmail(String email) {
		String normalizedEmail = normalizeEmail(email);
		return userRepository.findByEmailIgnoreCase(normalizedEmail)
				.orElseThrow(() -> new ResourceNotFoundException("User was not found."));
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByEmail(String email) {
		return userRepository.existsByEmailIgnoreCase(normalizeEmail(email));
	}

	@Override
	@Transactional
	public void changePassword(Integer userId, String newPassword) {
		User user = requireUser(userId);
		validatePassword(newPassword);
		user.setPasswordHash(passwordEncoder.encode(newPassword));
		user.setUpdatedAt(Instant.now());
		userRepository.save(user);
	}

	@Override
	@Transactional
	public void activateUser(Integer userId) {
		updateStatus(userId, UserStatus.ACTIVE);
	}

	@Override
	@Transactional
	public void deactivateUser(Integer userId) {
		updateStatus(userId, UserStatus.INACTIVE);
	}

	@Override
	@Transactional
	public void lockUser(Integer userId) {
		updateStatus(userId, UserStatus.LOCKED);
	}

	private void updateStatus(Integer userId, UserStatus status) {
		User user = requireUser(userId);
		user.setStatus(status);
		user.setUpdatedAt(Instant.now());
		userRepository.save(user);
	}

	private User requireUser(Integer userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User was not found."));
	}

	private String normalizeEmail(String email) {
		if (!StringUtils.hasText(email)) {
			throw new InvalidRequestException("Email is required.");
		}
		String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
		if (normalizedEmail.length() > EMAIL_MAX_LENGTH) {
			throw new InvalidRequestException("Email must be at most 150 characters.");
		}
		return normalizedEmail;
	}

	private void validatePassword(String password) {
		if (!StringUtils.hasText(password)) {
			throw new InvalidRequestException("Password is required.");
		}
		if (password.length() < PASSWORD_MIN_LENGTH || password.length() > PASSWORD_MAX_LENGTH) {
			throw new InvalidRequestException("Password must be between 8 and 255 characters.");
		}
	}
}
