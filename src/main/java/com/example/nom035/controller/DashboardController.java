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
import com.example.nom035.entity.ApplicationStatus;
import com.example.nom035.repository.CompanySurveyRepository;
import com.example.nom035.repository.EmployeeRepository;
import com.example.nom035.repository.ResponseRepository;
import com.example.nom035.repository.SurveyApplicationRepository;

import com.example.nom035.repository.UserRepository;
import com.example.nom035.entity.User;
import com.example.nom035.repository.CompanyRepository;
import com.example.nom035.entity.Company;
import com.example.nom035.dto.CompanyParticipationSummaryDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.access.annotation.Secured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

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

    @Autowired
    private CompanyRepository companyRepository;
    // -----------------------------
    // 4. Resumen global de participación por empresa
    // -----------------------------
    @GetMapping("/participation/summary")
    @Secured({"ROLE_ADMIN", "ROLE_COMPANY"})
    public ResponseEntity<?> getParticipationSummary() {
        logger.info("[getParticipationSummary] INICIO endpoint global de resumen por empresa");
        if (isAdmin()) {
            List<Company> companies = companyRepository.findAll();
            List<CompanyParticipationSummaryDto> summaryList = new ArrayList<>();
            for (Company company : companies) {
                Long companyId = company.getId();
                String companyName = company.getName();
                int totalEmployees = (int) employeeRepository.findByCompanyIdAndStatus(companyId, com.example.nom035.entity.Employee.EmployeeStatus.activo).size();
                // Encuestas completadas: contar aplicaciones de encuesta COMPLETADO para empleados activos
                int responded = 0;
                try {
                    responded = (int) surveyAppRepository.findByCompanySurvey_CompanyId(companyId).stream()
                        .filter(sa -> sa.getStatusEnum() == ApplicationStatus.COMPLETADO)
                        .count();
                } catch (Exception e) {
                    logger.warn("[getParticipationSummary] Error contando respondidos para empresa {}: {}", companyId, e.getMessage());
                }
                int pending = Math.max(totalEmployees - responded, 0);
                int percent = (totalEmployees > 0) ? (int) Math.round((responded * 100.0) / totalEmployees) : 0;
                String status;
                if (percent >= 85) {
                    status = "Cumple NOM-035";
                } else if (percent >= 50) {
                    status = "En Progreso";
                } else {
                    status = "Bajo";
                }
                summaryList.add(new CompanyParticipationSummaryDto(companyId, companyName, totalEmployees, responded, pending, percent, status));
            }
            return ResponseEntity.ok(summaryList);
        } else if (isCompany()) {
            Long myCompanyId = getCompanyIdForCurrentUser();
            if (myCompanyId == null) {
                logger.info("[getParticipationSummary] COMPANY user sin companyId, acceso denegado");
                return ResponseEntity.status(403).body("No autorizado");
            }
            Company company = companyRepository.findById(myCompanyId).orElse(null);
            if (company == null) {
                return ResponseEntity.ok(List.of());
            }
            int totalEmployees = (int) employeeRepository.findByCompanyIdAndStatus(myCompanyId, com.example.nom035.entity.Employee.EmployeeStatus.activo).size();
            int responded = 0;
            try {
                responded = (int) surveyAppRepository.findByCompanySurvey_CompanyId(myCompanyId).stream()
                    .filter(sa -> sa.getStatusEnum() == ApplicationStatus.COMPLETADO)
                    .count();
            } catch (Exception e) {
                logger.warn("[getParticipationSummary] Error contando respondidos para empresa {}: {}", myCompanyId, e.getMessage());
            }
            int pending = Math.max(totalEmployees - responded, 0);
            int percent = (totalEmployees > 0) ? (int) Math.round((responded * 100.0) / totalEmployees) : 0;
            String status;
            if (percent >= 85) {
                status = "Cumple NOM-035";
            } else if (percent >= 50) {
                status = "En Progreso";
            } else {
                status = "Bajo";
            }
            CompanyParticipationSummaryDto dto = new CompanyParticipationSummaryDto(myCompanyId, company.getName(), totalEmployees, responded, pending, percent, status);
            return ResponseEntity.ok(List.of(dto));
        } else {
            logger.info("[getParticipationSummary] Usuario sin rol adecuado, acceso denegado");
            return ResponseEntity.status(403).body("No autorizado");
        }
    }

    // Helper to get current authenticated User
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    // Helper to check if current user is ADMIN
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("[isAdmin] Usuario autenticado: {}", authentication.getName());
        logger.info("[isAdmin] Autoridades: {}", authentication.getAuthorities());
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        logger.info("[isAdmin] ¿Es ADMIN?: {}", isAdmin);
        return isAdmin;
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

    // -----------------------------
    // 1. Dashboard general por empresa
    // -----------------------------
    @GetMapping("/company/{companyId}")
    @Secured({"ROLE_ADMIN", "ROLE_COMPANY", "ROLE_EMPLOYEE"})
    public ResponseEntity<?> getCompanyDashboard(@PathVariable Long companyId) {
        logger.info("[getCompanyDashboard] INICIO endpoint para companyId={}", companyId);
        if (isAdmin()) {
            // acceso total
        } else if (isCompany() || isEmployee()) {
            Long myCompanyId = getCompanyIdForCurrentUser();
            if (myCompanyId == null || !myCompanyId.equals(companyId)) {
                logger.info("[getCompanyDashboard] Usuario no tiene permisos para la empresa solicitada, acceso denegado");
                return ResponseEntity.status(403).body("No autorizado");
            }
        } else {
            logger.info("[getCompanyDashboard] Usuario no tiene permisos para la empresa solicitada, acceso denegado");
            return ResponseEntity.status(403).body("No autorizado");
        }
        logger.info("[getCompanyDashboard] Consultando empleados activos de la empresa {}", companyId);
        List<EmployeeDto> employees = employeeRepository.findByCompanyIdAndStatus(companyId, com.example.nom035.entity.Employee.EmployeeStatus.activo)
            .stream()
            .map(EmployeeDto::fromEntity)
            .collect(Collectors.toList());
        logger.info("[getCompanyDashboard] Empleados activos encontrados: {}", employees.size());
        logger.info("[getCompanyDashboard] Empleados activos: {}", employees);

        logger.info("[getCompanyDashboard] Consultando status de aplicaciones de encuesta para la empresa {}", companyId);
        List<Object[]> statusCountsRaw = surveyAppRepository.countByStatusAndCompanyId(companyId);
        logger.info("[getCompanyDashboard] Status counts: {}", statusCountsRaw.size());
        List<Map<String, Object>> statusCounts = new ArrayList<>();
        for (Object[] row : statusCountsRaw) {
            String statusStr = (String) row[0];
            long count = (row[1] instanceof Long) ? (Long) row[1] : ((Number) row[1]).longValue();
            String statusLabel;
            try {
                statusLabel = com.example.nom035.entity.ApplicationStatus.from(statusStr).getValue();
            } catch (Exception e) {
                statusLabel = statusStr;
            }
            logger.info("[getCompanyDashboard] StatusCount row: status={}, count={}", statusLabel, count);
            Map<String, Object> map = new HashMap<>();
            map.put("status", statusLabel);
            map.put("count", count);
            statusCounts.add(map);
        }

        logger.info("[getCompanyDashboard] Consultando encuestas de la empresa {}", companyId);
        List<CompanySurveyDto> surveys = companySurveyRepository.findByCompanyId(companyId)
            .stream()
            .map(CompanySurveyDto::fromEntity)
            .collect(Collectors.toList());
    logger.info("[getCompanyDashboard] Encuestas encontradas: {}", surveys.size());
    logger.info("[getCompanyDashboard] Encuestas: {}", surveys);

        Map<String, Object> result = new HashMap<>();
        result.put("employees", employees);
    result.put("surveyStatusCounts", statusCounts);
        result.put("surveys", surveys);

        logger.info("[getCompanyDashboard] Respuesta construida correctamente");
        return ResponseEntity.ok(result);
    }

    // -----------------------------
    // 2. Promedio de riesgo por factor
    // -----------------------------
    @GetMapping("/company/{companyId}/risk")
    @Secured({"ROLE_ADMIN", "ROLE_COMPANY", "ROLE_EMPLOYEE"})
    public ResponseEntity<?> getRiskByFactor(@PathVariable Long companyId) {
        if (isAdmin()) {
            // acceso total
        } else if (isCompany() || isEmployee()) {
            Long myCompanyId = getCompanyIdForCurrentUser();
            if (myCompanyId == null || !myCompanyId.equals(companyId)) {
                return ResponseEntity.status(403).body("No autorizado");
            }
        } else {
            return ResponseEntity.status(403).body("No autorizado");
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
        if (isAdmin()) {
            // acceso total
        } else if (isCompany() || isEmployee()) {
            Long myCompanyId = getCompanyIdForCurrentUser();
            if (myCompanyId == null || !myCompanyId.equals(companyId)) {
                return ResponseEntity.status(403).body("No autorizado");
            }
        } else {
            return ResponseEntity.status(403).body("No autorizado");
        }
        List<CompanySurvey> surveys = companySurveyRepository.findByCompanyId(companyId);

        List<Map<String, Object>> participation = new ArrayList<>();

        for (CompanySurvey cs : surveys) {
            // Total empleados activos
            int totalEmployees = employeeRepository
                .findByCompanyIdAndStatus(companyId, com.example.nom035.entity.Employee.EmployeeStatus.activo)
                .size();
            long completed = surveyAppRepository.findByCompanySurveyId(cs.getId()).stream()
                .filter(sa -> sa.getStatusEnum() == ApplicationStatus.COMPLETADO)
                .count();

            Map<String, Object> map = new HashMap<>();
            map.put("surveyTitle", cs.getSurvey().getTitle());
            map.put("completionRate", totalEmployees > 0 ? (completed * 100.0 / totalEmployees) : 0);
            map.put("responded", completed);
            map.put("pending", Math.max(totalEmployees - (int) completed, 0));
            participation.add(map);
        }

        return ResponseEntity.ok(participation);
    }

    // -----------------------------
    // 3.1 Resumen canónico de participación por empresa (1 empresa)
    // -----------------------------
    @GetMapping("/company/{companyId}/participation/summary")
    @Secured({"ROLE_ADMIN", "ROLE_COMPANY", "ROLE_EMPLOYEE"})
    public ResponseEntity<?> getCompanyParticipationSummary(@PathVariable Long companyId) {
        if (isAdmin()) {
            // acceso total
        } else if (isCompany() || isEmployee()) {
            Long myCompanyId = getCompanyIdForCurrentUser();
            if (myCompanyId == null || !myCompanyId.equals(companyId)) {
                return ResponseEntity.status(403).body("No autorizado");
            }
        } else {
            return ResponseEntity.status(403).body("No autorizado");
        }
        Company company = companyRepository.findById(companyId).orElse(null);
        if (company == null) return ResponseEntity.notFound().build();

        int totalEmployees = employeeRepository
            .findByCompanyIdAndStatus(companyId, com.example.nom035.entity.Employee.EmployeeStatus.activo)
            .size();
        int responded = 0;
        try {
            responded = (int) surveyAppRepository.findByCompanySurvey_CompanyId(companyId).stream()
                .filter(sa -> sa.getStatusEnum() == ApplicationStatus.COMPLETADO)
                .count();
        } catch (Exception e) {
            logger.warn("[getCompanyParticipationSummary] Error contando respondidos para empresa {}: {}", companyId, e.getMessage());
        }
        int pending = Math.max(totalEmployees - responded, 0);
        int percent = (totalEmployees > 0) ? (int) Math.round((responded * 100.0) / totalEmployees) : 0;
        Map<String, Object> dto = new HashMap<>();
        dto.put("companyId", companyId);
        dto.put("companyName", company.getName());
        dto.put("totalEmployees", totalEmployees);
        dto.put("responded", responded);
        dto.put("pending", pending);
        dto.put("percent", percent);
        return ResponseEntity.ok(dto);
    }
}