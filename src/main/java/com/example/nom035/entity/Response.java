package com.example.nom035.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SurveyApplication getSurveyApplication() { return surveyApplication; }
    public void setSurveyApplication(SurveyApplication surveyApplication) { this.surveyApplication = surveyApplication; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public OptionAnswer getOptionAnswer() { return optionAnswer; }
    public void setOptionAnswer(OptionAnswer optionAnswer) { this.optionAnswer = optionAnswer; }
    public String getFreeText() { return freeText; }
    public void setFreeText(String freeText) { this.freeText = freeText; }
    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }
    public LocalDateTime getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(LocalDateTime answeredAt) { this.answeredAt = answeredAt; }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_application_id", nullable = false)
    @JsonIgnore
    private SurveyApplication surveyApplication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_answer_id")
    private OptionAnswer optionAnswer;

    private String freeText;

    private Integer value; // copiar el valor de optionAnswer.value para reportes rápidos

    private LocalDateTime answeredAt;
}