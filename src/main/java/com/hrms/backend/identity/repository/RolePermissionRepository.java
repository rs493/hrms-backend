package com.hrms.backend.identity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hrms.backend.identity.entity.RolePermission;
import com.hrms.backend.identity.entity.RolePermissionId;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {

	boolean existsByRole_RoleIdAndPermission_PermissionId(Integer roleId, Integer permissionId);

	Optional<RolePermission> findByRole_RoleIdAndPermission_PermissionId(Integer roleId, Integer permissionId);

	List<RolePermission> findByRole_RoleId(Integer roleId);

	@Query("""
			SELECT rp FROM RolePermission rp
			JOIN FETCH rp.permission
			WHERE rp.role.roleId = :roleId
			""")
	List<RolePermission> findByRole_RoleIdWithPermission(@Param("roleId") Integer roleId);

	List<RolePermission> findByPermission_PermissionId(Integer permissionId);
}
