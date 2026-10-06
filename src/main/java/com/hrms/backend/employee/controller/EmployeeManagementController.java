package com.hrms.backend.employee.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hrms.backend.employee.dto.EmployeeCreateRequest;
import com.hrms.backend.employee.dto.EmployeeResponse;
import com.hrms.backend.employee.dto.EmployeeUpdateRequest;
import com.hrms.backend.employee.entity.Employee;
import com.hrms.backend.employee.service.EmployeeManagementService;
import com.hrms.backend.identity.service.AuthorizationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class EmployeeManagementController {

	private static final String EMPLOYEE_CREATE_PERMISSION = "EMPLOYEE_CREATE";

	private static final String EMPLOYEE_VIEW_PERMISSION = "EMPLOYEE_VIEW";

	private static final String EMPLOYEE_UPDATE_PERMISSION = "EMPLOYEE_UPDATE";

	private final EmployeeManagementService employeeManagementService;
	private final AuthorizationService authorizationService;

	public EmployeeManagementController(
			EmployeeManagementService employeeManagementService,
			AuthorizationService authorizationService) {
		this.employeeManagementService = employeeManagementService;
		this.authorizationService = authorizationService;
	}

	@PostMapping("/employees")
	@ResponseStatus(HttpStatus.CREATED)
	public EmployeeResponse createEmployee(@Valid @RequestBody EmployeeCreateRequest request) {
		authorizationService.requirePermission(EMPLOYEE_CREATE_PERMISSION);
		return toResponse(employeeManagementService.createEmployee(
				request.getEmployeeCode(),
				request.getFirstName(),
				request.getLastName(),
				request.getPhone(),
				request.getDepartmentId(),
				request.getDesignationId(),
				request.getLocationId(),
				request.getShiftId(),
				request.getManagerId(),
				request.getEmploymentType(),
				request.getJoiningDate()));
	}

	@GetMapping("/employees")
	public List<EmployeeResponse> getAllEmployees() {
		authorizationService.requirePermission(EMPLOYEE_VIEW_PERMISSION);
		return employeeManagementService.getAllEmployees().stream().map(this::toResponse).toList();
	}

	@GetMapping("/employees/{employeeId}")
	public EmployeeResponse getEmployeeById(@PathVariable Integer employeeId) {
		authorizationService.requirePermission(EMPLOYEE_VIEW_PERMISSION);
		return toResponse(employeeManagementService.getEmployeeById(employeeId));
	}

	@GetMapping("/employees/{employeeId}/manager")
	public EmployeeResponse getManagerForEmployee(@PathVariable Integer employeeId) {
		authorizationService.requirePermission(EMPLOYEE_VIEW_PERMISSION);
		return toResponse(employeeManagementService.getManagerForEmployee(employeeId));
	}

	@GetMapping("/managers/{managerId}/employees")
	public List<EmployeeResponse> getEmployeesByManager(@PathVariable Integer managerId) {
		authorizationService.requirePermission(EMPLOYEE_VIEW_PERMISSION);
		return employeeManagementService.getEmployeesByManager(managerId).stream()
				.map(this::toResponse)
				.toList();
	}

	@PutMapping("/employees/{employeeId}")
	public EmployeeResponse updateEmployee(
			@PathVariable Integer employeeId,
			@Valid @RequestBody EmployeeUpdateRequest request) {
		authorizationService.requirePermission(EMPLOYEE_UPDATE_PERMISSION);
		return toResponse(employeeManagementService.updateEmployee(
				employeeId,
				request.getEmployeeCode(),
				request.getFirstName(),
				request.getLastName(),
				request.getPhone(),
				request.getDepartmentId(),
				request.getDesignationId(),
				request.getLocationId(),
				request.getShiftId(),
				request.getManagerId(),
				request.getEmploymentType(),
				request.getJoiningDate()));
	}

	@PatchMapping("/employees/{employeeId}/deactivate")
	public ResponseEntity<Void> deactivateEmployee(@PathVariable Integer employeeId) {
		authorizationService.requirePermission(EMPLOYEE_UPDATE_PERMISSION);
		employeeManagementService.deactivateEmployee(employeeId);
		return ResponseEntity.noContent().build();
	}

	private EmployeeResponse toResponse(Employee employee) {
		return new EmployeeResponse(
				employee.getEmployeeId(),
				employee.getEmployeeCode(),
				employee.getFirstName(),
				employee.getLastName(),
				employee.getPhone(),
				employee.getDepartmentId(),
				employee.getDesignationId(),
				employee.getLocationId(),
				employee.getShiftId(),
				employee.getManagerId(),
				employee.getEmploymentType(),
				employee.getJoiningDate(),
				employee.getStatus(),
				employee.getCreatedAt(),
				employee.getUpdatedAt());
	}
}
