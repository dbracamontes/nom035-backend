package com.example.nom035.dto;

public class DocumentUploadResponseDto {
    private Long jobId;
    private String status;

    public DocumentUploadResponseDto() {
    }

    public DocumentUploadResponseDto(Long jobId, String status) {
        this.jobId = jobId;
        this.status = status;
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
}
