package com.example.nom035.dto;

import com.example.nom035.entity.EmployeeDocs.DocumentStatus;

import java.time.LocalDateTime;

public class EmployeeDocsDto {
    private Long id;
    private String name;
    private DocumentStatus status;
    private LocalDateTime createdDate;
    private LocalDateTime deactivatedDate;
    private Long employeeId;
    private Long typeId;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String filePath;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public DocumentStatus getStatus() { return status; }
    public void setStatus(DocumentStatus status) { this.status = status; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public LocalDateTime getDeactivatedDate() { return deactivatedDate; }
    public void setDeactivatedDate(LocalDateTime deactivatedDate) { this.deactivatedDate = deactivatedDate; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public Long getTypeId() { return typeId; }
    public void setTypeId(Long typeId) { this.typeId = typeId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
}