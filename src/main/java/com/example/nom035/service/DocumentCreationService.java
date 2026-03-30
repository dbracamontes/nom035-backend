package com.example.nom035.service;

import com.example.nom035.dto.DocumentTemplateFieldDto;
import com.example.nom035.entity.DocumentJob;
import com.example.nom035.repository.DocumentJobRepository;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DocumentCreationService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentCreationService.class);
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\{\\{([\\p{L}\\p{N}_]+)}}|\\$\\{([\\p{L}\\p{N}_]+)}");

    private final DocumentJobRepository documentJobRepository;
    private final DocumentTemplateCatalogService documentTemplateCatalogService;
    private final Path storageBasePath;

    public DocumentCreationService(DocumentJobRepository documentJobRepository,
                                   DocumentTemplateCatalogService documentTemplateCatalogService,
                                   @Value("${docgen.storage-base-path:uploads/doc-generator/tmp}") String storageBasePath) {
        this.documentJobRepository = documentJobRepository;
        this.documentTemplateCatalogService = documentTemplateCatalogService;
        this.storageBasePath = Paths.get(storageBasePath);
    }

    public DocumentJob generateManual(String templateType, Map<String, String> inputFields) {
        return generateManual(templateType, inputFields, DocumentJobMetadata.empty());
    }

    public DocumentJob generateManual(String templateType,
                                      Map<String, String> inputFields,
                                      DocumentJobMetadata metadata) {
        DocumentTemplateCatalogService.TemplateType template = documentTemplateCatalogService.resolve(templateType);
        if (!template.isEnabled()) {
            throw new IllegalArgumentException("La plantilla aún no está habilitada: " + template.getDisplayName());
        }

        List<DocumentTemplateFieldDto> requiredFields = documentTemplateCatalogService.getFieldsByType(template.getCode());
        validateRequiredFields(requiredFields, inputFields);

        Path templatePath = documentTemplateCatalogService.resolveTemplatePath(template);
        Path jobDir = createJobDir();
        Path outputPath = jobDir.resolve(buildOutputFilename(template));

        DocumentJob job = new DocumentJob();
        job.setOriginalFilename(templatePath.getFileName().toString());
        job.setStoredPath(templatePath.toAbsolutePath().toString());
        job.setOcrProvider("docgen-manual");
        job.setModelUsed("docgen-manual");
        job.setTotalPages(1);
        job.setProcessedPages(0);
        job.setFileSizeBytes(sizeOf(templatePath));
        job.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        job.setStatus(DocumentJob.Status.GENERATING_WORD);
        job.setTemplateType(metadata.templateType() != null ? metadata.templateType() : template.getCode());
        job.setSourceModule(metadata.sourceModule());
        job.setClientName(metadata.clientName());
        job.setCiudadano(metadata.ciudadano());
        job.setCreatedByUser(metadata.createdByUser());
        job.setContractDate(metadata.contractDate());
        job.setVigenciaStartDate(metadata.vigenciaStartDate());
        job.setVigenciaEndDate(metadata.vigenciaEndDate());
        job = documentJobRepository.save(job);

        try {
            Map<String, String> normalized = normalizeValues(inputFields);
            generateDocx(templatePath, outputPath, normalized);
            job.setOutputDocxPath(outputPath.toAbsolutePath().toString());
            job.setProcessedPages(1);
            job.setCompletedAt(LocalDateTime.now());
            job.setStatus(DocumentJob.Status.DONE);
            job.touchUpdatedAt();
            documentJobRepository.save(job);
            logger.info("DocGen job {} completado para plantilla {}", job.getId(), template.getCode());
            return job;
        } catch (Exception e) {
            job.setStatus(DocumentJob.Status.FAILED);
            job.setFailureReason(e.getMessage());
            job.touchUpdatedAt();
            documentJobRepository.save(job);
            throw new IllegalStateException("Error al generar documento: " + e.getMessage(), e);
        }
    }

    public DocumentJob getJob(Long jobId) {
        return documentJobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job no encontrado"));
    }

    public Path getOutputPath(Long jobId) {
        DocumentJob job = getJob(jobId);
        if (job.getOutputDocxPath() == null || job.getOutputDocxPath().isBlank()) {
            throw new IllegalStateException("El documento aún no está disponible");
        }
        return Paths.get(job.getOutputDocxPath());
    }

    public String getPreviewText(Long jobId) {
        Path output = getOutputPath(jobId);
        try (InputStream in = Files.newInputStream(output);
             XWPFDocument document = new XWPFDocument(in);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer vista previa del documento", e);
        }
    }

    private void validateRequiredFields(List<DocumentTemplateFieldDto> requiredFields, Map<String, String> inputFields) {
        if (requiredFields == null || requiredFields.isEmpty()) {
            return;
        }
        Map<String, String> safeInput = inputFields == null ? Map.of() : inputFields;
        for (DocumentTemplateFieldDto field : requiredFields) {
            if (!field.isRequired()) {
                continue;
            }
            String value = safeInput.get(field.getKey());
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException("Falta campo requerido: " + field.getLabel() + " (" + field.getKey() + ")");
            }
        }
    }

    private Map<String, String> normalizeValues(Map<String, String> inputFields) {
        Map<String, String> normalized = new HashMap<>();
        if (inputFields == null) {
            return normalized;
        }
        inputFields.forEach((k, v) -> normalized.put(k, v == null ? "" : v));
        return normalized;
    }

    private Path createJobDir() {
        try {
            Path dir = storageBasePath.resolve("docgen-job-" + UUID.randomUUID());
            Files.createDirectories(dir);
            return dir;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear carpeta de trabajo", e);
        }
    }

    private long sizeOf(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            return 0L;
        }
    }

    private String buildOutputFilename(DocumentTemplateCatalogService.TemplateType template) {
        return template.getCode().toLowerCase() + "-" + System.currentTimeMillis() + ".docx";
    }

    private void generateDocx(Path templatePath, Path outputPath, Map<String, String> values) {
        try (InputStream in = Files.newInputStream(templatePath);
             XWPFDocument document = new XWPFDocument(in);
             OutputStream out = Files.newOutputStream(outputPath)) {

            replaceParagraphTokens(document.getParagraphs(), values);
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        replaceParagraphTokens(cell.getParagraphs(), values);
                    }
                }
            }

            document.getHeaderList().forEach(header -> replaceParagraphTokens(header.getParagraphs(), values));
            document.getFooterList().forEach(footer -> replaceParagraphTokens(footer.getParagraphs(), values));

            document.write(out);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el Word desde plantilla", e);
        }
    }

    private void replaceParagraphTokens(List<XWPFParagraph> paragraphs, Map<String, String> values) {
        for (XWPFParagraph paragraph : paragraphs) {
            String original = paragraph.getText();
            if (original == null || original.isBlank()) {
                continue;
            }
            List<Segment> segments = tokenizeAndReplace(original, values);
            if (segments == null) {
                continue;
            }

            XWPFRun styleSource = paragraph.getRuns() != null && !paragraph.getRuns().isEmpty() ? paragraph.getRuns().get(0) : null;
            CTRPr runProperties = null;
            if (styleSource != null && styleSource.getCTR() != null && styleSource.getCTR().isSetRPr()) {
                runProperties = (CTRPr) styleSource.getCTR().getRPr().copy();
            }
            int runCount = paragraph.getRuns() == null ? 0 : paragraph.getRuns().size();
            for (int i = runCount - 1; i >= 0; i--) {
                paragraph.removeRun(i);
            }

            for (Segment segment : segments) {
                if (segment.text() == null || segment.text().isEmpty()) {
                    continue;
                }
                XWPFRun run = paragraph.createRun();
                if (runProperties != null) {
                    run.getCTR().setRPr((CTRPr) runProperties.copy());
                }
                if (segment.highlight()) {
                    run.setColor("FF0000");
                } else {
                    run.setColor("000000");
                }
                run.setText(segment.text());
            }
        }
    }

    private List<Segment> tokenizeAndReplace(String text, Map<String, String> values) {
        Matcher matcher = TOKEN_PATTERN.matcher(text);
        int cursor = 0;
        boolean replacedAny = false;
        java.util.ArrayList<Segment> segments = new java.util.ArrayList<>();

        while (matcher.find()) {
            if (matcher.start() > cursor) {
                segments.add(new Segment(text.substring(cursor, matcher.start()), false));
            }
            String key = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            String replacement = values.get(key);
            if (replacement != null) {
                segments.add(new Segment(replacement, true));
                replacedAny = true;
            } else {
                segments.add(new Segment(matcher.group(), false));
            }
            cursor = matcher.end();
        }

        if (cursor < text.length()) {
            segments.add(new Segment(text.substring(cursor), false));
        }

        if (!replacedAny) {
            return null;
        }
        return segments;
    }

    private record Segment(String text, boolean highlight) {
    }

    public record DocumentJobMetadata(
        String sourceModule,
        String templateType,
        String clientName,
        String ciudadano,
        String createdByUser,
        LocalDate contractDate,
        LocalDate vigenciaStartDate,
        LocalDate vigenciaEndDate
    ) {
        public static DocumentJobMetadata empty() {
            return new DocumentJobMetadata(null, null, null, null, null, null, null, null);
        }
    }
}
