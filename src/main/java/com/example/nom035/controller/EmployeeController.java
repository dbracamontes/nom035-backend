package com.example.nom035.controller;

import com.example.nom035.entity.Employee;
import com.example.nom035.service.EmployeeService;
import com.example.nom035.dto.EmployeeDto;
import com.example.nom035.service.CompanyService;
import com.example.nom035.entity.Company;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.annotation.Secured;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;
    private final CompanyService companyService;

    public EmployeeController(EmployeeService employeeService, CompanyService companyService) {
        this.employeeService = employeeService;
        this.companyService = companyService;
    }

    @GetMapping
    @Secured("ROLE_EMPLOYEE")
    public List<EmployeeDto> getAll() {
        return employeeService.getAllEmployees()
            .stream()
            .map(EmployeeDto::fromEntity)
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Secured("ROLE_EMPLOYEE")
    public EmployeeDto getById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id)
            .map(EmployeeDto::fromEntity)
            .orElse(null);
    }

    @GetMapping("/company/{companyId}")
    @Secured("ROLE_EMPLOYEE")
    public List<EmployeeDto> getByCompany(@PathVariable Long companyId) {
        return employeeService.getEmployeesByCompanyId(companyId)
            .stream()
            .map(EmployeeDto::fromEntity)
            .collect(Collectors.toList());
    }

    @PostMapping
    @Secured("ROLE_EMPLOYEE")
    public EmployeeDto create(@RequestBody Employee employee) {
        // Resolve provided company id to a managed entity to avoid transient/nullable issues
        if (employee.getCompany() != null) {
            Long cid = employee.getCompany().getId();
            if (cid == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "company.id is required");
            }
            Company company = companyService.getCompanyById(cid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company not found"));
            employee.setCompany(company);
        }
        return EmployeeDto.fromEntity(employeeService.saveEmployee(employee));
    }

    @PutMapping("/{id}")
    public EmployeeDto update(@PathVariable Long id, @RequestBody Employee employee) {
        // Load existing employee to preserve fields that may not be provided in the request
        Employee existing = employeeService.getEmployeeById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        // Only overwrite fields when the request provides them; preserve company if not provided
        if (employee.getName() != null) existing.setName(employee.getName());
        if (employee.getEmail() != null) existing.setEmail(employee.getEmail());
        if (employee.getPosition() != null) existing.setPosition(employee.getPosition());
        if (employee.getDepartment() != null) existing.setDepartment(employee.getDepartment());
        if (employee.getSeniorityYears() != null) existing.setSeniorityYears(employee.getSeniorityYears());
        if (employee.getGender() != null) existing.setGender(employee.getGender());
        if (employee.getAge() != null) existing.setAge(employee.getAge());
        if (employee.getStatus() != null) existing.setStatus(employee.getStatus());

        // If company info provided, resolve to managed Company; if not provided, preserve existing
        if (employee.getCompany() != null) {
            Long cid = employee.getCompany().getId();
            if (cid == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "company.id is required");
            }
            Company company = companyService.getCompanyById(cid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company not found"));
            existing.setCompany(company);
        }

        return EmployeeDto.fromEntity(employeeService.saveEmployee(existing));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
    }
}