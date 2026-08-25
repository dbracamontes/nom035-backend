package com.example.nom035.service.impl;

import com.example.nom035.entity.Employee;
import com.example.nom035.entity.EmployeeDocs;
import com.example.nom035.repository.DocumentTypeRepository;
import com.example.nom035.repository.EmployeeDocsRepository;
import com.example.nom035.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeDocsServiceImplTest {

    @Mock
    private EmployeeDocsRepository employeeDocsRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DocumentTypeRepository documentTypeRepository;

    @InjectMocks
    private EmployeeDocsServiceImpl service;

    @TempDir
    Path tempDirectory;

    @Test
    void deleteFileShouldClearMetadataAndResetDocumentForNewUpload() throws IOException {
        long employeeId = 10L;
        long documentId = 20L;
        Path storedFile = Files.writeString(tempDirectory.resolve("document.pdf"), "test");

        Employee employee = new Employee();
        employee.setId(employeeId);

        EmployeeDocs document = new EmployeeDocs();
        document.setId(documentId);
        document.setEmployee(employee);
        document.setFileName("document.pdf");
        document.setContentType("application/pdf");
        document.setFileSize(Files.size(storedFile));
        document.setFilePath(storedFile.toString());
        document.setStatus(EmployeeDocs.DocumentStatus.APPROVED);
        document.setDeactivatedDate(LocalDateTime.now());

        when(employeeDocsRepository.findById(documentId)).thenReturn(Optional.of(document));

        service.deleteFile(employeeId, documentId);

        assertFalse(Files.exists(storedFile));
        assertNull(document.getFileName());
        assertNull(document.getContentType());
        assertNull(document.getFileSize());
        assertNull(document.getFilePath());
        assertEquals(EmployeeDocs.DocumentStatus.PENDING, document.getStatus());
        assertNull(document.getDeactivatedDate());
        verify(employeeDocsRepository).save(document);
    }
}
