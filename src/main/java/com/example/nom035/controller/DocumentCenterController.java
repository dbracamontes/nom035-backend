package com.example.nom035.controller;

import com.example.nom035.dto.DocumentCenterItemDto;
import com.example.nom035.entity.Company;
import com.example.nom035.entity.EmployeeDocs;
import com.example.nom035.entity.EmployeeDocs.DocumentStatus;
import com.example.nom035.entity.MedicaLebenCompanyDocs;
import com.example.nom035.entity.MedicaLebenCompanyWorkPhoto;
import com.example.nom035.repository.EmployeeDocsRepository;
import com.example.nom035.repository.MedicaLebenCompanyDocsRepository;
import com.example.nom035.repository.MedicaLebenCompanyWorkPhotoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Secured("ROLE_ADMIN")
public class DocumentCenterController {

    private static final List<String> WORK_PHOTO_TITLES = List.of(
        "I.- Fotos del área en donde se encuentran realizando las actividades los trabajadores.",
        "II.- Fotos de las salidas de emergencia.",
        "III.- Fotos del área de comida.",
        "IV.- Fotos de las instalaciones de la empresa (entrada).",
        "IV.- Fotos de las instalaciones de la empresa (salida).",
        "IV.- Fotos de las instalaciones de la empresa (escaleras).",
        "V.- Foto de los equipos de seguridad con que cuentan.");

    private final EmployeeDocsRepository employeeDocsRepository;
    private final MedicaLebenCompanyDocsRepository medicaLebenCompanyDocsRepository;
    private final MedicaLebenCompanyWorkPhotoRepository medicaLebenCompanyWorkPhotoRepository;

    public DocumentCenterController(EmployeeDocsRepository employeeDocsRepository,
                                   MedicaLebenCompanyDocsRepository medicaLebenCompanyDocsRepository,
                                   MedicaLebenCompanyWorkPhotoRepository medicaLebenCompanyWorkPhotoRepository) {
        this.employeeDocsRepository = employeeDocsRepository;
        this.medicaLebenCompanyDocsRepository = medicaLebenCompanyDocsRepository;
        this.medicaLebenCompanyWorkPhotoRepository = medicaLebenCompanyWorkPhotoRepository;
    }

    @GetMapping("/documents-center")
    public ResponseEntity<List<DocumentCenterItemDto>> getDocumentsCenter() {
        List<DocumentCenterItemDto> documents = new ArrayList<>();

        documents.addAll(employeeDocsRepository.findAll().stream()
                .filter(document -> document.getStatus() != DocumentStatus.INACTIVE)
                .filter(document -> document.getFilePath() != null && !document.getFilePath().isBlank())
                .map(this::toDocumentCenterItem)
                .collect(Collectors.toList()));

        medicaLebenCompanyDocsRepository.findAll().forEach(docs -> {
            documents.addAll(toCompanyDocumentCenterItems(docs));
            documents.addAll(toWorkPhotoDocumentCenterItems(docs));
        });

        return ResponseEntity.ok(documents);
    }

