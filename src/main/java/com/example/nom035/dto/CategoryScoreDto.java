package com.example.nom035.dto;

public class CategoryScoreDto {
    private String category;
    private int score;
    private String level;
    private Integer min;            // mínimo teórico de la categoría
    private Integer max;            // máximo teórico de la categoría
    private Integer responsesCount; // número de respuestas registradas
    private Integer questionsCount; // número de preguntas consideradas

    public CategoryScoreDto() {}
    public CategoryScoreDto(String category, int score, String level) {
        this.category = category;
        this.score = score;
        this.level = level;
    }
    public CategoryScoreDto(String category, int score, String level,
            Integer min, Integer max, Integer responsesCount, Integer questionsCount) {
        this.category = category;
        this.score = score;
        this.level = level;
        this.min = min;
        this.max = max;
        this.responsesCount = responsesCount;
        this.questionsCount = questionsCount;
    }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public Integer getMin() { return min; }
    public void setMin(Integer min) { this.min = min; }
    public Integer getMax() { return max; }
    public void setMax(Integer max) { this.max = max; }
    public Integer getResponsesCount() { return responsesCount; }
    public void setResponsesCount(Integer responsesCount) { this.responsesCount = responsesCount; }
    public Integer getQuestionsCount() { return questionsCount; }
    public void setQuestionsCount(Integer questionsCount) { this.questionsCount = questionsCount; }
}
