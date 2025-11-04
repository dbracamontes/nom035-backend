package com.example.nom035.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseCreateDto {
    
    @JsonProperty("surveyApplicationId")
    private Long surveyApplicationId;
    
    @JsonProperty("questionId") 
    private Long questionId;
    
    @JsonProperty("optionAnswerId")
    private Long optionAnswerId;
    
    @JsonProperty("textAnswer")
    private String textAnswer;

    // New: numeric value sent by frontend when no optionAnswerId is used
    @JsonProperty("value")
    private Integer value;

    // New: free text field name used by frontend
    @JsonProperty("freeText")
    private String freeText;
    
    // Constructors
    public ResponseCreateDto() {}
    
    public ResponseCreateDto(Long surveyApplicationId, Long questionId, Long optionAnswerId, String textAnswer) {
        this.surveyApplicationId = surveyApplicationId;
        this.questionId = questionId;
        this.optionAnswerId = optionAnswerId;
        this.textAnswer = textAnswer;
    }
    
    // Getters and Setters
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
        return "ResponseCreateDto{" +
                "surveyApplicationId=" + surveyApplicationId +
                ", questionId=" + questionId +
                ", optionAnswerId=" + optionAnswerId +
                ", textAnswer='" + textAnswer + '\'' +
                ", value=" + value +
                ", freeText='" + freeText + '\'' +
                '}';
    }
}