package com.example.nom035.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_survey_id", nullable = false)
    private CompanySurvey companySurvey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private Integer score; // suma de valores de respuestas

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel; // Bajo, Medio, Alto

    @OneToMany(mappedBy = "surveyApplication")
    private List<Response> responses;

    public enum ApplicationStatus {
        pendiente, en_progreso, completado
    }

    public enum RiskLevel {
        Bajo, Medio, Alto
    }
}