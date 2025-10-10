package com.example.nom035.controller;

import com.example.nom035.entity.Employee;
import com.example.nom035.service.EmployeeService;
import com.example.nom035.dto.EmployeeDto;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<EmployeeDto> getAll() {
        return employeeService.getAllEmployees()
            .stream()
            .map(EmployeeDto::fromEntity)
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public EmployeeDto getById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id)
            .map(EmployeeDto::fromEntity)
            .orElse(null);
    }

    @GetMapping("/company/{companyId}")
    public List<EmployeeDto> getByCompany(@PathVariable Long companyId) {
        return employeeService.getEmployeesByCompanyId(companyId)
            .stream()
            .map(EmployeeDto::fromEntity)
            .collect(Collectors.toList());
    }

    @PostMapping
    public EmployeeDto create(@RequestBody Employee employee) {
        return EmployeeDto.fromEntity(employeeService.saveEmployee(employee));
    }

    @PutMapping("/{id}")
    public EmployeeDto update(@PathVariable Long id, @RequestBody Employee employee) {
        employee.setId(id);
        return EmployeeDto.fromEntity(employeeService.saveEmployee(employee));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
    }
}