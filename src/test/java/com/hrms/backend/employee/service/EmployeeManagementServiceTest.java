package com.hrms.backend.employee.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.hrms.backend.common.exception.InvalidRequestException;
import com.hrms.backend.common.exception.ResourceConflictException;
import com.hrms.backend.common.exception.ResourceNotFoundException;
import com.hrms.backend.employee.entity.Employee;
import com.hrms.backend.employee.enums.EmployeeStatus;
import com.hrms.backend.employee.enums.EmploymentType;
import com.hrms.backend.employee.repository.EmployeeRepository;
import com.hrms.backend.employee.serviceImpl.EmployeeManagementServiceImpl;

class EmployeeManagementServiceTest {

	private final EmployeeRepository employeeRepository = org.mockito.Mockito.mock(EmployeeRepository.class);

	private EmployeeManagementService employeeManagementService;

	@BeforeEach
	void setUp() {
		employeeManagementService = new EmployeeManagementServiceImpl(employeeRepository);
	}

	@Test
	void createEmployee_savesInvitedEmployeeWithoutManager() {
		when(employeeRepository.existsByEmployeeCode("E001")).thenReturn(false);
		when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
			Employee employee = invocation.getArgument(0);
			employee.setEmployeeId(1);
			return employee;
		});

		Employee created = employeeManagementService.createEmployee(
				" E001 ",
				" Raj ",
				" Sharma ",
				" 9999999999 ",
				null,
				null,
				null,
				null,
				null,
				EmploymentType.FULL_TIME,
				LocalDate.of(2026, 1, 1));

		assertEquals(1, created.getEmployeeId());
		assertEquals("E001", created.getEmployeeCode());
		assertEquals("Raj", created.getFirstName());
		assertEquals("Sharma", created.getLastName());
		assertEquals("9999999999", created.getPhone());
		assertNull(created.getManagerId());
		assertEquals(EmployeeStatus.INVITED, created.getStatus());
		verify(employeeRepository).save(any(Employee.class));
	}

	@Test
	void createEmployee_alwaysUsesInvitedRegardlessOfOtherStatuses() {
		when(employeeRepository.existsByEmployeeCode("E010")).thenReturn(false);
		when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Employee created = employeeManagementService.createEmployee(
				"E010",
				"Raja",
				"Kumar",
				"7777777777",
				null,
				null,
				null,
				null,
				null,
				EmploymentType.FULL_TIME,
				LocalDate.of(2026, 3, 1));

		assertEquals(EmployeeStatus.INVITED, created.getStatus());
	}

	@Test
	void createEmployee_rejectsDuplicateCode() {
		when(employeeRepository.existsByEmployeeCode("E001")).thenReturn(true);

		assertThrows(
				ResourceConflictException.class,
				() -> employeeManagementService.createEmployee(
						"E001",
						"Raj",
						"Sharma",
						"9999999999",
						null,
						null,
						null,
						null,
						null,
						EmploymentType.FULL_TIME,
						LocalDate.of(2026, 1, 1)));

		verify(employeeRepository, never()).save(any());
	}

	@Test
	void createEmployee_rejectsBlankCode() {
		assertThrows(
				InvalidRequestException.class,
				() -> employeeManagementService.createEmployee(
						"  ",
						"Raj",
						"Sharma",
						"9999999999",
						null,
						null,
						null,
						null,
						null,
						EmploymentType.FULL_TIME,
						LocalDate.of(2026, 1, 1)));
	}

	@Test
	void createEmployee_rejectsMissingManager() {
		when(employeeRepository.existsByEmployeeCode("E002")).thenReturn(false);
		when(employeeRepository.existsById(99)).thenReturn(false);

		assertThrows(
				ResourceNotFoundException.class,
				() -> employeeManagementService.createEmployee(
						"E002",
						"Meera",
						"Patel",
						"8888888888",
						null,
						null,
						null,
						null,
						99,
						EmploymentType.FULL_TIME,
						LocalDate.of(2026, 2, 1)));

		verify(employeeRepository, never()).save(any());
	}

	@Test
	void createEmployee_assignsExistingManager() {
		when(employeeRepository.existsByEmployeeCode("E002")).thenReturn(false);
		when(employeeRepository.existsById(1)).thenReturn(true);
		when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
			Employee employee = invocation.getArgument(0);
			employee.setEmployeeId(2);
			return employee;
		});

		Employee created = employeeManagementService.createEmployee(
				"E002",
				"Meera",
				"Patel",
				"8888888888",
				null,
				null,
				null,
				null,
				1,
				EmploymentType.FULL_TIME,
				LocalDate.of(2026, 2, 1));

		assertEquals(1, created.getManagerId());
		assertEquals(EmployeeStatus.INVITED, created.getStatus());
	}

	@Test
	void getEmployeeById_returnsEmployee() {
		Employee employee = invitedEmployee(1, "E001");
		when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));

		Employee found = employeeManagementService.getEmployeeById(1);

		assertEquals(employee, found);
		assertEquals(EmployeeStatus.INVITED, found.getStatus());
	}

	@Test
	void getEmployeeById_missingThrows() {
		when(employeeRepository.findById(99)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> employeeManagementService.getEmployeeById(99));
	}

	@Test
	void getAllEmployees_returnsOrderedList() {
		List<Employee> employees = List.of(invitedEmployee(1, "E001"), invitedEmployee(2, "E002"));
		when(employeeRepository.findAllByOrderByEmployeeCodeAsc()).thenReturn(employees);

		assertEquals(employees, employeeManagementService.getAllEmployees());
	}

	@Test
	void updateEmployee_doesNotChangeStatus() {
		Employee employee = invitedEmployee(1, "E001");
		when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));
		when(employeeRepository.existsByEmployeeCodeAndEmployeeIdNot("E001", 1)).thenReturn(false);
		when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Employee updated = employeeManagementService.updateEmployee(
				1,
				"E001",
				"Raja",
				"Kumar",
				"9999999999",
				null,
				null,
				null,
				null,
				null,
				EmploymentType.FULL_TIME,
				LocalDate.of(2026, 1, 1));

		assertEquals("Raja", updated.getFirstName());
		assertEquals(EmployeeStatus.INVITED, updated.getStatus());
	}

	@Test
	void updateEmployee_rejectsSelfAsManager() {
		Employee employee = invitedEmployee(1, "E001");
		when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));
		when(employeeRepository.existsByEmployeeCodeAndEmployeeIdNot("E001", 1)).thenReturn(false);

		assertThrows(
				InvalidRequestException.class,
				() -> employeeManagementService.updateEmployee(
						1,
						"E001",
						"Raj",
						"Sharma",
						"9999999999",
						null,
						null,
						null,
						null,
						1,
						EmploymentType.FULL_TIME,
						LocalDate.of(2026, 1, 1)));

		verify(employeeRepository, never()).save(any());
	}

	@Test
	void updateEmployee_rejectsDuplicateCode() {
		Employee employee = invitedEmployee(1, "E001");
		when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));
		when(employeeRepository.existsByEmployeeCodeAndEmployeeIdNot("E002", 1)).thenReturn(true);

		assertThrows(
				ResourceConflictException.class,
				() -> employeeManagementService.updateEmployee(
						1,
						"E002",
						"Raj",
						"Sharma",
						"9999999999",
						null,
						null,
						null,
						null,
						null,
						EmploymentType.FULL_TIME,
						LocalDate.of(2026, 1, 1)));
	}

	@Test
	void deactivateEmployee_fromActiveSetsInactive() {
		Employee employee = invitedEmployee(1, "E001");
		employee.setStatus(EmployeeStatus.ACTIVE);
		when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));
		when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

		employeeManagementService.deactivateEmployee(1);

		ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
		verify(employeeRepository).save(captor.capture());
		assertEquals(EmployeeStatus.INACTIVE, captor.getValue().getStatus());
	}

	@Test
	void deactivateEmployee_rejectsInvitedEmployee() {
		Employee employee = invitedEmployee(1, "E001");
		when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));

		assertThrows(InvalidRequestException.class, () -> employeeManagementService.deactivateEmployee(1));
		verify(employeeRepository, never()).save(any());
	}

	@Test
	void markInvitedEmployeeActive_fromInvitedSetsActive() {
		Employee employee = invitedEmployee(1, "E001");
		when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));
		when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

		employeeManagementService.markInvitedEmployeeActive(1);

		ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
		verify(employeeRepository).save(captor.capture());
		assertEquals(EmployeeStatus.ACTIVE, captor.getValue().getStatus());
	}

	@Test
	void markInvitedEmployeeActive_rejectsActiveEmployee() {
		Employee employee = invitedEmployee(1, "E001");
		employee.setStatus(EmployeeStatus.ACTIVE);
		when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));

		assertThrows(
				InvalidRequestException.class,
				() -> employeeManagementService.markInvitedEmployeeActive(1));
		verify(employeeRepository, never()).save(any());
	}

	@Test
	void getManagerForEmployee_returnsManager() {
		Employee employee = invitedEmployee(2, "E002");
		employee.setManagerId(1);
		Employee manager = invitedEmployee(1, "E001");
		manager.setStatus(EmployeeStatus.ACTIVE);
		when(employeeRepository.findById(2)).thenReturn(Optional.of(employee));
		when(employeeRepository.findById(1)).thenReturn(Optional.of(manager));

		assertEquals(manager, employeeManagementService.getManagerForEmployee(2));
	}

	@Test
	void getEmployeesByManager_returnsDirectReports() {
		Employee manager = invitedEmployee(1, "E001");
		manager.setStatus(EmployeeStatus.ACTIVE);
		List<Employee> reports = List.of(invitedEmployee(2, "E002"), invitedEmployee(3, "E003"));
		when(employeeRepository.findById(1)).thenReturn(Optional.of(manager));
		when(employeeRepository.findByManagerIdOrderByEmployeeCodeAsc(1)).thenReturn(reports);

		assertEquals(reports, employeeManagementService.getEmployeesByManager(1));
	}

	private static Employee invitedEmployee(Integer id, String code) {
		Employee employee = new Employee(
				code,
				"First",
				"Last",
				"1111111111",
				EmploymentType.FULL_TIME,
				LocalDate.of(2026, 1, 1),
				EmployeeStatus.INVITED);
		employee.setEmployeeId(id);
		return employee;
	}
}
