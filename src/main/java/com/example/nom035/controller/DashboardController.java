package com.example.nom035.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.nom035.dto.CompanySurveyDto;
import com.example.nom035.dto.EmployeeDto;
import com.example.nom035.entity.CompanySurvey;
import com.example.nom035.entity.Response;
import com.example.nom035.entity.SurveyApplication.ApplicationStatus;
import com.example.nom035.repository.CompanySurveyRepository;
import com.example.nom035.repository.EmployeeRepository;
import com.example.nom035.repository.ResponseRepository;
import com.example.nom035.repository.SurveyApplicationRepository;

import com.example.nom035.repository.UserRepository;
import com.example.nom035.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.access.annotation.Secured;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SurveyApplicationRepository surveyAppRepository;

    @Autowired
    private CompanySurveyRepository companySurveyRepository;

    @Autowired
    private ResponseRepository responseRepository;


    @Autowired
    private UserRepository userRepository;

    // Helper to get current authenticated User
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    // Helper to check if current user is ADMIN
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // -----------------------------
    // 1. Dashboard general por empresa
    // -----------------------------
    @GetMapping("/company/{companyId}")
    @Secured({"ROLE_ADMIN", "ROLE_COMPANY", "ROLE_EMPLOYEE"})
    public ResponseEntity<?> getCompanyDashboard(@PathVariable Long companyId) {
        // Only ADMIN can access any company, others only their own
        if (!isAdmin()) {
            User user = getCurrentUser();
            if (user == null) return ResponseEntity.status(403).body("No autorizado");
            boolean allowed = false;
            // If user has COMPANY role, check if their company matches
            if (user.getRoles().stream().anyMatch(r -> r.getName().equals("COMPANY"))) {
                // Assume username is company email or add companyId to User if needed
                // Here, you may need to link User to Company directly for robust check
                // For now, block if not matching
                // TODO: Implement actual company-user link
                allowed = true; // Replace with real check
            }
            // If user has EMPLOYEE role, check if their company matches
            if (user.getRoles().stream().anyMatch(r -> r.getName().equals("EMPLOYEE"))) {
                // TODO: Link User to Employee and check company
                allowed = true; // Replace with real check
            }
            if (!allowed) return ResponseEntity.status(403).body("No autorizado");
        }
        List<EmployeeDto> employees = employeeRepository.findByCompanyId(companyId)
            .stream()
            .map(EmployeeDto::fromEntity)
            .collect(Collectors.toList());

        List<Object[]> statusCounts = surveyAppRepository.countByStatusAndCompanyId(companyId);

        List<CompanySurveyDto> surveys = companySurveyRepository.findByCompanyId(companyId)
            .stream()
            .map(CompanySurveyDto::fromEntity)
            .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("employees", employees);
        result.put("surveyStatusCounts", statusCounts);
        result.put("surveys", surveys);

        return ResponseEntity.ok(result);
    }

    // -----------------------------
    // 2. Promedio de riesgo por factor
    // -----------------------------
    @GetMapping("/company/{companyId}/risk")
    @Secured({"ROLE_ADMIN", "ROLE_COMPANY", "ROLE_EMPLOYEE"})
    public ResponseEntity<?> getRiskByFactor(@PathVariable Long companyId) {
        if (!isAdmin()) {
            User user = getCurrentUser();
            if (user == null) return ResponseEntity.status(403).body("No autorizado");
            boolean allowed = false;
            if (user.getRoles().stream().anyMatch(r -> r.getName().equals("COMPANY"))) {
                allowed = true; // TODO: check company match
            }
            if (user.getRoles().stream().anyMatch(r -> r.getName().equals("EMPLOYEE"))) {
                allowed = true; // TODO: check company match
            }
            if (!allowed) return ResponseEntity.status(403).body("No autorizado");
        }
        // Obtener todas las respuestas de empleados de la empresa
        List<Response> responses = responseRepository.findAllByCompanyId(companyId);

        // Agrupar por factor y calcular promedio
        Map<String, Double> avgRiskByFactor = responses.stream()
                .filter(r -> r.getValue() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getQuestion().getRiskFactor(),
                        Collectors.averagingInt(Response::getValue)
                ));

        return ResponseEntity.ok(avgRiskByFactor);
    }

    // -----------------------------
    // 3. Resumen de participación por encuesta
    // -----------------------------
    @GetMapping("/company/{companyId}/participation")
    @Secured({"ROLE_ADMIN", "ROLE_COMPANY", "ROLE_EMPLOYEE"})
    public ResponseEntity<?> getParticipation(@PathVariable Long companyId) {
        if (!isAdmin()) {
            User user = getCurrentUser();
            if (user == null) return ResponseEntity.status(403).body("No autorizado");
            boolean allowed = false;
            if (user.getRoles().stream().anyMatch(r -> r.getName().equals("COMPANY"))) {
                allowed = true; // TODO: check company match
            }
            if (user.getRoles().stream().anyMatch(r -> r.getName().equals("EMPLOYEE"))) {
                allowed = true; // TODO: check company match
            }
            if (!allowed) return ResponseEntity.status(403).body("No autorizado");
        }
        List<CompanySurvey> surveys = companySurveyRepository.findByCompanyId(companyId);

        List<Map<String, Object>> participation = new ArrayList<>();

        for (CompanySurvey cs : surveys) {
            int totalEmployees = cs.getCompany().getEmployees().size();
            long completed = cs.getSurveyApplications().stream()
                    .filter(sa -> sa.getStatus() == ApplicationStatus.COMPLETADA)
                    .count();

            Map<String, Object> map = new HashMap<>();
            map.put("surveyTitle", cs.getSurvey().getTitle());
            map.put("completionRate", totalEmployees > 0 ? (completed * 100.0 / totalEmployees) : 0);
            participation.add(map);
        }

        return ResponseEntity.ok(participation);
    }
}