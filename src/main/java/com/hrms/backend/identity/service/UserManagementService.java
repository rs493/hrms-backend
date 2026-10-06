package com.hrms.backend.identity.service;

import com.hrms.backend.employee.entity.Employee;
import com.hrms.backend.identity.entity.User;

public interface UserManagementService {

	User createAccount(String email, String rawPassword, Employee employee);

	User createInvitedAccount(String email, Employee employee);

	User getUserById(Integer userId);

	User getUserByEmail(String email);

	boolean existsByEmail(String email);

	void changePassword(Integer userId, String newPassword);

	void activateUser(Integer userId);

	void deactivateUser(Integer userId);

	void lockUser(Integer userId);
}
