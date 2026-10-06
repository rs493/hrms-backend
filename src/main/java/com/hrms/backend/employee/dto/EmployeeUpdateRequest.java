package com.hrms.backend.employee.dto;

import java.time.LocalDate;

import com.hrms.backend.employee.enums.EmploymentType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EmployeeUpdateRequest {

	@NotBlank(message = "Employee code is required.")
	@Size(max = 30, message = "Employee code must be at most 30 characters.")
	private String employeeCode;

	@NotBlank(message = "First name is required.")
	@Size(max = 100, message = "First name must be at most 100 characters.")
	private String firstName;

	@NotBlank(message = "Last name is required.")
	@Size(max = 100, message = "Last name must be at most 100 characters.")
	private String lastName;

	@NotBlank(message = "Phone is required.")
	@Size(max = 20, message = "Phone must be at most 20 characters.")
	private String phone;

	private Integer departmentId;

	private Integer designationId;

	private Integer locationId;

	private Integer shiftId;

	private Integer managerId;

	@NotNull(message = "Employment type is required.")
	private EmploymentType employmentType;

	@NotNull(message = "Joining date is required.")
	private LocalDate joiningDate;

	public EmployeeUpdateRequest() {
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
}
