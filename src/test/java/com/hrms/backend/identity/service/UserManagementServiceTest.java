package com.hrms.backend.identity.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hrms.backend.common.exception.InvalidRequestException;
import com.hrms.backend.common.exception.ResourceConflictException;
import com.hrms.backend.employee.entity.Employee;
import com.hrms.backend.employee.enums.EmployeeStatus;
import com.hrms.backend.employee.enums.EmploymentType;
import com.hrms.backend.identity.entity.User;
import com.hrms.backend.identity.enums.UserStatus;
import com.hrms.backend.identity.repository.UserRepository;
import com.hrms.backend.identity.serviceImpl.UserManagementServiceImpl;

class UserManagementServiceTest {

	private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	private UserManagementService userManagementService;

	@BeforeEach
	void setUp() {
		userManagementService = new UserManagementServiceImpl(userRepository, passwordEncoder);
	}

	@Test
	void createInvitedAccount_populatesPasswordHashAndSetsInvitedStatus() {
		Employee employee = employee();
		when(userRepository.existsByEmailIgnoreCase("raja@hrms.com")).thenReturn(false);
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
			User saved = invocation.getArgument(0);
			saved.setUserId(20);
			return saved;
		});

		User created = userManagementService.createInvitedAccount("raja@hrms.com", employee);

		assertEquals(20, created.getUserId());
		assertEquals("raja@hrms.com", created.getEmail());
		assertEquals(UserStatus.INVITED, created.getStatus());
		assertNotNull(created.getPasswordHash());
		assertTrue(created.getPasswordHash().startsWith("$2"));
		assertNotEquals("raja@hrms.com", created.getPasswordHash());

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(userCaptor.capture());
		assertEquals(UserStatus.INVITED, userCaptor.getValue().getStatus());
		assertNotNull(userCaptor.getValue().getPasswordHash());
	}

	@Test
	void createInvitedAccount_rejectsDuplicateEmail() {
		when(userRepository.existsByEmailIgnoreCase("raja@hrms.com")).thenReturn(true);

		assertThrows(
				ResourceConflictException.class,
				() -> userManagementService.createInvitedAccount("raja@hrms.com", employee()));
		verify(userRepository, never()).save(any());
	}

	@Test
	void changePassword_replacesExistingHash() {
		User user = new User("raja@hrms.com", passwordEncoder.encode("OldPassword1!"), UserStatus.INVITED);
		user.setUserId(20);
		when(userRepository.findById(20)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);

		userManagementService.changePassword(20, "NewSecurePassword123!");

		assertTrue(passwordEncoder.matches("NewSecurePassword123!", user.getPasswordHash()));
		assertTrue(!passwordEncoder.matches("OldPassword1!", user.getPasswordHash()));
		verify(userRepository).save(user);
	}

	@Test
	void changePassword_rejectsShortPassword() {
		User user = new User("raja@hrms.com", "hash", UserStatus.INVITED);
		user.setUserId(20);
		when(userRepository.findById(20)).thenReturn(Optional.of(user));

		assertThrows(InvalidRequestException.class, () -> userManagementService.changePassword(20, "short"));
		verify(userRepository, never()).save(any());
	}

	@Test
	void createAccount_encodesProvidedPassword() {
		Employee employee = employee();
		when(userRepository.existsByEmailIgnoreCase("active@hrms.com")).thenReturn(false);
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		User created = userManagementService.createAccount("active@hrms.com", "ActivePass1!", employee);

		assertEquals(UserStatus.ACTIVE, created.getStatus());
		assertTrue(passwordEncoder.matches("ActivePass1!", created.getPasswordHash()));
		verify(userRepository).existsByEmailIgnoreCase("active@hrms.com");
		verify(userRepository).save(any(User.class));
	}

	private static Employee employee() {
		Employee employee = new Employee(
				"EMP001",
				"Raja",
				"Kumar",
				"9999999999",
				EmploymentType.FULL_TIME,
				LocalDate.of(2026, 1, 1),
				EmployeeStatus.INVITED);
		employee.setEmployeeId(10);
		return employee;
	}
}
