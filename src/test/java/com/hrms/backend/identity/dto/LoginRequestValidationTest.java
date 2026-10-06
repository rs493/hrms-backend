package com.hrms.backend.identity.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class LoginRequestValidationTest {

	private Validator validator;

	@BeforeEach
	void setUp() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Test
	void validRequestHasNoViolations() {
		Set<ConstraintViolation<LoginRequest>> violations = validator.validate(
				new LoginRequest("admin@hrms.com", "Admin@123"));

		assertTrue(violations.isEmpty());
	}

	@Test
	void blankEmailAndPasswordAreRejected() {
		Set<String> messages = messages(new LoginRequest(" ", " "));

		assertTrue(messages.contains("Email is required."));
		assertTrue(messages.contains("Password is required."));
	}

	@Test
	void malformedEmailIsRejectedWithoutEchoingThePassword() {
		Set<String> messages = messages(new LoginRequest("not-an-email", "Admin@123"));

		assertEquals(Set.of("Email must be valid."), messages);
	}

	private Set<String> messages(LoginRequest request) {
		return validator.validate(request).stream()
				.map(ConstraintViolation::getMessage)
				.collect(Collectors.toSet());
	}
}
