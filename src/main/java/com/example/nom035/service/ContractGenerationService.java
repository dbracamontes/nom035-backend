package com.example.nom035.service;

import com.example.nom035.dto.ContractGenerateRequestDto;
import com.example.nom035.dto.ContractPrepareResponseDto;
import com.example.nom035.dto.DocumentPreviewChunkDto;
import com.example.nom035.dto.DocumentTemplateFieldDto;
import com.example.nom035.entity.DocumentJob;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ContractGenerationService {

    private static final String DEFAULT_TEMPLATE = "DOCUMENTO_04_1";
    private static final int PREVIEW_MAX_CHARS = 12000;
    private static final Pattern PATTERN_ESCRITURA = Pattern.compile(
        "(?is)(?:escritura\\s+p[úu]blica\\s+n[úu]mero|instrumento\\s+p[úu]blico\\s+n[úu]mero|instrumento\\s+n[úu]mero|acta\\s+constitutiva\\s+contenida\\s+en\\s+la\\s+escritura\\s+p[úu]blica\\s+n[úu]mero|escritura\\s+constitutiva\\s+n[úu]mero|protocolo\\s+n[úu]mero|documento\\s+constitutivo\\s+n[úu]mero|p[óo]liza\\s+n[úu]mero|p[óo]liza)\\s*[:#\\-]?\\s*([0-9]{1,10}[A-Za-z0-9/-]*)"
    );
    private static final Pattern PATTERN_FECHA_ACTA_A_LOS = Pattern.compile(
        "(?is)a\\s+los\\s+(.{6,220}?)(?:,\\s*ante\\s*m[íi]|\\s+ante\\s*m[íi])"
    );
    private static final Pattern PATTERN_FECHA_ACTA_DE_FECHA = Pattern.compile(
        "(?is)de\\s+fecha\\s+(.{6,180}?)(?:,\\s*(?:tirada|otorgada|pasada)\\s+ante|\\s*;)"
    );
    private static final Pattern PATTERN_LICENCIADO = Pattern.compile(
        "(?is)(?:ante\\s*m[íi]\\s+licenciado|fe\\s+del\\s+licenciado|licenciado)\\s+([A-ZÁÉÍÓÚÑ][A-ZÁÉÍÓÚÑ\\s.]{3,120}?)(?:,\\s*(?:corredor|notario)|\\s+(?:corredor|notario)|\\s+titular|,)"
    );
    private static final Pattern PATTERN_CORREDURIA = Pattern.compile(
        "(?is)(?:corredor\\s+p[úu]blico\\s+n(?:[úu]mero|o\\.?|°)?|corredur[ií]a\\s+p[úu]blica\\s+n(?:[úu]mero|o\\.?|°)?)\\s*[:#\\-]?\\s*([0-9]{1,5})"
    );

    private final DocumentInterpretationService documentInterpretationService;
    private final DocumentTemplateCatalogService documentTemplateCatalogService;
    private final DocumentCreationService documentCreationService;

    public ContractGenerationService(DocumentInterpretationService documentInterpretationService,
                                     DocumentTemplateCatalogService documentTemplateCatalogService,
                                     DocumentCreationService documentCreationService) {
        this.documentInterpretationService = documentInterpretationService;
        this.documentTemplateCatalogService = documentTemplateCatalogService;
        this.documentCreationService = documentCreationService;
    }

    public ContractPrepareResponseDto prepare(List<MultipartFile> files, String documentType, String templateType) {
        if (files == null || files.size() < 3) {
            throw new IllegalArgumentException("Se requieren al menos 3 documentos para preparar el contrato");
        }

        String resolvedTemplate = StringUtils.hasText(templateType) ? templateType : DEFAULT_TEMPLATE;
        List<Long> sourceJobIds = new ArrayList<>();
        StringBuilder mergedText = new StringBuilder();
        StringBuilder actaText = new StringBuilder();
        int processedDocuments = 0;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
            String effectiveDocumentType = detectDocumentType(filename, documentType);

            if (isPdfFile(filename)) {
                DocumentJob job = documentInterpretationService.process(file, effectiveDocumentType);
                sourceJobIds.add(job.getId());
                String interpretedText = appendJobPreview(mergedText, job.getId());
                if ("ACTA".equalsIgnoreCase(effectiveDocumentType) && StringUtils.hasText(interpretedText)) {
                    if (!actaText.isEmpty()) {
                        actaText.append("\n\n");
                    }
                    actaText.append(interpretedText);
                }
            } else {
                String extracted = extractTextFromNonPdf(file, filename);
                if (!StringUtils.hasText(extracted)) {
                    continue;
                }
                if (!mergedText.isEmpty()) {
                    mergedText.append("\n\n");
                }
                mergedText.append(extracted.trim());
                if ("ACTA".equalsIgnoreCase(effectiveDocumentType)) {
                    if (!actaText.isEmpty()) {
                        actaText.append("\n\n");
                    }
                    actaText.append(extracted.trim());
                }
            }
            processedDocuments++;
        }

        if (processedDocuments < 3) {
            throw new IllegalArgumentException("Debes subir 3 o más documentos válidos");
        }

        List<DocumentTemplateFieldDto> templateFields = documentTemplateCatalogService.getFieldsByType(resolvedTemplate);
        Map<String, String> suggestedValues = buildSuggestedValues(templateFields);
        applyActaSpecificExtraction(suggestedValues, actaText.toString(), mergedText.toString());

        ContractPrepareResponseDto response = new ContractPrepareResponseDto();
        response.setTemplateType(resolvedTemplate);
        response.setSourceJobIds(sourceJobIds);
        response.setFields(templateFields);
        response.setSuggestedValues(suggestedValues);
        response.setCombinedPreview(trimPreview(mergedText.toString()));
        return response;
    }

    public DocumentJob generate(ContractGenerateRequestDto request) {
        String resolvedTemplate = StringUtils.hasText(request.getTemplateType()) ? request.getTemplateType() : DEFAULT_TEMPLATE;
        Map<String, String> fields = request.getFields() == null ? Map.of() : request.getFields();
        return documentCreationService.generateManual(resolvedTemplate, fields);
    }

    private String appendJobPreview(StringBuilder mergedText, Long jobId) {
        List<DocumentPreviewChunkDto> chunks = documentInterpretationService.getPreview(jobId);
        if (chunks == null || chunks.isEmpty()) {
            return "";
        }
        StringBuilder captured = new StringBuilder();
        for (DocumentPreviewChunkDto chunk : chunks) {
            String candidate = StringUtils.hasText(chunk.getInterpretedText())
                ? chunk.getInterpretedText()
                : chunk.getRawText();
            if (!StringUtils.hasText(candidate)) {
                continue;
            }
            if (!mergedText.isEmpty()) {
                mergedText.append("\n\n");
            }
            mergedText.append(candidate.trim());
            if (!captured.isEmpty()) {
                captured.append("\n\n");
            }
            captured.append(candidate.trim());
        }
        return captured.toString();
    }

    private Map<String, String> buildSuggestedValues(List<DocumentTemplateFieldDto> templateFields) {
        Map<String, String> values = new LinkedHashMap<>();
        if (templateFields == null) {
            return values;
        }
        for (DocumentTemplateFieldDto field : templateFields) {
            if (field == null || !StringUtils.hasText(field.getKey())) {
                continue;
            }
            values.put(field.getKey(), "");
        }

        LocalDate now = LocalDate.now();
        if (values.containsKey("DIA")) {
            values.put("DIA", String.valueOf(now.getDayOfMonth()));
        }
        if (values.containsKey("MES")) {
            values.put("MES", now.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "MX")).toUpperCase(Locale.ROOT));
        }
        if (values.containsKey("AÑO")) {
            values.put("AÑO", String.valueOf(now.getYear()));
        }

        return values;
    }

    private void applyActaSpecificExtraction(Map<String, String> values, String actaText, String mergedText) {
        String source = StringUtils.hasText(actaText) ? actaText : mergedText;
        if (!StringUtils.hasText(source)) {
            return;
        }
        String escritura = extractFirstGroup(PATTERN_ESCRITURA, source);
        String fechaALos = extractFirstGroup(PATTERN_FECHA_ACTA_A_LOS, source);
        String fechaDeFecha = extractFirstGroup(PATTERN_FECHA_ACTA_DE_FECHA, source);
        String licenciado = extractFirstGroup(PATTERN_LICENCIADO, source);
        String correduria = extractFirstGroup(PATTERN_CORREDURIA, source);

        putIfBlank(values, "ESCRITURA_PUBLICA_ACTA_NUMERO", normalizeActaNumber(escritura));
        putIfBlank(values, "FECHA_ACTA", normalizeFechaActa(StringUtils.hasText(fechaALos) ? fechaALos : fechaDeFecha));
        putIfBlank(values, "LICENCIADO_ACTA_DA_FE", normalizePersonName(licenciado));
        putIfBlank(values, "CORREDURIA_PUBLICA_NO", normalizeActaNumber(correduria));
    }

    private String detectDocumentType(String filename, String defaultDocumentType) {
        String base = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        if (base.contains("acta")) {
            return "ACTA";
        }
        if (base.contains("asamblea")) {
            return "ASAMBLEA";
        }
        if (base.contains("constancia") || base.contains("fiscal") || base.contains("situacion")) {
            return "CONSTANCIA_SITUACION_FISCAL";
        }
        return StringUtils.hasText(defaultDocumentType) ? defaultDocumentType : "ACTA";
    }

    private boolean isPdfFile(String filename) {
        if (!StringUtils.hasText(filename)) {
            return false;
        }
        return filename.toLowerCase(Locale.ROOT).endsWith(".pdf");
    }

    private String extractTextFromNonPdf(MultipartFile file, String filename) {
        String lower = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        try {
            if (lower.endsWith(".docx")) {
                return extractDocxText(file);
            }
            if (lower.endsWith(".xls") || lower.endsWith(".xlsx")) {
                return extractExcelText(file);
            }
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")) {
                return extractImageText(file);
            }
            if (lower.endsWith(".txt")) {
                return new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }
            throw new IllegalArgumentException("Formato no soportado para extracción: " + filename);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el archivo " + filename, e);
        }
    }

    private String extractDocxText(MultipartFile file) throws IOException {
        try (InputStream in = file.getInputStream();
             XWPFDocument document = new XWPFDocument(in);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String extractExcelText(MultipartFile file) throws IOException {
        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            DataFormatter formatter = new DataFormatter();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        String value = formatter.formatCellValue(cell);
                        if (StringUtils.hasText(value)) {
                            if (!sb.isEmpty()) {
                                sb.append(' ');
                            }
                            sb.append(value.trim());
                        }
                    }
                }
            }
            return sb.toString();
        }
    }

    private String extractImageText(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            return "";
        }
        Tesseract tesseract = new Tesseract();
        String dataPath = System.getenv("TESSDATA_PREFIX");
        if (StringUtils.hasText(dataPath)) {
            tesseract.setDatapath(dataPath);
        }
        tesseract.setLanguage("spa");
        tesseract.setOcrEngineMode(1);
        tesseract.setPageSegMode(1);
        tesseract.setTessVariable("preserve_interword_spaces", "1");
        try {
            return tesseract.doOCR(image);
        } catch (TesseractException e) {
            throw new IllegalStateException("OCR falló para imagen: " + file.getOriginalFilename(), e);
        }
    }

    private String extractFirstGroup(Pattern pattern, String source) {
        if (!StringUtils.hasText(source)) {
            return "";
        }
        Matcher matcher = pattern.matcher(source);
        if (!matcher.find()) {
            return "";
        }
        String value = matcher.group(1);
        return value == null ? "" : value.trim();
    }

    private String sanitizeExtractedText(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return text
            .replaceAll("[\\r\\n]+", " ")
            .replaceAll("\\s{2,}", " ")
            .replaceAll("\\s+([,.;:])", "$1")
            .trim();
    }

    private String normalizeActaNumber(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String normalized = sanitizeExtractedText(text)
            .replaceAll("(?i)^n(?:[úu]mero|o\\.?|°)\\s*", "")
            .replaceAll("[^0-9A-Za-z/-]", "")
            .trim();
        return normalized;
    }

    private String normalizeFechaActa(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String cleaned = sanitizeExtractedText(text)
            .replaceAll("(?i)^del\\s+", "")
            .replaceAll("(?i)^día\\s+", "")
            .trim();
        return cleaned;
    }

    private String normalizePersonName(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String cleaned = sanitizeExtractedText(text)
            .replaceAll("(?i)^licenciado\\s+", "")
            .replaceAll("\\s{2,}", " ")
            .trim();
        if (cleaned.isEmpty()) {
            return "";
        }
        return java.util.Arrays.stream(cleaned.split("\\s+"))
            .map(token -> {
                if (token.isEmpty()) {
                    return token;
                }
                if (token.length() == 1) {
                    return token.toUpperCase(Locale.ROOT);
                }
                return token.substring(0, 1).toUpperCase(Locale.ROOT) + token.substring(1).toLowerCase(Locale.ROOT);
            })
            .reduce((a, b) -> a + " " + b)
            .orElse(cleaned);
    }

    private void putIfBlank(Map<String, String> values, String key, String candidate) {
        if (!StringUtils.hasText(key) || !StringUtils.hasText(candidate)) {
            return;
        }
        String current = values.get(key);
        if (!StringUtils.hasText(current)) {
            values.put(key, candidate);
        }
    }

    private String trimPreview(String fullText) {
        if (!StringUtils.hasText(fullText)) {
            return "";
        }
        if (fullText.length() <= PREVIEW_MAX_CHARS) {
            return fullText;
        }
        return fullText.substring(0, PREVIEW_MAX_CHARS) + "\n\n...[vista previa recortada]";
    }
}
