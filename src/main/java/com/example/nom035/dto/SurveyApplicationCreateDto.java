package com.example.nom035.dto;

public class SurveyApplicationCreateDto {
    private Long employeeId;
    private Long surveyId; // used to resolve CompanySurvey
    private String status; // free-form; will be mapped to enum
    private String startDate; // ISO-8601 string
    private String endDate;   // ISO-8601 string

    public SurveyApplicationCreateDto() {}

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public Long getSurveyId() { return surveyId; }
    public void setSurveyId(Long surveyId) { this.surveyId = surveyId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}
