package com.hrms.backend.onboarding.dto;

import com.hrms.backend.employee.enums.EmployeeStatus;
import com.hrms.backend.identity.enums.UserStatus;

public class ActivationResponse {

	private Integer employeeId;

	private Integer userId;

	private String email;

	private EmployeeStatus employeeStatus;

	private UserStatus userStatus;

	public ActivationResponse() {
	}

	public ActivationResponse(
			Integer employeeId,
			Integer userId,
			String email,
			EmployeeStatus employeeStatus,
			UserStatus userStatus) {
		this.employeeId = employeeId;
		this.userId = userId;
		this.email = email;
		this.employeeStatus = employeeStatus;
		this.userStatus = userStatus;
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
}
