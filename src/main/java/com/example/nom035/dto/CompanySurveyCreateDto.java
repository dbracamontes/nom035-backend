package com.example.nom035.dto;

import com.example.nom035.entity.CompanySurvey;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CompanySurveyCreateDto {
    private Long companyId;
    private Long surveyId;
    private LocalDate assignedAt;
    private LocalDate dueDate;
    private String companyVersion;
    private String status;
    private BigDecimal completionRate;
    private String notes;

    // Constructores
    public CompanySurveyCreateDto() {}

    public CompanySurveyCreateDto(Long companyId, Long surveyId, LocalDate assignedAt, 
                                  LocalDate dueDate, String companyVersion, String status,
                                  BigDecimal completionRate, String notes) {
        this.companyId = companyId;
        this.surveyId = surveyId;
        this.assignedAt = assignedAt;
        this.dueDate = dueDate;
        this.companyVersion = companyVersion;
        this.status = status;
        this.completionRate = completionRate;
        this.notes = notes;
    }

    // Getters y Setters
    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(Long surveyId) {
        this.surveyId = surveyId;
    }

    public LocalDate getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDate assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getCompanyVersion() {
        return companyVersion;
    }

    public void setCompanyVersion(String companyVersion) {
        this.companyVersion = companyVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(BigDecimal completionRate) {
        this.completionRate = completionRate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}