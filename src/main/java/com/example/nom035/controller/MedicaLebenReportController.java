package com.example.nom035.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream. Collectors;

import org.springframework. beans.factory.annotation.Autowired;
import org.springframework. http.HttpStatus;
import org.springframework. http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org. springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.nom035.dto.MedicaLebenReportDto;
import com.example. nom035.dto.MedicaLebenReportDto.CategoryDetail;
import com. example.nom035.entity.Response;
import com.example.nom035.entity.SurveyApplication;
import com.example.nom035.entity.User;
import com.example.nom035.repository.ResponseRepository;
import com.example. nom035.repository.SurveyApplicationRepository;
import com. example.nom035.repository.UserRepository;
import com.example. nom035.service.MedicaLebenScoringService;

@RestController
@RequestMapping("/api/reports/medica-leben")
public class MedicaLebenReportController {

    @Autowired
    private MedicaLebenScoringService scoringService;

    @Autowired
    private SurveyApplicationRepository surveyApplicationRepository;

    @Autowired
    private ResponseRepository responseRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * GET /api/reports/medica-leben/application/{applicationId}
     */
    @GetMapping("/application/{applicationId}")
    @Secured({"ROLE_ADMIN", "ROLE_COMPANY", "ROLE_EMPLOYEE"})
    public ResponseEntity<? > getApplicationReport(@PathVariable Long applicationId) {
        
        SurveyApplication sa = surveyApplicationRepository.findById(applicationId).orElse(null);
        if (sa == null) {
            return ResponseEntity.notFound().build();
        }

        // Validar que sea Médica Leben (survey_id = 2)
        if (sa.getCompanySurvey() == null || 
            sa.getCompanySurvey().getSurvey() == null || 
            sa.getCompanySurvey().getSurvey().getId() != 2) {
            return ResponseEntity.badRequest()
                .body("Esta aplicación no corresponde a la Encuesta Médica Leben");
        }

        if (! hasAccess(sa)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("No tienes permisos para ver este reporte");
        }

        List<Response> responses = responseRepository.findBySurveyApplicationId(applicationId);
        MedicaLebenScoringService.Result result = scoringService.score(responses);
        MedicaLebenReportDto report = buildReport(sa, result);
        
        return ResponseEntity.ok(report);
    }

    /**
     * GET /api/reports/medica-leben/company/{companyId}/summary
     */
    @GetMapping("/company/{companyId}/summary")
    @Secured({"ROLE_ADMIN", "ROLE_COMPANY"})
    public ResponseEntity<?> getCompanySummary(@PathVariable Long companyId) {
        
        if (!hasCompanyAccess(companyId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("No tienes permisos para ver reportes de esta compañía");
        }

        List<SurveyApplication> applications = surveyApplicationRepository. findAll().stream()
            .filter(sa -> sa.getCompanySurvey() != null && 
                         sa.getCompanySurvey().getCompany() != null &&
                         sa.getCompanySurvey().getCompany().getId().equals(companyId) &&
                         sa.getCompanySurvey().getSurvey() != null &&
                         sa.getCompanySurvey().getSurvey().getId() == 2)
            .collect(Collectors.toList());

        if (applications.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "message", "No hay aplicaciones de Médica Leben para esta compañía",
                "applicationsCount", 0
            ));
        }

        Map<String, Double> avgCategoryScores = new java.util.LinkedHashMap<>();
        Map<String, Integer> totalCategoryCounts = new java.util.LinkedHashMap<>();
        int totalCriticalEvents = 0;
        int totalApplications = 0;

        for (SurveyApplication sa : applications) {
            List<Response> responses = responseRepository. findBySurveyApplicationId(sa.getId());
            MedicaLebenScoringService.Result result = scoringService. score(responses);
            
            for (Map.Entry<String, Double> entry : result.categoryAverages.entrySet()) {
                String cat = entry.getKey();
                avgCategoryScores.put(cat, 
                    avgCategoryScores.getOrDefault(cat, 0.0) + entry.getValue());
                totalCategoryCounts.put(cat, 
                    totalCategoryCounts.getOrDefault(cat, 0) + 1);
            }
            
            totalCriticalEvents += (Integer) result.insights.getOrDefault("criticalEventsCount", 0);
            totalApplications++;
        }

        for (String cat : avgCategoryScores. keySet()) {
            int count = totalCategoryCounts. get(cat);
            if (count > 0) {
                avgCategoryScores.put(cat, avgCategoryScores.get(cat) / count);
            }
        }

        Map<String, Object> summary = new java.util.LinkedHashMap<>();
        summary.put("companyId", companyId);
        summary.put("applicationsCount", totalApplications);
        summary.put("categoryAverages", avgCategoryScores);
        summary.put("totalCriticalEvents", totalCriticalEvents);
        summary.put("avgCriticalEventsPerEmployee", 
            totalApplications > 0 ? (double) totalCriticalEvents / totalApplications : 0.0);

