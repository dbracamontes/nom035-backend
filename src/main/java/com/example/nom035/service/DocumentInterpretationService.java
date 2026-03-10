package com.example.nom035.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.nom035.dto.DocumentJobStatusDto;
import com.example.nom035.dto.DocumentPreviewChunkDto;
import com.example.nom035.entity.DocumentChunk;
import com.example.nom035.entity.DocumentJob;
import com.example.nom035.entity.DocumentOcrPage;
import com.example.nom035.repository.DocumentChunkRepository;
import com.example.nom035.repository.DocumentJobRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentInterpretationService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentInterpretationService.class);

    private final DocumentJobRepository documentJobRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentOcrService documentOcrService;
    private final DocumentChunkingService documentChunkingService;
    private final DocumentOpenAiService documentOpenAiService;
    private final DocumentWordService documentWordService;

    private final Path storageBasePath;
    private final int maxPages;
    private final long maxFileSizeBytes;
    private final String ocrProvider;
    private final String model;
    private final long simulateDelayMs;

    public DocumentInterpretationService(DocumentJobRepository documentJobRepository,
                                         DocumentChunkRepository documentChunkRepository,
                                         DocumentOcrService documentOcrService,
                                         DocumentChunkingService documentChunkingService,
                                         DocumentOpenAiService documentOpenAiService,
                                         DocumentWordService documentWordService,
                                         @Value("${docai.storage-base-path:uploads/doc-generator/tmp}") String storageBasePath,
                                         @Value("${docai.max-pages:30}") int maxPages,
                                         @Value("${docai.max-file-size-mb:20}") int maxFileSizeMb,
                                         @Value("${docai.ocr.provider:local}") String ocrProvider,
                                         @Value("${docai.openai.model:gpt-4.1-mini}") String model,
                                         @Value("${DOC_AI_SIMULATE_DELAY_MS:0}") long simulateDelayMs) {
        this.documentJobRepository = documentJobRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.documentOcrService = documentOcrService;
        this.documentChunkingService = documentChunkingService;
        this.documentOpenAiService = documentOpenAiService;
        this.documentWordService = documentWordService;
        this.storageBasePath = Paths.get(storageBasePath);
        this.maxPages = maxPages;
        this.maxFileSizeBytes = maxFileSizeMb * 1024L * 1024L;
        this.ocrProvider = ocrProvider;
        this.model = model;
        this.simulateDelayMs = simulateDelayMs;
    }

    public DocumentJob process(MultipartFile file, String documentTypeStr) {
        return process(file, documentTypeStr, null);
    }

    public DocumentJob process(MultipartFile file, String documentTypeStr, Integer pageLimitOverride) {
        validateFile(file);
        Path jobDir = createJobDir();
        Path storedPdf = storePdf(file, jobDir);
        int pageCount = countPages(storedPdf);
        int effectiveMaxPages = pageLimitOverride != null && pageLimitOverride > 0 ? pageLimitOverride : maxPages;
        int pagesToProcess = effectiveMaxPages > 0 ? Math.min(pageCount, effectiveMaxPages) : pageCount;
        if (pagesToProcess <= 0 && pageCount > 0) {
            pagesToProcess = pageCount;
        }
        logger.info("DocAI prepare '{}': pageCount={}, configuredMaxPages={}, pageLimitOverride={}, pagesToProcess={}",
            file.getOriginalFilename(), pageCount, maxPages, pageLimitOverride, pagesToProcess);

        DocumentJob job = new DocumentJob();
        job.setOriginalFilename(file.getOriginalFilename());
        job.setStoredPath(storedPdf.toString());
        job.setOcrProvider(ocrProvider);
        job.setModelUsed(model);
        try {
            DocumentJob.DocumentType dt = documentTypeStr == null ? DocumentJob.DocumentType.ACTA : DocumentJob.DocumentType.valueOf(documentTypeStr.toUpperCase());
            job.setDocumentType(dt);
        } catch (Exception e) {
            job.setDocumentType(DocumentJob.DocumentType.ACTA);
        }
        job.setTotalPages(pagesToProcess);
        job.setProcessedPages(0);
        job.setFileSizeBytes(file.getSize());
        job.setContentType(file.getContentType());
        job = documentJobRepository.save(job);

        try {
            updateStatus(job, DocumentJob.Status.OCR_RUNNING);
            List<DocumentOcrPage> pages = documentOcrService.runOcr(storedPdf, job, pagesToProcess);
            job.setProcessedPages(pages.size());
            updateStatus(job, DocumentJob.Status.OCR_COMPLETED);

            // Reset processedPages for interpretation progress tracking
            job.setProcessedPages(0);
            documentJobRepository.save(job);

            updateStatus(job, DocumentJob.Status.INTERPRETING);
            // Paso B (cleanText): limpiar cada página localmente antes de chunking
            for (DocumentOcrPage p : pages) {
                String cleaned = documentOcrService.cleanText(p.getText());
                p.setText(cleaned);
            }
            List<DocumentChunk> chunks = documentChunkingService.buildChunks(job, pages);
            for (DocumentChunk chunk : chunks) {
                String interpreted = documentOpenAiService.interpret(chunk.getRawText(), job.getDocumentType() != null ? job.getDocumentType().name() : "ACTA");
                chunk.setInterpretedText(interpreted);
                // increment processed pages according to this chunk's page range
                int pagesInChunk = Math.max(0, chunk.getPageEnd() - chunk.getPageStart() + 1);
                job.setProcessedPages((job.getProcessedPages() == null ? 0 : job.getProcessedPages()) + pagesInChunk);
                job.touchUpdatedAt();
                logger.debug("Job {}: about to save processedPages = {}/{}", job.getId(), job.getProcessedPages(), job.getTotalPages());
                documentJobRepository.save(job);
                logger.info("Job {}: updated processedPages = {}/{}", job.getId(), job.getProcessedPages(), job.getTotalPages());
                // If configured, sleep to simulate slower processing so frontend can poll intermediate states
                if (simulateDelayMs > 0) {
                    try {
                        Thread.sleep(simulateDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            documentChunkRepository.saveAll(chunks);
            updateStatus(job, DocumentJob.Status.INTERPRETED);

            updateStatus(job, DocumentJob.Status.GENERATING_WORD);
            Path docxPath = documentWordService.buildWord(job, chunks, jobDir);
            job.setOutputDocxPath(docxPath.toString());
            job.setCompletedAt(LocalDateTime.now());
            updateStatus(job, DocumentJob.Status.DONE);
        } catch (Exception e) {
            job.setFailureReason(e.getMessage());
            updateStatus(job, DocumentJob.Status.FAILED);
            throw e;
        }
        return job;
    }

    public DocumentJobStatusDto getStatus(Long jobId) {
        DocumentJob job = documentJobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job no encontrado"));
        DocumentJobStatusDto dto = new DocumentJobStatusDto();
        dto.setJobId(job.getId());
        dto.setStatus(job.getStatus().name());
        dto.setTotalPages(job.getTotalPages());
        dto.setProcessedPages(job.getProcessedPages());
        dto.setOriginalFilename(job.getOriginalFilename());
        dto.setCreatedAt(job.getCreatedAt());
        dto.setCompletedAt(job.getCompletedAt());
        dto.setFailureReason(job.getFailureReason());
        dto.setOutputReady(job.getStatus() == DocumentJob.Status.DONE && job.getOutputDocxPath() != null);
        dto.setDocumentType(job.getDocumentType() != null ? job.getDocumentType().name() : null);
        return dto;
    }

    public List<DocumentPreviewChunkDto> getPreview(Long jobId) {
        List<DocumentChunk> chunks = documentChunkRepository.findByJob_IdOrderByChunkIndexAsc(jobId);
        return chunks.stream().map(chunk -> {
            DocumentPreviewChunkDto dto = new DocumentPreviewChunkDto();
            dto.setChunkIndex(chunk.getChunkIndex());
            dto.setPageStart(chunk.getPageStart());
            dto.setPageEnd(chunk.getPageEnd());
            dto.setRawText(chunk.getRawText());
            dto.setInterpretedText(chunk.getInterpretedText());
            return dto;
        }).collect(Collectors.toList());
    }

    public Path getOutputPath(Long jobId) {
        DocumentJob job = documentJobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job no encontrado"));
        if (job.getOutputDocxPath() == null) {
            throw new IllegalStateException("El job aún no genera un Word");
        }
        return Paths.get(job.getOutputDocxPath());
    }

    public DocumentJob getJob(Long jobId) {
        return documentJobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job no encontrado"));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Archivo PDF requerido");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new IllegalArgumentException("Archivo demasiado grande. Límite " + maxFileSizeBytes / (1024 * 1024) + "MB");
        }
        if (file.getOriginalFilename() != null && !file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Solo se aceptan PDFs escaneados");
        }
    }

    private Path createJobDir() {
        try {
            Path dir = storageBasePath.resolve("docai-job-" + UUID.randomUUID());
            Files.createDirectories(dir);
            return dir;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear carpeta de trabajo: " + e.getMessage(), e);
        }
    }

    private Path storePdf(MultipartFile file, Path jobDir) {
        String name = file.getOriginalFilename();
        if (name == null || name.isBlank()) {
            name = "documento.pdf";
        }
        Path target = jobDir.resolve(name);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target);
            return target;
        } catch (IOException e) {
            throw new IllegalStateException("Error guardando PDF: " + e.getMessage(), e);
        }
    }

    private int countPages(Path pdf) {
        try (PDDocument document = PDDocument.load(pdf.toFile())) {
            return document.getNumberOfPages();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el PDF: " + e.getMessage(), e);
        }
    }

    private void updateStatus(DocumentJob job, DocumentJob.Status status) {
        job.setStatus(status);
        job.touchUpdatedAt();
        documentJobRepository.save(job);
        logger.info("Job {}: status -> {}", job.getId(), status);
    }
}
