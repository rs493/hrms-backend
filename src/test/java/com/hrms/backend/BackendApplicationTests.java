package com.hrms.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.hrms.backend.employee.repository.EmployeeRepository;
import com.hrms.backend.identity.repository.JwtTokenRepository;
import com.hrms.backend.identity.repository.PermissionRepository;
import com.hrms.backend.identity.repository.RolePermissionRepository;
import com.hrms.backend.identity.repository.RoleRepository;
import com.hrms.backend.identity.repository.UserRepository;
import com.hrms.backend.identity.repository.UserRoleRepository;
import com.hrms.backend.onboarding.repository.UserActivationTokenRepository;

@SpringBootTest
class BackendApplicationTests {

	@MockitoBean
	private EmployeeRepository employeeRepository;

	@MockitoBean
	private PermissionRepository permissionRepository;

	@MockitoBean
	private RoleRepository roleRepository;

	@MockitoBean
	private RolePermissionRepository rolePermissionRepository;

	@MockitoBean
	private UserRepository userRepository;

	@MockitoBean
	private UserRoleRepository userRoleRepository;

	@MockitoBean
	private JwtTokenRepository jwtTokenRepository;

	@MockitoBean
	private UserActivationTokenRepository userActivationTokenRepository;

	@Test
	void contextLoads() {
	}

}
