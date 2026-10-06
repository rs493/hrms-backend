package com.hrms.backend.identity.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

	private static final String SECRET = "0123456789abcdef0123456789abcdef";

	private static final Instant ISSUED_AT = Instant.parse("2026-09-28T12:00:00Z");

	private static final Instant EXPIRES_AT = ISSUED_AT.plus(Duration.ofHours(1));

	private final JwtProperties jwtProperties = new JwtProperties();

	private JwtService jwtService;

	@BeforeEach
	void setUp() {
		jwtProperties.setSecret(SECRET);
		jwtProperties.setExpiration(Duration.ofHours(1));
		jwtService = new JwtService(jwtProperties);
	}

	@Test
	void generateTokenIncludesIdentityClaimsAndExpiration() {
		String token = jwtService.generateToken(
				7,
				"admin@hrms.com",
				List.of("SUPER_ADMIN"),
				List.of("ROLE_MANAGE"),
				ISSUED_AT,
				EXPIRES_AT);

		SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
		Claims claims = Jwts.parser()
				.verifyWith(key)
				.clock(() -> Date.from(ISSUED_AT))
				.build()
				.parseSignedClaims(token)
				.getPayload();

		assertEquals("7", claims.getSubject());
		assertEquals("admin@hrms.com", claims.get("email", String.class));
		assertEquals(List.of("SUPER_ADMIN"), claims.get("roles", List.class));
		assertEquals(List.of("ROLE_MANAGE"), claims.get("permissions", List.class));
		assertEquals(ISSUED_AT, claims.getIssuedAt().toInstant());
		assertEquals(EXPIRES_AT, claims.getExpiration().toInstant());
	}

	@Test
	void missingSecretIsRejected() {
		jwtProperties.setSecret(" ");

		assertThrows(IllegalStateException.class, () -> jwtService.generateToken(
				7,
				"admin@hrms.com",
				List.of(),
				List.of(),
				ISSUED_AT,
				EXPIRES_AT));
	}
}
