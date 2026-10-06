package com.hrms.backend.identity.entity;

import java.time.Instant;
import java.util.Objects;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "jwt_tokens")
public class JwtToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "token_id")
	private Integer tokenId;

	@Column(name = "user_id", nullable = false, updatable = false)
	private Integer userId;

	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(name = "token")
	private String token;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "expires_at", nullable = false, updatable = false)
	private Instant expiresAt;

	public JwtToken() {
	}

	public JwtToken(Integer userId, String token, Instant createdAt, Instant expiresAt) {
		this.userId = userId;
		this.token = token;
		this.createdAt = createdAt;
		this.expiresAt = expiresAt;
	}

	public Integer getTokenId() {
		return tokenId;
	}

	public void setTokenId(Integer tokenId) {
		this.tokenId = tokenId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof JwtToken jwtToken)) {
			return false;
		}
		return tokenId != null && tokenId.equals(jwtToken.tokenId);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(tokenId);
	}

	@Override
	public String toString() {
		return "JwtToken{tokenId=" + tokenId + ", userId=" + userId + ", expiresAt=" + expiresAt + "}";
	}
}
