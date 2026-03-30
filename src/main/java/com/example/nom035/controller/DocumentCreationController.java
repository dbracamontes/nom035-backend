package com.example.nom035.controller;

import com.example.nom035.dto.DocumentGenerateManualRequestDto;
import com.example.nom035.dto.DocumentGenerateResponseDto;
import com.example.nom035.dto.DocumentGeneratedPreviewDto;
import com.example.nom035.dto.DocumentPreviewChunkDto;
import com.example.nom035.dto.DocumentTemplateDto;
import com.example.nom035.dto.ContractGenerateRequestDto;
import com.example.nom035.entity.DocumentJob;
import com.example.nom035.service.ContractGenerationService;
import com.example.nom035.service.DocxPdfConversionService;
import com.example.nom035.service.DocumentCreationService;
import com.example.nom035.service.DocumentPdfService;
import com.example.nom035.service.DocumentTemplateCatalogService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/docgen")
@Secured({"ROLE_ADMIN", "ROLE_COMPANY"})
public class DocumentCreationController {

    private final DocumentTemplateCatalogService documentTemplateCatalogService;
    private final DocumentCreationService documentCreationService;
    private final ContractGenerationService contractGenerationService;
    private final DocxPdfConversionService docxPdfConversionService;
    private final DocumentPdfService documentPdfService;

    public DocumentCreationController(DocumentTemplateCatalogService documentTemplateCatalogService,
                                      DocumentCreationService documentCreationService,
                                      ContractGenerationService contractGenerationService,
                                      DocxPdfConversionService docxPdfConversionService,
                                      DocumentPdfService documentPdfService) {
        this.documentTemplateCatalogService = documentTemplateCatalogService;
        this.documentCreationService = documentCreationService;
        this.contractGenerationService = contractGenerationService;
        this.docxPdfConversionService = docxPdfConversionService;
        this.documentPdfService = documentPdfService;
    }

    @GetMapping("/templates")
    public ResponseEntity<List<DocumentTemplateDto>> listTemplates() {
        return ResponseEntity.ok(documentTemplateCatalogService.listTemplates());
    }

    @GetMapping("/templates/{templateType}/fields")
    public ResponseEntity<List<com.example.nom035.dto.DocumentTemplateFieldDto>> getTemplateFields(@PathVariable String templateType) {
        return ResponseEntity.ok(documentTemplateCatalogService.getFieldsByType(templateType));
    }

    @PostMapping("/generate/manual")
    public ResponseEntity<DocumentGenerateResponseDto> generateManual(@Valid @RequestBody DocumentGenerateManualRequestDto request) {
        DocumentJob job;
        if (isContractTemplate(request.getTemplateType())) {
            ContractGenerateRequestDto contractRequest = new ContractGenerateRequestDto();
            contractRequest.setTemplateType(request.getTemplateType());
            contractRequest.setFields(request.getFields());
            job = contractGenerationService.generate(contractRequest);
        } else {
            job = documentCreationService.generateManual(request.getTemplateType(), request.getFields());
        }
        return ResponseEntity.ok(new DocumentGenerateResponseDto(job.getId(), job.getStatus().name(), request.getTemplateType()));
    }

    private boolean isContractTemplate(String templateType) {
        try {
            DocumentTemplateCatalogService.TemplateType resolved = documentTemplateCatalogService.resolve(templateType);
            return "CONTRATO".equalsIgnoreCase(resolved.getDisplayType());
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    @GetMapping("/{jobId}/preview")
    public ResponseEntity<DocumentGeneratedPreviewDto> preview(@PathVariable Long jobId) {
        String text = documentCreationService.getPreviewText(jobId);
        return ResponseEntity.ok(new DocumentGeneratedPreviewDto(jobId, text));
    }

    @GetMapping("/{jobId}/download/word")
    public ResponseEntity<byte[]> downloadWord(@PathVariable Long jobId) throws IOException {
        Path docx = documentCreationService.getOutputPath(jobId);
        byte[] bytes = Files.readAllBytes(docx);
        String filename = docx.getFileName().toString();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
            .body(bytes);
    }

    @GetMapping("/{jobId}/download/pdf")
    public ResponseEntity<Resource> downloadPdf(@PathVariable Long jobId) throws IOException {
        Path docx = documentCreationService.getOutputPath(jobId);
        Path tmp = Files.createTempDirectory("docgen_pdf_");
        Path pdf;
        try {
            pdf = docxPdfConversionService.convertDocxToPdf(docx, tmp, true);
        } catch (Exception ex) {
            String preview = documentCreationService.getPreviewText(jobId);
            DocumentPreviewChunkDto chunkDto = new DocumentPreviewChunkDto();
            chunkDto.setChunkIndex(0);
            chunkDto.setPageStart(1);
            chunkDto.setPageEnd(1);
            chunkDto.setInterpretedText(preview);
            pdf = documentPdfService.buildPdfFromPreview("Documento generado", "", List.of(chunkDto), tmp);
        }

        Resource resource = new UrlResource(pdf.toUri());
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=documento-generado.pdf")
            .contentLength(resource.contentLength())
            .contentType(MediaType.APPLICATION_PDF)
            .body(resource);
    }
}
