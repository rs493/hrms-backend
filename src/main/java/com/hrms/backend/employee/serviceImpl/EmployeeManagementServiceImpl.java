package com.hrms.backend.employee.serviceImpl;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.hrms.backend.common.exception.InvalidRequestException;
import com.hrms.backend.common.exception.ResourceConflictException;
import com.hrms.backend.common.exception.ResourceNotFoundException;
import com.hrms.backend.employee.entity.Employee;
import com.hrms.backend.employee.enums.EmployeeStatus;
import com.hrms.backend.employee.enums.EmploymentType;
import com.hrms.backend.employee.repository.EmployeeRepository;
import com.hrms.backend.employee.service.EmployeeManagementService;

@Service
public class EmployeeManagementServiceImpl implements EmployeeManagementService {

	private static final int EMPLOYEE_CODE_MAX_LENGTH = 30;

	private static final int NAME_MAX_LENGTH = 100;

	private static final int PHONE_MAX_LENGTH = 20;

	private final EmployeeRepository employeeRepository;

	public EmployeeManagementServiceImpl(EmployeeRepository employeeRepository) {
		this.employeeRepository = employeeRepository;
	}

	@Override
	@Transactional
	public Employee createEmployee(
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
			LocalDate joiningDate) {
		String normalizedCode = normalizeEmployeeCode(employeeCode);
		String normalizedFirstName = normalizeRequiredText(firstName, "First name", NAME_MAX_LENGTH);
		String normalizedLastName = normalizeRequiredText(lastName, "Last name", NAME_MAX_LENGTH);
		String normalizedPhone = normalizeRequiredText(phone, "Phone", PHONE_MAX_LENGTH);
		requireEmploymentType(employmentType);
		requireJoiningDate(joiningDate);
		if (employeeRepository.existsByEmployeeCode(normalizedCode)) {
			throw new ResourceConflictException("An employee with this employee code already exists.");
		}
		validateManager(managerId, null);

		Employee employee = new Employee(
				normalizedCode,
				normalizedFirstName,
				normalizedLastName,
				normalizedPhone,
				employmentType,
				joiningDate,
				EmployeeStatus.INVITED);
		employee.setDepartmentId(departmentId);
		employee.setDesignationId(designationId);
		employee.setLocationId(locationId);
		employee.setShiftId(shiftId);
		employee.setManagerId(managerId);
		return employeeRepository.save(employee);
	}

	@Override
	@Transactional(readOnly = true)
	public Employee getEmployeeById(Integer employeeId) {
		return requireEmployee(employeeId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Employee> getAllEmployees() {
		return employeeRepository.findAllByOrderByEmployeeCodeAsc();
	}

	@Override
	@Transactional(readOnly = true)
	public Employee getManagerForEmployee(Integer employeeId) {
		Employee employee = requireEmployee(employeeId);
		if (employee.getManagerId() == null) {
			throw new ResourceNotFoundException("Manager was not found.");
		}
		return requireEmployee(employee.getManagerId());
	}

	@Override
	@Transactional(readOnly = true)
	public List<Employee> getEmployeesByManager(Integer managerId) {
		requireEmployee(managerId);
		return employeeRepository.findByManagerIdOrderByEmployeeCodeAsc(managerId);
	}

	@Override
	@Transactional
	public Employee updateEmployee(
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
			LocalDate joiningDate) {
		Employee employee = requireEmployee(employeeId);
		String normalizedCode = normalizeEmployeeCode(employeeCode);
		String normalizedFirstName = normalizeRequiredText(firstName, "First name", NAME_MAX_LENGTH);
		String normalizedLastName = normalizeRequiredText(lastName, "Last name", NAME_MAX_LENGTH);
		String normalizedPhone = normalizeRequiredText(phone, "Phone", PHONE_MAX_LENGTH);
		requireEmploymentType(employmentType);
		requireJoiningDate(joiningDate);
		if (employeeRepository.existsByEmployeeCodeAndEmployeeIdNot(normalizedCode, employeeId)) {
			throw new ResourceConflictException("An employee with this employee code already exists.");
		}
		validateManager(managerId, employeeId);

		employee.setEmployeeCode(normalizedCode);
		employee.setFirstName(normalizedFirstName);
		employee.setLastName(normalizedLastName);
		employee.setPhone(normalizedPhone);
		employee.setDepartmentId(departmentId);
		employee.setDesignationId(designationId);
		employee.setLocationId(locationId);
		employee.setShiftId(shiftId);
		employee.setManagerId(managerId);
		employee.setEmploymentType(employmentType);
		employee.setJoiningDate(joiningDate);
		employee.setUpdatedAt(Instant.now());
		return employeeRepository.save(employee);
	}

	@Override
	@Transactional
	public void deactivateEmployee(Integer employeeId) {
		Employee employee = requireEmployee(employeeId);
		if (employee.getStatus() != EmployeeStatus.ACTIVE) {
			throw new InvalidRequestException("Only ACTIVE employees can be deactivated.");
		}
		employee.setStatus(EmployeeStatus.INACTIVE);
		employee.setUpdatedAt(Instant.now());
		employeeRepository.save(employee);
	}

	@Override
	@Transactional
	public void markInvitedEmployeeActive(Integer employeeId) {
		Employee employee = requireEmployee(employeeId);
		if (employee.getStatus() != EmployeeStatus.INVITED) {
			throw new InvalidRequestException("Only INVITED employees can be activated through onboarding.");
		}
		employee.setStatus(EmployeeStatus.ACTIVE);
		employee.setUpdatedAt(Instant.now());
		employeeRepository.save(employee);
	}

	private Employee requireEmployee(Integer employeeId) {
		if (employeeId == null) {
			throw new InvalidRequestException("Employee id is required.");
		}
		return employeeRepository.findById(employeeId)
				.orElseThrow(() -> new ResourceNotFoundException("Employee was not found."));
	}

	private void validateManager(Integer managerId, Integer employeeId) {
		if (managerId == null) {
			return;
		}
		if (employeeId != null && managerId.equals(employeeId)) {
			throw new InvalidRequestException("An employee cannot be their own manager.");
		}
		if (!employeeRepository.existsById(managerId)) {
			throw new ResourceNotFoundException("Manager was not found.");
		}
	}

	private String normalizeEmployeeCode(String employeeCode) {
		return normalizeRequiredText(employeeCode, "Employee code", EMPLOYEE_CODE_MAX_LENGTH);
	}

	private String normalizeRequiredText(String value, String fieldLabel, int maxLength) {
		if (!StringUtils.hasText(value)) {
			throw new InvalidRequestException(fieldLabel + " is required.");
		}
		String normalized = value.trim();
		if (normalized.length() > maxLength) {
			throw new InvalidRequestException(
					fieldLabel + " must be at most " + maxLength + " characters.");
		}
		return normalized;
	}

	private void requireEmploymentType(EmploymentType employmentType) {
		if (employmentType == null) {
			throw new InvalidRequestException("Employment type is required.");
		}
	}

	private void requireJoiningDate(LocalDate joiningDate) {
		if (joiningDate == null) {
			throw new InvalidRequestException("Joining date is required.");
		}
	}
}
