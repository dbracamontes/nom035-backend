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