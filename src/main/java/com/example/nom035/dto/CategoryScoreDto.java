package com.example.nom035.dto;

public class CategoryScoreDto {
    private String category;
    private int score;
    private String level;

    public CategoryScoreDto() {}
    public CategoryScoreDto(String category, int score, String level) {
        this.category = category;
        this.score = score;
        this.level = level;
    }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}
