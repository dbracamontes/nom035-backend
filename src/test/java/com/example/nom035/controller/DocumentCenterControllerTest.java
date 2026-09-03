package com.example.nom035.controller;

import com.example.nom035.config.GlobalExceptionHandler;
import com.example.nom035.dto.DocumentCenterItemDto;
import com.example.nom035.entity.Company;
import com.example.nom035.entity.DocumentType;
import com.example.nom035.entity.Employee;
import com.example.nom035.entity.EmployeeDocs;
import com.example.nom035.entity.MedicaLebenCompanyDocs;
import com.example.nom035.entity.MedicaLebenCompanyWorkPhoto;
import com.example.nom035.repository.CompanyRepository;
import com.example.nom035.repository.EmployeeDocsRepository;
import com.example.nom035.repository.MedicaLebenCompanyDocsRepository;
import com.example.nom035.repository.MedicaLebenCompanyWorkPhotoRepository;
import com.example.nom035.service.CompanyService;
import com.example.nom035.service.EmployeeService;
import com.example.nom035.service.MedicaLebenStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentCenterControllerTest {

    @Mock
    private EmployeeDocsRepository employeeDocsRepository;

    @Mock
    private MedicaLebenCompanyDocsRepository medicaLebenCompanyDocsRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private MedicaLebenCompanyWorkPhotoRepository companyWorkPhotoRepository;

    @Mock
    private CompanyService companyService;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private MedicaLebenStorageService medicaLebenStorageService;

    @InjectMocks
    private DocumentCenterController controller;

    @Test
    void approveDocumentDecisionShouldPersistStatusAndExposeEmployeeId() {
        Employee employee = new Employee();
        employee.setId(10L);
        employee.setName("Ana");

        Company company = new Company();
        company.setId(7L);
        company.setName("Mi Empresa");
        employee.setCompany(company);

        DocumentType type = new DocumentType();
        type.setId(1L);
        type.setName("Identificación");

        EmployeeDocs doc = new EmployeeDocs();
        doc.setId(1L);
        doc.setName("INE");
        doc.setEmployee(employee);
        doc.setType(type);
        doc.setStatus(EmployeeDocs.DocumentStatus.PENDING);
        doc.setCreatedDate(LocalDateTime.now());
        doc.setFileName("ine.pdf");
        doc.setFilePath("/tmp/ine.pdf");

        when(employeeDocsRepository.findById(1L)).thenReturn(Optional.of(doc));
        when(employeeDocsRepository.save(any(EmployeeDocs.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<DocumentCenterItemDto> response = controller.decideDocument(1L, "APPROVED", "Documento aprobado correctamente.");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Aprobado", response.getBody().getStatus());
        assertEquals(10L, response.getBody().getEmployeeId());
        assertEquals(EmployeeDocs.DocumentStatus.APPROVED, doc.getStatus());
    }

    @Test
    void missingCompanyDecisionShouldDefaultToPending() {
        Company company = new Company();
        company.setId(25L);
        company.setName("Acme S.A.");

        MedicaLebenCompanyDocs docs = new MedicaLebenCompanyDocs();
        docs.setId(99L);
        docs.setCompany(company);
        docs.setActaConstitutiva("acta_constitutiva_escudo.pdf");
        docs.setStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);

        when(medicaLebenCompanyDocsRepository.findById(99L)).thenReturn(Optional.of(docs));
        when(medicaLebenCompanyDocsRepository.save(any(MedicaLebenCompanyDocs.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<DocumentCenterItemDto> response = controller.decideDocument(99001L, null, "");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Pendiente", response.getBody().getStatus());
        assertEquals(MedicaLebenCompanyDocs.DocumentStatus.PENDING, docs.getStatus());
    }

    @Test
    void companyDocumentDecisionShouldPersistStatusUsingSyntheticDecisionId() {
        Company company = new Company();
        company.setId(25L);
        company.setName("Acme S.A.");

        MedicaLebenCompanyDocs docs = new MedicaLebenCompanyDocs();
        docs.setId(99L);
        docs.setCompany(company);
        docs.setActaConstitutiva("acta_constitutiva_escudo.pdf");
        docs.setStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
        docs.setActaConstitutivaStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);

        when(medicaLebenCompanyDocsRepository.findById(99L)).thenReturn(Optional.of(docs));
        when(medicaLebenCompanyDocsRepository.save(any(MedicaLebenCompanyDocs.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<DocumentCenterItemDto> response = controller.decideDocument(99001L, "REJECTED", "Documento rechazado.");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Rechazado", response.getBody().getStatus());
        assertEquals(MedicaLebenCompanyDocs.DocumentStatus.REJECTED, docs.getActaConstitutivaStatus());
        assertEquals(MedicaLebenCompanyDocs.DocumentStatus.REJECTED, docs.getStatus());
    }

    @Test
    void companyBundleStatusShouldFollowFieldStateInsteadOfStaleAggregateStatus() {
        Company company = new Company();
        company.setId(25L);
        company.setName("Acme S.A.");

        MedicaLebenCompanyDocs docs = new MedicaLebenCompanyDocs();
        docs.setId(99L);
        docs.setCompany(company);
        docs.setActaConstitutiva("acta_constitutiva_escudo.pdf");
        docs.setAsamblea("asamblea_minuta.pdf");
        docs.setStatus(MedicaLebenCompanyDocs.DocumentStatus.APPROVED);
        docs.setActaConstitutivaStatus(MedicaLebenCompanyDocs.DocumentStatus.APPROVED);
        docs.setAsambleaStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);

        CompanyController controller = new CompanyController(companyService, employeeService, medicaLebenCompanyDocsRepository, medicaLebenStorageService);
        when(companyService.getCompanyById(25L)).thenReturn(Optional.of(company));
        when(medicaLebenCompanyDocsRepository.findByCompany(company)).thenReturn(Optional.of(docs));

        ResponseEntity<?> response = controller.getMedicaLebenDocs(25L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("PENDING", ((CompanyController.MedicaLebenCompanyDocsResponse) response.getBody()).getStatus());
    }

    @Test
    void uploadDocsShouldResetAllFieldStatusesToPendingOnResubmission() throws Exception {
        Company company = new Company();
        company.setId(25L);
        company.setName("Acme S.A.");
        company.setHasMedicaLebenDocs(true);

        MedicaLebenCompanyDocs existing = new MedicaLebenCompanyDocs();
        existing.setId(99L);
        existing.setCompany(company);
        existing.setStatus(MedicaLebenCompanyDocs.DocumentStatus.APPROVED);
        existing.setActaConstitutivaStatus(MedicaLebenCompanyDocs.DocumentStatus.APPROVED);
        existing.setAsambleaStatus(MedicaLebenCompanyDocs.DocumentStatus.REJECTED);
        existing.setConstanciaSituacionFiscalStatus(MedicaLebenCompanyDocs.DocumentStatus.APPROVED);
        existing.setPoderNotarialStatus(MedicaLebenCompanyDocs.DocumentStatus.REJECTED);
        existing.setIdentificacionRepresentanteStatus(MedicaLebenCompanyDocs.DocumentStatus.APPROVED);
        existing.setComprobanteDomicilioStatus(MedicaLebenCompanyDocs.DocumentStatus.REJECTED);
        existing.setEstadoCuentaBancariaStatus(MedicaLebenCompanyDocs.DocumentStatus.APPROVED);
        existing.setComprobanteEmaEbaStatus(MedicaLebenCompanyDocs.DocumentStatus.REJECTED);

        MockMultipartFile file = new MockMultipartFile(
                "acta_constitutiva",
                "acta.pdf",
                "application/pdf",
                "bytes".getBytes()
        );

        MedicaLebenStorageService storageService = new MedicaLebenStorageService(
                medicaLebenCompanyDocsRepository,
                companyWorkPhotoRepository,
                companyRepository
        );
        storageService.setBasePathForTesting("target/uploads/medica-leben");
        when(medicaLebenCompanyDocsRepository.findByCompany(company)).thenReturn(Optional.of(existing));
        when(medicaLebenCompanyDocsRepository.save(any(MedicaLebenCompanyDocs.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MedicaLebenCompanyDocs saved = storageService.uploadDocs(
                company,
                file,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertEquals(MedicaLebenCompanyDocs.DocumentStatus.PENDING, saved.getStatus());
        assertEquals(MedicaLebenCompanyDocs.DocumentStatus.PENDING, saved.getActaConstitutivaStatus());
        assertEquals(MedicaLebenCompanyDocs.DocumentStatus.PENDING, saved.getAsambleaStatus());
        assertEquals(MedicaLebenCompanyDocs.DocumentStatus.PENDING, saved.getConstanciaSituacionFiscalStatus());
        assertEquals(MedicaLebenCompanyDocs.DocumentStatus.PENDING, saved.getPoderNotarialStatus());
        assertEquals(MedicaLebenCompanyDocs.DocumentStatus.PENDING, saved.getIdentificacionRepresentanteStatus());
        assertEquals(MedicaLebenCompanyDocs.DocumentStatus.PENDING, saved.getComprobanteDomicilioStatus());
        assertEquals(MedicaLebenCompanyDocs.DocumentStatus.PENDING, saved.getEstadoCuentaBancariaStatus());
        assertEquals(MedicaLebenCompanyDocs.DocumentStatus.PENDING, saved.getComprobanteEmaEbaStatus());
    }

    @Test
    void companyDocumentDecisionShouldNotOverwriteOtherFieldsWithSameCompanyBundle() {
        Company company = new Company();
        company.setId(25L);
        company.setName("Acme S.A.");

        MedicaLebenCompanyDocs docs = new MedicaLebenCompanyDocs();
        docs.setId(99L);
        docs.setCompany(company);
        docs.setActaConstitutiva("acta_constitutiva_escudo.pdf");
        docs.setAsamblea("asamblea_minuta.pdf");
        docs.setStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
        docs.setActaConstitutivaStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
        docs.setAsambleaStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);

        when(medicaLebenCompanyDocsRepository.findById(99L)).thenReturn(Optional.of(docs));
        when(medicaLebenCompanyDocsRepository.save(any(MedicaLebenCompanyDocs.class))).thenAnswer(invocation -> invocation.getArgument(0));

        controller.decideDocument(99001L, "REJECTED", "Documento rechazado.");

        assertEquals(MedicaLebenCompanyDocs.DocumentStatus.REJECTED, docs.getActaConstitutivaStatus());
        assertEquals(MedicaLebenCompanyDocs.DocumentStatus.PENDING, docs.getAsambleaStatus());
        assertEquals(MedicaLebenCompanyDocs.DocumentStatus.REJECTED, docs.getStatus());
    }

    @Test
    void getDocumentsCenterShouldExcludeDocumentsWithoutAttachedFile() {
        Employee employee = new Employee();
        employee.setId(2L);
        employee.setName("Luis");

        Company company = new Company();
        company.setId(5L);
        company.setName("Empresa Test");
        employee.setCompany(company);

        DocumentType type = new DocumentType();
        type.setId(3L);
        type.setName("Comprobante");

        EmployeeDocs visibleDoc = new EmployeeDocs();
        visibleDoc.setId(11L);
        visibleDoc.setName("Comprobante activo");
        visibleDoc.setEmployee(employee);
        visibleDoc.setType(type);
        visibleDoc.setStatus(EmployeeDocs.DocumentStatus.PENDING);
        visibleDoc.setCreatedDate(LocalDateTime.now());
        visibleDoc.setFileName("comprobante.pdf");
        visibleDoc.setFilePath("/tmp/comprobante.pdf");

        EmployeeDocs deletedDoc = new EmployeeDocs();
        deletedDoc.setId(12L);
        deletedDoc.setName("Comprobante borrado");
        deletedDoc.setEmployee(employee);
        deletedDoc.setType(type);
        deletedDoc.setStatus(EmployeeDocs.DocumentStatus.PENDING);
        deletedDoc.setCreatedDate(LocalDateTime.now());
        deletedDoc.setFileName(null);
        deletedDoc.setFilePath(null);

        when(employeeDocsRepository.findAll()).thenReturn(List.of(visibleDoc, deletedDoc));
        when(medicaLebenCompanyDocsRepository.findAll()).thenReturn(List.of());

        ResponseEntity<List<DocumentCenterItemDto>> response = controller.getDocumentsCenter();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(11L, response.getBody().get(0).getId());
    }

    @Test
    void getDocumentsCenterShouldIncludeCompanyDocsForApprovalReview() {
        Company company = new Company();
        company.setId(25L);
        company.setName("Acme S.A.");

        MedicaLebenCompanyDocs docs = new MedicaLebenCompanyDocs();
        docs.setId(99L);
        docs.setCompany(company);
        docs.setActaConstitutiva("acta_constitutiva_escudo.pdf");
        docs.setAsamblea("asamblea_minuta.pdf");

        when(employeeDocsRepository.findAll()).thenReturn(List.of());
        when(medicaLebenCompanyDocsRepository.findAll()).thenReturn(List.of(docs));

        ResponseEntity<List<DocumentCenterItemDto>> response = controller.getDocumentsCenter();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().size());
        assertEquals("Acta constitutiva", response.getBody().get(0).getTitle());
        assertEquals("COMPANY_DOC", response.getBody().get(0).getSource());
        assertEquals(25L, response.getBody().get(0).getCompanyId());
    }

    @Test
    void getDocumentsCenterShouldIncludeWorkAreaPhotosForApprovalReview() {
        Company company = new Company();
        company.setId(25L);
        company.setName("Acme S.A.");

        MedicaLebenCompanyDocs docs = new MedicaLebenCompanyDocs();
        docs.setId(99L);
        docs.setCompany(company);

        MedicaLebenCompanyWorkPhoto photo = new MedicaLebenCompanyWorkPhoto();
        photo.setId(7L);
        photo.setCompanyDocs(docs);
        photo.setUrl("foto_area_trabajo_1.jpg");
        photo.setDescription("Área de trabajo");
        photo.setSortOrder(1);
        photo.setStatus(MedicaLebenCompanyWorkPhoto.PhotoStatus.PENDING);

        docs.setWorkPhotos(List.of(photo));

        when(employeeDocsRepository.findAll()).thenReturn(List.of());
        when(medicaLebenCompanyDocsRepository.findAll()).thenReturn(List.of(docs));

        ResponseEntity<List<DocumentCenterItemDto>> response = controller.getDocumentsCenter();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().stream().filter(item -> "PHOTO".equals(item.getSource())).count());
        assertEquals("I.- Fotos del área en donde se encuentran realizando las actividades los trabajadores.", response.getBody().get(0).getTitle());
        assertEquals("Acme S.A.", response.getBody().get(0).getOwner());
    }

    @Test
    void uploadDocsShouldRejectBlankPayloadsWithoutCreatingEmptyRecord() {
        Company company = new Company();
        company.setId(7L);
        company.setName("Empresa sin documentos");

        com.example.nom035.service.MedicaLebenStorageService service =
                new com.example.nom035.service.MedicaLebenStorageService(
                        medicaLebenCompanyDocsRepository,
                        org.mockito.Mockito.mock(com.example.nom035.repository.MedicaLebenCompanyWorkPhotoRepository.class),
                        org.mockito.Mockito.mock(com.example.nom035.repository.CompanyRepository.class)
                );

        org.springframework.test.util.ReflectionTestUtils.setField(service, "basePath", "/tmp/nom035-test");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.uploadDocs(
                        company,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );

        assertEquals("No se seleccionó ningún documento para guardar.", ex.getMessage());
    }

    @Test
    void storageFolderShouldAlwaysMatchCompanyIdPathUsedByFileController() {
        Company company = new Company();
        company.setId(42L);
        company.setTaxId("ABC-123");

        com.example.nom035.service.MedicaLebenStorageService service =
                new com.example.nom035.service.MedicaLebenStorageService(
                        medicaLebenCompanyDocsRepository,
                        org.mockito.Mockito.mock(com.example.nom035.repository.MedicaLebenCompanyWorkPhotoRepository.class),
                        org.mockito.Mockito.mock(com.example.nom035.repository.CompanyRepository.class)
                );

        String folderName = (String) org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                service,
                "resolveCompanyFolderName",
                company
        );

        assertEquals("company-42", folderName);
    }

    @Test
    void storageBasePathShouldResolveAgainstProjectDirectoryWhenRelative() {
        com.example.nom035.service.MedicaLebenStorageService service =
                new com.example.nom035.service.MedicaLebenStorageService(
                        medicaLebenCompanyDocsRepository,
                        org.mockito.Mockito.mock(com.example.nom035.repository.MedicaLebenCompanyWorkPhotoRepository.class),
                        org.mockito.Mockito.mock(com.example.nom035.repository.CompanyRepository.class)
                );

        org.springframework.test.util.ReflectionTestUtils.setField(service, "basePath", "uploads/medica-leben");

        java.nio.file.Path resolved = (java.nio.file.Path) org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                service,
                "resolveBaseDirectory"
        );

        assertEquals(java.nio.file.Path.of(System.getProperty("user.dir"), "uploads/medica-leben").normalize(), resolved.normalize());
    }

    @Test
    void validationExceptionsShouldReturnBadRequestInsteadOfInternalServerError() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        org.springframework.web.context.request.WebRequest request = org.mockito.Mockito.mock(org.springframework.web.context.request.WebRequest.class);

        ResponseEntity<String> response = handler.handleIllegalArgumentException(
                new IllegalArgumentException("No se seleccionó ningún documento para guardar."),
                request
        );

        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertEquals("Error de validación: No se seleccionó ningún documento para guardar.", response.getBody());
    }
}
