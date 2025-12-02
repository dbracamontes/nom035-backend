package com.example.nom035.controller;

import com.example.nom035.dto.SurveyApplicationCreateDto;
import com.example.nom035.dto.SurveyApplicationDto;
import com.example.nom035.dto.SurveyApplicationCheckDto;
import com.example.nom035.entity.SurveyApplication;
import com.example.nom035.service.SurveyApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.annotation.Secured;

@RestController
@RequestMapping("/api/survey-applications")
@CrossOrigin(origins = "*")
public class SurveyApplicationController {
    @org.springframework.beans.factory.annotation.Autowired
    private com.example.nom035.repository.EmployeeRepository employeeRepository;
    @org.springframework.beans.factory.annotation.Autowired
    private com.example.nom035.repository.UserRepository userRepository;

    private com.example.nom035.entity.User getCurrentUser() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }
    private boolean isAdmin() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
    private boolean isCompany() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COMPANY"));
    }
    private boolean isEmployee() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
    }
    private Long getCompanyIdForCurrentUser() {
        com.example.nom035.entity.User user = getCurrentUser();
        if (user == null) return null;
        return user.getCompanyId();
    }

    private final SurveyApplicationService service;
    @org.springframework.beans.factory.annotation.Autowired
    private com.example.nom035.service.CompanySurveyService companySurveyService;

    public SurveyApplicationController(SurveyApplicationService service) {
        this.service = service;
    }

    @GetMapping
    @Secured({"ROLE_EMPLOYEE", "ROLE_ADMIN", "ROLE_COMPANY"})
    public List<SurveyApplicationDto> list() {
        if (isAdmin()) {
            return service.getAll().stream().map(SurveyApplicationDto::fromEntity).collect(Collectors.toList());
        } else if (isCompany()) {
            Long myCompanyId = getCompanyIdForCurrentUser();
            if (myCompanyId == null) return List.of();
            return service.getAll().stream()
                .filter(sa -> sa.getCompanySurvey() != null && sa.getCompanySurvey().getCompany() != null
                        && sa.getCompanySurvey().getCompany().getId().equals(myCompanyId))
                .map(SurveyApplicationDto::fromEntity)
                .collect(Collectors.toList());
        } else if (isEmployee()) {
            // Limit employee to only their own survey applications
            com.example.nom035.entity.User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getEmail() == null) return List.of();
            com.example.nom035.entity.Employee employee = employeeRepository.findByEmail(currentUser.getEmail()).orElse(null);
            if (employee == null) return List.of();
            Long myEmployeeId = employee.getId();
            return service.getAll().stream()
                .filter(sa -> sa.getEmployee() != null && sa.getEmployee().getId().equals(myEmployeeId))
                .map(SurveyApplicationDto::fromEntity)
                .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    @GetMapping("/{id}")
    @Secured({"ROLE_EMPLOYEE", "ROLE_ADMIN", "ROLE_COMPANY"})
    public ResponseEntity<SurveyApplicationDto> get(@PathVariable Long id) {
        SurveyApplication sa = service.getById(id);
        if (sa == null) return ResponseEntity.notFound().build();

        // Company scoping: only allow companies to access applications of their own company
        if (isCompany()) {
            Long myCompanyId = getCompanyIdForCurrentUser();
            boolean sameCompany = sa.getCompanySurvey() != null && sa.getCompanySurvey().getCompany() != null
                    && sa.getCompanySurvey().getCompany().getId() != null
                    && sa.getCompanySurvey().getCompany().getId().equals(myCompanyId);
            if (!sameCompany) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        // Employee scoping: only allow an employee to access their own application
        if (isEmployee()) {
            com.example.nom035.entity.User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getEmail() == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            com.example.nom035.entity.Employee employee = employeeRepository.findByEmail(currentUser.getEmail()).orElse(null);
            if (employee == null || sa.getEmployee() == null || !employee.getId().equals(sa.getEmployee().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.ok(SurveyApplicationDto.fromEntity(sa));
    }

    @GetMapping("/check")
    @Secured({"ROLE_EMPLOYEE", "ROLE_ADMIN", "ROLE_COMPANY"})
    public ResponseEntity<SurveyApplicationCheckDto> check(@RequestParam Long employeeId,
                                                           @RequestParam Long surveyId) {
        try {
            // Company scoping: verify the employee belongs to the same company
            if (isCompany()) {
                Long myCompanyId = getCompanyIdForCurrentUser();
                com.example.nom035.entity.Employee employee = employeeRepository.findById(employeeId).orElse(null);
                if (employee == null || employee.getCompany() == null || employee.getCompany().getId() == null || !employee.getCompany().getId().equals(myCompanyId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }

            // Employee scoping: ensure the requested check is for the current employee
            if (isEmployee()) {
                com.example.nom035.entity.User currentUser = getCurrentUser();
                if (currentUser == null || currentUser.getEmail() == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                com.example.nom035.entity.Employee employee = employeeRepository.findByEmail(currentUser.getEmail()).orElse(null);
                if (employee == null || employee.getId() == null || !employee.getId().equals(employeeId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }

            SurveyApplicationCheckDto dto = service.check(employeeId, surveyId);
            return ResponseEntity.ok(dto);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @Secured({"ROLE_EMPLOYEE", "ROLE_ADMIN", "ROLE_COMPANY"})
    public ResponseEntity<SurveyApplicationDto> create(@RequestBody SurveyApplicationCreateDto dto) {
        try {
            if (isAdmin()) {
                SurveyApplication created = service.create(dto);
                return ResponseEntity.status(HttpStatus.CREATED).body(SurveyApplicationDto.fromEntity(created));
            } else if (isCompany()) {
                Long myCompanyId = getCompanyIdForCurrentUser();
                if (myCompanyId == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                // Validar que el empleado pertenece a la empresa
                com.example.nom035.entity.Employee employee = employeeRepository.findById(dto.getEmployeeId()).orElse(null);
                if (employee == null || employee.getCompany() == null || !employee.getCompany().getId().equals(myCompanyId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                SurveyApplication created = service.create(dto);
                return ResponseEntity.status(HttpStatus.CREATED).body(SurveyApplicationDto.fromEntity(created));
            } else if (isEmployee()) {
                // Los empleados pueden crear aplicaciones de encuesta
                // Si no se proporciona employeeId, buscar por el email del usuario actual
                if (dto.getEmployeeId() == null) {
                    com.example.nom035.entity.User currentUser = getCurrentUser();
                    if (currentUser == null || currentUser.getEmail() == null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                    }
                    // Buscar empleado por email
                    com.example.nom035.entity.Employee employee = employeeRepository.findByEmail(currentUser.getEmail()).orElse(null);
                    if (employee == null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(null); // Could return error message: "No employee found with email: " + currentUser.getEmail()
                    }
                    dto.setEmployeeId(employee.getId());
                }
                SurveyApplication created = service.create(dto);
                return ResponseEntity.status(HttpStatus.CREATED).body(SurveyApplicationDto.fromEntity(created));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/calculate-risk")
    public ResponseEntity<SurveyApplicationDto> calculateRiskLevel(@PathVariable Long id) {
        try {
            SurveyApplication updated = service.recalculateRiskLevel(id);
            return ResponseEntity.ok(SurveyApplicationDto.fromEntity(updated));
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/complete")
    @Secured({"ROLE_EMPLOYEE", "ROLE_ADMIN", "ROLE_COMPANY"})
    public ResponseEntity<SurveyApplicationDto> completeApplication(@PathVariable Long id) {
        try {
            SurveyApplication sa = service.getById(id);
            if (sa == null) return ResponseEntity.notFound().build();
            
            // Actualizar el estado a completado
            sa.setStatusEnum(com.example.nom035.entity.ApplicationStatus.COMPLETADO);
            sa.setCompletedAt(java.time.LocalDateTime.now());
            
            // Calcular el nivel de riesgo
            service.calculateAndSetRiskLevel(sa);
            
            // Guardar los cambios
            SurveyApplication updated = service.updateApplication(sa);
            // Recalcular el completionRate de la empresa
            if (sa.getCompanySurvey() != null && sa.getCompanySurvey().getId() != null) {
                companySurveyService.recalculateCompletionRate(sa.getCompanySurvey().getId());
            }
            return ResponseEntity.ok(SurveyApplicationDto.fromEntity(updated));
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}