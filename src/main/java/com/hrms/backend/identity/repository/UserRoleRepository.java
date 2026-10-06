package com.hrms.backend.identity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hrms.backend.identity.entity.UserRole;
import com.hrms.backend.identity.entity.UserRoleId;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

	boolean existsByUser_UserIdAndRole_RoleId(Integer userId, Integer roleId);

	List<UserRole> findByUser_UserId(Integer userId);

	List<UserRole> findByRole_RoleId(Integer roleId);
}
