package com.example.nom035.entity.converter;

import com.example.nom035.entity.EmployeeDocs.DocumentStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class EmployeeDocsStatusConverter implements AttributeConverter<DocumentStatus, String> {
    @Override
    public String convertToDatabaseColumn(DocumentStatus attribute) {
        if (attribute == null) {
            return DocumentStatus.PENDING.name();
        }
        if (attribute == DocumentStatus.ACTIVE) {
            return DocumentStatus.PENDING.name();
        }
        if (attribute == DocumentStatus.INACTIVE) {
            return DocumentStatus.APPROVED.name();
        }
        return attribute.name();
    }

    @Override
    public DocumentStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return DocumentStatus.PENDING;
        }
        String normalized = dbData.trim();
        if ("Active".equalsIgnoreCase(normalized) || "ACTIVE".equalsIgnoreCase(normalized) || "PENDING".equalsIgnoreCase(normalized)) {
            return DocumentStatus.PENDING;
        }
        if ("Inactive".equalsIgnoreCase(normalized) || "INACTIVE".equalsIgnoreCase(normalized) || "APPROVED".equalsIgnoreCase(normalized)) {
            return DocumentStatus.APPROVED;
        }
        if ("REJECTED".equalsIgnoreCase(normalized) || "Rejected".equalsIgnoreCase(normalized)) {
            return DocumentStatus.REJECTED;
        }
        return DocumentStatus.fromString(normalized);
    }
}
