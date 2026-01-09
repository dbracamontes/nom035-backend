package com.example.nom035.controller;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.nom035.dto.CategoryScoreDto;
import com.example.nom035.dto.DictamenDto;
import com.example.nom035.entity.SurveyApplication;
import com.example.nom035.entity.Response;
import com.example.nom035.entity.User;
import com.example.nom035.repository.ResponseRepository;
import com.example.nom035.repository.SurveyApplicationRepository;
import com.example.nom035.repository.UserRepository;
import com.example.nom035.service.Nom035ScoringService;
import com.example.nom035.service.PdfReportService;
import com.example.nom035.service.PdfBrandingConfig;
import com.example.nom035.service.JasperPonderacionReportService;
import com.example.nom035.service.MedicaLebenScoringService;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    @Autowired
    private SurveyApplicationRepository surveyApplicationRepository;

    @Autowired
    private ResponseRepository responseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Nom035ScoringService scoringService;

    @Autowired
    private PdfReportService pdfReportService;

    @Autowired
    private JasperPonderacionReportService jasperPonderacionReportService;

    @Autowired
    private MedicaLebenScoringService medicaLebenScoringService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return null;
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
    private boolean isCompany() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COMPANY"));
    }

    @GetMapping("/application/{applicationId}/dictamen")
    @Secured({"ROLE_ADMIN", "ROLE_COMPANY", "ROLE_EMPLOYEE"})
    public ResponseEntity<?> getApplicationDictamen(@PathVariable Long applicationId) {
        SurveyApplication sa = surveyApplicationRepository.findById(applicationId).orElse(null);
        if (sa == null) return ResponseEntity.notFound().build();

        // Access control: company users can only see their company
        if (!isAdmin() && isCompany()) {
            User u = getCurrentUser();
            if (u == null || u.getCompanyId() == null || !u.getCompanyId().equals(sa.getCompanySurvey().getCompany().getId())) {
                return ResponseEntity.status(403).body("No autorizado");
            }
        }

        List<Response> responses = responseRepository.findBySurveyApplicationId(applicationId);
        Long surveyId = sa.getCompanySurvey() != null && sa.getCompanySurvey().getSurvey() != null
            ? sa.getCompanySurvey().getSurvey().getId()
            : null;

        DictamenDto dto = new DictamenDto();
        dto.setApplicationId(applicationId);
        dto.setEmployeeId(sa.getEmployee() != null ? sa.getEmployee().getId() : null);
        dto.setEmployeeName(sa.getEmployee() != null ? sa.getEmployee().getName() : null);
        dto.setEmployeeEmail(sa.getEmployee() != null ? sa.getEmployee().getEmail() : null);
        dto.setPosition(sa.getEmployee() != null ? sa.getEmployee().getPosition() : null);
        dto.setDepartment(sa.getEmployee() != null ? sa.getEmployee().getDepartment() : null);
        dto.setCompanyId(sa.getCompanySurvey() != null && sa.getCompanySurvey().getCompany()!=null ? sa.getCompanySurvey().getCompany().getId() : null);
        dto.setCompanyName(sa.getCompanySurvey() != null && sa.getCompanySurvey().getCompany()!=null ? sa.getCompanySurvey().getCompany().getName() : null);
        dto.setSurveyId(surveyId);
        dto.setApplicationDate(sa.getStartedAt() != null ? sa.getStartedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : null);
        dto.setCompletedDate(sa.getCompletedAt() != null ? sa.getCompletedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : null);

        // Extraer datos demográficos de las respuestas de la encuesta
        // Mapeo de question IDs a campos del reporte (Encuesta Médica Leben)
        Map<Long, String> responseMap = new HashMap<>();
        for (Response r : responses) {
            if (r.getQuestion() != null && r.getQuestion().getId() != null) {
                Long qId = r.getQuestion().getId();
                String value = r.getFreeText();
                if (value == null || value.isBlank()) {
                    if (r.getOptionAnswer() != null && r.getOptionAnswer().getText() != null) {
                        value = r.getOptionAnswer().getText();
                    }
                }
                if (value != null && !value.isBlank()) {
                    responseMap.put(qId, value);
                }
            }
        }

        // IDs de preguntas demográficas de la encuesta Médica Leben:
        // 75: Fecha de nacimiento (calcular edad)
        // 76: Estado civil
        // 77: Género
        // 78: Nivel de estudios
        // 79: Antigüedad en el puesto actual
        // 80: ¿Ha desempeñado la misma actividad en otros lugares de trabajo?
        // 81: ¿Cuántos días a la semana trabaja?
        // 82: ¿Cuántas horas al día trabaja?
        // 83: Tipo de transporte
        // 84: Tiempo de traslado (ida y vuelta)
        // 85: Tipo de vivienda
        
        // Edad (de fecha de nacimiento)
        String birthDate = responseMap.get(75L);
        if (birthDate != null && !birthDate.isBlank()) {
            try {
                java.time.LocalDate birth = java.time.LocalDate.parse(birthDate);
                int age = java.time.Period.between(birth, java.time.LocalDate.now()).getYears();
                dto.setAge(String.valueOf(age));
            } catch (Exception e) {
                dto.setAge("No disponible");
            }
        } else {
            dto.setAge("No disponible");
        }
        
        dto.setMaritalStatus(responseMap.getOrDefault(76L, "No disponible"));
        dto.setGender(responseMap.getOrDefault(77L, "No disponible"));
        dto.setStudies(responseMap.getOrDefault(78L, "No disponible"));
        dto.setSeniority(responseMap.getOrDefault(79L, "No disponible"));
        dto.setSameActivity(responseMap.getOrDefault(80L, "No disponible"));
        dto.setWorkingDays(responseMap.getOrDefault(81L, "No disponible"));
        dto.setHoursPerDay(responseMap.getOrDefault(82L, "No disponible"));
        dto.setTransportType(responseMap.getOrDefault(83L, "No disponible"));
        dto.setCommuteTime(responseMap.getOrDefault(84L, "No disponible"));
        dto.setHousing(responseMap.getOrDefault(85L, "No disponible"));
        dto.setWeeklyGasoline("No disponible"); // No hay pregunta específica para esto
        dto.setTransportCost("No disponible"); // No hay pregunta específica para esto

        List<CategoryScoreDto> cats = new ArrayList<>();

        if (surveyId != null && surveyId == 2L) {
            // Médica Leben scoring
            MedicaLebenScoringService.Result res = medicaLebenScoringService.score(responses);
            dto.setGlobalScore(res.globalScore);
            dto.setGlobalLevel(res.globalLevel);
            dto.setGlobalMin(res.globalMinPossible);
            dto.setGlobalMax(res.globalMaxPossible);
            dto.setTotalResponses(res.totalResponses);
            dto.setTraumaticEventsCount((Integer) res.insights.getOrDefault("criticalEventsCount", 0));
            dto.setTraumaticAlert(res.insights.getOrDefault("hasHighRiskEvents", false) == Boolean.TRUE);

            for (Map.Entry<String, Integer> e : res.categoryScores.entrySet()) {
                String key = e.getKey();
                String level = res.categoryLevels.getOrDefault(key, "N/A");
                cats.add(new CategoryScoreDto(
                    key,
                    e.getValue(),
                    level,
                    res.categoryMinPossible.getOrDefault(key, 0),
                    res.categoryMaxPossible.getOrDefault(key, 0),
                    res.categoryCounts.getOrDefault(key, 0),
                    res.categoryCounts.getOrDefault(key, 0)
                ));
            }
            dto.setCategories(cats);

            StringBuilder sb = new StringBuilder();
            sb.append("Nivel global ").append(dto.getGlobalLevel()).append(".");
            if (dto.isTraumaticAlert()) {
                sb.append(" Se detectaron eventos críticos: ")
                  .append(dto.getTraumaticEventsCount())
                  .append(". Requiere seguimiento clínico.");
            }
            dto.setConclusion(sb.toString());

        } else {
            // Flujo original NOM-035
            Nom035ScoringService.Result res = scoringService.score(responses);
            dto.setGlobalScore(res.globalScore);
            dto.setGlobalLevel(res.globalLevel);
            dto.setTraumaticEventsCount(res.traumaticEventsCount);
            dto.setTraumaticAlert(res.traumaticAlert);

            for (Map.Entry<String, Integer> e : res.categoryScores.entrySet()) {
                String level = res.categoryLevels.getOrDefault(e.getKey(), "N/A");
                cats.add(new CategoryScoreDto(e.getKey(), e.getValue(), level));
            }
            dto.setCategories(cats);

            String risky = cats.stream()
                .filter(c -> {
                    String lv = c.getLevel();
                    return "Medio".equalsIgnoreCase(lv) || "Alto".equalsIgnoreCase(lv) || "Muy alto".equalsIgnoreCase(lv);
                })
                .map(CategoryScoreDto::getCategory)
                .collect(Collectors.joining(", "));
            StringBuilder sb = new StringBuilder();
            sb.append("Nivel global ").append(dto.getGlobalLevel()).append(".");
            if (!risky.isBlank()) {
                sb.append(" Riesgo en: ").append(risky).append(".");
            }
            if (dto.isTraumaticAlert()) {
                sb.append(" Alerta: ").append(dto.getTraumaticEventsCount()).append(" respuestas afirmativas en Acontecimientos Traumáticos Severos. Requiere canalización clínica.");
            }
            dto.setConclusion(sb.toString());
        }

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/company/{companyId}/dictamen-summary")
    @Secured({"ROLE_ADMIN", "ROLE_COMPANY"})
    public ResponseEntity<?> getCompanyDictamenSummary(@PathVariable Long companyId) {
        if (!isAdmin()) {
            // Company role restriction
            if (isCompany()) {
                User u = getCurrentUser();
                if (u == null || u.getCompanyId() == null || !u.getCompanyId().equals(companyId)) {
                    return ResponseEntity.status(403).body("No autorizado");
                }
            } else {
                return ResponseEntity.status(403).body("No autorizado");
            }
        }

        List<SurveyApplication> apps = surveyApplicationRepository.findByCompanySurvey_CompanyId(companyId);
        int appsCount = apps.size();

        Map<String, Integer> levelDist = new LinkedHashMap<>();
        levelDist.put("Nulo", 0);
        levelDist.put("Bajo", 0);
        levelDist.put("Medio", 0);
        levelDist.put("Alto", 0);
        levelDist.put("Muy alto", 0);

        Map<String, Integer> sumCategoryScores = new LinkedHashMap<>();
        int traumaticTotal = 0;

        for (SurveyApplication sa : apps) {
            List<Response> responses = responseRepository.findBySurveyApplicationId(sa.getId());
            Nom035ScoringService.Result res = scoringService.score(responses);
            levelDist.computeIfPresent(res.globalLevel, (k, v) -> v + 1);
            traumaticTotal += res.traumaticEventsCount;
            // accumulate category scores
            for (Map.Entry<String, Integer> e : res.categoryScores.entrySet()) {
                sumCategoryScores.put(e.getKey(), sumCategoryScores.getOrDefault(e.getKey(), 0) + e.getValue());
            }
        }

        Map<String, Double> avgCategoryScores = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : sumCategoryScores.entrySet()) {
            avgCategoryScores.put(e.getKey(), appsCount > 0 ? e.getValue() / (double) appsCount : 0.0);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("applicationsCount", appsCount);
        out.put("globalLevelDistribution", levelDist);
        out.put("avgCategoryScores", avgCategoryScores);
        out.put("traumaticYesCount", traumaticTotal);
        return ResponseEntity.ok(out);
    }

    // --- New PDF endpoints ---
    @GetMapping(value = "/application/{applicationId}/dictamen.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Secured({"ROLE_ADMIN", "ROLE_COMPANY", "ROLE_EMPLOYEE"})
    public ResponseEntity<byte[]> getApplicationDictamenPdf(
            @PathVariable Long applicationId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String subtitle,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String footerText,
            @RequestParam(required = false) String primaryHex,
            @RequestParam(required = false) String secondaryHex,
            @RequestParam(required = false) String logoClasspath
    ) {
        ResponseEntity<?> jsonResp = getApplicationDictamen(applicationId);
        if (!jsonResp.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(jsonResp.getStatusCode()).build();
        }
        DictamenDto dto = (DictamenDto) jsonResp.getBody();

        // Do NOT expose internal numeric IDs in the generated PDF payload/file name
        // Clear identifiers from the DTO before passing to the PDF builder
        dto.setEmployeeId(null);
        dto.setCompanyId(null);
        // Also clear application and survey identifiers to avoid any id leakage in PDF content
        dto.setApplicationId(null);
        dto.setSurveyId(null);

        boolean hasBrand = title != null || subtitle != null || companyName != null || footerText != null || primaryHex != null || secondaryHex != null || logoClasspath != null;
        byte[] pdf;
        if (hasBrand) {
            PdfBrandingConfig brand = new PdfBrandingConfig()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setCompanyName(companyName)
                .setFooterText(footerText)
                .setPrimaryHex(primaryHex)
                .setSecondaryHex(secondaryHex)
                .setLogoClasspath(logoClasspath);
            pdf = pdfReportService.buildApplicationDictamenPdf(dto, brand);
        } else {
            pdf = pdfReportService.buildApplicationDictamenPdf(dto);
        }

        // Build a safe filename that does not include numeric IDs. Prefer the employee name if available.
        String rawName = dto.getEmployeeName() != null ? dto.getEmployeeName() : "application";
        String safeName = rawName.replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9_.-]", "");
        String filename = "dictamen-" + safeName + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(value = "/company/{companyId}/dictamen-summary.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Secured({"ROLE_ADMIN", "ROLE_COMPANY"})
    public ResponseEntity<byte[]> getCompanyDictamenSummaryPdf(
            @PathVariable Long companyId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String subtitle,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String footerText,
            @RequestParam(required = false) String primaryHex,
            @RequestParam(required = false) String secondaryHex,
            @RequestParam(required = false) String logoClasspath
    ) {
        ResponseEntity<?> jsonResp = getCompanyDictamenSummary(companyId);
        if (!jsonResp.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(jsonResp.getStatusCode()).build();
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> summary = (Map<String, Object>) jsonResp.getBody();

        // Remove/clear any numeric company identifiers from the PDF payload so they are not exposed
        summary.remove("companyId");

        boolean hasBrand = title != null || subtitle != null || companyName != null || footerText != null || primaryHex != null || secondaryHex != null || logoClasspath != null;
        byte[] pdf;
        if (hasBrand) {
            PdfBrandingConfig brand = new PdfBrandingConfig()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setCompanyName(companyName)
                .setFooterText(footerText)
                .setPrimaryHex(primaryHex)
                .setSecondaryHex(secondaryHex)
                .setLogoClasspath(logoClasspath);
            pdf = pdfReportService.buildCompanySummaryPdf(summary, brand);
        } else {
            pdf = pdfReportService.buildCompanySummaryPdf(summary);
        }

        // Build a safe filename that does not include the numeric companyId. Prefer provided companyName or the one in the summary.
        String rawCompany = companyName != null ? companyName : (summary.get("companyName") instanceof String ? (String) summary.get("companyName") : "company");
        String safeCompany = rawCompany.replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9_.-]", "");
        String filename = "dictamen-summary-company-" + safeCompany + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(value = "/application/{applicationId}/ponderaciones.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Secured({"ROLE_ADMIN", "ROLE_COMPANY", "ROLE_EMPLOYEE"})
    public ResponseEntity<byte[]> getApplicationPonderacionesPdf(
            @PathVariable Long applicationId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String subtitle,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String footerText,
            @RequestParam(required = false) String primaryHex,
            @RequestParam(required = false) String secondaryHex,
            @RequestParam(required = false) String logoClasspath
    ) {
        ResponseEntity<?> jsonResp = getApplicationDictamen(applicationId);
        if (!jsonResp.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(jsonResp.getStatusCode()).build();
        }
        DictamenDto dto = (DictamenDto) jsonResp.getBody();
        if (dto == null) {
            return ResponseEntity.internalServerError().build();
        }

        // Evitar exponer IDs internos en el PDF
        dto.setEmployeeId(null);
        dto.setCompanyId(null);
        dto.setApplicationId(null);
        dto.setSurveyId(null);

        boolean hasBrand = title != null || subtitle != null || companyName != null || footerText != null || primaryHex != null || secondaryHex != null || logoClasspath != null;
        PdfBrandingConfig brand = hasBrand ? new PdfBrandingConfig()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setCompanyName(companyName)
            .setFooterText(footerText)
            .setPrimaryHex(primaryHex)
            .setSecondaryHex(secondaryHex)
            .setLogoClasspath(logoClasspath)
            : new PdfBrandingConfig()
                .setTitle("Ponderación Medica Leben")
                .setSubtitle(null)
                .setCompanyName(dto.getCompanyName())
                .setFooterText("Confidencial")
                .setPrimaryHex("#2196F3")
                .setSecondaryHex("#9C27B0")
                .setLogoClasspath("/branding/logo_medica_leben_png.png");

        byte[] pdf = jasperPonderacionReportService.buildPonderaciones(dto, brand);

        String rawName = dto.getEmployeeName() != null ? dto.getEmployeeName() : "application";
        String safeName = rawName.replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9_.-]", "");
        String filename = "ponderaciones-" + safeName + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}