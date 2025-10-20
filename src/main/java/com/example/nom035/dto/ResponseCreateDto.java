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
    
    @Override
    public String toString() {
        return "ResponseCreateDto{" +
                "surveyApplicationId=" + surveyApplicationId +
                ", questionId=" + questionId +
                ", optionAnswerId=" + optionAnswerId +
                ", textAnswer='" + textAnswer + '\'' +
                '}';
    }
}