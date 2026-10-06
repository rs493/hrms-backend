package com.hrms.backend.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hrms.backend.identity.entity.JwtToken;

public interface JwtTokenRepository extends JpaRepository<JwtToken, Integer> {
}
