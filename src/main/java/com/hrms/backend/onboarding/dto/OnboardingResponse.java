package com.hrms.backend.onboarding.dto;

import com.hrms.backend.employee.enums.EmployeeStatus;
import com.hrms.backend.identity.enums.RoleName;
import com.hrms.backend.identity.enums.UserStatus;

public class OnboardingResponse {

	private Integer employeeId;

	private Integer userId;

	private String employeeCode;

	private String email;

	private EmployeeStatus employeeStatus;

	private UserStatus userStatus;

	private RoleName role;

	public OnboardingResponse() {
	}

	public OnboardingResponse(
			Integer employeeId,
			Integer userId,
			String employeeCode,
			String email,
			EmployeeStatus employeeStatus,
			UserStatus userStatus,
			RoleName role) {
		this.employeeId = employeeId;
		this.userId = userId;
		this.employeeCode = employeeCode;
		this.email = email;
		this.employeeStatus = employeeStatus;
		this.userStatus = userStatus;
		this.role = role;
	}

	public Integer getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getEmployeeCode() {
		return employeeCode;
	}

	public void setEmployeeCode(String employeeCode) {
		this.employeeCode = employeeCode;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public EmployeeStatus getEmployeeStatus() {
		return employeeStatus;
	}

	public void setEmployeeStatus(EmployeeStatus employeeStatus) {
		this.employeeStatus = employeeStatus;
	}

	public UserStatus getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(UserStatus userStatus) {
		this.userStatus = userStatus;
	}

	public RoleName getRole() {
		return role;
	}

	public void setRole(RoleName role) {
		this.role = role;
	}
}
