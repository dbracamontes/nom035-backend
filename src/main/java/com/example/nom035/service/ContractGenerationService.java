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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    private static final Logger log = LoggerFactory.getLogger(ContractGenerationService.class);

    private static final String DEFAULT_TEMPLATE = "DOCUMENTO_04_1";
    private static final int PREVIEW_MAX_CHARS = 12000;
    private static final int PAGE_LIMIT_ACTA = 4;
    private static final int PAGE_LIMIT_ASAMBLEA = 4;
    private static final int PAGE_LIMIT_CONSTANCIA_SITUACION_FISCAL = 3;
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
    private static final Pattern PATTERN_ESCRITURA_ASAMBLEA = Pattern.compile(
        "(?is)(?:acta\\s+de\\s+asamblea\\s+n[úu]mero|acta\\s+de\\s+asamblea\\s+extraordinaria\\s+n[úu]mero|acta\\s+de\\s+asamblea\\s+ordinaria\\s+n[úu]mero|asamblea\\s+protocolizada\\s+bajo\\s+el\\s+instrumento\\s+n[úu]mero|instrumento\\s+n[úu]mero|escritura\\s+p[úu]blica\\s+n[úu]mero)\\s*[:#\\-]?\\s*([0-9]{1,10}[A-Za-z0-9/-]*)"
    );
    private static final Pattern PATTERN_ESCRITURA_ASAMBLEA_LABEL = Pattern.compile(
        "(?im)^\\s*(?:por\\s+)?instrumento\\s*(?:n(?:[úu]mero|o\\.?|°))?\\s*[:#\\-]?\\s*([0-9]{1,10}[A-Za-z0-9/-]*)\\s*$"
    );
    private static final Pattern PATTERN_ESCRITURA_ASAMBLEA_INLINE = Pattern.compile(
        "(?is)(?:por\\s+)?instrumento\\s*(?:n(?:[úu]mero|o\\.?|°))?\\s*[:#\\-]?\\s*([0-9]{1,10}[A-Za-z0-9/-]*)"
    );
    private static final Pattern PATTERN_FECHA_ASAMBLEA_A_LOS = Pattern.compile(
        "(?is)a\\s+los\\s+(.{6,220}?)(?:,\\s*ante\\s*m[íi]|\\s+ante\\s*m[íi]|,\\s*ante\\s+la\\s+fe)"
    );
    private static final Pattern PATTERN_FECHA_ASAMBLEA_DE_FECHA = Pattern.compile(
        "(?is)de\\s+fecha\\s+(.{6,180}?)(?:,\\s*(?:tirada|otorgada|pasada|protocolizada)\\s+ante|\\s*;)"
    );
    private static final Pattern PATTERN_FECHA_ASAMBLEA_LABEL = Pattern.compile(
        "(?im)^\\s*de\\s+fecha\\s*[:#\\-]?\\s*([^\\r\\n]{6,120})\\s*$"
    );
    private static final Pattern PATTERN_FECHA_ASAMBLEA_NUMERIC_LABEL = Pattern.compile(
        "(?is)de\\s+fecha\\s*[:#\\-]?\\s*([0-9]{1,2}\\s*[/-]\\s*[0-9]{1,2}\\s*[/-]\\s*[0-9]{2,4})"
    );
    private static final Pattern PATTERN_FECHA_ASAMBLEA_INLINE = Pattern.compile(
        "(?is)de\\s+fecha\\s*[:#\\-]?\\s*([0-9]{1,2}[/-][0-9]{1,2}[/-][0-9]{2,4}|[0-9a-záéíóúñ\\s]{8,120}?)(?:\\s{2,}|\\s+nombre\\s*:|\\s+ante\\s+la\\s+fe|,\\s*tirada|\\.)"
    );
    private static final Pattern PATTERN_LICENCIADO_ASAMBLEA = Pattern.compile(
        "(?is)(?:ante\\s*m[íi]\\s+licenciado|ante\\s+la\\s+fe\\s+del\\s+licenciado|fe\\s+del\\s+licenciado|licenciado)\\s+([A-ZÁÉÍÓÚÑ][A-ZÁÉÍÓÚÑ\\s.]{3,120}?)(?:,\\s*(?:notario|corredor)|\\s+(?:notario|corredor)|\\s+titular|,)"
    );
    private static final Pattern PATTERN_NOMBRE_LABEL = Pattern.compile(
        "(?im)^\\s*nombre\\s*[:#\\-]?\\s*([A-ZÁÉÍÓÚÑa-záéíóúñ\\s.]{5,120})\\s*$"
    );
    private static final Pattern PATTERN_NOMBRE_INLINE = Pattern.compile(
        "(?is)nombre\\s*[:#\\-]?\\s*([A-ZÁÉÍÓÚÑa-záéíóúñ\\s.]{5,120}?)(?:\\s+no\\.?\\s*:|\\s+estado\\s*:|\\s+municipio\\s*:|\\s{2,}|,)"
    );
    private static final Pattern PATTERN_LICENCIADO_FROM_NOMBRE_NO = Pattern.compile(
        "(?is)nombre\\s*[:#\\-]?\\s*([A-ZÁÉÍÓÚÑa-záéíóúñ\\s.]{5,120}?)\\s+no\\.?\\s*[:#\\-]?\\s*[0-9]{1,5}"
    );
    private static final Pattern PATTERN_NOTARIA_PUBLICA = Pattern.compile(
        "(?is)(?:notar[ií]a\\s+p[úu]blica\\s+n(?:[úu]mero|o\\.?|°)?|notario\\s+p[úu]blico\\s+n(?:[úu]mero|o\\.?|°)?)\\s*[:#\\-]?\\s*([0-9]{1,5})"
    );
    private static final Pattern PATTERN_NOTARIA_NO_LABEL = Pattern.compile(
        "(?im)^\\s*no\\.?\\s*[:#\\-]?\\s*([0-9]{1,5})\\s*$"
    );
    private static final Pattern PATTERN_NOTARIA_NO_INLINE = Pattern.compile(
        "(?is)(?:notar[ií]a\\s+p[úu]blica\\s*)?no\\.?\\s*[:#\\-]?\\s*([0-9]{1,5})"
    );
    private static final Pattern PATTERN_NOTARIA_NO_AFTER_NOMBRE = Pattern.compile(
        "(?is)nombre\\s*[:#\\-]?\\s*[A-ZÁÉÍÓÚÑa-záéíóúñ\\s.]{5,120}?\\s+no\\.?\\s*[:#\\-]?\\s*([0-9]{1,5})"
    );
    private static final Pattern PATTERN_CIUDAD_ASAMBLEA = Pattern.compile(
        "(?is)en\\s+la\\s+ciudad\\s+de\\s+([A-ZÁÉÍÓÚÑa-záéíóúñ\\s]{3,80}?)(?:,|\\.)"
    );
    private static final Pattern PATTERN_CIUDAD_ASAMBLEA_PLAZA = Pattern.compile(
        "(?is)de\\s+la\\s+plaza\\s+del\\s+estado\\s+de\\s+([A-ZÁÉÍÓÚÑa-záéíóúñ\\s]{3,80}?)(?:,|\\.)"
    );
    private static final Pattern PATTERN_MUNICIPIO_LABEL = Pattern.compile(
        "(?im)^\\s*municipio\\s*[:#\\-]?\\s*([A-ZÁÉÍÓÚÑa-záéíóúñ\\s]{3,80})\\s*$"
    );
    private static final Pattern PATTERN_MUNICIPIO_INLINE = Pattern.compile(
        "(?is)municipio\\s*[:#\\-]?\\s*([A-ZÁÉÍÓÚÑa-záéíóúñ\\s]{3,80}?)(?:\\s+estado\\s*:|\\s{2,}|,|\\.)"
    );
    private static final Pattern PATTERN_ESTADO_LABEL = Pattern.compile(
        "(?im)^\\s*estado\\s*[:#\\-]?\\s*([A-ZÁÉÍÓÚÑa-záéíóúñ\\s]{3,80})\\s*$"
    );
    private static final Pattern PATTERN_CIUDADANO = Pattern.compile(
        "(?is)(?:el|la)\\s+ciudadan[oa]\\s+([A-ZÁÉÍÓÚÑ][A-ZÁÉÍÓÚÑ\\s]{4,120}?)(?:,|\\s+quien|\\s+en\\s+su\\s+car[áa]cter|\\s+mayor\\s+de\\s+edad)"
    );
    private static final Pattern PATTERN_CIUDADANO_MEXICANA = Pattern.compile(
        "(?is)\\b([A-ZÁÉÍÓÚÑ]{2,}(?:\\s+[A-ZÁÉÍÓÚÑ]{2,}){2,6})\\s*Mexican[ao]\\b"
    );
    private static final Pattern PATTERN_CIUDADANO_FUNCIONARIO = Pattern.compile(
        "(?is)\\b([A-ZÁÉÍÓÚÑ]{2,}(?:\\s+[A-ZÁÉÍÓÚÑ]{2,}){2,6})\\b\\s+[A-Z]{3,5}[A-Z0-9]{6,14}\\s+ADMINISTRADOR\\s+GENERAL\\s+[ÚU]NICO"
    );
    private static final Pattern PATTERN_CIUDADANO_SENORA = Pattern.compile(
        "(?is)(?:señora|senora|c\\.)\\s+([A-ZÁÉÍÓÚÑ][A-ZÁÉÍÓÚÑ\\s]{4,120}?)(?:,\\s*quien|,|\\s+quien)"
    );
    private static final Pattern PATTERN_CIUDADANO_SOLICITUD = Pattern.compile(
        "(?is)(?:consta\\s+que\\s+a\\s+solicitud\\s+de|a\\s+solicitud\\s+de)\\s*[:#\\-]?\\s*([A-ZÁÉÍÓÚÑa-záéíóúñ][A-ZÁÉÍÓÚÑa-záéíóúñ\\s]{4,160}?)(?:\\s+como\\s+representantes?\\(s\\)|\\s+como\\s+representante|,|\\.)"
    );
    private static final Pattern PATTERN_CIUDADANO_TABLE_NAME = Pattern.compile(
        "(?is)\\b([A-ZÁÉÍÓÚÑ]{2,}\\s+[A-ZÁÉÍÓÚÑ]{2,}\\s+[A-ZÁÉÍÓÚÑ]{2,})\\s*Mexican[ao]"
    );
    private static final Pattern PATTERN_CIUDADANO_MEXICANA_RFC = Pattern.compile(
        "(?is)([A-ZÁÉÍÓÚÑa-záéíóúñ]{2,}(?:\\s+[A-ZÁÉÍÓÚÑa-záéíóúñ]{2,}){2,6})\\s+Mexican[ao]\\s+[A-ZÑ&]{3,5}[A-Z0-9]{6,14}"
    );
    private static final Pattern PATTERN_CIUDADANO_CURP_CARGO = Pattern.compile(
        "(?is)curp\\s+cargo\\s+([A-ZÁÉÍÓÚÑa-záéíóúñ][A-ZÁÉÍÓÚÑa-záéíóúñ\\s]{4,140}?)(?:\\s+Mexican[ao]|\\s+[A-Z]{3,5}[A-Z0-9]{6,14}|,|\\.)"
    );
    private static final Pattern PATTERN_HEADER_FUNCIONARIOS = Pattern.compile(
        "(?is)nombramiento\\s+de\\s+funcionarios\\s+y/o\\s+apoderados\\s+y\\s+sus\\s+respectivas\\s+facultades"
    );
    private static final Pattern PATTERN_HEADER_COLUMNAS_FUNCIONARIOS = Pattern.compile(
        "(?is)nombre\\s+primer\\s+apellido\\s+segundo\\s+apellido\\s+rfc/?curp\\s+cargo\\s+facultades"
    );
    private static final Pattern PATTERN_CIUDADANO_FUNCIONARIO_ROW = Pattern.compile(
        "(?is)([A-ZÁÉÍÓÚÑa-záéíóúñ]{2,}(?:\\s+[A-ZÁÉÍÓÚÑa-záéíóúñ]{2,}){2,6})\\s+[A-Z]{3,5}[A-Z0-9]{6,14}\\s+(?:ADMINISTRADOR\\s+GENERAL\\s+[ÚU]NICO|APODERAD[OA]\\s+LEGAL|REPRESENTANTE\\s+LEGAL)"
    );
    private static final Pattern PATTERN_CIUDADANO_FUNCIONARIO_ROW_GLUED_RFC = Pattern.compile(
        "(?is)([A-ZÁÉÍÓÚÑa-záéíóúñ]{2,}(?:\\s+[A-ZÁÉÍÓÚÑa-záéíóúñ]{2,}){2,6})(?=[A-ZÑ&]{4}\\d{6}[A-Z0-9]{3}\\s*(?:ADMINISTRADOR\\s+GENERAL\\s+[ÚU]NICO|APODERAD[OA]\\s+LEGAL|REPRESENTANTE\\s+LEGAL))"
    );
    private static final Pattern PATTERN_CIUDADANO_FUNCIONARIO_ROW_FUZZY = Pattern.compile(
        "(?is)([A-ZÁÉÍÓÚÑ][A-ZÁÉÍÓÚÑa-záéíóúñ\\s]{8,140}?)(?=\\s*[A-Z]{3,5}\\d{6}[A-Z0-9]{2,8}\\s*(?:ADMINISTRADOR\\s+GENERAL\\s+[ÚU]NICO|APODERAD[OA]\\s+LEGAL|REPRESENTANTE\\s+LEGAL))"
    );
    private static final Pattern PATTERN_CIUDADANO_NAME_RFC = Pattern.compile(
        "(?is)([A-ZÁÉÍÓÚÑa-záéíóúñ]{2,}(?:\\s+[A-ZÁÉÍÓÚÑa-záéíóúñ]{2,}){2,6})\\s+[A-Z]{3,5}[A-Z0-9]{6,14}"
    );
    private static final Pattern PATTERN_CIUDADANO_NAME_RFC_FUZZY = Pattern.compile(
        "(?is)([A-ZÁÉÍÓÚÑ][A-ZÁÉÍÓÚÑa-záéíóúñ\\s]{8,140}?)(?=\\s*[A-Z]{3,5}\\d{6}[A-Z0-9]{2,8})"
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
        StringBuilder asambleaText = new StringBuilder();
        int processedDocuments = 0;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
            String effectiveDocumentType = detectDocumentType(filename, documentType);

            if (isPdfFile(filename)) {
                Integer pageLimitOverride = resolvePageLimitByDocumentType(effectiveDocumentType);
                DocumentJob job = documentInterpretationService.process(file, effectiveDocumentType, pageLimitOverride);
                sourceJobIds.add(job.getId());
                String interpretedText = appendJobPreview(mergedText, job.getId());
                String extractionText = collectJobExtractionText(job.getId());
                if ("ACTA".equalsIgnoreCase(effectiveDocumentType) && StringUtils.hasText(extractionText)) {
                    if (!actaText.isEmpty()) {
                        actaText.append("\n\n");
                    }
                    actaText.append(extractionText);
                } else if ("ASAMBLEA".equalsIgnoreCase(effectiveDocumentType) && StringUtils.hasText(extractionText)) {
                    if (!asambleaText.isEmpty()) {
                        asambleaText.append("\n\n");
                    }
                    asambleaText.append(extractionText);
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
                } else if ("ASAMBLEA".equalsIgnoreCase(effectiveDocumentType)) {
                    if (!asambleaText.isEmpty()) {
                        asambleaText.append("\n\n");
                    }
                    asambleaText.append(extracted.trim());
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
        applyAsambleaSpecificExtraction(suggestedValues, asambleaText.toString(), mergedText.toString());

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

    private String collectJobExtractionText(Long jobId) {
        List<DocumentPreviewChunkDto> chunks = documentInterpretationService.getPreview(jobId);
        if (chunks == null || chunks.isEmpty()) {
            return "";
        }
        StringBuilder captured = new StringBuilder();
        for (DocumentPreviewChunkDto chunk : chunks) {
            String raw = chunk.getRawText();
            String interpreted = chunk.getInterpretedText();
            String preferred = StringUtils.hasText(raw) ? raw : interpreted;
            if (StringUtils.hasText(preferred)) {
                if (!captured.isEmpty()) {
                    captured.append("\n\n");
                }
                captured.append(preferred.trim());
            }
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

    private void applyAsambleaSpecificExtraction(Map<String, String> values, String asambleaText, String mergedText) {
        String source = StringUtils.hasText(asambleaText) ? asambleaText : mergedText;
        if (!StringUtils.hasText(source)) {
            return;
        }

        String escrituraAsamblea = firstNonBlank(
            extractFirstGroup(PATTERN_ESCRITURA_ASAMBLEA_LABEL, source),
            extractFirstGroup(PATTERN_ESCRITURA_ASAMBLEA_INLINE, source),
            extractFirstGroup(PATTERN_ESCRITURA_ASAMBLEA, source)
        );
        String fechaAsamblea = firstNonBlank(
            extractFirstGroup(PATTERN_FECHA_ASAMBLEA_NUMERIC_LABEL, source),
            extractFirstGroup(PATTERN_FECHA_ASAMBLEA_LABEL, source),
            extractFirstGroup(PATTERN_FECHA_ASAMBLEA_INLINE, source),
            extractFirstGroup(PATTERN_FECHA_ASAMBLEA_A_LOS, source),
            extractFirstGroup(PATTERN_FECHA_ASAMBLEA_DE_FECHA, source)
        );
        String licenciadoAsamblea = firstNonBlank(
            extractFirstGroup(PATTERN_LICENCIADO_ASAMBLEA, source),
            extractFirstGroup(PATTERN_LICENCIADO_FROM_NOMBRE_NO, source),
            extractFirstGroup(PATTERN_NOMBRE_LABEL, source),
            extractFirstGroup(PATTERN_NOMBRE_INLINE, source)
        );
        String notariaNo = firstNonBlank(
            extractFirstGroup(PATTERN_NOTARIA_NO_AFTER_NOMBRE, source),
            extractFirstGroup(PATTERN_NOTARIA_PUBLICA, source),
            extractFirstGroup(PATTERN_NOTARIA_NO_LABEL, source),
            extractFirstGroup(PATTERN_NOTARIA_NO_INLINE, source)
        );
        String ciudad = normalizeCityWithState(
            firstNonBlank(
                extractFirstGroup(PATTERN_MUNICIPIO_LABEL, source),
                extractFirstGroup(PATTERN_MUNICIPIO_INLINE, source),
                extractFirstGroup(PATTERN_CIUDAD_ASAMBLEA, source),
                extractFirstGroup(PATTERN_CIUDAD_ASAMBLEA_PLAZA, source)
            ),
            extractFirstGroup(PATTERN_ESTADO_LABEL, source)
        );
        String ciudadano = inferCiudadanoFromAsamblea(source);
        if (!StringUtils.hasText(ciudadano)) {
            log.debug("No se pudo inferir CIUDADANO en asamblea. Preview fuente: {}", trimPreview(source));
        }

        putIfBlank(values, "CIUDADANO", normalizePersonName(ciudadano));
        putIfBlank(values, "ESCRITURA_PUBLICA_ASAMBLEA_NO", normalizeActaNumber(escrituraAsamblea));
        putIfBlank(values, "FECHA_ASAMBLEA", normalizeFechaActa(fechaAsamblea));
        putIfBlank(values, "LICENCIADO_ASAMBLEA_DA_FE", normalizePersonName(licenciadoAsamblea));
        putIfBlank(values, "NOTARIA_PUBLICA_NO", normalizeActaNumber(notariaNo));
        putIfBlank(values, "CIUDAD_ASAMBLEA", ciudad);
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

    private Integer resolvePageLimitByDocumentType(String documentType) {
        if (!StringUtils.hasText(documentType)) {
            return null;
        }
        if ("ACTA".equalsIgnoreCase(documentType)) {
            return PAGE_LIMIT_ACTA;
        }
        if ("ASAMBLEA".equalsIgnoreCase(documentType)) {
            return PAGE_LIMIT_ASAMBLEA;
        }
        if ("CONSTANCIA_SITUACION_FISCAL".equalsIgnoreCase(documentType)) {
            return PAGE_LIMIT_CONSTANCIA_SITUACION_FISCAL;
        }
        return null;
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

    private String extractLastGroup(Pattern pattern, String source) {
        if (!StringUtils.hasText(source)) {
            return "";
        }
        Matcher matcher = pattern.matcher(source);
        String last = "";
        while (matcher.find()) {
            String value = matcher.group(1);
            if (value != null && StringUtils.hasText(value.trim())) {
                last = value.trim();
            }
        }
        return last;
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
        String formattedNumeric = tryFormatNumericDate(cleaned);
        if (StringUtils.hasText(formattedNumeric)) {
            return formattedNumeric;
        }
        return cleaned;
    }

    private String normalizePersonName(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String cleaned = sanitizeExtractedText(text)
            // OCR of table headers may leak into the extracted person name.
            .replaceAll("(?i)^nombre\\s+primer\\s+apellido\\s+segundo\\s+apellido\\s+rfc/?curp\\s+cargo\\s*", "")
            .replaceAll("(?i)^(?:rfc/?curp\\s+cargo\\s*)+", "")
            .replaceAll("(?i)^(?:curp\\s+cargo\\s*)+", "")
            .replaceAll("(?i)^(?:cargo\\s+)+", "")
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

    private String normalizeCity(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String cleaned = sanitizeExtractedText(text)
            .replaceAll("(?i)^estado\\s+de\\s+", "")
            .replaceAll("\\s{2,}", " ")
            .trim();
        if (cleaned.isEmpty()) {
            return "";
        }
        return java.util.Arrays.stream(cleaned.split("\\s+"))
            .map(token -> token.isEmpty() ? token : token.substring(0, 1).toUpperCase(Locale.ROOT) + token.substring(1).toLowerCase(Locale.ROOT))
            .reduce((a, b) -> a + " " + b)
            .orElse(cleaned);
    }

    private String normalizeCityWithState(String city, String state) {
        String normalizedCity = normalizeCity(city);
        String normalizedState = normalizeCity(state);
        if (!StringUtils.hasText(normalizedCity)) {
            return "";
        }
        if (!StringUtils.hasText(normalizedState)) {
            return normalizedCity;
        }
        if (normalizedCity.toLowerCase(Locale.ROOT).contains(normalizedState.toLowerCase(Locale.ROOT))) {
            return normalizedCity;
        }
        return normalizedCity + ", " + normalizedState;
    }

    private String tryFormatNumericDate(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String normalized = text.trim().replaceAll("\\s+", "");
        Matcher matcher = Pattern.compile("^(\\d{1,2})[/-](\\d{1,2})[/-](\\d{2,4})$").matcher(normalized);
        if (!matcher.find()) {
            return "";
        }
        try {
            int day = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            int year = Integer.parseInt(matcher.group(3));
            if (year < 100) {
                year = 2000 + year;
            }
            LocalDate date = LocalDate.of(year, month, day);
            String monthName = date.format(DateTimeFormatter.ofPattern("MMMM", new Locale("es", "MX"))).toLowerCase(Locale.ROOT);
            return day + " de " + monthName + " del año " + year;
        } catch (Exception ignored) {
            return "";
        }
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "";
    }

    private String inferCiudadanoFromAsamblea(String source) {
        if (!StringUtils.hasText(source)) {
            return "";
        }

        // Prefer explicit shareholder/partner rows (Nombre + Mexicana/o + RFC), taking the latest occurrence.
        String fromMexicanaRfc = extractLastGroup(PATTERN_CIUDADANO_MEXICANA_RFC, source);
        if (isValidCiudadanoCandidate(fromMexicanaRfc)) {
            return fromMexicanaRfc;
        }

        // 0) Most specific signal in these registry formats.
        String fromCurpCargo = extractFirstGroup(PATTERN_CIUDADANO_CURP_CARGO, source);
        if (isValidCiudadanoCandidate(fromCurpCargo)) {
            return fromCurpCargo;
        }

        // 1) Best signal: table row with cargo (ADMINISTRADOR/APODERADO/REPRESENTANTE).
        String fromFuncionarioRow = extractFirstGroup(PATTERN_CIUDADANO_FUNCIONARIO_ROW, source);
        if (isValidCiudadanoCandidate(fromFuncionarioRow)) {
            return fromFuncionarioRow;
        }
        String fromFuncionarioRowGlued = extractFirstGroup(PATTERN_CIUDADANO_FUNCIONARIO_ROW_GLUED_RFC, source);
        if (isValidCiudadanoCandidate(fromFuncionarioRowGlued)) {
            return fromFuncionarioRowGlued;
        }
        String fromFuncionarioRowFuzzy = extractFirstGroup(PATTERN_CIUDADANO_FUNCIONARIO_ROW_FUZZY, source);
        if (isValidCiudadanoCandidate(fromFuncionarioRowFuzzy)) {
            return fromFuncionarioRowFuzzy;
        }

        // 2) If the funcionarios heading exists, only inspect that nearby block to reduce false positives.
        Matcher headerMatcher = PATTERN_HEADER_FUNCIONARIOS.matcher(source);
        if (headerMatcher.find()) {
            int start = headerMatcher.start();
            int end = Math.min(source.length(), start + 3000);
            String section = source.substring(start, end);
            String fromSectionFuncionario = extractFirstGroup(PATTERN_CIUDADANO_FUNCIONARIO_ROW, section);
            if (isValidCiudadanoCandidate(fromSectionFuncionario)) {
                return fromSectionFuncionario;
            }
            String fromSectionFuncionarioGlued = extractFirstGroup(PATTERN_CIUDADANO_FUNCIONARIO_ROW_GLUED_RFC, section);
            if (isValidCiudadanoCandidate(fromSectionFuncionarioGlued)) {
                return fromSectionFuncionarioGlued;
            }
            String fromSectionFuncionarioFuzzy = extractFirstGroup(PATTERN_CIUDADANO_FUNCIONARIO_ROW_FUZZY, section);
            if (isValidCiudadanoCandidate(fromSectionFuncionarioFuzzy)) {
                return fromSectionFuncionarioFuzzy;
            }
            String fromSectionNameRfc = extractFirstGroup(PATTERN_CIUDADANO_NAME_RFC, section);
            if (isValidCiudadanoCandidate(fromSectionNameRfc)) {
                return fromSectionNameRfc;
            }
            String fromSectionNameRfcFuzzy = extractFirstGroup(PATTERN_CIUDADANO_NAME_RFC_FUZZY, section);
            if (isValidCiudadanoCandidate(fromSectionNameRfcFuzzy)) {
                return fromSectionNameRfcFuzzy;
            }

            Matcher columnasMatcher = PATTERN_HEADER_COLUMNAS_FUNCIONARIOS.matcher(section);
            if (columnasMatcher.find()) {
                int rowStart = columnasMatcher.end();
                int rowEnd = Math.min(section.length(), rowStart + 1200);
                String tableRows = section.substring(rowStart, rowEnd);
                String fromRows = firstNonBlank(
                    extractFirstGroup(PATTERN_CIUDADANO_FUNCIONARIO_ROW, tableRows),
                    extractFirstGroup(PATTERN_CIUDADANO_FUNCIONARIO_ROW_GLUED_RFC, tableRows),
                    extractFirstGroup(PATTERN_CIUDADANO_FUNCIONARIO_ROW_FUZZY, tableRows),
                    extractFirstGroup(PATTERN_CIUDADANO_NAME_RFC, tableRows),
                    extractFirstGroup(PATTERN_CIUDADANO_NAME_RFC_FUZZY, tableRows)
                );
                if (isValidCiudadanoCandidate(fromRows)) {
                    return fromRows;
                }
            }
        }

        // 3) Fallbacks from previous strategy.
        String fallback = firstNonBlank(
            extractFirstGroup(PATTERN_CIUDADANO_SOLICITUD, source),
            extractFirstGroup(PATTERN_CIUDADANO, source),
            extractFirstGroup(PATTERN_CIUDADANO_FUNCIONARIO, source),
            extractFirstGroup(PATTERN_CIUDADANO_FUNCIONARIO_ROW_FUZZY, source),
            extractLastGroup(PATTERN_CIUDADANO_MEXICANA, source),
            extractFirstGroup(PATTERN_CIUDADANO_TABLE_NAME, source),
            extractFirstGroup(PATTERN_CIUDADANO_NAME_RFC_FUZZY, source),
            extractFirstGroup(PATTERN_CIUDADANO_SENORA, source)
        );
        return isValidCiudadanoCandidate(fallback) ? fallback : "";
    }

    private boolean isValidCiudadanoCandidate(String candidate) {
        if (!StringUtils.hasText(candidate)) {
            return false;
        }
        String c = sanitizeExtractedText(candidate);
        if (!StringUtils.hasText(c)) {
            return false;
        }
        // Reject table/header noise and role descriptions.
        if (c.matches("(?is).*(facultades?|que\\s+conserva|cargo|gerente|administrador|apoderad[oa]|representante|rfc|curp|nombre\\s+primer\\s+apellido).*")) {
            return false;
        }
        // Require at least 3 tokens for a valid full name.
        String[] tokens = c.trim().split("\\s+");
        return tokens.length >= 3;
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
