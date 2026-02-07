package com.example.nom035.controller;

import com.example.nom035.dto.DocumentJobStatusDto;
import com.example.nom035.dto.DocumentPreviewChunkDto;
import com.example.nom035.dto.DocumentUploadResponseDto;
import com.example.nom035.entity.DocumentJob;
import com.example.nom035.service.DocumentInterpretationService;
import com.example.nom035.service.DocumentPdfService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@Secured({"ROLE_ADMIN", "ROLE_COMPANY"})
public class DocumentInterpretationController {

    private final DocumentInterpretationService documentInterpretationService;
    private final DocumentPdfService documentPdfService;

    public DocumentInterpretationController(DocumentInterpretationService documentInterpretationService,
                                            DocumentPdfService documentPdfService) {
        this.documentInterpretationService = documentInterpretationService;
        this.documentPdfService = documentPdfService;
    }

    @PostMapping(path = "/interpret", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentUploadResponseDto> interpret(@RequestParam("file") MultipartFile file,
                                                               @RequestParam(value = "documentType", required = false) String documentType) {
        DocumentJob job = documentInterpretationService.process(file, documentType);
        return ResponseEntity.ok(new DocumentUploadResponseDto(job.getId(), job.getStatus().name()));
    }

    @GetMapping("/{jobId}/status")
    public ResponseEntity<DocumentJobStatusDto> status(@PathVariable Long jobId) {
        return ResponseEntity.ok(documentInterpretationService.getStatus(jobId));
    }

    @GetMapping("/{jobId}/preview")
    public ResponseEntity<List<DocumentPreviewChunkDto>> preview(@PathVariable Long jobId) {
        return ResponseEntity.ok(documentInterpretationService.getPreview(jobId));
    }

    @GetMapping("/{jobId}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long jobId) throws IOException {
        Path docx = documentInterpretationService.getOutputPath(jobId);
        byte[] bytes = Files.readAllBytes(docx);
        String filename = docx.getFileName().toString();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
            .body(bytes);
    }

        @GetMapping("/{id}/download/pdf")
        public ResponseEntity<Resource> downloadPdf(@PathVariable Long id) throws IOException {
            // Keep existing path for backwards compatibility but route through the helper
            return buildPdfResponse(id);
        }

        @GetMapping("/{id}/downloadPdf")
        public ResponseEntity<Resource> downloadPdfAlt(@PathVariable Long id) throws IOException {
            // Alternate path to avoid static resource handler collisions
            return buildPdfResponse(id);
        }

        private ResponseEntity<Resource> buildPdfResponse(Long id) throws IOException {
            DocumentJob job = documentInterpretationService.getJob(id);
            var preview = documentInterpretationService.getPreview(id);
            Path tmp = Files.createTempDirectory("doc_pdf_");
            Path pdf = documentPdfService.buildPdfFromPreview(job.getOriginalFilename(), "", preview, tmp);
            Resource resource = new UrlResource(pdf.toUri());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=interpreted-document.pdf")
                    .contentLength(resource.contentLength())
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        }
}
