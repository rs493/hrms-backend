package com.hrms.backend.common.exception;

public class AuthenticationFailedException extends RuntimeException {

	public AuthenticationFailedException() {
		super("Invalid email or password.");
	}
}
