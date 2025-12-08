package com.example.nom035.service.impl;

import com.example.nom035.dto.EmployeeDocsCreateDto;
import com.example.nom035.dto.EmployeeDocsDto;
import com.example.nom035.dto.EmployeeDocsUpdateDto;
import com.example.nom035.entity.DocumentType;
import com.example.nom035.entity.Employee;
import com.example.nom035.entity.EmployeeDocs;
import com.example.nom035.entity.EmployeeDocs.DocumentStatus;
import com.example.nom035.exception.BadRequestException;
import com.example.nom035.exception.ResourceNotFoundException;
import com.example.nom035.repository.DocumentTypeRepository;
import com.example.nom035.repository.EmployeeDocsRepository;
import com.example.nom035.repository.EmployeeRepository;
import com.example.nom035.service.EmployeeDocsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeDocsServiceImpl implements EmployeeDocsService {

    private final EmployeeDocsRepository employeeDocsRepository;
    private final EmployeeRepository employeeRepository;
    private final DocumentTypeRepository documentTypeRepository;

    @org.springframework.beans.factory.annotation.Value("${app.employee-docs.upload-base-path:uploads/employees}")
    private String uploadBasePath;

    public EmployeeDocsServiceImpl(EmployeeDocsRepository employeeDocsRepository,
                                   EmployeeRepository employeeRepository,
                                   DocumentTypeRepository documentTypeRepository) {
        this.employeeDocsRepository = employeeDocsRepository;
        this.employeeRepository = employeeRepository;
        this.documentTypeRepository = documentTypeRepository;
    }

    @Override
    public EmployeeDocsDto create(EmployeeDocsCreateDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id " + dto.getEmployeeId()));

        DocumentType type = documentTypeRepository.findById(dto.getTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("DocumentType not found with id " + dto.getTypeId()));

        EmployeeDocs entity = new EmployeeDocs();
        entity.setName(dto.getName());
        entity.setEmployee(employee);
        entity.setType(type);
        entity.setStatus(DocumentStatus.ACTIVE);
        entity.setCreatedDate(LocalDateTime.now());

        entity = employeeDocsRepository.save(entity);
        return toDto(entity);
    }

    @Override
    public EmployeeDocsDto update(Long id, EmployeeDocsUpdateDto dto) {
        EmployeeDocs entity = employeeDocsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeDocs not found with id " + id));

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getTypeId() != null) {
            DocumentType type = documentTypeRepository.findById(dto.getTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("DocumentType not found with id " + dto.getTypeId()));
            entity.setType(type);
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
            if (dto.getStatus() == DocumentStatus.INACTIVE && entity.getDeactivatedDate() == null) {
                entity.setDeactivatedDate(LocalDateTime.now());
            }
        }

        entity = employeeDocsRepository.save(entity);
        return toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDocsDto getById(Long id) {
        EmployeeDocs entity = employeeDocsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeDocs not found with id " + id));
        return toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDocsDto> listByEmployee(Long employeeId) {
        return employeeDocsRepository.findByEmployeeId(employeeId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDocsDto> listByStatus(DocumentStatus status) {
        return employeeDocsRepository.findByStatus(status).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deactivate(Long id, LocalDateTime deactivatedDate) {
        EmployeeDocs entity = employeeDocsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeDocs not found with id " + id));
        if (entity.getStatus() == DocumentStatus.INACTIVE) {
            throw new BadRequestException("Document is already inactive");
        }
        entity.setStatus(DocumentStatus.INACTIVE);
        entity.setDeactivatedDate(deactivatedDate != null ? deactivatedDate : LocalDateTime.now());
        employeeDocsRepository.save(entity);
    }

    @Override
    public void delete(Long id) {
        if (!employeeDocsRepository.existsById(id)) {
            throw new ResourceNotFoundException("EmployeeDocs not found with id " + id);
        }
        employeeDocsRepository.deleteById(id);
    }

    @Override
    public EmployeeDocsDto uploadFile(Long employeeId, Long docId, org.springframework.web.multipart.MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }
        EmployeeDocs entity = employeeDocsRepository.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeDocs not found with id " + docId));
        if (entity.getEmployee() == null || !entity.getEmployee().getId().equals(employeeId)) {
            throw new ResourceNotFoundException("Document does not belong to employee " + employeeId);
        }
        try {
            java.nio.file.Path baseDir = java.nio.file.Paths.get(uploadBasePath, "employee-" + employeeId, "docs");
            java.nio.file.Files.createDirectories(baseDir);
            String originalName = file.getOriginalFilename();
            String safeName = (originalName != null ? originalName.replaceAll("[^a-zA-Z0-9._-]", "_") : "file");
            String storedName = java.util.UUID.randomUUID() + "_" + safeName;
            java.nio.file.Path target = baseDir.resolve(storedName);
            java.nio.file.Files.copy(file.getInputStream(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            entity.setFileName(originalName);
            entity.setContentType(file.getContentType());
            entity.setFileSize(file.getSize());
            entity.setFilePath(target.toString());
            entity = employeeDocsRepository.save(entity);
            return toDto(entity);
        } catch (java.io.IOException ex) {
            throw new BadRequestException("Failed to store file: " + ex.getMessage());
        }
    }

    @Override
    public void deleteFile(Long employeeId, Long docId) {
        EmployeeDocs entity = employeeDocsRepository.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeDocs not found with id " + docId));
        if (entity.getEmployee() == null || !entity.getEmployee().getId().equals(employeeId)) {
            throw new ResourceNotFoundException("Document does not belong to employee " + employeeId);
        }
        String path = entity.getFilePath();
        if (path != null && !path.isBlank()) {
            java.nio.file.Path p = java.nio.file.Paths.get(path);
            try {
                java.nio.file.Files.deleteIfExists(p);
            } catch (java.io.IOException ignored) { }
        }
        entity.setFileName(null);
        entity.setContentType(null);
        entity.setFileSize(null);
        entity.setFilePath(null);
        employeeDocsRepository.save(entity);
    }

    private EmployeeDocsDto toDto(EmployeeDocs entity) {
        EmployeeDocsDto dto = new EmployeeDocsDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setStatus(entity.getStatus());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setDeactivatedDate(entity.getDeactivatedDate());
        dto.setEmployeeId(entity.getEmployee() != null ? entity.getEmployee().getId() : null);
        dto.setTypeId(entity.getType() != null ? entity.getType().getId() : null);
        dto.setFileName(entity.getFileName());
        dto.setContentType(entity.getContentType());
        dto.setFileSize(entity.getFileSize());
        dto.setFilePath(entity.getFilePath());
        return dto;
    }
}