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

    public SurveyApplicationController(SurveyApplicationService service) {
        this.service = service;
    }

    @GetMapping
    @Secured({"ROLE_EMPLOYEE", "ROLE_ADMIN"})
    public List<SurveyApplicationDto> list() {
        if (isAdmin()) {
            return service.getAll().stream().map(SurveyApplicationDto::fromEntity).collect(Collectors.toList());
        } else if (isCompany() || isEmployee()) {
            Long myCompanyId = getCompanyIdForCurrentUser();
            if (myCompanyId == null) return List.of();
            return service.getAll().stream()
                .filter(sa -> {
                    // Obtener companyId real desde companySurvey
                    if (sa.getCompanySurvey() != null && sa.getCompanySurvey().getCompany() != null) {
                        return sa.getCompanySurvey().getCompany().getId().equals(myCompanyId);
                    }
                    return false;
                })
                .map(SurveyApplicationDto::fromEntity)
                .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    @GetMapping("/{id}")
    @Secured({"ROLE_EMPLOYEE", "ROLE_ADMIN"})
    public ResponseEntity<SurveyApplicationDto> get(@PathVariable Long id) {
        SurveyApplication sa = service.getById(id);
        if (sa == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(SurveyApplicationDto.fromEntity(sa));
    }

    @GetMapping("/check")
    @Secured({"ROLE_EMPLOYEE", "ROLE_ADMIN"})
    public ResponseEntity<SurveyApplicationCheckDto> check(@RequestParam("employeeId") Long employeeId,
                                                           @RequestParam("surveyId") Long surveyId) {
        try {
            SurveyApplicationCheckDto dto = service.check(employeeId, surveyId);
            return ResponseEntity.ok(dto);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @Secured({"ROLE_EMPLOYEE", "ROLE_ADMIN"})
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
}