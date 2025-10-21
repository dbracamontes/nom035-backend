package com.example.nom035.dto;

public class ResponseDto {
    private Long id;
    private Long surveyApplicationId;
    private Long questionId;
    private Long optionAnswerId;
    private String freeText;

    public ResponseDto() {}

    public ResponseDto(Long id, Long surveyApplicationId, Long questionId, Long optionAnswerId, String freeText) {
        this.id = id;
        this.surveyApplicationId = surveyApplicationId;
        this.questionId = questionId;
        this.optionAnswerId = optionAnswerId;
        this.freeText = freeText;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSurveyApplicationId() { return surveyApplicationId; }
    public void setSurveyApplicationId(Long surveyApplicationId) { this.surveyApplicationId = surveyApplicationId; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public Long getOptionAnswerId() { return optionAnswerId; }
    public void setOptionAnswerId(Long optionAnswerId) { this.optionAnswerId = optionAnswerId; }

    public String getFreeText() { return freeText; }
    public void setFreeText(String freeText) { this.freeText = freeText; }
}
