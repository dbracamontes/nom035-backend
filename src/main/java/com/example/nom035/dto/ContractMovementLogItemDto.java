package com.example.nom035.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ContractMovementLogItemDto {

    private Long jobId;
    private String templateType;
    private String status;
    private LocalDate contractDate;
    private LocalDate vigenciaStartDate;
    private LocalDate vigenciaEndDate;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getContractDate() {
        return contractDate;
    }

    public void setContractDate(LocalDate contractDate) {
        this.contractDate = contractDate;
    }

    public LocalDate getVigenciaStartDate() {
        return vigenciaStartDate;
    }

    public void setVigenciaStartDate(LocalDate vigenciaStartDate) {
        this.vigenciaStartDate = vigenciaStartDate;
    }

    public LocalDate getVigenciaEndDate() {
        return vigenciaEndDate;
    }

    public void setVigenciaEndDate(LocalDate vigenciaEndDate) {
        this.vigenciaEndDate = vigenciaEndDate;
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
}
