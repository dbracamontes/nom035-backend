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
    private Integer globalMin;
    private Integer globalMax;
    private Integer totalResponses;
    private String globalLevel;

    private int traumaticEventsCount;
    private boolean traumaticAlert;

    private List<CategoryScoreDto> categories;

    private String conclusion; // brief dictamen text

    // Demográficos ampliados (rellenables con "No disponible" si faltan datos)
    private String employeeEmail;
    private String department;
    private String position;
    private String age;
    private String maritalStatus;
    private String gender;
    private String studies;
    private String seniority;
    private String sameActivity;
    private String workingDays;
    private String hoursPerDay;
    private String transportType;
    private String weeklyGasoline;
    private String commuteTime;
    private String transportCost;
    private String housing;
    private String applicationDate;
    private String completedDate;

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
    public Integer getGlobalMin() { return globalMin; }
    public void setGlobalMin(Integer globalMin) { this.globalMin = globalMin; }
    public Integer getGlobalMax() { return globalMax; }
    public void setGlobalMax(Integer globalMax) { this.globalMax = globalMax; }
    public Integer getTotalResponses() { return totalResponses; }
    public void setTotalResponses(Integer totalResponses) { this.totalResponses = totalResponses; }
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
    public String getEmployeeEmail() { return employeeEmail; }
    public void setEmployeeEmail(String employeeEmail) { this.employeeEmail = employeeEmail; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }
    public String getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getStudies() { return studies; }
    public void setStudies(String studies) { this.studies = studies; }
    public String getSeniority() { return seniority; }
    public void setSeniority(String seniority) { this.seniority = seniority; }
    public String getSameActivity() { return sameActivity; }
    public void setSameActivity(String sameActivity) { this.sameActivity = sameActivity; }
    public String getWorkingDays() { return workingDays; }
    public void setWorkingDays(String workingDays) { this.workingDays = workingDays; }
    public String getHoursPerDay() { return hoursPerDay; }
    public void setHoursPerDay(String hoursPerDay) { this.hoursPerDay = hoursPerDay; }
    public String getTransportType() { return transportType; }
    public void setTransportType(String transportType) { this.transportType = transportType; }
    public String getWeeklyGasoline() { return weeklyGasoline; }
    public void setWeeklyGasoline(String weeklyGasoline) { this.weeklyGasoline = weeklyGasoline; }
    public String getCommuteTime() { return commuteTime; }
    public void setCommuteTime(String commuteTime) { this.commuteTime = commuteTime; }
    public String getTransportCost() { return transportCost; }
    public void setTransportCost(String transportCost) { this.transportCost = transportCost; }
    public String getHousing() { return housing; }
    public void setHousing(String housing) { this.housing = housing; }
    public String getApplicationDate() { return applicationDate; }
    public void setApplicationDate(String applicationDate) { this.applicationDate = applicationDate; }
    public String getCompletedDate() { return completedDate; }
    public void setCompletedDate(String completedDate) { this.completedDate = completedDate; }
}