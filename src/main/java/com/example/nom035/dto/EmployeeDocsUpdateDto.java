package com.example.nom035.dto;

import com.example.nom035.entity.EmployeeDocs.DocumentStatus;
import jakarta.validation.constraints.Size;

public class EmployeeDocsUpdateDto {
    @Size(max = 255)
    private String name;

    private Long typeId;

    private DocumentStatus status;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getTypeId() { return typeId; }
    public void setTypeId(Long typeId) { this.typeId = typeId; }
    public DocumentStatus getStatus() { return status; }
    public void setStatus(DocumentStatus status) { this.status = status; }
}
