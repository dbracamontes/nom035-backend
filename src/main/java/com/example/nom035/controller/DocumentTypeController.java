package com.example.nom035.controller;

import com.example.nom035.dto.DocumentTypeDto;
import com.example.nom035.service.DocumentTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/document-types")
@Secured({"ROLE_ADMIN", "ROLE_COMPANY"})
public class DocumentTypeController {

    private final DocumentTypeService documentTypeService;

    public DocumentTypeController(DocumentTypeService documentTypeService) {
        this.documentTypeService = documentTypeService;
    }

    @GetMapping
    public ResponseEntity<List<DocumentTypeDto>> listAll() {
        return ResponseEntity.ok(documentTypeService.listAll());
    }
}
