package com.example.nom035.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanySurvey {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @JsonIgnore
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    private LocalDate assignedAt;

    private LocalDate dueDate;

    private String companyVersion;

    @Enumerated(EnumType.STRING)
    private SurveyStatus status;

    private BigDecimal completionRate;

    private String notes;

    @OneToMany(mappedBy = "companySurvey")
    @JsonIgnore
    private List<SurveyApplication> surveyApplications;
    
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Company getCompany() { return company; }
        public void setCompany(Company company) { this.company = company; }
        public Survey getSurvey() { return survey; }
        public void setSurvey(Survey survey) { this.survey = survey; }
        public LocalDate getAssignedAt() { return assignedAt; }
        public void setAssignedAt(LocalDate assignedAt) { this.assignedAt = assignedAt; }
        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
        public String getCompanyVersion() { return companyVersion; }
        public void setCompanyVersion(String companyVersion) { this.companyVersion = companyVersion; }
        public SurveyStatus getStatus() { return status; }
        public void setStatus(SurveyStatus status) { this.status = status; }
        public BigDecimal getCompletionRate() { return completionRate; }
        public void setCompletionRate(BigDecimal completionRate) { this.completionRate = completionRate; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public List<SurveyApplication> getSurveyApplications() { return surveyApplications; }
        public void setSurveyApplications(List<SurveyApplication> surveyApplications) { this.surveyApplications = surveyApplications; }

    public enum SurveyStatus  {
        activo, inactivo
    }
}