package com.example.nom035.dto;

public class QuestionDto {
    private String text;
    private String type;
    private String options;
    private String answerScores;
    // getters and setters
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }
    public String getAnswerScores() { return answerScores; }
    public void setAnswerScores(String answerScores) { this.answerScores = answerScores; }
}