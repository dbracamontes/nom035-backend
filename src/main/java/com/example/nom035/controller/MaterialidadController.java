package com.example.nom035.controller;

import com.example.nom035.dto.DocumentGenerateResponseDto;
import com.example.nom035.entity.DocumentJob;
import com.example.nom035.service.MaterialidadService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/materialidad")
@Secured({"ROLE_ADMIN", "ROLE_COMPANY"})
public class MaterialidadController {

    private final MaterialidadService materialidadService;

    public MaterialidadController(MaterialidadService materialidadService) {
        this.materialidadService = materialidadService;
    }

    @PostMapping("/medica-leben/companies/{companyId}/generate")
    public ResponseEntity<DocumentGenerateResponseDto> generate(@PathVariable Long companyId) {
        DocumentJob job = materialidadService.generate(companyId);
        return ResponseEntity.ok(new DocumentGenerateResponseDto(
                job.getId(), job.getStatus().name(), "MATERIALIDAD_MEDICA_LEBEN"));
    }
}