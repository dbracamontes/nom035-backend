package com.example.nom035.controller;

import com.example.nom035.dto.EmployeeDocsCreateDto;
import com.example.nom035.dto.EmployeeDocsDto;
import com.example.nom035.dto.EmployeeDocsUpdateDto;
import com.example.nom035.service.EmployeeDocsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/employees/{employeeId}/documents")
@Secured({"ROLE_ADMIN", "ROLE_COMPANY"})
public class EmployeeDocsController {

    private final EmployeeDocsService employeeDocsService;

    public EmployeeDocsController(EmployeeDocsService employeeDocsService) {
        this.employeeDocsService = employeeDocsService;
    }

    // 1) GET /employees/{employeeId}/documents
    @GetMapping
    public ResponseEntity<List<EmployeeDocsDto>> getDocumentsByEmployee(@PathVariable Long employeeId) {
        List<EmployeeDocsDto> docs = employeeDocsService.listByEmployee(employeeId);
        return ResponseEntity.ok(docs);
    }

    // 2) GET /employees/{employeeId}/documents/{docId}
    @GetMapping("/{docId}")
    public ResponseEntity<EmployeeDocsDto> getDocumentById(@PathVariable Long employeeId,
                                                           @PathVariable Long docId) {
        EmployeeDocsDto dto = employeeDocsService.getById(docId);
        if (dto.getEmployeeId() == null || !dto.getEmployeeId().equals(employeeId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(dto);
    }

    // 3) POST /employees/{employeeId}/documents
    @PostMapping
    public ResponseEntity<EmployeeDocsDto> createDocument(@PathVariable Long employeeId,
                                                          @RequestBody EmployeeDocsCreateDto createDto) {
        createDto.setEmployeeId(employeeId);
        EmployeeDocsDto created = employeeDocsService.create(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 4) PUT /employees/{employeeId}/documents/{docId}
    @PutMapping("/{docId}")
    public ResponseEntity<EmployeeDocsDto> updateDocument(@PathVariable Long employeeId,
                                                          @PathVariable Long docId,
                                                          @RequestBody EmployeeDocsUpdateDto updateDto) {
        EmployeeDocsDto existing = employeeDocsService.getById(docId);
        if (existing.getEmployeeId() == null || !existing.getEmployeeId().equals(employeeId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        EmployeeDocsDto updated = employeeDocsService.update(docId, updateDto);
        return ResponseEntity.ok(updated);
    }

    // 5) PUT /employees/{employeeId}/documents/{docId}/deactivate
    @PutMapping("/{docId}/deactivate")
    public ResponseEntity<Void> deactivateDocument(@PathVariable Long employeeId,
                                                   @PathVariable Long docId) {
        EmployeeDocsDto existing = employeeDocsService.getById(docId);
        if (existing.getEmployeeId() == null || !existing.getEmployeeId().equals(employeeId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        employeeDocsService.deactivate(docId, LocalDateTime.now());
        return ResponseEntity.ok().build();
    }

    // 6) DELETE /employees/{employeeId}/documents/{docId}
    @DeleteMapping("/{docId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long employeeId,
                                               @PathVariable Long docId) {
        EmployeeDocsDto existing = employeeDocsService.getById(docId);
        if (existing.getEmployeeId() == null || !existing.getEmployeeId().equals(employeeId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        employeeDocsService.delete(docId);
        return ResponseEntity.noContent().build();
    }

    // POST /api/employees/{employeeId}/documents/{docId}/file
    @PostMapping(path = "/{docId}/file", consumes = "multipart/form-data")
    public ResponseEntity<EmployeeDocsDto> uploadDocumentFile(@PathVariable Long employeeId,
                                                              @PathVariable Long docId,
                                                              @RequestParam("file") MultipartFile file) {
        EmployeeDocsDto updated = employeeDocsService.uploadFile(employeeId, docId, file);
        return ResponseEntity.ok(updated);
    }

    // DELETE /api/employees/{employeeId}/documents/{docId}/file
    @DeleteMapping("/{docId}/file")
    public ResponseEntity<Void> deleteDocumentFile(@PathVariable Long employeeId,
                                                   @PathVariable Long docId) {
        employeeDocsService.deleteFile(employeeId, docId);
        return ResponseEntity.noContent().build();
    }
}