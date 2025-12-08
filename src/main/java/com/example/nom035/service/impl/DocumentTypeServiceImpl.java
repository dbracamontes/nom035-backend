package com.example.nom035.service.impl;

import com.example.nom035.dto.DocumentTypeDto;
import com.example.nom035.entity.DocumentType;
import com.example.nom035.exception.ResourceNotFoundException;
import com.example.nom035.repository.DocumentTypeRepository;
import com.example.nom035.service.DocumentTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DocumentTypeServiceImpl implements DocumentTypeService {

    private final DocumentTypeRepository documentTypeRepository;

    public DocumentTypeServiceImpl(DocumentTypeRepository documentTypeRepository) {
        this.documentTypeRepository = documentTypeRepository;
    }

    @Override
    public DocumentTypeDto create(DocumentTypeDto dto) {
        DocumentType entity = new DocumentType();
        entity.setName(dto.getName());
        entity = documentTypeRepository.save(entity);
        dto.setId(entity.getId());
        return dto;
    }

    @Override
    public DocumentTypeDto update(Long id, DocumentTypeDto dto) {
        DocumentType entity = documentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentType not found with id " + id));
        entity.setName(dto.getName());
        entity = documentTypeRepository.save(entity);
        DocumentTypeDto result = new DocumentTypeDto();
        result.setId(entity.getId());
        result.setName(entity.getName());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentTypeDto getById(Long id) {
        DocumentType entity = documentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentType not found with id " + id));
        DocumentTypeDto dto = new DocumentTypeDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentTypeDto> listAll() {
        return documentTypeRepository.findAll().stream().map(entity -> {
            DocumentTypeDto dto = new DocumentTypeDto();
            dto.setId(entity.getId());
            dto.setName(entity.getName());
            return dto;
        }).collect(Collectors.toList());
    }
}
