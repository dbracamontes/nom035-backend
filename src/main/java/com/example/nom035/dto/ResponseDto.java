package com.example.nom035.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseDto {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("surveyApplicationId")
    private Long surveyApplicationId;
    
    @JsonProperty("questionId") 
    private Long questionId;
    
    @JsonProperty("optionAnswerId")
    private Long optionAnswerId;
    
    // Backward-compat: some clients expect textAnswer
    @JsonProperty("textAnswer")
    private String textAnswer;

    // New: explicit fields used by current frontend
    @JsonProperty("value")
    private Integer value;

    @JsonProperty("freeText")
    private String freeText;
    
    // Constructors
    public ResponseDto() {}
    
    public ResponseDto(Long id, Long surveyApplicationId, Long questionId, Long optionAnswerId, String textAnswer) {
        this.id = id;
        this.surveyApplicationId = surveyApplicationId;
        this.questionId = questionId;
        this.optionAnswerId = optionAnswerId;
        this.textAnswer = textAnswer;
        // Keep freeText in sync by default
        this.freeText = textAnswer;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getSurveyApplicationId() {
        return surveyApplicationId;
    }
    
    public void setSurveyApplicationId(Long surveyApplicationId) {
        this.surveyApplicationId = surveyApplicationId;
    }
    
    public Long getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }
    
    public Long getOptionAnswerId() {
        return optionAnswerId;
    }
    
    public void setOptionAnswerId(Long optionAnswerId) {
        this.optionAnswerId = optionAnswerId;
    }
    
    public String getTextAnswer() {
        return textAnswer;
    }
    
    public void setTextAnswer(String textAnswer) {
        this.textAnswer = textAnswer;
    }

    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }

    public String getFreeText() { return freeText; }
    public void setFreeText(String freeText) { this.freeText = freeText; }
    
    @Override
    public String toString() {
        return "ResponseDto{" +
                "id=" + id +
                ", surveyApplicationId=" + surveyApplicationId +
                ", questionId=" + questionId +
                ", optionAnswerId=" + optionAnswerId +
                ", textAnswer='" + textAnswer + '\'' +
                ", value=" + value +
                ", freeText='" + freeText + '\'' +
                '}';
    }
}