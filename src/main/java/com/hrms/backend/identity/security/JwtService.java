package com.hrms.backend.identity.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private static final int MINIMUM_SECRET_BYTES = 32;

	private final JwtProperties jwtProperties;

	public JwtService(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
	}

	public String generateToken(
			Integer userId,
			String email,
			List<String> roles,
			List<String> permissions,
			Instant issuedAt,
			Instant expiresAt) {
		SecretKey key = Keys.hmacShaKeyFor(signingSecret());
		return Jwts.builder()
				.subject(String.valueOf(userId))
				.claim("email", email)
				.claim("roles", roles)
				.claim("permissions", permissions)
				.issuedAt(Date.from(issuedAt))
				.expiration(Date.from(expiresAt))
				.signWith(key)
				.compact();
	}

	public Claims parseAndValidateToken(String token) {
		SecretKey key = Keys.hmacShaKeyFor(signingSecret());
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	private byte[] signingSecret() {
		String secret = jwtProperties.getSecret();
		if (!StringUtils.hasText(secret)) {
			throw new IllegalStateException("JWT signing secret is not configured. Set HRMS_JWT_SECRET.");
		}
		byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
		if (secretBytes.length < MINIMUM_SECRET_BYTES) {
			throw new IllegalStateException(
					"JWT signing secret is too short. Set HRMS_JWT_SECRET to at least 32 bytes.");
		}
		return secretBytes;
	}
}
