package com.example.nom035.service;

import com.example.nom035.dto.DocumentTypeDto;

import java.util.List;

public interface DocumentTypeService {
    DocumentTypeDto create(DocumentTypeDto dto);
    DocumentTypeDto update(Long id, DocumentTypeDto dto);
    DocumentTypeDto getById(Long id);
    List<DocumentTypeDto> listAll();
}
