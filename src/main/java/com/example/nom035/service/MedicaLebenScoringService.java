package com.example.nom035.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.nom035.entity.Question;
import com.example.nom035.entity.Response;
import com.example.nom035.repository. OptionAnswerRepository;
import com.example.nom035.repository. QuestionRepository;

/**
 * Servicio de scoring específico para la Encuesta Médica Leben (Survey ID = 2)
 * Calcula puntajes totales y por categoría basándose en response. value
 */
@Service
public class MedicaLebenScoringService {

    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private OptionAnswerRepository optionAnswerRepository;

    // Categorías de la Encuesta Médica Leben
    public static final String CAT_DATOS_GENERALES = "Datos generales";
    public static final String CAT_AMBIENTE_LABORAL = "Ambiente laboral";
    public static final String CAT_CONDICIONES_LABORALES = "Condiciones laborales";
    public static final String CAT_HABITOS_SALUD = "Hábitos y salud";
    public static final String CAT_SINTOMAS_DOLOR = "Síntomas y dolor";
    public static final String CAT_EVENTOS_CRITICOS = "Eventos críticos";

    /**
     * Resultado del scoring de Médica Leben
     */
    public static class Result {
        public final Map<String, Integer> categoryScores;
        public final Map<String, Integer> categoryCounts;
        public final Map<String, Double> categoryAverages;
        public final Map<String, Integer> categoryMaxPossible;
        public final Map<String, Integer> categoryMinPossible;
        public final Map<String, String> categoryLevels;
        public final Map<String, Integer> categoryTotalQuestions; // Total de preguntas por categoría
        public final int globalScore;
        public final int totalResponses;
        public final int totalQuestions;
        public final double globalAverage;
        public final int globalMaxPossible;
        public final int globalMinPossible;
        public final String globalLevel;
        public final Map<String, Object> insights;

        public Result(
            Map<String, Integer> categoryScores,
            Map<String, Integer> categoryCounts,
            Map<String, Double> categoryAverages,
            Map<String, Integer> categoryMaxPossible,
            Map<String, Integer> categoryMinPossible,
            Map<String, String> categoryLevels,
            Map<String, Integer> categoryTotalQuestions,
            int globalScore,
            int totalResponses,
            int totalQuestions,
            double globalAverage,
            int globalMaxPossible,
            int globalMinPossible,
            String globalLevel,
            Map<String, Object> insights
        ) {
            this.categoryScores = categoryScores;
            this.categoryCounts = categoryCounts;
            this.categoryAverages = categoryAverages;
            this.categoryMaxPossible = categoryMaxPossible;
            this.categoryMinPossible = categoryMinPossible;
            this.categoryLevels = categoryLevels;
            this.categoryTotalQuestions = categoryTotalQuestions;
            this.globalScore = globalScore;
            this.totalResponses = totalResponses;
            this.totalQuestions = totalQuestions;
            this.globalAverage = globalAverage;
            this.globalMaxPossible = globalMaxPossible;
            this.globalMinPossible = globalMinPossible;
            this.globalLevel = globalLevel;
            this.insights = insights;
        }
    }

    /**
     * Calcula el scoring completo para una lista de respuestas de Médica Leben
     */
    public Result score(List<Response> responses) {
        List<Response> safeResponses = responses != null ? responses : java.util.Collections.emptyList();

        // Obtener todas las preguntas de Médica Leben (survey_id = 2)
        List<Question> allQuestions = questionRepository.findBySurveyId(2L);
        
        // Calcular rangos teóricos por categoría
        Map<String, Integer> categoryMaxPossible = new LinkedHashMap<>();
        Map<String, Integer> categoryMinPossible = new LinkedHashMap<>();
        calculateTheoreticalRanges(allQuestions, categoryMaxPossible, categoryMinPossible);

        // Contar total de preguntas por categoría
        Map<String, Integer> categoryTotalQuestions = new LinkedHashMap<>();
        for (String cat : getAllCategories()) { categoryTotalQuestions.put(cat, 0); }
        for (Question q : allQuestions) {
            String cat = normalizeCategory(q.getCategory());
            if (cat != null) {
                categoryTotalQuestions.put(cat, categoryTotalQuestions.getOrDefault(cat, 0) + 1);
            }
        }

        // Inicializar contadores para valores actuales
        Map<String, Integer> categoryScores = new LinkedHashMap<>();
        Map<String, Integer> categoryCounts = new LinkedHashMap<>();
        initializeCategories(categoryScores, categoryCounts);

        int totalResponses = 0;
        int globalScore = 0;

        // Map para insights adicionales
        Map<String, Object> insights = new HashMap<>();
        List<String> criticalEvents = new ArrayList<>();
        Map<String, Integer> symptomCounts = new HashMap<>();

        // Procesar cada respuesta
        for (Response r : safeResponses) {
            if (r == null || r.getQuestion() == null || r.getValue() == null) {
                continue;
            }

            Integer value = r.getValue();
            Question q = r.getQuestion();
            String category = normalizeCategory(q.getCategory());

            if (category == null) {
                continue;
            }

            // Acumular por categoría
            categoryScores.put(category, categoryScores.getOrDefault(category, 0) + value);
            categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);

            globalScore += value;
            totalResponses++;

            // Detectar eventos críticos
            if (CAT_EVENTOS_CRITICOS.equals(category) && isHighRiskValue(value, q)) {
                criticalEvents.add(q.getText());
            }

            // Contar síntomas
            if (CAT_SINTOMAS_DOLOR.equals(category) && isPositiveSymptom(value, q)) {
                String symptomType = extractSymptomType(q.getText());
                symptomCounts.put(symptomType, symptomCounts.getOrDefault(symptomType, 0) + 1);
            }
        }

