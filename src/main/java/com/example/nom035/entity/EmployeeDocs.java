package com.example.nom035.entity;

import com.example.nom035.entity.converter.EmployeeDocsStatusConverter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_docs")
public class EmployeeDocs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Convert(converter = EmployeeDocsStatusConverter.class)
    @Column(nullable = false, length = 16)
    private DocumentStatus status = DocumentStatus.PENDING;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "deactivated_date")
    private LocalDateTime deactivatedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private DocumentType type;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_path", length = 500)
    private String filePath;

    public enum DocumentStatus {
        PENDING, APPROVED, REJECTED, ACTIVE, INACTIVE;

        public static DocumentStatus fromString(String value) {
            if (value == null) return null;
            String normalized = value.trim();

            if ("Active".equalsIgnoreCase(normalized) || "ACTIVE".equalsIgnoreCase(normalized)
                    || "PENDING".equalsIgnoreCase(normalized)) {
                return PENDING;
            }
            if ("Inactive".equalsIgnoreCase(normalized) || "INACTIVE".equalsIgnoreCase(normalized)
                    || "APPROVED".equalsIgnoreCase(normalized)) {
                return APPROVED;
            }
            if ("Rejected".equalsIgnoreCase(normalized) || "REJECTED".equalsIgnoreCase(normalized)) {
                return REJECTED;
            }

            for (DocumentStatus status : DocumentStatus.values()) {
                if (status.name().equalsIgnoreCase(normalized)
                        || status.name().replace("_", "").equalsIgnoreCase(normalized.replace("_", ""))) {
                    return status;
                }
            }
            throw new IllegalArgumentException("No enum constant DocumentStatus." + value);
        }

        public String toDatabaseValue() {
            return this.name();
        }
    }

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
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public DocumentType getType() { return type; }
    public void setType(DocumentType type) { this.type = type; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    // Setter para aceptar String y mapear case-insensitive
    public void setStatus(String status) {
        this.status = DocumentStatus.fromString(status);
    }
}