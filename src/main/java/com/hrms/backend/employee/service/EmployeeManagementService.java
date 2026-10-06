package com.hrms.backend.employee.service;

import java.time.LocalDate;
import java.util.List;

import com.hrms.backend.employee.entity.Employee;
import com.hrms.backend.employee.enums.EmploymentType;

public interface EmployeeManagementService {

	Employee createEmployee(
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
			LocalDate joiningDate);

	Employee getEmployeeById(Integer employeeId);

	List<Employee> getAllEmployees();

	Employee getManagerForEmployee(Integer employeeId);

	List<Employee> getEmployeesByManager(Integer managerId);

	Employee updateEmployee(
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
			LocalDate joiningDate);

	void deactivateEmployee(Integer employeeId);

	/**
	 * Completes onboarding activation by transitioning an INVITED employee to ACTIVE.
	 * Not part of the public Employee Management deactivate/CRUD lifecycle API.
	 */
	void markInvitedEmployeeActive(Integer employeeId);
}
