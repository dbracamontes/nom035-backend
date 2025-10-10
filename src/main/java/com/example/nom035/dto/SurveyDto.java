package com.example.nom035.dto;

import com.example.nom035.entity.Survey;
import java.time.LocalDateTime;

public class SurveyDto {
    private Long id;
    private String title;
    private String description;
    private String guideType;
    private Boolean active;
    private Long baseSurveyId;
    private LocalDateTime createdAt;
    // Optionally, add question count or summary fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getGuideType() { return guideType; }
    public void setGuideType(String guideType) { this.guideType = guideType; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Long getBaseSurveyId() { return baseSurveyId; }
    public void setBaseSurveyId(Long baseSurveyId) { this.baseSurveyId = baseSurveyId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static SurveyDto fromEntity(Survey survey) {
        SurveyDto dto = new SurveyDto();
        dto.setId(survey.getId());
        dto.setTitle(survey.getTitle());
        dto.setDescription(survey.getDescription());
        dto.setGuideType(survey.getGuideType() != null ? survey.getGuideType().name() : null);
        dto.setActive(survey.getActive());
        dto.setBaseSurveyId(survey.getBaseSurvey() != null ? survey.getBaseSurvey().getId() : null);
        dto.setCreatedAt(survey.getCreatedAt());
        return dto;
    }
}