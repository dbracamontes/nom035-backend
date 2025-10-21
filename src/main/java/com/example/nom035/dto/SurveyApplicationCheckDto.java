package com.example.nom035.dto;

import java.time.LocalDateTime;

public class SurveyApplicationCheckDto {
    private boolean found;
    private Long applicationId;
    private boolean completed;
    private String status;
    private LocalDateTime completedAt;
    private int responsesCount;
    private int questionsCount;

    public boolean isFound() { return found; }
    public void setFound(boolean found) { this.found = found; }
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public int getResponsesCount() { return responsesCount; }
    public void setResponsesCount(int responsesCount) { this.responsesCount = responsesCount; }
    public int getQuestionsCount() { return questionsCount; }
    public void setQuestionsCount(int questionsCount) { this.questionsCount = questionsCount; }
}
