package com.example.nom035.dto;

import com.example.nom035.entity.CompanySurvey;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CompanySurveyDto {
    private Long id;
    private Long companyId;
    private Long surveyId;
    private LocalDate assignedAt;
    private LocalDate dueDate;
    private String companyVersion;
    private String status;
    private BigDecimal completionRate;
    private String notes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public Long getSurveyId() { return surveyId; }
    public void setSurveyId(Long surveyId) { this.surveyId = surveyId; }
    public LocalDate getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDate assignedAt) { this.assignedAt = assignedAt; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public String getCompanyVersion() { return companyVersion; }
    public void setCompanyVersion(String companyVersion) { this.companyVersion = companyVersion; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getCompletionRate() { return completionRate; }
    public void setCompletionRate(BigDecimal completionRate) { this.completionRate = completionRate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public static CompanySurveyDto fromEntity(CompanySurvey entity) {
        CompanySurveyDto dto = new CompanySurveyDto();
        dto.setId(entity.getId());
        dto.setCompanyId(entity.getCompany() != null ? entity.getCompany().getId() : null);
        dto.setSurveyId(entity.getSurvey() != null ? entity.getSurvey().getId() : null);
        dto.setAssignedAt(entity.getAssignedAt());
        dto.setDueDate(entity.getDueDate());
        dto.setCompanyVersion(entity.getCompanyVersion());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        dto.setCompletionRate(entity.getCompletionRate());
        dto.setNotes(entity.getNotes());
        return dto;
    }
}
