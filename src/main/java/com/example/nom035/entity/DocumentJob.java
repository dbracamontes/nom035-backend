package com.example.nom035.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "doc_jobs")
public class DocumentJob {

    public enum Status {
        UPLOADED,
        OCR_RUNNING,
        OCR_COMPLETED,
        INTERPRETING,
        INTERPRETED,
        GENERATING_WORD,
        DONE,
        FAILED
    }

    public enum DocumentType {
        ACTA,
        ASAMBLEA,
        CONSTANCIA_SITUACION_FISCAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(nullable = false, length = 500)
    private String storedPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Status status = Status.UPLOADED;

    @Column(nullable = false, length = 64)
    private String ocrProvider;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private DocumentType documentType = DocumentType.ACTA;

    @Column(nullable = false, length = 64)
    private String modelUsed;

    private Integer totalPages;
    private Integer processedPages;
    private Long fileSizeBytes;
    private String contentType;

    @Column(length = 500)
    private String failureReason;

    @Column(length = 500)
    private String outputDocxPath;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private LocalDateTime completedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getStoredPath() {
        return storedPath;
    }

    public void setStoredPath(String storedPath) {
        this.storedPath = storedPath;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getOcrProvider() {
        return ocrProvider;
    }

    public void setOcrProvider(String ocrProvider) {
        this.ocrProvider = ocrProvider;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public void setModelUsed(String modelUsed) {
        this.modelUsed = modelUsed;
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

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getOutputDocxPath() {
        return outputDocxPath;
    }

    public void setOutputDocxPath(String outputDocxPath) {
        this.outputDocxPath = outputDocxPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public void touchUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }
}
