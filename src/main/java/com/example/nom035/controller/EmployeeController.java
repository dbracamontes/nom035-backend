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
import org.springframework.beans.factory.annotation.Autowired;
import com.example.nom035.repository.UserRepository;
import com.example.nom035.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;
    private final CompanyService companyService;

    @Autowired
    private UserRepository userRepository;

    public EmployeeController(EmployeeService employeeService, CompanyService companyService) {
        this.employeeService = employeeService;
        this.companyService = companyService;
    }

    // Helper para obtener el usuario autenticado
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    // Helper para saber si es ADMIN
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // Helper para obtener el id de la empresa asociada al usuario
    private Long getCompanyIdForCurrentUser() {
        User user = getCurrentUser();
        if (user == null) return null;
        // TODO: Relacionar User con Company directamente para robustez
        // Por ahora, se asume que el username es el email de la empresa y hay coincidencia 1:1
        // Si tienes un campo companyId en User, úsalo aquí
        return null; // Implementar lógica real según tu modelo
    }

    @GetMapping
    @Secured("ROLE_EMPLOYEE")
    public List<EmployeeDto> getAll() {
        if (isAdmin()) {
            return employeeService.getAllEmployees()
                .stream()
                .map(EmployeeDto::fromEntity)
                .collect(Collectors.toList());
        } else {
            Long companyId = getCompanyIdForCurrentUser();
            if (companyId == null) return List.of();
            return employeeService.getAllEmployees().stream()
                .filter(e -> e.getCompany() != null && e.getCompany().getId().equals(companyId))
                .map(EmployeeDto::fromEntity)
                .collect(Collectors.toList());
        }
    }

    @GetMapping("/{id}")
    @Secured("ROLE_EMPLOYEE")
    public EmployeeDto getById(@PathVariable Long id) {
        Optional<Employee> empOpt = employeeService.getEmployeeById(id);
        if (empOpt.isEmpty()) return null;
        Employee emp = empOpt.get();
        if (!isAdmin()) {
            Long companyId = getCompanyIdForCurrentUser();
            if (companyId == null || emp.getCompany() == null || !emp.getCompany().getId().equals(companyId)) {
                return null;
            }
        }
        return EmployeeDto.fromEntity(emp);
    }

    @GetMapping("/company/{companyId}")
    @Secured("ROLE_EMPLOYEE")
    public List<EmployeeDto> getByCompany(@PathVariable Long companyId) {
        if (!isAdmin()) {
            Long myCompanyId = getCompanyIdForCurrentUser();
            if (myCompanyId == null || !myCompanyId.equals(companyId)) {
                return List.of();
            }
        }
        return employeeService.getEmployeesByCompanyId(companyId)
            .stream()
            .map(EmployeeDto::fromEntity)
            .collect(Collectors.toList());
    }

    @PostMapping
    @Secured("ROLE_EMPLOYEE")
    public EmployeeDto create(@RequestBody Employee employee) {
        if (!isAdmin()) {
            Long companyId = getCompanyIdForCurrentUser();
            if (companyId == null || employee.getCompany() == null || !employee.getCompany().getId().equals(companyId)) {
                return null;
            }
        }
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
        Employee existing = employeeService.getEmployeeById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        if (!isAdmin()) {
            Long companyId = getCompanyIdForCurrentUser();
            if (companyId == null || existing.getCompany() == null || !existing.getCompany().getId().equals(companyId)) {
                return null;
            }
        }
        // Only overwrite fields when the request provides them; preserve company if not provided
        if (employee.getName() != null) existing.setName(employee.getName());
        if (employee.getEmail() != null) existing.setEmail(employee.getEmail());
        if (employee.getPosition() != null) existing.setPosition(employee.getPosition());
        if (employee.getDepartment() != null) existing.setDepartment(employee.getDepartment());
        if (employee.getSeniorityYears() != null) existing.setSeniorityYears(employee.getSeniorityYears());
        if (employee.getGender() != null) existing.setGender(employee.getGender());
        if (employee.getAge() != null) existing.setAge(employee.getAge());
        if (employee.getStatus() != null) existing.setStatus(employee.getStatus());
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
        Optional<Employee> empOpt = employeeService.getEmployeeById(id);
        if (empOpt.isEmpty()) return;
        Employee emp = empOpt.get();
        if (!isAdmin()) {
            Long companyId = getCompanyIdForCurrentUser();
            if (companyId == null || emp.getCompany() == null || !emp.getCompany().getId().equals(companyId)) {
                return;
            }
        }
        employeeService.deleteEmployee(id);
    }
}