package com.example.nom035.dto;

public class DocumentGenerateResponseDto {
    private Long jobId;
    private String status;
    private String templateType;

    public DocumentGenerateResponseDto() {
    }

    public DocumentGenerateResponseDto(Long jobId, String status, String templateType) {
        this.jobId = jobId;
        this.status = status;
        this.templateType = templateType;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }
}
