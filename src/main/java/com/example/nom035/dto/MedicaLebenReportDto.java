package com.example.nom035.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicaLebenReportDto {
    
    private Long applicationId;
    private String companyName;
    private String employeeName;
    
    // Scores globales
    private int globalScore;
    private int globalMaxPossible;
    private int globalMinPossible;
    private int totalResponses;
    private int totalQuestions;
    private double globalAverage;
    private String globalLevel;
    
    // Scores por categoría
    private List<CategoryDetail> categories;
    
    // Insights adicionales
    private int criticalEventsCount;
    private List<String> criticalEvents;
    private Map<String, Integer> symptomCounts;
    private boolean hasHighRiskEvents;
    
    // Recomendaciones
    private List<String> recommendations;

    // Notas generadas a partir de preguntas abiertas específicas (159, 163, 167, 170, 171, 172, 174, 177, etc.)
    private List<String> notas;
    
    // NUEVO: preguntas no contestadas (solo id y texto) para fines de testing
    private List<Map<String, Object>> unansweredQuestions;

    // NUEVO: sección de debug de respuestas, agrupada por categoría de pregunta
    // Estructura esperada:
    // {
    //   "categoria1": [
    //      {"questionId": 1, "responseId": 10, "value": 2, "optionAnswerId": 5, "optionAnswerValue": 2},
    //      ...
    //   ],
    //   "categoria2": [ ... ]
    // }
    private Map<String, List<Map<String, Object>>> debugByCategory;

    // NUEVO: sección de debug de rangos teóricos por categoría y pregunta
    // Estructura:
    // {
    //   "General": [ { questionId, questionText, minValue, maxValue }, ... ],
    //   "Ambiente laboral": [ ... ]
    // }
    private Map<String, List<Map<String, Object>>> debugRangesByCategory;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDetail {
        private String name;
        private int score;
        private int maxPossible;
        private int minPossible;
        private int count;
        private int totalQuestionsInCategory;
        private double average;
        private String level;
    }
}