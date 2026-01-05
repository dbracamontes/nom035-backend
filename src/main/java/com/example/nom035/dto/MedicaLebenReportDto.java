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
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDetail {
        private String name;
        private int score;
        private int maxPossible;
        private int minPossible;
        private int count;
        private double average;
        private String level;
    }
}