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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @Autowired
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
        if (!companyService.getCompanyById(id).isPresent()) {
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
        if (!companyService.getCompanyById(id).isPresent()) {
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
    public ResponseEntity<MedicaLebenCompanyDocs> getMedicaLebenDocs(@PathVariable Long companyId) {
        Company company = companyService.getCompanyById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        return mlDocsRepository.findByCompany(company)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(path = "/{companyId}/medica-leben/docs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MedicaLebenCompanyDocs> uploadMedicaLebenDocs(
            @PathVariable Long companyId,
            @RequestPart(name = "acta_constitutiva", required = false) MultipartFile actaConstitutiva,
            @RequestPart(name = "constancia_situacion_fiscal", required = false) MultipartFile constanciaSituacionFiscal,
            @RequestPart(name = "poder_notarial", required = false) MultipartFile poderNotarial,
            @RequestPart(name = "identificacion_representante", required = false) MultipartFile identificacionRepresentante,
            @RequestPart(name = "comprobante_domicilio", required = false) MultipartFile comprobanteDomicilio,
            @RequestPart(name = "estado_cuenta_bancaria", required = false) MultipartFile estadoCuentaBancaria,
            @RequestPart(name = "comprobante_ema_eba", required = false) MultipartFile comprobanteEmaEba
    ) throws Exception {
        Company company = companyService.getCompanyById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        MedicaLebenCompanyDocs docs = mlStorageService.uploadDocs(
                company,
                actaConstitutiva,
                constanciaSituacionFiscal,
                poderNotarial,
                identificacionRepresentante,
                comprobanteDomicilio,
                estadoCuentaBancaria,
                comprobanteEmaEba
        );
        return ResponseEntity.ok(docs);
    }

    @PostMapping(path = "/{companyId}/medica-leben/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MedicaLebenCompanyWorkPhoto> uploadMedicaLebenPhoto(
            @PathVariable Long companyId,
            @RequestPart("photo") MultipartFile photo,
            @RequestPart(name = "description", required = false) String description,
            @RequestPart(name = "sortOrder", required = false) Integer sortOrder
    ) throws Exception {
        Company company = companyService.getCompanyById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        MedicaLebenCompanyDocs docs = mlDocsRepository.findByCompany(company)
                .orElseThrow(() -> new RuntimeException("Medica LEBEN docs not initialized for company"));

        int order = (sortOrder != null) ? sortOrder : 0;
        MedicaLebenCompanyWorkPhoto saved = mlStorageService.uploadPhoto(docs, photo, description, order);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{companyId}/medica-leben/photos")
    public ResponseEntity<List<MedicaLebenCompanyWorkPhoto>> listMedicaLebenPhotos(@PathVariable Long companyId) {
        Company company = companyService.getCompanyById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        MedicaLebenCompanyDocs docs = mlDocsRepository.findByCompany(company)
                .orElseThrow(() -> new RuntimeException("Medica LEBEN docs not initialized for company"));

        List<MedicaLebenCompanyWorkPhoto> photos = mlStorageService.listPhotos(docs);
        return ResponseEntity.ok(photos);
    }
}