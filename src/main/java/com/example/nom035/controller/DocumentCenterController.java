package com.example.nom035.controller;

import com.example.nom035.dto.DocumentCenterItemDto;
import com.example.nom035.entity.EmployeeDocs;
import com.example.nom035.entity.EmployeeDocs.DocumentStatus;
import com.example.nom035.repository.EmployeeDocsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Secured("ROLE_ADMIN")
public class DocumentCenterController {

    private final EmployeeDocsRepository employeeDocsRepository;

    public DocumentCenterController(EmployeeDocsRepository employeeDocsRepository) {
        this.employeeDocsRepository = employeeDocsRepository;
    }

    @GetMapping("/documents-center")
    public ResponseEntity<List<DocumentCenterItemDto>> getDocumentsCenter() {
        List<DocumentCenterItemDto> documents = employeeDocsRepository.findAll().stream()
                .filter(document -> document.getStatus() != DocumentStatus.INACTIVE)
                .map(this::toDocumentCenterItem)
                .collect(Collectors.toList());
        return ResponseEntity.ok(documents);
    }

    @PutMapping("/documents-center/{documentId}/decision")
    public ResponseEntity<DocumentCenterItemDto> decideDocument(@PathVariable Long documentId,
                                                               @RequestParam(defaultValue = "APPROVED") String decision,
                                                               @RequestParam(required = false) String message) {
        EmployeeDocs document = employeeDocsRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with id " + documentId));

        DocumentStatus nextStatus = parseDecision(decision);
        document.setStatus(nextStatus);
        if (nextStatus == DocumentStatus.REJECTED) {
            document.setDeactivatedDate(LocalDateTime.now());
        } else {
            document.setDeactivatedDate(null);
        }

        EmployeeDocs saved = employeeDocsRepository.save(document);
        return ResponseEntity.ok(toDocumentCenterItem(saved));
    }

    private DocumentStatus parseDecision(String decision) {
        if (decision == null) {
            return DocumentStatus.APPROVED;
        }
        String normalized = decision.trim().toUpperCase(Locale.ROOT);
        if ("REJECTED".equals(normalized) || "RECHAZADO".equals(normalized)) {
            return DocumentStatus.REJECTED;
        }
        if ("PENDING".equals(normalized) || "PENDIENTE".equals(normalized)) {
            return DocumentStatus.PENDING;
        }
        return DocumentStatus.APPROVED;
    }

    private DocumentCenterItemDto toDocumentCenterItem(EmployeeDocs entity) {
        Long employeeId = entity.getEmployee() != null ? entity.getEmployee().getId() : null;
        String employeeName = entity.getEmployee() != null ? entity.getEmployee().getName() : "Empleado sin asignar";
        String companyName = entity.getEmployee() != null && entity.getEmployee().getCompany() != null
                ? entity.getEmployee().getCompany().getName()
                : "Sin empresa";
        String fileName = entity.getFileName();
        String extension = fileName != null ? fileName.substring(fileName.lastIndexOf('.') + 1).toUpperCase(Locale.ROOT) : "PDF";

        DocumentCenterItemDto dto = new DocumentCenterItemDto();
        dto.setId(entity.getId());
        dto.setEmployeeId(employeeId);
        dto.setTitle(entity.getName());
        dto.setDivision(companyName);
        dto.setModule("Documentos del empleado");
        dto.setCategory(entity.getType() != null ? entity.getType().getName() : "Documento");
        dto.setStatus(mapStatus(entity.getStatus()));
        dto.setOwner(employeeName);
        dto.setUploadedAt(formatDate(entity.getCreatedDate()));
        dto.setFileType(extension);
        dto.setSize(formatFileSize(entity.getFileSize()));
        dto.setRelatedTo("Empleado: " + employeeName + " · Empresa: " + companyName);
        dto.setTags(List.of(dto.getCategory()));
        dto.setSecurity("Interno");
        dto.setPreviewText("Documento pendiente de revisión por parte del responsable de la operación.");
        dto.setSource("EMPLOYEE_DOC");
        dto.setRequiresApproval(true);
        if (employeeId != null) {
            dto.setDownloadUrl("/api/employees/" + employeeId + "/documents/" + entity.getId() + "/file");
            dto.setPreviewUrl("/api/employees/" + employeeId + "/documents/" + entity.getId() + "/file");
        }
        return dto;
    }

    private String mapStatus(DocumentStatus status) {
        if (status == null) {
            return "Pendiente";
        }
        return switch (status) {
            case APPROVED -> "Aprobado";
            case REJECTED -> "Rechazado";
            case ACTIVE -> "En revisión";
            case INACTIVE -> "Aprobado";
            default -> "Pendiente";
        };
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Sin fecha";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private String formatFileSize(Long sizeInBytes) {
        if (sizeInBytes == null || sizeInBytes <= 0) {
            return "Sin tamaño";
        }
        double size = sizeInBytes;
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return String.format(java.util.Locale.US, "%.1f %s", size, units[unitIndex]);
    }
}
