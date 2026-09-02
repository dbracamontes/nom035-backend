package com.example.nom035.controller;

import com.example.nom035.entity.Company;
import com.example.nom035.entity.Employee;
import com.example.nom035.entity.MedicaLebenCompanyDocs;
import com.example.nom035.entity.MedicaLebenCompanyWorkPhoto;
import com.example.nom035.repository.MedicaLebenCompanyDocsRepository;
import com.example.nom035.service.CompanyService;
import com.example.nom035.service.EmployeeService;
import com.example.nom035.service.MedicaLebenStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Controller para operaciones de companies.
 *
 * IMPORTANTE:
 * - Ajusta los imports y nombres de servicio (CompanyService, EmployeeService)
 *   si en tu proyecto tienen otro paquete / nombre.
 */
@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final Logger log = LoggerFactory.getLogger(CompanyController.class);

    private final CompanyService companyService;
    private final EmployeeService employeeService;
    private final MedicaLebenCompanyDocsRepository mlDocsRepository;
    private final MedicaLebenStorageService mlStorageService;

    @Value("${app.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

    public CompanyController(CompanyService companyService, EmployeeService employeeService,
                             MedicaLebenCompanyDocsRepository mlDocsRepository,
                             MedicaLebenStorageService mlStorageService) {
        this.companyService = companyService;
        this.employeeService = employeeService;
        this.mlDocsRepository = mlDocsRepository;
        this.mlStorageService = mlStorageService;
    }

    // Helpers para respuestas de error de validación
    private ResponseEntity<?> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(java.util.Map.of(
                        "error", "VALIDATION_ERROR",
                        "message", message
                ));
    }

    private String validateCompany(Company company) {
        if (company.getName() == null || company.getName().trim().isEmpty()) {
            return "El nombre de la empresa es obligatorio.";
        }
        if (company.getName().length() > 150) {
            return "El nombre de la empresa no puede exceder 150 caracteres.";
        }
        if (company.getTaxId() == null || company.getTaxId().trim().isEmpty()) {
            return "El RFC/Tax ID es obligatorio.";
        }
        if (company.getTaxId().length() > 20) {
            return "El RFC/Tax ID no puede exceder 20 caracteres.";
        }
        return null;
    }

    private String resolveCompanyConstraintMessage(DataIntegrityViolationException ex) {
        String lower = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage().toLowerCase()
                : ex.getMessage().toLowerCase();

        if (lower.contains("uq_company_name")) {
            return "El nombre de la empresa ya está registrado.";
        }
        if (lower.contains("uq_company_tax_id")) {
            return "El RFC/Tax ID ya está registrado.";
        }
        if (lower.contains("tax_id") && lower.contains("null")) {
            return "El RFC/Tax ID es obligatorio.";
        }
        return "No se pudo guardar la empresa por un error de datos (nombre/RFC).";
    }

    /**
     * Obtener todas las companies.
     * Permitido a ROLE_COMPANY y ROLE_ADMIN.
     */
    @GetMapping
    @Secured({"ROLE_COMPANY", "ROLE_ADMIN"})
    public ResponseEntity<List<Company>> getAllCompanies() {
        log.debug("Request to get all companies");
        List<Company> list = companyService.getAllCompanies();
        return ResponseEntity.ok(list);
    }

    /**
     * Obtener una company por ID.
     * Permitido a ROLE_COMPANY y ROLE_ADMIN.
     */
    @GetMapping("/{id}")
    @Secured({"ROLE_COMPANY", "ROLE_ADMIN"})
    public ResponseEntity<Company> getCompanyById(@PathVariable Long id) {
        log.debug("Request to get company with id={}", id);
        return companyService.getCompanyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crear una nueva company.
     * Permitido solo a ROLE_ADMIN.
     */
    @PostMapping
    @Secured("ROLE_ADMIN")
    public ResponseEntity<?> createCompany(@RequestBody Company company) {
        log.debug("Request to create company: {}", company.getName());
        if (company.getId() != null) {
            return badRequest("No se debe enviar ID al crear una empresa nueva.");
        }
        String validationError = validateCompany(company);
        if (validationError != null) {
            return badRequest(validationError);
        }
        try {
            Company saved = companyService.saveCompany(company);
            return ResponseEntity.ok(saved);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Data integrity violation when creating company", ex);
            String msg = resolveCompanyConstraintMessage(ex);
            return badRequest(msg);
        }
    }

    /**
     * Actualizar una company existente.
     * Permitido solo a ROLE_ADMIN.
     */
    @PutMapping("/{id}")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<?> updateCompany(@PathVariable Long id, @RequestBody Company company) {
        log.debug("Request to update company with id={}", id);
        if (companyService.getCompanyById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        company.setId(id);
        String validationError = validateCompany(company);
        if (validationError != null) {
            return badRequest(validationError);
        }
        try {
            Company updated = companyService.saveCompany(company);
            return ResponseEntity.ok(updated);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Data integrity violation when updating company", ex);
            String msg = resolveCompanyConstraintMessage(ex);
            return badRequest(msg);
        }
    }

    /**
     * Eliminar una company.
     * Permitido solo a ROLE_ADMIN.
     */
    @DeleteMapping("/{id}")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        log.debug("Request to delete company with id={}", id);
        if (companyService.getCompanyById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtener empleados de una company por id.
     * Permitido a ROLE_COMPANY y ROLE_ADMIN.
     */
    @GetMapping("/{companyId}/employees")
    @Secured({"ROLE_COMPANY", "ROLE_ADMIN"})
    public ResponseEntity<List<Employee>> getCompanyEmployees(@PathVariable Long companyId) {
        log.debug("Request to get employees for companyId={}", companyId);
        List<Employee> employees = employeeService.getEmployeesByCompanyId(companyId);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{companyId}/medica-leben/docs")
    public ResponseEntity<MedicaLebenCompanyDocsResponse> getMedicaLebenDocs(@PathVariable Long companyId) {
        Company company = companyService.getCompanyById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        return mlDocsRepository.findByCompany(company)
                .map(docs -> ResponseEntity.ok(toDocsResponse(companyId, docs)))
                .orElseGet(() -> ResponseEntity.ok(new MedicaLebenCompanyDocsResponse()));
    }

    @PostMapping(path = "/{companyId}/medica-leben/docs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MedicaLebenCompanyDocs> uploadMedicaLebenDocs(
            @PathVariable Long companyId,
            @RequestParam(name = "acta_constitutiva", required = false) MultipartFile actaConstitutiva,
            @RequestParam(name = "asamblea", required = false) MultipartFile asamblea,
            @RequestParam(name = "constancia_situacion_fiscal", required = false) MultipartFile constanciaSituacionFiscal,
            @RequestParam(name = "poder_notarial", required = false) MultipartFile poderNotarial,
            @RequestParam(name = "identificacion_representante", required = false) MultipartFile identificacionRepresentante,
            @RequestParam(name = "comprobante_domicilio", required = false) MultipartFile comprobanteDomicilio,
            @RequestParam(name = "estado_cuenta_bancaria", required = false) MultipartFile estadoCuentaBancaria,
            @RequestParam(name = "comprobante_ema_eba", required = false) MultipartFile comprobanteEmaEba,
            @RequestParam(name = "actaConstitutiva", required = false) MultipartFile legacyActaConstitutiva,
            @RequestParam(name = "constanciaSituacionFiscal", required = false) MultipartFile legacyConstanciaSituacionFiscal,
            @RequestParam(name = "poderNotarial", required = false) MultipartFile legacyPoderNotarial,
            @RequestParam(name = "identificacionRepresentante", required = false) MultipartFile legacyIdentificacionRepresentante,
            @RequestParam(name = "comprobanteDomicilio", required = false) MultipartFile legacyComprobanteDomicilio,
            @RequestParam(name = "estadoCuentaBancaria", required = false) MultipartFile legacyEstadoCuentaBancaria,
            @RequestParam(name = "comprobanteEmaEba", required = false) MultipartFile legacyComprobanteEmaEba
    ) throws Exception {
        Company company = companyService.getCompanyById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        MedicaLebenCompanyDocs docs = mlStorageService.uploadDocs(
                company,
                actaConstitutiva != null && !actaConstitutiva.isEmpty() ? actaConstitutiva : legacyActaConstitutiva,
                asamblea,
                constanciaSituacionFiscal != null && !constanciaSituacionFiscal.isEmpty() ? constanciaSituacionFiscal : legacyConstanciaSituacionFiscal,
                poderNotarial != null && !poderNotarial.isEmpty() ? poderNotarial : legacyPoderNotarial,
                identificacionRepresentante != null && !identificacionRepresentante.isEmpty() ? identificacionRepresentante : legacyIdentificacionRepresentante,
                comprobanteDomicilio != null && !comprobanteDomicilio.isEmpty() ? comprobanteDomicilio : legacyComprobanteDomicilio,
                estadoCuentaBancaria != null && !estadoCuentaBancaria.isEmpty() ? estadoCuentaBancaria : legacyEstadoCuentaBancaria,
                comprobanteEmaEba != null && !comprobanteEmaEba.isEmpty() ? comprobanteEmaEba : legacyComprobanteEmaEba
        );
        return ResponseEntity.ok(docs);
    }

    @PostMapping(path = "/{companyId}/medica-leben/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MedicaLebenCompanyWorkPhoto> uploadMedicaLebenPhoto(
            @PathVariable Long companyId,
            @RequestPart("photo") MultipartFile photo,
            @RequestParam(name = "description", required = false) String description,
            @RequestParam(name = "sortOrder", required = false) Integer sortOrder
    ) throws Exception {
        Company company = companyService.getCompanyById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        MedicaLebenCompanyDocs docs = mlDocsRepository.findByCompany(company)
                .orElseGet(() -> {
                    MedicaLebenCompanyDocs created = MedicaLebenCompanyDocs.builder()
                            .company(company)
                            .status(MedicaLebenCompanyDocs.DocumentStatus.PENDING)
                            .build();
                    return mlDocsRepository.save(created);
                });

        int order = (sortOrder != null) ? sortOrder : 0;
        MedicaLebenCompanyWorkPhoto saved = mlStorageService.uploadPhoto(docs, photo, description, order);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{companyId}/medica-leben/photos")
    public ResponseEntity<List<MedicaLebenCompanyWorkPhoto>> listMedicaLebenPhotos(@PathVariable Long companyId) {
        Company company = companyService.getCompanyById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        MedicaLebenCompanyDocs docs = mlDocsRepository.findByCompany(company).orElse(null);
        if (docs == null) {
            return ResponseEntity.ok(List.of());
        }

        List<MedicaLebenCompanyWorkPhoto> photos = mlStorageService.listPhotos(docs);
        return ResponseEntity.ok(photos);
    }

    @DeleteMapping("/{companyId}/medica-leben/docs/{field}")
    public ResponseEntity<?> deleteMedicaLebenDoc(@PathVariable Long companyId,
                                                  @PathVariable String field) {
        try {
            Company company = companyService.getCompanyById(companyId)
                    .orElseThrow(() -> new RuntimeException("Company not found"));

            // Accept both camelCase (frontend DTO) and snake_case (storage) names
            String normalized = field.trim();
            String snake;
            switch (normalized) {
                case "actaConstitutiva", "acta_constitutiva" -> snake = "acta_constitutiva";
                case "asamblea" -> snake = "asamblea";
                case "constanciaSituacionFiscal", "constancia_situacion_fiscal" -> snake = "constancia_situacion_fiscal";
                case "poderNotarial", "poder_notarial" -> snake = "poder_notarial";
                case "identificacionRepresentante", "identificacion_representante" -> snake = "identificacion_representante";
                case "comprobanteDomicilio", "comprobante_domicilio" -> snake = "comprobante_domicilio";
                case "estadoCuentaBancaria", "estado_cuenta_bancaria" -> snake = "estado_cuenta_bancaria";
                case "comprobanteEmaEba", "comprobante_ema_eba" -> snake = "comprobante_ema_eba";
                default -> throw new IllegalArgumentException("Unknown document field: " + field);
            }

            MedicaLebenCompanyDocs updated = mlStorageService.deleteDoc(company, snake);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of(
                            "error", "INVALID_FIELD",
                            "message", ex.getMessage()
                    ));
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of(
                            "error", "IO_ERROR",
                            "message", "No se pudo eliminar el archivo físico"
                    ));
        }
    }

    @DeleteMapping("/{companyId}/medica-leben/photos/{photoId}")
    public ResponseEntity<?> deleteMedicaLebenPhoto(@PathVariable Long companyId,
                                                    @PathVariable Long photoId) {
        try {
            // Validate that company exists to avoid deleting from wrong context
            companyService.getCompanyById(companyId)
                    .orElseThrow(() -> new RuntimeException("Company not found"));

            mlStorageService.deletePhotoById(photoId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of(
                            "error", "NOT_FOUND",
                            "message", ex.getMessage()
                    ));
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of(
                            "error", "IO_ERROR",
                            "message", "No se pudo eliminar el archivo físico"
                    ));
        }
    }

    private MedicaLebenCompanyDocsResponse toDocsResponse(Long companyId, MedicaLebenCompanyDocs docs) {
        MedicaLebenCompanyDocsResponse dto = new MedicaLebenCompanyDocsResponse();
        dto.setId(docs.getId());
        dto.setCompanyId(companyId);

        String aggregateStatus = resolveCompanyBundleStatus(docs);
        dto.setStatus(aggregateStatus);
        dto.setActaConstitutivaStatus(docs.getActaConstitutivaStatus() != null ? docs.getActaConstitutivaStatus().name() : MedicaLebenCompanyDocs.DocumentStatus.PENDING.name());
        dto.setAsambleaStatus(docs.getAsambleaStatus() != null ? docs.getAsambleaStatus().name() : MedicaLebenCompanyDocs.DocumentStatus.PENDING.name());
        dto.setConstanciaSituacionFiscalStatus(docs.getConstanciaSituacionFiscalStatus() != null ? docs.getConstanciaSituacionFiscalStatus().name() : MedicaLebenCompanyDocs.DocumentStatus.PENDING.name());
        dto.setPoderNotarialStatus(docs.getPoderNotarialStatus() != null ? docs.getPoderNotarialStatus().name() : MedicaLebenCompanyDocs.DocumentStatus.PENDING.name());
        dto.setIdentificacionRepresentanteStatus(docs.getIdentificacionRepresentanteStatus() != null ? docs.getIdentificacionRepresentanteStatus().name() : MedicaLebenCompanyDocs.DocumentStatus.PENDING.name());
        dto.setComprobanteDomicilioStatus(docs.getComprobanteDomicilioStatus() != null ? docs.getComprobanteDomicilioStatus().name() : MedicaLebenCompanyDocs.DocumentStatus.PENDING.name());
        dto.setEstadoCuentaBancariaStatus(docs.getEstadoCuentaBancariaStatus() != null ? docs.getEstadoCuentaBancariaStatus().name() : MedicaLebenCompanyDocs.DocumentStatus.PENDING.name());
        dto.setComprobanteEmaEbaStatus(docs.getComprobanteEmaEbaStatus() != null ? docs.getComprobanteEmaEbaStatus().name() : MedicaLebenCompanyDocs.DocumentStatus.PENDING.name());

        dto.setActaConstitutiva(buildUrl(companyId, docs.getActaConstitutiva()));
        dto.setAsamblea(buildUrl(companyId, docs.getAsamblea()));
        dto.setConstanciaSituacionFiscal(buildUrl(companyId, docs.getConstanciaSituacionFiscal()));
        dto.setPoderNotarial(buildUrl(companyId, docs.getPoderNotarial()));
        dto.setIdentificacionRepresentante(buildUrl(companyId, docs.getIdentificacionRepresentante()));
        dto.setComprobanteDomicilio(buildUrl(companyId, docs.getComprobanteDomicilio()));
        dto.setEstadoCuentaBancaria(buildUrl(companyId, docs.getEstadoCuentaBancaria()));
        dto.setComprobanteEmaEba(buildUrl(companyId, docs.getComprobanteEmaEba()));

        return dto;
    }

    private String resolveCompanyBundleStatus(MedicaLebenCompanyDocs docs) {
        if (docs == null) {
            return MedicaLebenCompanyDocs.DocumentStatus.PENDING.name();
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
            return docs.getStatus() != null ? docs.getStatus().name() : MedicaLebenCompanyDocs.DocumentStatus.PENDING.name();
        }
        if (hasRejected) {
            return MedicaLebenCompanyDocs.DocumentStatus.REJECTED.name();
        }
        if (hasPending && !hasApproved) {
            return MedicaLebenCompanyDocs.DocumentStatus.PENDING.name();
        }
        if (hasApproved && !hasPending) {
            return MedicaLebenCompanyDocs.DocumentStatus.APPROVED.name();
        }
        if (hasApproved && hasPending) {
            return MedicaLebenCompanyDocs.DocumentStatus.PENDING.name();
        }
        return MedicaLebenCompanyDocs.DocumentStatus.PENDING.name();
    }

    private String buildUrl(Long companyId, String filename) {
        if (filename == null || filename.isBlank()) {
            return null;
        }
        String base = publicBaseUrl != null ? publicBaseUrl.trim() : "";
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/api/medica-leben/companies/" + companyId + "/docs/" + filename;
    }

    public static class MedicaLebenCompanyDocsResponse {
        private Long id;
        private Long companyId;
        private String status;
        private String actaConstitutivaStatus;
        private String asambleaStatus;
        private String constanciaSituacionFiscalStatus;
        private String poderNotarialStatus;
        private String identificacionRepresentanteStatus;
        private String comprobanteDomicilioStatus;
        private String estadoCuentaBancariaStatus;
        private String comprobanteEmaEbaStatus;
        private String actaConstitutiva;
        private String asamblea;
        private String constanciaSituacionFiscal;
        private String poderNotarial;
        private String identificacionRepresentante;
        private String comprobanteDomicilio;
        private String estadoCuentaBancaria;
        private String comprobanteEmaEba;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getCompanyId() { return companyId; }
        public void setCompanyId(Long companyId) { this.companyId = companyId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getActaConstitutivaStatus() { return actaConstitutivaStatus; }
        public void setActaConstitutivaStatus(String actaConstitutivaStatus) { this.actaConstitutivaStatus = actaConstitutivaStatus; }

        public String getAsambleaStatus() { return asambleaStatus; }
        public void setAsambleaStatus(String asambleaStatus) { this.asambleaStatus = asambleaStatus; }

        public String getConstanciaSituacionFiscalStatus() { return constanciaSituacionFiscalStatus; }
        public void setConstanciaSituacionFiscalStatus(String constanciaSituacionFiscalStatus) { this.constanciaSituacionFiscalStatus = constanciaSituacionFiscalStatus; }

        public String getPoderNotarialStatus() { return poderNotarialStatus; }
        public void setPoderNotarialStatus(String poderNotarialStatus) { this.poderNotarialStatus = poderNotarialStatus; }

        public String getIdentificacionRepresentanteStatus() { return identificacionRepresentanteStatus; }
        public void setIdentificacionRepresentanteStatus(String identificacionRepresentanteStatus) { this.identificacionRepresentanteStatus = identificacionRepresentanteStatus; }

        public String getComprobanteDomicilioStatus() { return comprobanteDomicilioStatus; }
        public void setComprobanteDomicilioStatus(String comprobanteDomicilioStatus) { this.comprobanteDomicilioStatus = comprobanteDomicilioStatus; }

        public String getEstadoCuentaBancariaStatus() { return estadoCuentaBancariaStatus; }
        public void setEstadoCuentaBancariaStatus(String estadoCuentaBancariaStatus) { this.estadoCuentaBancariaStatus = estadoCuentaBancariaStatus; }

        public String getComprobanteEmaEbaStatus() { return comprobanteEmaEbaStatus; }
        public void setComprobanteEmaEbaStatus(String comprobanteEmaEbaStatus) { this.comprobanteEmaEbaStatus = comprobanteEmaEbaStatus; }

        public String getActaConstitutiva() { return actaConstitutiva; }
        public void setActaConstitutiva(String actaConstitutiva) { this.actaConstitutiva = actaConstitutiva; }

        public String getAsamblea() { return asamblea; }
        public void setAsamblea(String asamblea) { this.asamblea = asamblea; }

        public String getConstanciaSituacionFiscal() { return constanciaSituacionFiscal; }
        public void setConstanciaSituacionFiscal(String constanciaSituacionFiscal) { this.constanciaSituacionFiscal = constanciaSituacionFiscal; }

        public String getPoderNotarial() { return poderNotarial; }
        public void setPoderNotarial(String poderNotarial) { this.poderNotarial = poderNotarial; }

        public String getIdentificacionRepresentante() { return identificacionRepresentante; }
        public void setIdentificacionRepresentante(String identificacionRepresentante) { this.identificacionRepresentante = identificacionRepresentante; }

        public String getComprobanteDomicilio() { return comprobanteDomicilio; }
        public void setComprobanteDomicilio(String comprobanteDomicilio) { this.comprobanteDomicilio = comprobanteDomicilio; }

        public String getEstadoCuentaBancaria() { return estadoCuentaBancaria; }
        public void setEstadoCuentaBancaria(String estadoCuentaBancaria) { this.estadoCuentaBancaria = estadoCuentaBancaria; }

        public String getComprobanteEmaEba() { return comprobanteEmaEba; }
        public void setComprobanteEmaEba(String comprobanteEmaEba) { this.comprobanteEmaEba = comprobanteEmaEba; }
    }
}