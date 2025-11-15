package com.example.nom035.dto;

import java.util.List;
import java.util.Map;

public class DictamenDto {
    private Long applicationId;
    private Long employeeId;
    private Long companyId;
    private Long surveyId;

    private String employeeName; // Added display name fields
    private String companyName;

    private int globalScore;
    private String globalLevel;

    private int traumaticEventsCount;
    private boolean traumaticAlert;

    private List<CategoryScoreDto> categories;

    private String conclusion; // brief dictamen text

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public Long getSurveyId() { return surveyId; }
    public void setSurveyId(Long surveyId) { this.surveyId = surveyId; }
    public int getGlobalScore() { return globalScore; }
    public void setGlobalScore(int globalScore) { this.globalScore = globalScore; }
    public String getGlobalLevel() { return globalLevel; }
    public void setGlobalLevel(String globalLevel) { this.globalLevel = globalLevel; }
    public int getTraumaticEventsCount() { return traumaticEventsCount; }
    public void setTraumaticEventsCount(int traumaticEventsCount) { this.traumaticEventsCount = traumaticEventsCount; }
    public boolean isTraumaticAlert() { return traumaticAlert; }
    public void setTraumaticAlert(boolean traumaticAlert) { this.traumaticAlert = traumaticAlert; }
    public List<CategoryScoreDto> getCategories() { return categories; }
    public void setCategories(List<CategoryScoreDto> categories) { this.categories = categories; }
    public String getConclusion() { return conclusion; }
    public void setConclusion(String conclusion) { this.conclusion = conclusion; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
}