        return ResponseEntity.ok(summary);
    }

    private MedicaLebenReportDto buildReport(
        SurveyApplication sa, 
        MedicaLebenScoringService.Result result
    ) {
        MedicaLebenReportDto report = new MedicaLebenReportDto();
        
        report.setApplicationId(sa.getId());
        report.setCompanyName(sa.getCompanySurvey().getCompany().getName());
        report.setEmployeeName(sa.getEmployee().getName());
        
        report.setGlobalScore(result. globalScore);
        report.setGlobalMaxPossible(result.globalMaxPossible);
        report.setGlobalMinPossible(result.globalMinPossible);
        report.setTotalResponses(result.totalResponses);
        report.setGlobalAverage(result.globalAverage);
        report.setGlobalLevel(result.globalLevel);
        
        List<CategoryDetail> categories = new ArrayList<>();
        for (String catName : result.categoryScores.keySet()) {
            CategoryDetail detail = new CategoryDetail();
            detail.setName(catName);
            detail.setScore(result. categoryScores.get(catName));
            detail.setMaxPossible(result.categoryMaxPossible.get(catName));
            detail.setMinPossible(result.categoryMinPossible. get(catName));
            detail.setCount(result.categoryCounts.get(catName));
            detail.setAverage(result.categoryAverages.get(catName));
            detail. setLevel(result.categoryLevels.get(catName));
            categories.add(detail);
        }
        report.setCategories(categories);
        
        report.setCriticalEventsCount((Integer) result.insights.getOrDefault("criticalEventsCount", 0));
        report.setCriticalEvents((List<String>) result.insights.getOrDefault("criticalEvents", new ArrayList<>()));
        report.setSymptomCounts((Map<String, Integer>) result.insights.getOrDefault("symptomCounts", Map.of()));
        report.setHasHighRiskEvents((Boolean) result.insights.getOrDefault("hasHighRiskEvents", false));
        
        report.setRecommendations(generateRecommendations(result));
        
        return report;
    }

    private List<String> generateRecommendations(MedicaLebenScoringService. Result result) {
        List<String> recommendations = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : result.categoryLevels.entrySet()) {
            String category = entry.getKey();
            String level = entry.getValue();
            double avg = result.categoryAverages. get(category);
            
            if ("Crítico".equals(level) || "Riesgo alto".equals(level)) {
                recommendations.add(generateRecommendationForCategory(category, avg));
            }
        }
        
        int criticalCount = (Integer) result.insights.getOrDefault("criticalEventsCount", 0);
        if (criticalCount > 0) {
            recommendations.add("Se detectaron " + criticalCount + 
                " eventos críticos.  Se recomienda seguimiento psicológico inmediato.");
        }
        
        Map<String, Integer> symptoms = (Map<String, Integer>) result.insights.getOrDefault("symptomCounts", Map.of());
        if (!symptoms.isEmpty() && symptoms.values().stream().mapToInt(Integer::intValue).sum() > 5) {
            recommendations.add("Se detectan múltiples síntomas. Se recomienda evaluación médica.");
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("Los resultados están dentro de parámetros aceptables.  Continuar con monitoreo periódico.");
        }
        
        return recommendations;
    }

    private String generateRecommendationForCategory(String category, double avg) {
        switch (category) {
            case MedicaLebenScoringService.CAT_AMBIENTE_LABORAL:
                return "Ambiente laboral requiere mejoras:  revisar ventilación, iluminación, ruido y temperatura.";
            case MedicaLebenScoringService.CAT_CONDICIONES_LABORALES:
                return "Condiciones laborales críticas: evaluar carga de trabajo, EPP y capacitación en seguridad.";
            case MedicaLebenScoringService.CAT_SINTOMAS_DOLOR:
                return "Alto nivel de síntomas reportados: implementar pausas activas y evaluación ergonómica.";
            case MedicaLebenScoringService.CAT_EVENTOS_CRITICOS:
                return "Eventos críticos detectados:  activar protocolo de atención psicológica. ";
            case MedicaLebenScoringService.CAT_HABITOS_SALUD:
                return "Hábitos de salud requieren atención:  promover programas de bienestar y actividad física.";
            default:
                return "La categoría " + category + " requiere atención. ";
        }
    }

    private boolean hasAccess(SurveyApplication sa) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return false;

        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_COMPANY"))) {
            return user. getCompanyId() != null && 
                   user.getCompanyId().equals(sa.getCompanySurvey().getCompany().getId());
        }

        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"))) {
            return user.getEmployeeId() != null && 
                   user.getEmployeeId().equals(sa.getEmployee().getId());
        }

        return false;
    }

    private boolean hasCompanyAccess(Long companyId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return false;

        return user.getCompanyId() != null && user.getCompanyId().equals(companyId);
    }
}