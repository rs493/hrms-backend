package com.hrms.backend.identity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hrms.backend.identity.entity.Role;
import com.hrms.backend.identity.enums.RoleName;

public interface RoleRepository extends JpaRepository<Role, Integer> {

	Optional<Role> findByRoleName(RoleName roleName);

	boolean existsByRoleName(RoleName roleName);
}
