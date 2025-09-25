package com.example.nom035.dto;

import java.util.List;

public class SurveyDto {
    private String title;
    private String description;
    private List<QuestionDto> questions;
    // getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<QuestionDto> getQuestions() { return questions; }
    public void setQuestions(List<QuestionDto> questions) { this.questions = questions; }
}
