package com.example.nom035.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.nom035.entity.Question;
import com.example.nom035.entity.Response;

@Service
public class Nom035ScoringService {

    // Standard NOM-035 categories
    public static final String CAT_AMBIENTE = "Condiciones del ambiente de trabajo";
    public static final String CAT_CARGA = "Carga de trabajo";
    public static final String CAT_CONTROL = "Falta de control sobre el trabajo";
    public static final String CAT_JORNADA = "Jornada de trabajo";
    public static final String CAT_INTERFERENCIA = "Interferencia en la relación trabajo-familia";
    public static final String CAT_LIDERAZGO = "Liderazgo y relaciones en el trabajo";
    public static final String CAT_ENTORNO = "Entorno organizacional";

    private static final Set<String> POSITIVE_CATEGORY_TOKENS = Set.of(
        // Categories typically phrased positively, inverse-scored
        "Liderazgo", "Relaciones", "Desempeño", "Reconocimiento", "Pertenencia", "Capacitación", "Funciones", "Desarrollo"
    );

    // Map dataset-specific categories to NOM-035 standard buckets
    private static final Map<String, String> CATEGORY_MAP;
    static {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("Ambiente", CAT_AMBIENTE);
        m.put("Carga", CAT_CARGA);
        m.put("Actividad", CAT_CARGA); // demandas emocionales/actividad -> carga
        m.put("Control", CAT_CONTROL);
        m.put("Autonomía", CAT_CONTROL);
        m.put("Participación", CAT_CONTROL);
        m.put("Cambio", CAT_CONTROL);
        m.put("Jornada", CAT_JORNADA);
        m.put("Relaciones", CAT_LIDERAZGO);
        m.put("Liderazgo", CAT_LIDERAZGO);
        m.put("Liderazgo trabajadores", CAT_LIDERAZGO);
        m.put("Violencia", CAT_LIDERAZGO);
        m.put("Reconocimiento", CAT_ENTORNO);
        m.put("Pertenencia", CAT_ENTORNO);
        m.put("Desempeño", CAT_ENTORNO);
        m.put("Capacitación", CAT_ENTORNO);
        m.put("Funciones", CAT_ENTORNO);
        m.put("Desarrollo", CAT_ENTORNO);
        CATEGORY_MAP = Collections.unmodifiableMap(m);
    }

    public static class Result {
        public final Map<String, Integer> categoryScores;
        public final Map<String, String> categoryLevels;
        public final int globalScore;
        public final String globalLevel;
        public final int traumaticEventsCount;
        public final boolean traumaticAlert;

        public Result(Map<String, Integer> categoryScores, Map<String, String> categoryLevels, int globalScore, String globalLevel, int traumaticEventsCount) {
            this.categoryScores = categoryScores;
            this.categoryLevels = categoryLevels;
            this.globalScore = globalScore;
            this.globalLevel = globalLevel;
            this.traumaticEventsCount = traumaticEventsCount;
            this.traumaticAlert = traumaticEventsCount > 0;
        }
    }

