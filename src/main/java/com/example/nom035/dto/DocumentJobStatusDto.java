package com.example.nom035.dto;

import java.time.LocalDateTime;

public class DocumentJobStatusDto {
    private Long jobId;
    private String status;
    private Integer totalPages;
    private Integer processedPages;
    private String originalFilename;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String failureReason;
    private boolean outputReady;
    private String documentType;

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

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getProcessedPages() {
        return processedPages;
    }

    public void setProcessedPages(Integer processedPages) {
        this.processedPages = processedPages;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public boolean isOutputReady() {
        return outputReady;
    }

    public void setOutputReady(boolean outputReady) {
        this.outputReady = outputReady;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
}