        // Calcular promedios
        Map<String, Double> categoryAverages = calculateAverages(categoryScores, categoryCounts);
        double globalAverage = totalResponses > 0 ? (double) globalScore / totalResponses :  0.0;

        // Calcular totales teóricos globales
        int globalMaxPossible = categoryMaxPossible.values().stream().mapToInt(Integer::intValue).sum();
        int globalMinPossible = categoryMinPossible.values().stream().mapToInt(Integer::intValue).sum();

        // Interpretar niveles de riesgo
        Map<String, String> categoryLevels = interpretCategoryLevels(categoryAverages);
        String globalLevel = interpretGlobalLevel(globalAverage, categoryAverages);

        // Agregar insights
        insights.put("criticalEventsCount", criticalEvents.size());
        insights.put("criticalEvents", criticalEvents);
        insights.put("symptomCounts", symptomCounts);
        insights.put("hasHighRiskEvents", criticalEvents.size() > 0);

        return new Result(
            categoryScores,
            categoryCounts,
            categoryAverages,
            categoryMaxPossible,
            categoryMinPossible,
            categoryLevels,
            categoryTotalQuestions,
            globalScore,
            totalResponses,
            allQuestions != null ? allQuestions.size() : 0,
            globalAverage,
            globalMaxPossible,
            globalMinPossible,
            globalLevel,
            insights
        );
    }

    /**
     * ⭐ Calcula los rangos teóricos (min/max) por categoría
     * basándose en los valores de option_answer
     */
    private void calculateTheoreticalRanges(
        List<Question> questions,
        Map<String, Integer> categoryMaxPossible,
        Map<String, Integer> categoryMinPossible
    ) {
        // Inicializar categorías
        for (String cat : getAllCategories()) {
            categoryMaxPossible.put(cat, 0);
            categoryMinPossible.put(cat, 0);
        }

        // Para cada pregunta, obtener min/max de sus opciones
        for (Question q : questions) {
            String category = normalizeCategory(q.getCategory());
            if (category == null) {
                continue;
            }

            // Obtener min y max para esta pregunta
            Integer minValue = optionAnswerRepository.findMinValueByQuestionId(q.getId());
            Integer maxValue = optionAnswerRepository.findMaxValueByQuestionId(q.getId());

            if (minValue != null && maxValue != null) {
                // Acumular en la categoría correspondiente
                categoryMinPossible.put(category, 
                    categoryMinPossible.getOrDefault(category, 0) + minValue);
                categoryMaxPossible.put(category, 
                    categoryMaxPossible.getOrDefault(category, 0) + maxValue);
            }
        }
    }

    /**
     * Retorna la lista de todas las categorías
     */
    private List<String> getAllCategories() {
        return Arrays.asList(
            CAT_DATOS_GENERALES,
            CAT_AMBIENTE_LABORAL,
            CAT_CONDICIONES_LABORALES,
            CAT_HABITOS_SALUD,
            CAT_SINTOMAS_DOLOR,
            CAT_EVENTOS_CRITICOS
        );
    }

    /**
     * Inicializa las categorías con valores en 0
     */
    private void initializeCategories(Map<String, Integer> scores, Map<String, Integer> counts) {
        for (String cat : getAllCategories()) {
            scores.put(cat, 0);
            counts.put(cat, 0);
        }
    }

    /**
     * Normaliza el nombre de la categoría
     */
    private String normalizeCategory(String category) {
        if (category == null) return null;
        
        String normalized = category.trim();
        
        // Mapear a categorías estándar
        switch (normalized) {
            case "Datos generales":
                // Mostrar como "General" en reportes de Médica Leben
                return CAT_DATOS_GENERALES;
            case "Ambiente laboral":
                return CAT_AMBIENTE_LABORAL;
            case "Condiciones laborales":
                return CAT_CONDICIONES_LABORALES;
            case "Hábitos y salud":
                return CAT_HABITOS_SALUD;
            case "Síntomas y dolor":
                return CAT_SINTOMAS_DOLOR;
            case "Eventos críticos":
                return CAT_EVENTOS_CRITICOS;
            default:
                return null;
        }
    }

    /**
     * Calcula promedios por categoría
     */
    private Map<String, Double> calculateAverages(
        Map<String, Integer> scores, 
        Map<String, Integer> counts
    ) {
        Map<String, Double> averages = new LinkedHashMap<>();
        
        for (String category : scores.keySet()) {
            int score = scores.get(category);
            int count = counts.get(category);
            double avg = count > 0 ? (double) score / count : 0.0;
            averages.put(category, avg);
        }
        
        return averages;
    }

    /**
     * Interpreta niveles de riesgo por categoría
     */
    private Map<String, String> interpretCategoryLevels(Map<String, Double> averages) {
        Map<String, String> levels = new LinkedHashMap<>();
        
        for (Map.Entry<String, Double> entry : averages.entrySet()) {
            String category = entry.getKey();
            double avg = entry.getValue();
            levels.put(category, interpretCategoryLevel(category, avg));
        }
        
        return levels;
    }

    /**
     * Interpreta el nivel de riesgo para una categoría específica
     */
    private String interpretCategoryLevel(String category, double average) {
        // Rangos personalizados por categoría
        switch (category) {
            case CAT_AMBIENTE_LABORAL:
            case CAT_CONDICIONES_LABORALES:
                // Escala 0-5:  condiciones físicas y laborales
                if (average <= 1.5) return "Óptimo";
                if (average <= 2.5) return "Aceptable";
                if (average <= 3.5) return "Requiere atención";
                return "Crítico";

            case CAT_SINTOMAS_DOLOR:
            case CAT_EVENTOS_CRITICOS:
                // Mayor sensibilidad para síntomas y eventos
                if (average <= 1.0) return "Sin riesgo";
                if (average <= 2.0) return "Riesgo bajo";
                if (average <= 3.0) return "Riesgo medio";
                return "Riesgo alto";

            case CAT_HABITOS_SALUD:
                // Hábitos saludables
                if (average <= 2.0) return "Buenos hábitos";
                if (average <= 3.0) return "Hábitos regulares";
                if (average <= 4.0) return "Requiere mejora";
                return "Requiere intervención";

            case CAT_DATOS_GENERALES:
            default:
                return "Informativo";
        }
    }

    /**
     * Interpreta el nivel de riesgo global
     */
    private String interpretGlobalLevel(double globalAvg, Map<String, Double> categoryAverages) {
        long highRiskCategories = categoryAverages.values().stream()
            .filter(avg -> avg > 3.0)
            .count();

        if (globalAvg <= 2.0) {
            return "Bajo riesgo";
        } else if (globalAvg <= 3.0) {
            return highRiskCategories >= 2 ? "Riesgo medio-alto" : "Riesgo medio";
        } else if (globalAvg <= 4.0) {
            return "Riesgo alto";
        } else {
            return "Riesgo muy alto";
        }
    }

    private boolean isHighRiskValue(Integer value, Question q) {
        if (value == null) return false;
        
        String text = q.getText() != null ? q.getText().toLowerCase() : "";
        
        if (text.contains("accidente") || text.contains("violento") || 
            text.contains("asalto") || text.contains("secuestro")) {
            return value >= 1;
        }
        
        return value >= 3;
    }

    private boolean isPositiveSymptom(Integer value, Question q) {
        return value != null && value >= 2;
    }

    private String extractSymptomType(String text) {
        if (text == null) return "Otro";
        
        String lower = text.toLowerCase();
        if (lower.contains("garganta")) return "Síntomas de garganta";
        if (lower.contains("bucal")) return "Síntomas bucales";
        if (lower. contains("cutáneo") || lower.contains("piel")) return "Síntomas cutáneos";
        if (lower. contains("digestivo")) return "Síntomas digestivos";
        if (lower.contains("gripe")) return "Síntomas tipo gripe";
        if (lower.contains("dolor")) return "Síntomas dolorosos";
        
        return "Otros síntomas";
    }

    private Result emptyResult() {
        Map<String, Integer> emptyScores = new LinkedHashMap<>();
        Map<String, Integer> emptyCounts = new LinkedHashMap<>();
        Map<String, Double> emptyAverages = new LinkedHashMap<>();
        Map<String, Integer> emptyMax = new LinkedHashMap<>();
        Map<String, Integer> emptyMin = new LinkedHashMap<>();
        Map<String, String> emptyLevels = new LinkedHashMap<>();
        Map<String, Integer> emptyTotals = new LinkedHashMap<>();
         
        initializeCategories(emptyScores, emptyCounts);
        
        for (String cat : emptyScores.keySet()) {
            emptyAverages.put(cat, 0.0);
            emptyMax.put(cat, 0);
            emptyMin.put(cat, 0);
            emptyLevels.put(cat, "Sin datos");
            emptyTotals.put(cat, 0);
        }

        return new Result(
            emptyScores,
            emptyCounts,
            emptyAverages,
            emptyMax,
            emptyMin,
            emptyLevels,
            emptyTotals,
            0,
            0,
            0,
            0.0,
            0,
            0,
            "Sin datos",
            new HashMap<>()
        );
    }
}