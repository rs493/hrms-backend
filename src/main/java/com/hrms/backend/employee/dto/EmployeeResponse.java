package com.hrms.backend.employee.dto;

import java.time.Instant;
import java.time.LocalDate;

import com.hrms.backend.employee.enums.EmployeeStatus;
import com.hrms.backend.employee.enums.EmploymentType;

public class EmployeeResponse {

	private Integer employeeId;

	private String employeeCode;

	private String firstName;

	private String lastName;

	private String phone;

	private Integer departmentId;

	private Integer designationId;

	private Integer locationId;

	private Integer shiftId;

	private Integer managerId;

	private EmploymentType employmentType;

	private LocalDate joiningDate;

	private EmployeeStatus status;

	private Instant createdAt;

	private Instant updatedAt;

	public EmployeeResponse() {
	}

	public EmployeeResponse(
			Integer employeeId,
			String employeeCode,
			String firstName,
			String lastName,
			String phone,
			Integer departmentId,
			Integer designationId,
			Integer locationId,
			Integer shiftId,
			Integer managerId,
			EmploymentType employmentType,
			LocalDate joiningDate,
			EmployeeStatus status,
			Instant createdAt,
			Instant updatedAt) {
		this.employeeId = employeeId;
		this.employeeCode = employeeCode;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phone = phone;
		this.departmentId = departmentId;
		this.designationId = designationId;
		this.locationId = locationId;
		this.shiftId = shiftId;
		this.managerId = managerId;
		this.employmentType = employmentType;
		this.joiningDate = joiningDate;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public Integer getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

	public String getEmployeeCode() {
		return employeeCode;
	}

	public void setEmployeeCode(String employeeCode) {
		this.employeeCode = employeeCode;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Integer getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Integer departmentId) {
		this.departmentId = departmentId;
	}

	public Integer getDesignationId() {
		return designationId;
	}

	public void setDesignationId(Integer designationId) {
		this.designationId = designationId;
	}

	public Integer getLocationId() {
		return locationId;
	}

	public void setLocationId(Integer locationId) {
		this.locationId = locationId;
	}

	public Integer getShiftId() {
		return shiftId;
	}

	public void setShiftId(Integer shiftId) {
		this.shiftId = shiftId;
	}

	public Integer getManagerId() {
		return managerId;
	}

	public void setManagerId(Integer managerId) {
		this.managerId = managerId;
	}

	public EmploymentType getEmploymentType() {
		return employmentType;
	}

	public void setEmploymentType(EmploymentType employmentType) {
		this.employmentType = employmentType;
	}

	public LocalDate getJoiningDate() {
		return joiningDate;
	}

	public void setJoiningDate(LocalDate joiningDate) {
		this.joiningDate = joiningDate;
	}

	public EmployeeStatus getStatus() {
		return status;
	}

	public void setStatus(EmployeeStatus status) {
		this.status = status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
