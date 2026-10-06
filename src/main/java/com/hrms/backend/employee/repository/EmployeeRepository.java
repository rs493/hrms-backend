package com.hrms.backend.employee.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hrms.backend.employee.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

	Optional<Employee> findByEmployeeCode(String employeeCode);

	boolean existsByEmployeeCode(String employeeCode);

	boolean existsByEmployeeCodeAndEmployeeIdNot(String employeeCode, Integer employeeId);

	List<Employee> findAllByOrderByEmployeeCodeAsc();

	List<Employee> findByManagerIdOrderByEmployeeCodeAsc(Integer managerId);
}
