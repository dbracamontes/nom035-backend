package com.example.nom035.service;

import com.example.nom035.entity.Employee;
import com.example.nom035.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    private final UserCleanupService userCleanupService;

    public EmployeeService(EmployeeRepository employeeRepository, UserCleanupService userCleanupService) {
        this.employeeRepository = employeeRepository;
        this.userCleanupService = userCleanupService;
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    public List<Employee> getEmployeesByCompanyId(Long companyId) {
        return employeeRepository.findByCompanyId(companyId);
    }

    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        userCleanupService.deleteUsersForEmployees(List.of(id));
        employeeRepository.deleteById(id);
    }
}