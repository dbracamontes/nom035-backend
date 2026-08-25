package com.example.nom035.controller;

import com.example.nom035.dto.DocumentCenterItemDto;
import com.example.nom035.entity.Company;
import com.example.nom035.entity.DocumentType;
import com.example.nom035.entity.Employee;
import com.example.nom035.entity.EmployeeDocs;
import com.example.nom035.repository.EmployeeDocsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentCenterControllerTest {

    @Mock
    private EmployeeDocsRepository employeeDocsRepository;

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

        ResponseEntity<List<DocumentCenterItemDto>> response = controller.getDocumentsCenter();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(11L, response.getBody().get(0).getId());
    }
}