    public Result score(List<Response> responses) {
        if (responses == null) responses = List.of();

        // Aggregate normalized scores per standard category
        Map<String, Integer> scores = new LinkedHashMap<>();
        initCategory(scores);

        int traumaticYes = 0;

        for (Response r : responses) {
            if (r == null || r.getQuestion() == null) continue;
            Integer raw = r.getValue();
            if (raw == null) continue;

            Question q = r.getQuestion();
            String stdCat = mapToStandardCategory(q.getCategory());
            if (stdCat == null) continue;

            int norm = normalizeTo0to4(raw);
            // Inverse for positive-phrased categories
            if (shouldInvert(q)) {
                norm = 4 - norm;
            }

            scores.put(stdCat, scores.getOrDefault(stdCat, 0) + norm);

            if (isTraumaticEventQuestion(q) && isAffirmative(r)) {
                traumaticYes++;
            }
        }

        int global = scores.values().stream().mapToInt(Integer::intValue).sum();
        Map<String, String> levels = scores.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> interpretCategoryLevel(e.getKey(), e.getValue()),
                (a, b) -> a,
                LinkedHashMap::new
            ));

        String globalLevel = interpretGlobalLevel(global);
        return new Result(scores, levels, global, globalLevel, traumaticYes);
    }

    private void initCategory(Map<String, Integer> scores) {
        // Initialize to ensure deterministic keys order
        scores.put(CAT_AMBIENTE, 0);
        scores.put(CAT_CARGA, 0);
        scores.put(CAT_CONTROL, 0);
        scores.put(CAT_JORNADA, 0);
        scores.put(CAT_INTERFERENCIA, 0); // May remain 0 if no mapped items
        scores.put(CAT_LIDERAZGO, 0);
        scores.put(CAT_ENTORNO, 0);
    }

    private String mapToStandardCategory(String datasetCategory) {
        if (datasetCategory == null) return null;
        String key = datasetCategory.trim();
        String mapped = CATEGORY_MAP.get(key);
        if (mapped != null) return mapped;

        // Heuristics for interference: try to detect by key terms
        String kLower = key.toLowerCase(Locale.ROOT);
        if (kLower.contains("famil") || kLower.contains("vida-trabajo")) {
            return CAT_INTERFERENCIA;
        }
        // Fall back to ENTORNO if unknown
        return CAT_ENTORNO;
    }

    private boolean shouldInvert(Question q) {
        // Invert if the dataset category is typically positive phrasing
        String cat = q.getCategory();
        if (cat != null) {
            for (String token : POSITIVE_CATEGORY_TOKENS) {
                if (token.equalsIgnoreCase(cat)) return true;
            }
        }
        // Also invert if the text appears clearly positive (heuristic)
        String t = q.getText();
        if (t == null) return false;
        String tl = t.toLowerCase(Locale.ROOT);
        return tl.contains("le ayuda") || tl.contains("le permite") || tl.contains("puede") || tl.contains("confía") || tl.contains("paga a tiempo") || tl.contains("orgullo") || tl.contains("se siente comprometido") || tl.contains("recibe capacitación") || tl.contains("le informan con claridad") || tl.contains("le indican a quien") || tl.contains("se le explica");
    }

    private int normalizeTo0to4(int raw) {
        // Support seeds using 1..5 (Nunca..Siempre) or 0..4
        if (raw >= 1 && raw <= 5) return raw - 1;
        if (raw >= 0 && raw <= 4) return raw;
        // Clamp unexpected
        return Math.max(0, Math.min(4, raw));
    }

    private boolean isTraumaticEventQuestion(Question q) {
        String gt = q.getGuideType();
        if (gt != null) {
            String gl = gt.toLowerCase(Locale.ROOT);
            if (gl.contains("acontec") || gl.contains("guía de referencia ii") || gl.equals("ii") || gl.contains("traum")) {
                return true;
            }
        }
        // Heuristic by text
        String t = q.getText();
        if (t == null) return false;
        String tl = t.toLowerCase(Locale.ROOT);
        return tl.contains("muerte") || tl.contains("accidente grave") || tl.contains("violencia extrema") || tl.contains("amenaza") || tl.contains("secuestro");
    }

    private boolean isAffirmative(Response r) {
        Integer v = r.getValue();
        if (v == null) return false;
        // For yes/no questions we expect 1 (Sí) or 0 (No); for 1..5 scale, treat upper values as affirmative presence
        if (v == 1 || v == 4 || v == 5) return true;
        // If normalized 0..4, consider >=1 as presence for traumatic items
        int norm = normalizeTo0to4(v);
        return norm >= 1;
    }

    // Category range interpretation per provided spec
    private String interpretCategoryLevel(String category, int score) {
        switch (category) {
            case CAT_AMBIENTE:
                return range(score, new int[]{5, 9, 13, 17, 20});
            case CAT_CARGA:
                return range(score, new int[]{15, 20, 30, 40, 60});
            case CAT_CONTROL:
                return range(score, new int[]{11, 15, 20, 25, 34});
            case CAT_JORNADA:
                return range(score, new int[]{1, 4, 6, 8, 10});
            case CAT_INTERFERENCIA:
                return range(score, new int[]{3, 6, 10, 13, 16});
            case CAT_LIDERAZGO:
                return range(score, new int[]{14, 29, 44, 59, 80});
            case CAT_ENTORNO:
                return range(score, new int[]{10, 20, 30, 40, 50});
            default:
                return "N/A";
        }
    }

    private String range(int score, int[] bounds) {
        // bounds: [nuloMax, bajoMax, medioMax, altoMax, muyAltoMax]
        if (score <= bounds[0]) return "Nulo";
        if (score <= bounds[1]) return "Bajo";
        if (score <= bounds[2]) return "Medio";
        if (score <= bounds[3]) return "Alto";
        return "Muy alto"; // >= last bound
    }

    private String interpretGlobalLevel(int total) {
        if (total <= 49) return "Nulo";
        if (total <= 75) return "Bajo";
        if (total <= 99) return "Medio";
        if (total <= 140) return "Alto";
        return "Muy alto";
    }
}
