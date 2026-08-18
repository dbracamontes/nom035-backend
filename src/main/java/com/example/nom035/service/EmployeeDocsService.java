package com.example.nom035.service;

import com.example.nom035.dto.EmployeeDocsCreateDto;
import com.example.nom035.dto.EmployeeDocsDto;
import com.example.nom035.dto.EmployeeDocsUpdateDto;
import com.example.nom035.entity.EmployeeDocs.DocumentStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public interface EmployeeDocsService {
    EmployeeDocsDto create(EmployeeDocsCreateDto dto);
    EmployeeDocsDto update(Long id, EmployeeDocsUpdateDto dto);
    EmployeeDocsDto getById(Long id);
    List<EmployeeDocsDto> listByEmployee(Long employeeId);
    List<EmployeeDocsDto> listByStatus(DocumentStatus status);
    void deactivate(Long id, LocalDateTime deactivatedDate);
    void delete(Long id);

    // File handling for document content
    EmployeeDocsDto uploadFile(Long employeeId, Long docId, MultipartFile file);
    void deleteFile(Long employeeId, Long docId);
    ResponseEntity<byte[]> downloadFile(Long employeeId, Long docId);
    ResponseEntity<byte[]> previewFile(Long employeeId, Long docId);
}