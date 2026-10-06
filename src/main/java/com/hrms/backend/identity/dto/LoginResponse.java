package com.hrms.backend.identity.dto;

import java.time.Instant;

public class LoginResponse {

	private String accessToken;

	private String tokenType;

	private Instant expiresAt;

	public LoginResponse() {
	}

	public LoginResponse(String accessToken, String tokenType, Instant expiresAt) {
		this.accessToken = accessToken;
		this.tokenType = tokenType;
		this.expiresAt = expiresAt;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}
}
