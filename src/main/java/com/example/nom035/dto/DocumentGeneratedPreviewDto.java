package com.example.nom035.dto;

public class DocumentGeneratedPreviewDto {
    private Long jobId;
    private String text;

    public DocumentGeneratedPreviewDto() {
    }

    public DocumentGeneratedPreviewDto(Long jobId, String text) {
        this.jobId = jobId;
        this.text = text;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
