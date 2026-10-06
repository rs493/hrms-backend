package com.hrms.backend.identity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hrms.backend.identity.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {

	Optional<User> findByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCase(String email);
}
