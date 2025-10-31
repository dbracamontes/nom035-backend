package com.example.nom035.controller;

import com.example.nom035.entity.Company;
import com.example.nom035.entity.Employee;
import com.example.nom035.service.CompanyService;
import com.example.nom035.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para operaciones de companies.
 *
 * IMPORTANTE:
 * - Ajusta los imports y nombres de servicio (CompanyService, EmployeeService)
 *   si en tu proyecto tienen otro paquete / nombre.
 */
@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final Logger log = LoggerFactory.getLogger(CompanyController.class);

    private final CompanyService companyService;
    private final EmployeeService employeeService;

    @Autowired
    public CompanyController(CompanyService companyService, EmployeeService employeeService) {
        this.companyService = companyService;
        this.employeeService = employeeService;
    }

    /**
     * Obtener todas las companies.
     * Permitido a ROLE_COMPANY y ROLE_ADMIN.
     */
    @GetMapping
    @Secured({"ROLE_COMPANY", "ROLE_ADMIN"})
    public ResponseEntity<List<Company>> getAllCompanies() {
        log.debug("Request to get all companies");
        List<Company> list = companyService.getAllCompanies();
        return ResponseEntity.ok(list);
    }

    /**
     * Obtener empleados de una company por id.
     * Permitido a ROLE_COMPANY y ROLE_ADMIN.
     */
    @GetMapping("/{companyId}/employees")
    @Secured({"ROLE_COMPANY", "ROLE_ADMIN"})
    public ResponseEntity<List<Employee>> getCompanyEmployees(@PathVariable Long companyId) {
        log.debug("Request to get employees for companyId={}", companyId);
        List<Employee> employees = employeeService.getEmployeesByCompanyId(companyId);
        return ResponseEntity.ok(employees);
    }

    // Puedes añadir otros endpoints aquí y protegerlos de la misma forma.
}