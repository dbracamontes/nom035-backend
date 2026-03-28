package com.example.nom035.controller;

import com.example.nom035.dto.ContractGenerateRequestDto;
import com.example.nom035.dto.ContractMovementLogItemDto;
import com.example.nom035.dto.ContractPrepareResponseDto;
import com.example.nom035.dto.DocumentGenerateResponseDto;
import com.example.nom035.entity.DocumentJob;
import com.example.nom035.service.ContractGenerationService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@Secured({"ROLE_ADMIN", "ROLE_COMPANY"})
public class ContractGenerationController {

    private final ContractGenerationService contractGenerationService;

    public ContractGenerationController(ContractGenerationService contractGenerationService) {
        this.contractGenerationService = contractGenerationService;
    }

    @PostMapping(path = "/prepare", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContractPrepareResponseDto> prepare(
        @RequestParam("files") List<MultipartFile> files,
        @RequestParam(value = "documentType", required = false) String documentType,
        @RequestParam(value = "templateType", required = false) String templateType
    ) {
        return ResponseEntity.ok(contractGenerationService.prepare(files, documentType, templateType));
    }

    @GetMapping("/movements")
    public ResponseEntity<List<ContractMovementLogItemDto>> movements() {
        return ResponseEntity.ok(contractGenerationService.getMovementLog());
    }

    @PostMapping("/generate")
    public ResponseEntity<DocumentGenerateResponseDto> generate(@Valid @RequestBody ContractGenerateRequestDto request) {
        DocumentJob job = contractGenerationService.generate(request);
        return ResponseEntity.ok(new DocumentGenerateResponseDto(job.getId(), job.getStatus().name(), request.getTemplateType()));
    }
}
