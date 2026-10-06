package com.hrms.backend.employee.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.hrms.backend.employee.dto.EmployeeCreateRequest;
import com.hrms.backend.employee.dto.EmployeeUpdateRequest;
import com.hrms.backend.employee.entity.Employee;
import com.hrms.backend.employee.enums.EmployeeStatus;
import com.hrms.backend.employee.enums.EmploymentType;
import com.hrms.backend.employee.service.EmployeeManagementService;
import com.hrms.backend.identity.service.AuthorizationService;

class EmployeeManagementControllerAuthorizationTest {

	private final EmployeeManagementService employeeManagementService =
			org.mockito.Mockito.mock(EmployeeManagementService.class);
	private final AuthorizationService authorizationService =
			org.mockito.Mockito.mock(AuthorizationService.class);

	private EmployeeManagementController controller;

	@BeforeEach
	void setUp() {
		controller = new EmployeeManagementController(employeeManagementService, authorizationService);
	}

	@Test
	void createEmployee_requiresEmployeeCreate() {
		EmployeeCreateRequest request = createRequest();
		when(employeeManagementService.createEmployee(
				anyString(), anyString(), anyString(), anyString(),
				any(), any(), any(), any(), any(), any(), any()))
				.thenReturn(employee());

		controller.createEmployee(request);

		verify(authorizationService).requirePermission("EMPLOYEE_CREATE");
		verify(employeeManagementService).createEmployee(
				anyString(), anyString(), anyString(), anyString(),
				any(), any(), any(), any(), any(), any(), any());
	}

	@Test
	void getEmployeeById_requiresEmployeeView() {
		when(employeeManagementService.getEmployeeById(10)).thenReturn(employee());

		controller.getEmployeeById(10);

		verify(authorizationService).requirePermission("EMPLOYEE_VIEW");
		verify(employeeManagementService).getEmployeeById(10);
	}

	@Test
	void getAllEmployees_requiresEmployeeView() {
		when(employeeManagementService.getAllEmployees()).thenReturn(List.of(employee()));

		controller.getAllEmployees();

		verify(authorizationService).requirePermission("EMPLOYEE_VIEW");
	}

	@Test
	void updateEmployee_requiresEmployeeUpdate() {
		EmployeeUpdateRequest request = updateRequest();
		when(employeeManagementService.updateEmployee(
				anyInt(), anyString(), anyString(), anyString(), anyString(),
				any(), any(), any(), any(), any(), any(), any()))
				.thenReturn(employee());

		controller.updateEmployee(10, request);

		verify(authorizationService).requirePermission("EMPLOYEE_UPDATE");
	}

	@Test
	void deactivateEmployee_requiresEmployeeUpdate() {
		controller.deactivateEmployee(10);

		verify(authorizationService).requirePermission("EMPLOYEE_UPDATE");
		verify(employeeManagementService).deactivateEmployee(10);
	}

	@Test
	void getEmployeeById_rejectsWithoutEmployeeView() {
		doThrow(new AccessDeniedException("Required permission is missing: EMPLOYEE_VIEW"))
				.when(authorizationService)
				.requirePermission("EMPLOYEE_VIEW");

		assertThrows(AccessDeniedException.class, () -> controller.getEmployeeById(10));
		verify(employeeManagementService, never()).getEmployeeById(anyInt());
	}

	private static EmployeeCreateRequest createRequest() {
		EmployeeCreateRequest request = new EmployeeCreateRequest();
		request.setEmployeeCode("EMP001");
		request.setFirstName("Raja");
		request.setLastName("Kumar");
		request.setPhone("9999999999");
		request.setEmploymentType(EmploymentType.FULL_TIME);
		request.setJoiningDate(LocalDate.of(2026, 1, 1));
		return request;
	}

	private static EmployeeUpdateRequest updateRequest() {
		EmployeeUpdateRequest request = new EmployeeUpdateRequest();
		request.setEmployeeCode("EMP001");
		request.setFirstName("Raja");
		request.setLastName("Kumar");
		request.setPhone("9999999999");
		request.setEmploymentType(EmploymentType.FULL_TIME);
		request.setJoiningDate(LocalDate.of(2026, 1, 1));
		return request;
	}

	private static Employee employee() {
		Employee employee = new Employee(
				"EMP001",
				"Raja",
				"Kumar",
				"9999999999",
				EmploymentType.FULL_TIME,
				LocalDate.of(2026, 1, 1),
				EmployeeStatus.INVITED);
		employee.setEmployeeId(10);
		return employee;
	}
}
