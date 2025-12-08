package com.example.nom035.controller;

import com.example.nom035.entity.Employee;
import com.example.nom035.service.EmployeeService;
import com.example.nom035.dto.EmployeeDto;
import com.example.nom035.service.CompanyService;
import com.example.nom035.entity.Company;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
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
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    private final EmployeeService employeeService;
    private final CompanyService companyService;

    @Autowired
    private UserRepository userRepository;

    // Helper para obtener el usuario autenticado
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    // Helper para saber si es ADMIN
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        logger.info("[isAdmin] Usuario autenticado: {}", username);
        logger.info("[isAdmin] Authorities: {}", authentication.getAuthorities());
        boolean result = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        logger.info("[isAdmin] ¿Es admin?: {}", result);
        return result;
    }

    // Helper para saber si es COMPANY
    private boolean isCompany() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COMPANY"));
    }

    // Helper para saber si es EMPLOYEE
    private boolean isEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
    }

    // Helper para obtener el companyId del usuario autenticado (si aplica)
    private Long getCompanyIdForCurrentUser() {
        User user = getCurrentUser();
        if (user == null) return null;
        return user.getCompanyId();
    }

    public EmployeeController(EmployeeService employeeService, CompanyService companyService) {
        this.employeeService = employeeService;
        this.companyService = companyService;
    }


    @GetMapping
    @Secured({"ROLE_ADMIN", "ROLE_COMPANY", "ROLE_EMPLOYEE"})
    public List<EmployeeDto> getAll() {
        if (isAdmin()) {
            return employeeService.getAllEmployees()
                .stream()
                .map(EmployeeDto::fromEntity)
                .collect(Collectors.toList());
        } else if (isCompany() || isEmployee()) {
            Long companyId = getCompanyIdForCurrentUser();
            if (companyId == null) return List.of();
            return employeeService.getAllEmployees().stream()
                .filter(e -> e.getCompany() != null && e.getCompany().getId().equals(companyId))
                .map(EmployeeDto::fromEntity)
                .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    @GetMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_COMPANY", "ROLE_EMPLOYEE"})
    public EmployeeDto getById(@PathVariable Long id) {
        Optional<Employee> empOpt = employeeService.getEmployeeById(id);
        if (empOpt.isEmpty()) return null;
        Employee emp = empOpt.get();
        if (isAdmin()) {
            return EmployeeDto.fromEntity(emp);
        } else if (isCompany() || isEmployee()) {
            Long companyId = getCompanyIdForCurrentUser();
            if (companyId == null || emp.getCompany() == null || !emp.getCompany().getId().equals(companyId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver este empleado");
            }
            return EmployeeDto.fromEntity(emp);
        } else {
            return null;
        }
    }

    @GetMapping("/company/{companyId}")
    @Secured({"ROLE_ADMIN", "ROLE_COMPANY", "ROLE_EMPLOYEE"})
    public List<EmployeeDto> getByCompany(@PathVariable Long companyId) {
        if (isAdmin()) {
            return employeeService.getEmployeesByCompanyId(companyId)
                .stream()
                .map(EmployeeDto::fromEntity)
                .collect(Collectors.toList());
        } else if (isCompany() || isEmployee()) {
            Long myCompanyId = getCompanyIdForCurrentUser();
            if (myCompanyId == null || !myCompanyId.equals(companyId)) {
                return List.of();
            }
            return employeeService.getEmployeesByCompanyId(companyId)
                .stream()
                .map(EmployeeDto::fromEntity)
                .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    @PostMapping
    @Secured({"ROLE_ADMIN", "ROLE_COMPANY"})
    public EmployeeDto create(@RequestBody Employee employee) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        logger.info("[create] Intentando crear empleado. Usuario autenticado: {}", username);
        logger.info("[create] Authorities: {}", authentication.getAuthorities());

        // If caller is COMPANY role, force company to be the caller's company
        if (!isAdmin()) {
            Long myCompanyId = getCompanyIdForCurrentUser();
            if (myCompanyId == null) {
                logger.warn("[create] Usuario de empresa sin companyId: {}", username);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para crear empleados");
            }
            // If request includes a company, ensure it matches the user's company
            if (employee.getCompany() != null) {
                Long cid = employee.getCompany().getId();
                if (cid == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "company.id is required");
                }
                if (!cid.equals(myCompanyId)) {
                    logger.warn("[create] Usuario intenta crear empleado para otra empresa: {} -> {}", username, cid);
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes crear empleados para otra empresa");
                }
                Company company = companyService.getCompanyById(cid)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company not found"));
                employee.setCompany(company);
            } else {
                // Set the employee's company to the user's company when not provided
                Company company = companyService.getCompanyById(myCompanyId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company not found"));
                employee.setCompany(company);
            }
        } else {
            // Admin: resolve provided company id to a managed entity (if present)
            if (employee.getCompany() != null) {
                Long cid = employee.getCompany().getId();
                if (cid == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "company.id is required");
                }
                Company company = companyService.getCompanyById(cid)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company not found"));
                employee.setCompany(company);
            }
        }

        return EmployeeDto.fromEntity(employeeService.saveEmployee(employee));
    }

    @PutMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_COMPANY"})
    public EmployeeDto update(@PathVariable Long id, @RequestBody Employee employee) {
        Employee existing = employeeService.getEmployeeById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        if (!isAdmin()) {
            Long companyId = getCompanyIdForCurrentUser();
            if (companyId == null || existing.getCompany() == null || !existing.getCompany().getId().equals(companyId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para modificar este empleado");
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
        if (employee.getCurp() != null) existing.setCurp(employee.getCurp());
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
    @Secured({"ROLE_ADMIN", "ROLE_COMPANY"})
    public void delete(@PathVariable Long id) {
        Optional<Employee> empOpt = employeeService.getEmployeeById(id);
        if (empOpt.isEmpty()) return;
        Employee emp = empOpt.get();
        if (!isAdmin()) {
            Long companyId = getCompanyIdForCurrentUser();
            if (companyId == null || emp.getCompany() == null || !emp.getCompany().getId().equals(companyId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para eliminar este empleado");
            }
        }
        employeeService.deleteEmployee(id);
    }
}