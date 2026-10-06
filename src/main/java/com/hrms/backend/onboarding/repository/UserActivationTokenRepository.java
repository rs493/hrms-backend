package com.hrms.backend.onboarding.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hrms.backend.onboarding.entity.UserActivationToken;

public interface UserActivationTokenRepository extends JpaRepository<UserActivationToken, Integer> {

	Optional<UserActivationToken> findByTokenHash(String tokenHash);
}
