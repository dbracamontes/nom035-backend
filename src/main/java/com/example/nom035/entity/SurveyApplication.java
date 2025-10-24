package com.example.nom035.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyApplication {
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CompanySurvey getCompanySurvey() { return companySurvey; }
    public void setCompanySurvey(CompanySurvey companySurvey) { this.companySurvey = companySurvey; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
    public List<Response> getResponses() { return responses; }
    public void setResponses(List<Response> responses) { this.responses = responses; }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_survey_id", nullable = false)
    @JsonIgnore 
    private CompanySurvey companySurvey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnore
    private Employee employee;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private Integer score; // suma de valores de respuestas

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel; // Bajo, Medio, Alto

    public enum RiskLevel {
        Bajo, Medio, Alto
    }

    @OneToMany(mappedBy = "surveyApplication")
    private List<Response> responses;

    public enum ApplicationStatus {
        PENDIENTE, EN_PROGRESO, COMPLETADA, CANCELADA
    }
}