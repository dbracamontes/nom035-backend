package com.example.nom035.entity;

import jakarta.persistence.*;

@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    private String type; // e.g., "single-choice", "multiple-choice", "text"

    private String options; // JSON or comma-separated

    private String answerScores; // JSON: {"A":1,"B":2,"C":3,"D":4}

    @ManyToOne
    @JoinColumn(name = "survey_id")
    private Survey survey;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }

    public String getAnswerScores() { return answerScores; }
    public void setAnswerScores(String answerScores) { this.answerScores = answerScores; }

    public Survey getSurvey() { return survey; }
    public void setSurvey(Survey survey) { this.survey = survey; }
}