package com.hrms.backend.onboarding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ActivationRequest {

	@NotBlank(message = "Activation token is required.")
	private String token;

	@NotBlank(message = "New password is required.")
	@Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters.")
	private String newPassword;

	@NotBlank(message = "Confirm password is required.")
	private String confirmPassword;

	public ActivationRequest() {
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}
}
