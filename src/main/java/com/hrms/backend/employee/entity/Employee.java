package com.hrms.backend.employee.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

import com.hrms.backend.employee.enums.EmployeeStatus;
import com.hrms.backend.employee.enums.EmploymentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "employees")
public class Employee {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "employee_id")
	private Integer employeeId;

	@Column(name = "employee_code", nullable = false, unique = true, length = 30)
	private String employeeCode;

	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;

	@Column(name = "last_name", nullable = false, length = 100)
	private String lastName;

	@Column(name = "phone", nullable = false, length = 20)
	private String phone;

	@Column(name = "department_id")
	private Integer departmentId;

	@Column(name = "designation_id")
	private Integer designationId;

	@Column(name = "location_id")
	private Integer locationId;

	@Column(name = "shift_id")
	private Integer shiftId;

	@Column(name = "manager_id")
	private Integer managerId;

	@Enumerated(EnumType.STRING)
	@Column(name = "employment_type", nullable = false)
	private EmploymentType employmentType;

	@Column(name = "joining_date", nullable = false)
	private LocalDate joiningDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private EmployeeStatus status;

	@Column(name = "created_at", nullable = false, insertable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false, insertable = false)
	private Instant updatedAt;

	public Employee() {
	}

	public Employee(
			String employeeCode,
			String firstName,
			String lastName,
			String phone,
			EmploymentType employmentType,
			LocalDate joiningDate,
			EmployeeStatus status) {
		this.employeeCode = employeeCode;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phone = phone;
		this.employmentType = employmentType;
		this.joiningDate = joiningDate;
		this.status = status;
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

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Employee employee)) {
			return false;
		}
		return employeeId != null && employeeId.equals(employee.employeeId);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(employeeId);
	}

	@Override
	public String toString() {
		return "Employee{employeeId=" + employeeId + ", employeeCode='" + employeeCode + "'}";
	}
}
