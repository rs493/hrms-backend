package com.hrms.backend.identity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hrms.backend.identity.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Integer> {

	Optional<Permission> findByPermissionCode(String permissionCode);

	boolean existsByPermissionCode(String permissionCode);

	List<Permission> findAllByOrderByPermissionCodeAsc();
}
