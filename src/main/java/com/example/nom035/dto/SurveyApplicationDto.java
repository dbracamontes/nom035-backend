package com.example.nom035.dto;

import com.example.nom035.entity.SurveyApplication;
import java.time.LocalDateTime;

public class SurveyApplicationDto {
    private Long id;
    private Long employeeId;
    private Long companySurveyId;
    private Long surveyId; // convenience
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String status;
    private Integer score;
    private String riskLevel;

    public SurveyApplicationDto() {}

    public SurveyApplicationDto(Long id, Long employeeId, Long companySurveyId, Long surveyId,
                                LocalDateTime startedAt, LocalDateTime completedAt, String status,
                                Integer score, String riskLevel) {
        this.id = id;
        this.employeeId = employeeId;
        this.companySurveyId = companySurveyId;
        this.surveyId = surveyId;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.status = status;
        this.score = score;
        this.riskLevel = riskLevel;
    }

    public static SurveyApplicationDto fromEntity(SurveyApplication sa) {
        return new SurveyApplicationDto(
                sa.getId(),
                sa.getEmployee() != null ? sa.getEmployee().getId() : null,
                sa.getCompanySurvey() != null ? sa.getCompanySurvey().getId() : null,
                sa.getCompanySurvey() != null && sa.getCompanySurvey().getSurvey() != null ? sa.getCompanySurvey().getSurvey().getId() : null,
                sa.getStartedAt(),
                sa.getCompletedAt(),
                sa.getStatus() != null ? sa.getStatus().name() : null,
                sa.getScore(),
                sa.getRiskLevel() != null ? sa.getRiskLevel().name() : null
        );
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public Long getCompanySurveyId() { return companySurveyId; }
    public void setCompanySurveyId(Long companySurveyId) { this.companySurveyId = companySurveyId; }
    public Long getSurveyId() { return surveyId; }
    public void setSurveyId(Long surveyId) { this.surveyId = surveyId; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
}
