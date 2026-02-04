package com.example.nom035.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.nom035.dto.MedicaLebenReportDto;
import com.example.nom035.dto.MedicaLebenReportDto.CategoryDetail;
import com.example.nom035.entity.Response;
import com.example.nom035.entity.SurveyApplication;
import com.example.nom035.entity.User;
import com.example.nom035.repository.ResponseRepository;
import com.example.nom035.repository.SurveyApplicationRepository;
import com.example.nom035.repository.UserRepository;
import com.example.nom035.service.MedicaLebenScoringService;
import com.example.nom035.entity.MatrixOptionAnswer;
import com.example.nom035.repository.MatrixOptionAnswerRepository;

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

    @Autowired
    private MatrixOptionAnswerRepository matrixOptionAnswerRepository;

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
        
        report.setGlobalScore(result.globalScore);
        report.setGlobalMaxPossible(result.globalMaxPossible);
        report.setGlobalMinPossible(result.globalMinPossible);
        report.setTotalResponses(result.totalResponses);
        report.setTotalQuestions(result.totalQuestions);
        report.setGlobalAverage(result.globalAverage);
        report.setGlobalLevel(result.globalLevel);

        // Calcular factor de ajuste a partir de los niveles de categoría
        int countLow = 0;
        int countHigh = 0;
        for (String catName : result.categoryScores.keySet()) {
            String level = result.categoryLevels.get(catName);
            if ("Bajo".equalsIgnoreCase(level)) {
                countLow++;
            } else if ("Alto".equalsIgnoreCase(level)) {
                countHigh++;
            }
        }
        report.setCountLow(countLow);
        report.setCountHigh(countHigh);
        double adjustmentFactor = (((countLow * 3) + (countHigh * 5)) / 100.0) + 1.0;
        report.setAdjustmentFactor(adjustmentFactor);
        
        List<CategoryDetail> categories = new ArrayList<>();
        for (String catName : result.categoryScores.keySet()) {
            CategoryDetail detail = new CategoryDetail();
            detail.setName(catName);
            detail.setScore(result.categoryScores.get(catName));
            detail.setMaxPossible(result.categoryMaxPossible.get(catName));
            detail.setMinPossible(result.categoryMinPossible. get(catName));
            detail.setCount(result.categoryCounts.get(catName));
            detail.setTotalQuestionsInCategory(result.categoryTotalQuestions.getOrDefault(catName, 0));
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

        // Generar lista de notas a partir de respuestas de ciertas preguntas
        List<String> notas = generateNotasForApplication(sa.getId());
        report.setNotas(notas);

        // === NUEVO: exponer preguntas no contestadas para pruebas ===
        Object unansweredObj = result.insights.get("unansweredQuestions");
        if (unansweredObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> unanswered = (List<Map<String, Object>>) unansweredObj;
            // Reducimos a solo id y texto como pidió el usuario
            List<Map<String, Object>> unansweredSimple = unanswered.stream()
                .map(q -> {
                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("id", q.get("id"));
                    m.put("text", q.get("text"));
                    return m;
                })
                .collect(Collectors.toList());
            report.setUnansweredQuestions(unansweredSimple);
        }

        // === NUEVO: sección de debug agrupada por question.category ===
        // Estructura: Map<categoria, List<map-con-campos-debug>>
        Map<String, List<Map<String, Object>>> debugByCategory = sa.getResponses().stream()
            .collect(Collectors.groupingBy(
                r -> r.getQuestion() != null && r.getQuestion().getCategory() != null
                        ? r.getQuestion().getCategory()
                        : "(SIN_CATEGORIA)",
                Collectors.mapping(r -> {
                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                    Long questionId = r.getQuestion() != null ? r.getQuestion().getId() : null;
                    m.put("questionId", questionId);
                    m.put("responseId", r.getId());

                    // Default values (non-matrix)
                    Integer value = r.getValue();
                    Long optionId = r.getOptionAnswer() != null ? r.getOptionAnswer().getId() : null;
                    Integer optionValue = r.getOptionAnswer() != null ? r.getOptionAnswer().getValue() : null;

                    if ("MATRIX".equalsIgnoreCase(
                            r.getQuestion() != null ? r.getQuestion().getType() : null)) {
                        // Para MATRIX: usar freeText como fila (category) y optionAnswer.text como columna
                        String matrixCategory = r.getFreeText();
                        String matrixText = r.getOptionAnswer() != null ? r.getOptionAnswer().getText() : null;

                        if (questionId != null && matrixCategory != null && matrixText != null) {
                            matrixOptionAnswerRepository
                                .findByQuestionIdAndCategoryAndText(questionId, matrixCategory, matrixText)
                                .ifPresent(mo -> {
                                    // Sobrescribir con los datos reales de matrix_option_answer
                                    m.put("matrixOptionAnswerId", mo.getId());
                                    m.put("matrixCategory", mo.getCategory());
                                    m.put("matrixText", mo.getText());
                                    m.put("value", mo.getValue());
                                });
                        }

                        // Por claridad, también exponemos lo que llegó en Response
                        m.put("matrixFreeTextRaw", matrixCategory);
                        m.put("matrixColumnTextRaw", matrixText);
                    } else {
                        m.put("optionAnswerId", optionId);
                        m.put("optionAnswerValue", optionValue);
                    }

                    // Para no perder compatibilidad, si aún no se ha seteado value, usar el de Response
                    if (!m.containsKey("value")) {
                        m.put("value", value);
                    }

                    return m;
                }, Collectors.toList())
            ));

        report.setDebugByCategory(debugByCategory);

        // === NUEVO: debug de rangos teóricos por categoría y pregunta ===
        Object rangesObj = result.insights.get("debugRangesByCategory");
        if (rangesObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, List<Map<String, Object>>> ranges = (Map<String, List<Map<String, Object>>>) rangesObj;
            report.setDebugRangesByCategory(ranges);
        }

        return report;
    }

    @SuppressWarnings("unchecked")
    private List<String> generateRecommendations(MedicaLebenScoringService.Result result) {
        List<String> recommendations = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : result.categoryLevels.entrySet()) {
            String category = entry.getKey();
            String level = entry.getValue();
            double avg = result.categoryAverages.get(category);
            
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

    /**
     * Genera un texto de recomendación para una categoría específica
     * en función de su puntaje promedio.
     */
    private String generateRecommendationForCategory(String category, double average) {
        StringBuilder sb = new StringBuilder();
        sb.append("En la categoría \"").append(category).append("\" se observa un puntaje promedio de ")
          .append(String.format("%.2f", average)).append(". ");

        // Mensajes genéricos según el nivel de riesgo implícito por el promedio
        if (average >= 3.0) {
            sb.append("Se recomienda una valoración médica y psicológica más profunda, así como seguimiento cercano.");
        } else if (average >= 2.0) {
            sb.append("Se sugiere monitoreo periódico y medidas preventivas para reducir el riesgo.");
        } else {
            sb.append("Se recomienda mantener las condiciones actuales y continuar con las acciones preventivas.");
        }

        return sb.toString();
    }

    // Genera las notas de texto libre para preguntas específicas de Médica Leben
    private List<String> generateNotasForApplication(Long applicationId) {
        List<Response> responses = responseRepository.findBySurveyApplicationId(applicationId);
        List<String> notas = new ArrayList<>();

        for (Response r : responses) {
            if (r.getQuestion() == null || r.getQuestion().getId() == null) {
                continue;
            }
            Long qid = r.getQuestion().getId();
            String freeText = r.getFreeText();
            if (freeText != null) {
                freeText = freeText.trim();
            }

            if (qid == 159L) {
                if (freeText != null && !freeText.isEmpty()) {
                    notas.add("La cantidad de cigarros al día y los años que lleva fumando o que fumo?: " + freeText);
                }
            } else if (qid == 163L) {
                if (freeText != null && !freeText.isEmpty()) {
                    notas.add("El tipo de alcohol (cerveza, tequila, etc.), la cantidad de consumo por semana (latas, vasos, botella) y el número de años que ha consumido alcohol: " + freeText);
                }
            } else if (qid == 167L) {
                if (freeText != null && !freeText.isEmpty()) {
                    String noteText = freeText;
                    // Intentar parsear JSON de multi_select para presentar labels legibles en vez de raw JSON
                    try {
                        if (freeText.trim().startsWith("{")) {
                            ObjectMapper om = new ObjectMapper();
                            Map<String, Object> json = om.readValue(freeText, Map.class);
                            Object labelsObj = json.get("optionLabels");
                            if (labelsObj instanceof java.util.List) {
                                @SuppressWarnings("unchecked")
                                java.util.List<String> labels = (java.util.List<String>) labelsObj;
                                if (!labels.isEmpty()) {
                                    noteText = String.join(", ", labels);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        // si falla el parseo, dejar texto crudo
                    }
                    if (noteText != null && !noteText.isEmpty()) {
                        notas.add("¿Tiene usted alguno de los siguientes padecimientos heredofamiliares o crónicos degenerativos?: " + noteText);
                    }
                } else if (r.getOptionAnswer() != null && r.getOptionAnswer().getText() != null) {
                    notas.add("¿Tiene usted alguno de los siguientes padecimientos heredofamiliares o crónicos degenerativos?: " + r.getOptionAnswer().getText());
                }
            } else if (qid == 170L) {
                if (freeText != null && !freeText.isEmpty()) {
                    notas.add("Favor de especificar el nombre del medicamento(s) o suplementos que consume: " + freeText);
                }
            } else if (qid == 171L) {
                if (freeText != null && !freeText.isEmpty()) {
                    notas.add("Alergias (Si o No) y especificar a qué es alérgico: " + freeText);
                } else if (r.getOptionAnswer() != null && r.getOptionAnswer().getText() != null) {
                    notas.add("Alergias (Si o No) y especificar a qué es alérgico: " + r.getOptionAnswer().getText());
                }
            } else if (qid == 172L) {
                if (freeText != null && !freeText.isEmpty()) {
                    notas.add("¿Alguna vez ha sufrido un accidente de trabajo?: " + freeText);
                } else if (r.getOptionAnswer() != null && r.getOptionAnswer().getText() != null) {
                    notas.add("¿Alguna vez ha sufrido un accidente de trabajo?: " + r.getOptionAnswer().getText());
                }
            } else if (qid == 174L) {
                if (freeText != null && !freeText.isEmpty()) {
                    notas.add("En su historial de trabajo, ¿ha tenido alguna incapacidad?: " + freeText);
                } else if (r.getOptionAnswer() != null && r.getOptionAnswer().getText() != null) {
                    notas.add("En su historial de trabajo, ¿ha tenido alguna incapacidad?: " + r.getOptionAnswer().getText());
                }
            } else if (qid == 177L) {
                if (freeText != null && !freeText.isEmpty()) {
                    notas.add("Tipo de droga que consume o ha consumido: " + freeText);
                }
            }
        }

        return notas;
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