    @PutMapping("/documents-center/{documentId}/decision")
    public ResponseEntity<DocumentCenterItemDto> decideDocument(@PathVariable Long documentId,
                                                               @RequestParam(defaultValue = "PENDING") String decision,
                                                               @RequestParam(required = false) String message) {
        Optional<EmployeeDocs> employeeDocs = employeeDocsRepository.findById(documentId);
        if (employeeDocs.isPresent()) {
            EmployeeDocs document = employeeDocs.get();
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

        Optional<MedicaLebenCompanyWorkPhoto> workPhoto = medicaLebenCompanyWorkPhotoRepository.findById(documentId);
        if (workPhoto.isPresent()) {
            MedicaLebenCompanyWorkPhoto photo = workPhoto.get();
            MedicaLebenCompanyWorkPhoto.PhotoStatus nextStatus = parsePhotoDecision(decision);
            photo.setStatus(nextStatus);
            MedicaLebenCompanyWorkPhoto saved = medicaLebenCompanyWorkPhotoRepository.save(photo);
            int order = saved.getSortOrder() > 0 ? saved.getSortOrder() : 1;
            return ResponseEntity.ok(toWorkPhotoDocumentCenterItem(saved, order, saved.getCompanyDocs().getCompany()));
        }

        long companyDocId = Math.floorDiv(documentId, 1000L);
        int fieldIndex = Math.toIntExact(documentId % 1000L);
        MedicaLebenCompanyDocs companyDocs = medicaLebenCompanyDocsRepository.findById(companyDocId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with id " + documentId));

        String field = resolveCompanyDocumentField(fieldIndex);
        MedicaLebenCompanyDocs.DocumentStatus nextStatus = parseCompanyDecision(decision);
        applyCompanyFieldStatus(companyDocs, field, nextStatus);
        companyDocs.setStatus(resolveBundleStatus(companyDocs));
        medicaLebenCompanyDocsRepository.save(companyDocs);

        return ResponseEntity.ok(toCompanyDocumentCenterItem(companyDocs, fieldIndex));
    }

    private DocumentStatus parseDecision(String decision) {
        if (decision == null || decision.trim().isEmpty()) {
            return DocumentStatus.PENDING;
        }
        String normalized = decision.trim().toUpperCase(Locale.ROOT);
        if ("REJECTED".equals(normalized) || "RECHAZADO".equals(normalized)) {
            return DocumentStatus.REJECTED;
        }
        if ("PENDING".equals(normalized) || "PENDIENTE".equals(normalized)) {
            return DocumentStatus.PENDING;
        }
        if ("APPROVED".equals(normalized) || "APROBADO".equals(normalized)) {
            return DocumentStatus.APPROVED;
        }
        return DocumentStatus.PENDING;
    }

    private MedicaLebenCompanyDocs.DocumentStatus parseCompanyDecision(String decision) {
        if (decision == null || decision.trim().isEmpty()) {
            return MedicaLebenCompanyDocs.DocumentStatus.PENDING;
        }
        String normalized = decision.trim().toUpperCase(Locale.ROOT);
        if ("REJECTED".equals(normalized) || "RECHAZADO".equals(normalized)) {
            return MedicaLebenCompanyDocs.DocumentStatus.REJECTED;
        }
        if ("PENDING".equals(normalized) || "PENDIENTE".equals(normalized)) {
            return MedicaLebenCompanyDocs.DocumentStatus.PENDING;
        }
        if ("APPROVED".equals(normalized) || "APROBADO".equals(normalized)) {
            return MedicaLebenCompanyDocs.DocumentStatus.APPROVED;
        }
        return MedicaLebenCompanyDocs.DocumentStatus.PENDING;
    }

    private MedicaLebenCompanyWorkPhoto.PhotoStatus parsePhotoDecision(String decision) {
        if (decision == null || decision.trim().isEmpty()) {
            return MedicaLebenCompanyWorkPhoto.PhotoStatus.PENDING;
        }
        String normalized = decision.trim().toUpperCase(Locale.ROOT);
        if ("REJECTED".equals(normalized) || "RECHAZADO".equals(normalized)) {
            return MedicaLebenCompanyWorkPhoto.PhotoStatus.REJECTED;
        }
        if ("PENDING".equals(normalized) || "PENDIENTE".equals(normalized)) {
            return MedicaLebenCompanyWorkPhoto.PhotoStatus.PENDING;
        }
        if ("APPROVED".equals(normalized) || "APROBADO".equals(normalized)) {
            return MedicaLebenCompanyWorkPhoto.PhotoStatus.APPROVED;
        }
        return MedicaLebenCompanyWorkPhoto.PhotoStatus.PENDING;
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

    private List<DocumentCenterItemDto> toCompanyDocumentCenterItems(MedicaLebenCompanyDocs docs) {
        if (docs == null || docs.getCompany() == null) {
            return List.of();
        }

        Company company = docs.getCompany();
        List<DocumentCenterItemDto> items = new ArrayList<>();
        List<String> fieldSpecs = List.of(
                "acta_constitutiva", "asamblea", "constancia_situacion_fiscal", "poder_notarial",
                "identificacion_representante", "comprobante_domicilio", "estado_cuenta_bancaria",
                "comprobante_ema_eba"
        );

        for (int i = 0; i < fieldSpecs.size(); i++) {
            String field = fieldSpecs.get(i);
            String filename = getCompanyFieldValue(docs, field);
            if (filename == null || filename.isBlank()) {
                continue;
            }
            items.add(toCompanyDocumentCenterItem(docs, i + 1, field, filename, company));
        }

        return items;
    }

    private DocumentCenterItemDto toCompanyDocumentCenterItem(MedicaLebenCompanyDocs docs, int fieldIndex, String field, String filename, Company company) {
        String title = switch (field) {
            case "acta_constitutiva" -> "Acta constitutiva";
            case "asamblea" -> "Asamblea";
            case "constancia_situacion_fiscal" -> "Constancia de situación fiscal";
            case "poder_notarial" -> "Poder notarial";
            case "identificacion_representante" -> "Identificación del representante legal";
            case "comprobante_domicilio" -> "Comprobante de domicilio";
            case "estado_cuenta_bancaria" -> "Estado de cuenta bancaria";
            case "comprobante_ema_eba" -> "Comprobante EMA/EBA";
            default -> "Documento de empresa";
        };

        DocumentCenterItemDto dto = new DocumentCenterItemDto();
        dto.setId(buildCompanyDocumentDecisionId(docs.getId(), fieldIndex));
        dto.setCompanyId(company.getId());
        dto.setCompanyDocId(docs.getId());
        dto.setDocumentKey(field);
        dto.setTitle(title);
        dto.setDivision(company.getName());
        dto.setModule("Médica LEBEN");
        dto.setCategory("Documentación de empresa");
        dto.setStatus(mapCompanyStatus(getCompanyFieldStatus(docs, field)));
        dto.setOwner(company.getName());
        dto.setUploadedAt(formatDate(docs.getCreatedAt()));
        dto.setFileType(extractExtension(filename));
        dto.setSize("Sin tamaño");
        dto.setRelatedTo("Empresa: " + company.getName());
        dto.setTags(List.of("MEDICA_LEBEN", "EMPRESA"));
        dto.setSecurity("Interno");
        dto.setPreviewText("Documento pendiente de revisión por parte del responsable del proceso.");
        dto.setSource("COMPANY_DOC");
        dto.setRequiresApproval(true);
        dto.setDownloadUrl("/api/medica-leben/companies/" + company.getId() + "/docs/" + filename);
        dto.setPreviewUrl(dto.getDownloadUrl());
        return dto;
    }

    private List<DocumentCenterItemDto> toWorkPhotoDocumentCenterItems(MedicaLebenCompanyDocs docs) {
        if (docs == null || docs.getCompany() == null) {
            return List.of();
        }

        List<MedicaLebenCompanyWorkPhoto> photos = medicaLebenCompanyWorkPhotoRepository.findByCompanyDocsOrderBySortOrderAsc(docs);
        if ((photos == null || photos.isEmpty()) && docs.getWorkPhotos() != null) {
            photos = docs.getWorkPhotos();
        }
        if (photos == null || photos.isEmpty()) {
            return List.of();
        }

        List<MedicaLebenCompanyWorkPhoto> orderedPhotos = new ArrayList<>(photos);
        orderedPhotos.sort((left, right) -> Integer.compare(left.getSortOrder(), right.getSortOrder()));

        List<DocumentCenterItemDto> items = new ArrayList<>();
        for (int i = 0; i < orderedPhotos.size(); i++) {
            MedicaLebenCompanyWorkPhoto photo = orderedPhotos.get(i);
            if (photo == null || photo.getUrl() == null || photo.getUrl().isBlank()) {
                continue;
            }
            items.add(toWorkPhotoDocumentCenterItem(photo, i + 1, docs.getCompany()));
        }
        return items;
    }

    private String resolveWorkPhotoTitle(int order) {
        if (order >= 1 && order <= WORK_PHOTO_TITLES.size()) {
            return WORK_PHOTO_TITLES.get(order - 1);
        }
        return order > 1 ? "Foto del área de trabajo " + order : "Foto del área de trabajo";
    }

    private DocumentCenterItemDto toWorkPhotoDocumentCenterItem(MedicaLebenCompanyWorkPhoto photo, int order, Company company) {
        DocumentCenterItemDto dto = new DocumentCenterItemDto();
        dto.setId(photo.getId());
        dto.setCompanyId(company.getId());
        dto.setCompanyDocId(photo.getCompanyDocs() != null ? photo.getCompanyDocs().getId() : null);
        dto.setDocumentKey("foto_area_trabajo_" + order);
        dto.setTitle(resolveWorkPhotoTitle(order));
        dto.setDivision(company.getName());
        dto.setModule("Médica LEBEN");
        dto.setCategory("Fotos del área de trabajo");
        dto.setStatus(mapPhotoStatus(photo.getStatus()));
        dto.setOwner(company.getName());
        dto.setUploadedAt(formatDate(photo.getCreatedAt()));
        dto.setFileType(extractExtension(photo.getUrl()));
        dto.setSize("Sin tamaño");
        dto.setRelatedTo("Empresa: " + company.getName());
        dto.setTags(List.of("MEDICA_LEBEN", "FOTO", "AREA_TRABAJO"));
        dto.setSecurity("Interno");
        dto.setPreviewText("Foto pendiente de revisión por parte del responsable del proceso.");
        dto.setSource("PHOTO");
        dto.setRequiresApproval(true);
        String downloadUrl = "/api/medica-leben/companies/" + company.getId() + "/photos/" + photo.getUrl();
        dto.setDownloadUrl(downloadUrl);
        dto.setPreviewUrl(downloadUrl);
        return dto;
    }

    private DocumentCenterItemDto toCompanyDocumentCenterItem(MedicaLebenCompanyDocs docs, int fieldIndex) {
        List<String> fieldSpecs = List.of(
                "acta_constitutiva", "asamblea", "constancia_situacion_fiscal", "poder_notarial",
                "identificacion_representante", "comprobante_domicilio", "estado_cuenta_bancaria",
                "comprobante_ema_eba"
        );
        String field = fieldSpecs.get(Math.max(0, Math.min(fieldIndex - 1, fieldSpecs.size() - 1)));
        String filename = getCompanyFieldValue(docs, field);
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("No file available for company document field " + field);
        }
        return toCompanyDocumentCenterItem(docs, fieldIndex, field, filename, docs.getCompany());
    }

    private Long buildCompanyDocumentDecisionId(Long companyDocId, int fieldIndex) {
        if (companyDocId == null || companyDocId <= 0) {
            return 0L;
        }
        return companyDocId * 1000L + fieldIndex;
    }

    private String resolveCompanyDocumentField(int fieldIndex) {
        List<String> fieldSpecs = List.of(
                "acta_constitutiva", "asamblea", "constancia_situacion_fiscal", "poder_notarial",
                "identificacion_representante", "comprobante_domicilio", "estado_cuenta_bancaria",
                "comprobante_ema_eba"
        );
        int safeIndex = Math.max(1, Math.min(fieldIndex, fieldSpecs.size()));
        return fieldSpecs.get(safeIndex - 1);
    }

    private void applyCompanyFieldStatus(MedicaLebenCompanyDocs docs, String field, MedicaLebenCompanyDocs.DocumentStatus status) {
        if (docs == null || field == null) {
            return;
        }
        switch (field) {
            case "acta_constitutiva" -> docs.setActaConstitutivaStatus(status);
            case "asamblea" -> docs.setAsambleaStatus(status);
            case "constancia_situacion_fiscal" -> docs.setConstanciaSituacionFiscalStatus(status);
            case "poder_notarial" -> docs.setPoderNotarialStatus(status);
            case "identificacion_representante" -> docs.setIdentificacionRepresentanteStatus(status);
            case "comprobante_domicilio" -> docs.setComprobanteDomicilioStatus(status);
            case "estado_cuenta_bancaria" -> docs.setEstadoCuentaBancariaStatus(status);
            case "comprobante_ema_eba" -> docs.setComprobanteEmaEbaStatus(status);
            default -> {
            }
        }
    }

    private MedicaLebenCompanyDocs.DocumentStatus getCompanyFieldStatus(MedicaLebenCompanyDocs docs, String field) {
        if (docs == null || field == null) {
            return MedicaLebenCompanyDocs.DocumentStatus.PENDING;
        }
        return switch (field) {
            case "acta_constitutiva" -> docs.getActaConstitutivaStatus() != null ? docs.getActaConstitutivaStatus() : MedicaLebenCompanyDocs.DocumentStatus.PENDING;
            case "asamblea" -> docs.getAsambleaStatus() != null ? docs.getAsambleaStatus() : MedicaLebenCompanyDocs.DocumentStatus.PENDING;
            case "constancia_situacion_fiscal" -> docs.getConstanciaSituacionFiscalStatus() != null ? docs.getConstanciaSituacionFiscalStatus() : MedicaLebenCompanyDocs.DocumentStatus.PENDING;
            case "poder_notarial" -> docs.getPoderNotarialStatus() != null ? docs.getPoderNotarialStatus() : MedicaLebenCompanyDocs.DocumentStatus.PENDING;
            case "identificacion_representante" -> docs.getIdentificacionRepresentanteStatus() != null ? docs.getIdentificacionRepresentanteStatus() : MedicaLebenCompanyDocs.DocumentStatus.PENDING;
            case "comprobante_domicilio" -> docs.getComprobanteDomicilioStatus() != null ? docs.getComprobanteDomicilioStatus() : MedicaLebenCompanyDocs.DocumentStatus.PENDING;
            case "estado_cuenta_bancaria" -> docs.getEstadoCuentaBancariaStatus() != null ? docs.getEstadoCuentaBancariaStatus() : MedicaLebenCompanyDocs.DocumentStatus.PENDING;
            case "comprobante_ema_eba" -> docs.getComprobanteEmaEbaStatus() != null ? docs.getComprobanteEmaEbaStatus() : MedicaLebenCompanyDocs.DocumentStatus.PENDING;
            default -> MedicaLebenCompanyDocs.DocumentStatus.PENDING;
        };
    }

    private MedicaLebenCompanyDocs.DocumentStatus resolveBundleStatus(MedicaLebenCompanyDocs docs) {
        if (docs == null) {
            return MedicaLebenCompanyDocs.DocumentStatus.PENDING;
        }

        boolean hasFieldStatus = false;
        boolean hasRejected = false;
        boolean hasApproved = false;
        boolean hasPending = false;

        for (MedicaLebenCompanyDocs.DocumentStatus status : List.of(
                docs.getActaConstitutivaStatus(),
                docs.getAsambleaStatus(),
                docs.getConstanciaSituacionFiscalStatus(),
                docs.getPoderNotarialStatus(),
                docs.getIdentificacionRepresentanteStatus(),
                docs.getComprobanteDomicilioStatus(),
                docs.getEstadoCuentaBancariaStatus(),
                docs.getComprobanteEmaEbaStatus())) {
            if (status == null) {
                continue;
            }
            hasFieldStatus = true;
            switch (status) {
                case REJECTED -> hasRejected = true;
                case APPROVED -> hasApproved = true;
                case PENDING -> hasPending = true;
            }
        }

        if (!hasFieldStatus) {
            return docs.getStatus() != null ? docs.getStatus() : MedicaLebenCompanyDocs.DocumentStatus.PENDING;
        }
        if (hasRejected) {
            return MedicaLebenCompanyDocs.DocumentStatus.REJECTED;
        }
        if (hasPending && !hasApproved) {
            return MedicaLebenCompanyDocs.DocumentStatus.PENDING;
        }
        if (hasApproved && !hasPending) {
            return MedicaLebenCompanyDocs.DocumentStatus.APPROVED;
        }
        return MedicaLebenCompanyDocs.DocumentStatus.PENDING;
    }

    private String getCompanyFieldValue(MedicaLebenCompanyDocs docs, String field) {
        return switch (field) {
            case "acta_constitutiva" -> docs.getActaConstitutiva();
            case "asamblea" -> docs.getAsamblea();
            case "constancia_situacion_fiscal" -> docs.getConstanciaSituacionFiscal();
            case "poder_notarial" -> docs.getPoderNotarial();
            case "identificacion_representante" -> docs.getIdentificacionRepresentante();
            case "comprobante_domicilio" -> docs.getComprobanteDomicilio();
            case "estado_cuenta_bancaria" -> docs.getEstadoCuentaBancaria();
            case "comprobante_ema_eba" -> docs.getComprobanteEmaEba();
            default -> null;
        };
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

    private String mapCompanyStatus(MedicaLebenCompanyDocs.DocumentStatus status) {
        if (status == null) {
            return "Pendiente";
        }
        return switch (status) {
            case APPROVED -> "Aprobado";
            case REJECTED -> "Rechazado";
            default -> "Pendiente";
        };
    }

    private String mapPhotoStatus(MedicaLebenCompanyWorkPhoto.PhotoStatus status) {
        if (status == null) {
            return "Pendiente";
        }
        return switch (status) {
            case APPROVED -> "Aprobado";
            case REJECTED -> "Rechazado";
            default -> "Pendiente";
        };
    }

    private String extractExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "PDF";
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == fileName.length() - 1) {
            return "PDF";
        }
        return fileName.substring(lastDot + 1).toUpperCase(Locale.ROOT);
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
