package com.example.nom035.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.nom035.dto.ContractGenerateRequestDto;
import com.example.nom035.dto.ContractMovementLogItemDto;
import com.example.nom035.dto.ContractPrepareResponseDto;
import com.example.nom035.dto.DocumentPreviewChunkDto;
import com.example.nom035.dto.DocumentTemplateFieldDto;
import com.example.nom035.entity.DocumentJob;
import com.example.nom035.repository.DocumentJobRepository;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
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
    private static final String DOC_TYPE_ACTA = "ACTA";
    private static final String DOC_TYPE_ASAMBLEA = "ASAMBLEA";
    private static final String DOC_TYPE_CONSTANCIA = "CONSTANCIA_SITUACION_FISCAL";
    private static final String SOURCE_MODULE_CONTRACT_GENERATION = "CONTRACT_GENERATION";
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
    private static final Pattern PATTERN_CONSTANCIA_IDENTIFICACION_SECTION = Pattern.compile(
        "(?is)datos\\s+de\\s+identificaci[oó]n\\s+del\\s+contribuyente\\b(.*?)(?=datos\\s+del\\s+domicilio(?:\\s+registrado|\\s+fiscal)?\\b|datos\\s+del\\s+r[eé]gimen|r[eé]gimen\\s+de\\s+capital|actividades?\\s+econ[oó]micas?|$)"
    );
    private static final Pattern PATTERN_CONSTANCIA_DOMICILIO_SECTION = Pattern.compile(
        "(?is)datos\\s+del\\s+domicilio(?:\\s+registrado|\\s+fiscal)?\\b(.*?)(?=datos\\s+de\\s+ubicaci[oó]n|actividades?\\s+econ[oó]micas?|r[eé]gimen\\s+de\\s+capital|$)"
    );
    private static final Pattern PATTERN_CONSTANCIA_RFC = Pattern.compile(
        "(?is)\\brfc(?:\\s*/\\s*curp)?\\s*[:\\-]?\\s*([A-ZÑ&]{3,4}\\s*[A-Z0-9]{6}\\s*[A-Z0-9]{3})\\b"
    );
    private static final Pattern PATTERN_CONSTANCIA_RFC_CLAVE = Pattern.compile(
        "(?is)(?:registro\\s+federal\\s+de\\s+contribuyentes|clave)\\s*(?:bajo\\s+la\\s+clave|rfc|fiscal)?\\s*[:\\-]?\\s*([A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3})\\b"
    );
    private static final Pattern PATTERN_CONSTANCIA_RFC_GENERIC = Pattern.compile(
        "(?is)\\b([A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3})\\b"
    );
    private static final String DOMICILIO_NEXT_LABEL =
        "(?=\\b(?:tipo\\s+de\\s+vialidad|tipo\\s+vialidad|vialidad|nombre\\s+de\\s+vialidad|nombre\\s+vialidad|"
            + "n[uú]mero\\s+exterior|n[uú]mero\\s+interior|num\\.?\\s+exterior|num\\.?\\s+interior|"
            + "no\\.?\\s*ext(?:erior)?|no\\.?\\s*int(?:erior)?|nombre\\s+de\\s+la\\s+colonia|colonia|"
            + "nombre\\s+de\\s+la\\s+localidad|localidad|municipio|nombre\\s+de\\s+la\\s+entidad\\s+federativa|"
            + "entidad\\s+federativa|estado|c[oó]digo\\s+postal|c\\.?p\\.?|cp)\\b|$)";
    private static final Pattern PATTERN_DOMICILIO_TIPO_VIALIDAD = Pattern.compile(
        "(?is)(?:tipo\\s+de\\s+vialidad|tipo\\s+vialidad|vialidad)\\s*[:\\-]?\\s*(.+?)" + DOMICILIO_NEXT_LABEL
    );
    private static final Pattern PATTERN_DOMICILIO_NOMBRE_VIALIDAD = Pattern.compile(
        "(?is)(?:nombre\\s+de\\s+vialidad|nombre\\s+vialidad)\\s*[:\\-]?\\s*(.+?)" + DOMICILIO_NEXT_LABEL
    );
    private static final Pattern PATTERN_DOMICILIO_NUMERO_EXTERIOR = Pattern.compile(
        "(?is)(?:n[uú]mero|num\\.?|no\\.?)\\s*(?:exterior|ext\\b)\\s*[:\\-]?\\s*(.+?)" + DOMICILIO_NEXT_LABEL
    );
    private static final Pattern PATTERN_DOMICILIO_NUMERO_INTERIOR = Pattern.compile(
        "(?is)(?:n[uú]mero|num\\.?|no\\.?)\\s*(?:interior|int\\b)\\s*[:\\-]?\\s*(.+?)" + DOMICILIO_NEXT_LABEL
    );
    private static final Pattern PATTERN_DOMICILIO_COLONIA = Pattern.compile(
        "(?is)(?:nombre\\s+de\\s+la\\s+colonia|colonia)\\s*[:\\-]?\\s*(.+?)" + DOMICILIO_NEXT_LABEL
    );
    private static final Pattern PATTERN_DOMICILIO_LOCALIDAD = Pattern.compile(
        "(?is)(?:nombre\\s+de\\s+la\\s+localidad|localidad|municipio)\\s*[:\\-]?\\s*(.+?)" + DOMICILIO_NEXT_LABEL
    );
    private static final Pattern PATTERN_DOMICILIO_ENTIDAD = Pattern.compile(
        "(?is)(?:nombre\\s+de\\s+la\\s+entidad\\s+federativa|entidad\\s+federativa|estado)\\s*[:\\-]?\\s*(.+?)" + DOMICILIO_NEXT_LABEL
    );
    private static final Pattern PATTERN_DOMICILIO_CP = Pattern.compile(
        "(?is)(?:c[oó]digo\\s+postal|c\\.?p\\.?|cp)\\s*[:\\-]?\\s*(.+?)" + DOMICILIO_NEXT_LABEL
    );
    private static final Pattern PATTERN_NON_WORD_OR_SPACE = Pattern.compile("[^\\p{L}\\p{N}\\s]");

    private final DocumentInterpretationService documentInterpretationService;
    private final DocumentTemplateCatalogService documentTemplateCatalogService;
    private final DocumentCreationService documentCreationService;
    private final DocumentOpenAiService documentOpenAiService;
    private final DocumentJobRepository documentJobRepository;
    private final ObjectMapper objectMapper;

    public ContractGenerationService(DocumentInterpretationService documentInterpretationService,
                                     DocumentTemplateCatalogService documentTemplateCatalogService,
                                     DocumentCreationService documentCreationService,
                                     DocumentOpenAiService documentOpenAiService,
                                     DocumentJobRepository documentJobRepository) {
        this.documentInterpretationService = documentInterpretationService;
        this.documentTemplateCatalogService = documentTemplateCatalogService;
        this.documentCreationService = documentCreationService;
        this.documentOpenAiService = documentOpenAiService;
        this.documentJobRepository = documentJobRepository;
        this.objectMapper = new ObjectMapper();
    }

    public ContractPrepareResponseDto prepare(List<MultipartFile> files, String documentType, String templateType) {
        if (files == null || files.size() < 3) {
            throw new IllegalArgumentException("Se requieren al menos 3 documentos para preparar el contrato");
        }

        String resolvedTemplate = StringUtils.hasText(templateType) ? templateType : DEFAULT_TEMPLATE;
        List<Long> sourceJobIds = new ArrayList<>();
        StringBuilder mergedText = new StringBuilder();
        Map<String, StringBuilder> extractionTextByType = new LinkedHashMap<>();
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
                appendJobPreview(mergedText, job.getId());
                String extractionText = collectJobExtractionText(job.getId());
                appendByDocumentType(extractionTextByType, effectiveDocumentType, extractionText);
            } else {
                String extracted = extractTextFromNonPdf(file, filename);
                if (!StringUtils.hasText(extracted)) {
                    continue;
                }
                if (!mergedText.isEmpty()) {
                    mergedText.append("\n\n");
                }
                mergedText.append(extracted.trim());
                appendByDocumentType(extractionTextByType, effectiveDocumentType, extracted);
            }
            processedDocuments++;
        }

        if (processedDocuments < 3) {
            throw new IllegalArgumentException("Debes subir 3 o más documentos válidos");
        }

        List<DocumentTemplateFieldDto> templateFields = documentTemplateCatalogService.getFieldsByType(resolvedTemplate);
        Map<String, String> suggestedValues = buildSuggestedValues(templateFields);
        applyDocumentExtractionPipeline(suggestedValues, extractionTextByType, mergedText.toString());
        Map<String, String> aiFieldCandidates = collectAiContractFieldCandidates(mergedText.toString());
        applyAiFieldCandidates(suggestedValues, aiFieldCandidates);

        ContractPrepareResponseDto response = new ContractPrepareResponseDto();
        response.setTemplateType(resolvedTemplate);
        response.setSourceJobIds(sourceJobIds);
        response.setFields(templateFields);
        response.setSuggestedValues(suggestedValues);
        response.setCombinedPreview(trimPreview(mergedText.toString()));
        return response;
    }

    private void applyDocumentExtractionPipeline(Map<String, String> values,
                                                 Map<String, StringBuilder> extractionTextByType,
                                                 String mergedText) {
        applyActaSpecificExtraction(values, resolveSourceText(extractionTextByType, DOC_TYPE_ACTA, mergedText));
        applyAsambleaSpecificExtraction(values, resolveSourceText(extractionTextByType, DOC_TYPE_ASAMBLEA, mergedText));
        applyConstanciaSpecificExtraction(values, resolveSourceText(extractionTextByType, DOC_TYPE_CONSTANCIA, mergedText));
    }

    public DocumentJob generate(ContractGenerateRequestDto request) {
        String resolvedTemplate = StringUtils.hasText(request.getTemplateType()) ? request.getTemplateType() : DEFAULT_TEMPLATE;
        Map<String, String> fields = request.getFields() == null ? Map.of() : request.getFields();
        String ciudadano = trimToNull(fields.get("CIUDADANO"));
        String clientName = resolveClientName(fields);
        if (clientName == null) {
            clientName = ciudadano;
        }
        String createdByUser = resolveCurrentUsername();

        LocalDate contractDate = parseDateFromFields(fields, "DIA", "MES", "AÑO");

        LocalDate vigenciaStartDate = parseDateFromFields(
            fields,
            "DIA_INICIO_VIGENCIA",
            "MES_INICIO_VIGENCIA",
            "AÑO_INICIO_VIGENCIA"
        );
        if (vigenciaStartDate == null) {
            vigenciaStartDate = parseDateFromFields(fields, "VID", "VIM", "VIA");
        }

        LocalDate vigenciaEndDate = parseDateFromFields(
            fields,
            "DIA_TERMINO_VIGENCIA",
            "MES_TERMINO_VIGENCIA",
            "AÑO_TERMINO_VIGENCIA"
        );
        if (vigenciaEndDate == null) {
            vigenciaEndDate = parseDateFromFields(fields, "VTD", "VTM", "VTA");
        }

        DocumentCreationService.DocumentJobMetadata metadata = new DocumentCreationService.DocumentJobMetadata(
            SOURCE_MODULE_CONTRACT_GENERATION,
            resolvedTemplate,
            clientName,
            ciudadano,
            createdByUser,
            contractDate,
            vigenciaStartDate,
            vigenciaEndDate
        );

        return documentCreationService.generateManual(resolvedTemplate, fields, metadata);
    }

    public List<ContractMovementLogItemDto> getMovementLog() {
        List<DocumentJob> jobs = documentJobRepository
            .findTop200BySourceModuleOrderByCreatedAtDesc(SOURCE_MODULE_CONTRACT_GENERATION);

        return jobs.stream().map(job -> {
            ContractMovementLogItemDto dto = new ContractMovementLogItemDto();
            dto.setJobId(job.getId());
            dto.setCreatedByUser(job.getCreatedByUser());
            dto.setTemplateType(job.getTemplateType());
            dto.setClientName(job.getClientName() != null ? job.getClientName() : job.getCiudadano());
            dto.setStatus(job.getStatus() != null ? job.getStatus().name() : null);
            dto.setContractDate(job.getContractDate());
            dto.setVigenciaStartDate(job.getVigenciaStartDate());
            dto.setVigenciaEndDate(job.getVigenciaEndDate());
            dto.setCreatedAt(job.getCreatedAt());
            dto.setCompletedAt(job.getCompletedAt());
            return dto;
        }).toList();
    }

    private String resolveCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String username = trimToNull(authentication.getName());
        if (username == null || "anonymousUser".equalsIgnoreCase(username)) {
            return null;
        }
        return username;
    }

    private LocalDate parseDateFromFields(Map<String, String> fields, String dayKey, String monthKey, String yearKey) {
        if (fields == null || fields.isEmpty()) {
            return null;
        }
        String dayRaw = trimToNull(fields.get(dayKey));
        String monthRaw = trimToNull(fields.get(monthKey));
        String yearRaw = trimToNull(fields.get(yearKey));
        if (dayRaw == null || monthRaw == null || yearRaw == null) {
            return null;
        }

        Integer day = parseInteger(dayRaw);
        Integer year = parseInteger(yearRaw);
        Integer month = parseMonth(monthRaw);
        if (day == null || year == null || month == null) {
            return null;
        }

        try {
            return LocalDate.of(year, month, day);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer parseMonth(String monthRaw) {
        Integer numericMonth = parseInteger(monthRaw);
        if (numericMonth != null && numericMonth >= 1 && numericMonth <= 12) {
            return numericMonth;
        }

        String normalized = Normalizer
            .normalize(monthRaw, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .trim()
            .toUpperCase(Locale.ROOT);

        return switch (normalized) {
            case "ENERO" -> 1;
            case "FEBRERO" -> 2;
            case "MARZO" -> 3;
            case "ABRIL" -> 4;
            case "MAYO" -> 5;
            case "JUNIO" -> 6;
            case "JULIO" -> 7;
            case "AGOSTO" -> 8;
            case "SEPTIEMBRE" -> 9;
            case "OCTUBRE" -> 10;
            case "NOVIEMBRE" -> 11;
            case "DICIEMBRE" -> 12;
            default -> null;
        };
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String resolveClientName(Map<String, String> fields) {
        if (fields == null || fields.isEmpty()) {
            return null;
        }

        String[] priorityKeys = {
            "RAZON_SOCIAL",
            "NOMBRE_EMPRESA",
            "EMPRESA",
            "CLIENTE",
            "NOMBRE_CLIENTE",
            "COMPANY_NAME",
            "CONTRATANTE",
            "CIUDADANO"
        };

        for (String key : priorityKeys) {
            String candidate = trimToNull(fields.get(key));
            if (candidate != null) {
                return candidate;
            }
        }

        return fields.entrySet().stream()
            .filter(entry -> StringUtils.hasText(entry.getValue()))
            .sorted(Comparator.comparing(entry -> entry.getKey() == null ? "" : entry.getKey()))
            .filter(entry -> {
                String key = String.valueOf(entry.getKey()).toUpperCase(Locale.ROOT);
                return key.contains("RAZON")
                    || key.contains("EMPRESA")
                    || key.contains("CLIENTE")
                    || key.contains("CONTRATANTE")
                    || key.contains("COMPANY");
            })
            .map(entry -> trimToNull(entry.getValue()))
            .filter(StringUtils::hasText)
            .findFirst()
            .orElse(null);
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

    private Map<String, String> collectAiContractFieldCandidates(String consolidatedText) {
        Map<String, String> extracted = new LinkedHashMap<>();
        if (!StringUtils.hasText(consolidatedText)) {
            return extracted;
        }

        try {
            String interpreted = documentOpenAiService.interpret(consolidatedText, "CONTRACT_PACKAGE");
            if (!StringUtils.hasText(interpreted) || !interpreted.trim().startsWith("{")) {
                return extracted;
            }
            JsonNode root = objectMapper.readTree(interpreted);
            JsonNode candidates = root.path("contract_field_candidates");
            if (!candidates.isObject()) {
                return extracted;
            }
            candidates.fields().forEachRemaining(entry -> {
                String key = entry.getKey() == null ? "" : entry.getKey().trim().toUpperCase(Locale.ROOT);
                String value = entry.getValue() == null ? "" : sanitizeExtractedText(entry.getValue().asText(""));
                if (StringUtils.hasText(key) && StringUtils.hasText(value)) {
                    extracted.putIfAbsent(key, value);
                }
            });
        } catch (Exception ex) {
            log.debug("No se pudo parsear extraction IA consolidada para contrato", ex);
        }

        return extracted;
    }

    private void applyAiFieldCandidates(Map<String, String> values, Map<String, String> aiFieldCandidates) {
        if (values == null || aiFieldCandidates == null || aiFieldCandidates.isEmpty()) {
            return;
        }
        aiFieldCandidates.forEach((key, candidate) -> {
            if (!StringUtils.hasText(key) || !StringUtils.hasText(candidate)) {
                return;
            }
            if (values.containsKey(key)) {
                putIfBlank(values, key, candidate);
            }
        });
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

    private void applyActaSpecificExtraction(Map<String, String> values, String source) {
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

    private void applyAsambleaSpecificExtraction(Map<String, String> values, String source) {
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

    private void applyConstanciaSpecificExtraction(Map<String, String> values, String source) {
        if (!StringUtils.hasText(source)) {
            return;
        }

        String identificacionSection = extractFirstGroup(PATTERN_CONSTANCIA_IDENTIFICACION_SECTION, source);
        String domicilioSection = extractFirstGroup(PATTERN_CONSTANCIA_DOMICILIO_SECTION, source);

        String rfc = inferRfcFromConstancia(identificacionSection, source);
        String domicilio = buildConstanciaDomicilio(domicilioSection);

        putIfBlank(values, "RFC", normalizeRfc(rfc));
        putIfBlank(values, "DOMICILIO", domicilio);
    }

    private String inferRfcFromConstancia(String identificacionSection, String source) {
        return firstNonBlank(
            extractFirstGroup(PATTERN_CONSTANCIA_RFC, identificacionSection),
            extractFirstGroup(PATTERN_CONSTANCIA_RFC_CLAVE, identificacionSection),
            extractFirstGroup(PATTERN_CONSTANCIA_RFC, source),
            extractFirstGroup(PATTERN_CONSTANCIA_RFC_CLAVE, source),
            extractFirstGroup(PATTERN_CONSTANCIA_RFC_GENERIC, identificacionSection),
            extractFirstGroup(PATTERN_CONSTANCIA_RFC_GENERIC, source)
        );
    }

    private String buildConstanciaDomicilio(String domicilioSection) {
        if (!StringUtils.hasText(domicilioSection)) {
            return "";
        }

        List<String> parts = new ArrayList<>();
        addAddressPart(parts, extractFirstGroup(PATTERN_DOMICILIO_TIPO_VIALIDAD, domicilioSection));
        addAddressPart(parts, extractFirstGroup(PATTERN_DOMICILIO_NOMBRE_VIALIDAD, domicilioSection));
        addAddressPart(parts, extractFirstGroup(PATTERN_DOMICILIO_NUMERO_EXTERIOR, domicilioSection));
        addAddressPart(parts, extractFirstGroup(PATTERN_DOMICILIO_NUMERO_INTERIOR, domicilioSection));
        addAddressPart(parts, extractFirstGroup(PATTERN_DOMICILIO_COLONIA, domicilioSection));
        addAddressPart(parts, extractFirstGroup(PATTERN_DOMICILIO_LOCALIDAD, domicilioSection));
        addAddressPart(parts, extractFirstGroup(PATTERN_DOMICILIO_ENTIDAD, domicilioSection));
        addAddressPart(parts, extractFirstGroup(PATTERN_DOMICILIO_CP, domicilioSection));

        return formatConstanciaDomicilio(parts);
    }

    private void addAddressPart(List<String> parts, String value) {
        String normalized = normalizeAddressComponent(value);
        if (StringUtils.hasText(normalized)) {
            parts.add(normalized);
        }
    }

    private String formatConstanciaDomicilio(List<String> parts) {
        if (parts == null || parts.isEmpty()) {
            return "";
        }

        String tipo = safePart(parts, 0);
        String vialidad = safePart(parts, 1);
        String numeroExterior = safePart(parts, 2);
        String numeroInterior = safePart(parts, 3);
        String colonia = safePart(parts, 4);
        String localidad = safePart(parts, 5);
        String entidad = safePart(parts, 6);
        String cp = safePart(parts, 7);

        String vialidadCompleta = firstNonBlank(
            joinNonBlank(" ", toTitleCase(tipo), toTitleCase(vialidad)),
            toTitleCase(vialidad),
            toTitleCase(tipo)
        );

        List<String> rendered = new ArrayList<>();
        addRendered(rendered, vialidadCompleta);
        addRendered(rendered, StringUtils.hasText(numeroExterior) ? "numero " + sanitizeExtractedText(numeroExterior) : "");

        String interiorSegment = "";
        if (StringUtils.hasText(numeroInterior)) {
            String interiorRaw = sanitizeExtractedText(numeroInterior).replaceAll("^[\"'“”]+|[\"'“”]+$", "");
            interiorSegment = "Interior \"" + interiorRaw + "\"";
        }
        if (StringUtils.hasText(colonia)) {
            interiorSegment = StringUtils.hasText(interiorSegment)
                ? interiorSegment + " Colonia " + toTitleCase(colonia)
                : "Colonia " + toTitleCase(colonia);
        }
        addRendered(rendered, interiorSegment);

        addRendered(rendered, toTitleCase(localidad));
        addRendered(rendered, toTitleCase(entidad));
        addRendered(rendered, StringUtils.hasText(cp) ? "Codigo Postal " + sanitizeExtractedText(cp) : "");

        return String.join(", ", rendered);
    }

    private String safePart(List<String> parts, int index) {
        if (parts == null || index < 0 || index >= parts.size()) {
            return "";
        }
        return parts.get(index);
    }

    private void addRendered(List<String> rendered, String value) {
        if (StringUtils.hasText(value)) {
            rendered.add(value);
        }
    }

    private String joinNonBlank(String separator, String... values) {
        List<String> chunks = new ArrayList<>();
        if (values != null) {
            for (String value : values) {
                if (StringUtils.hasText(value)) {
                    chunks.add(value.trim());
                }
            }
        }
        return String.join(separator, chunks);
    }

    private String resolveSourceText(Map<String, StringBuilder> extractionTextByType,
                                     String documentType,
                                     String mergedText) {
        String typeText = getTextByDocumentType(extractionTextByType, documentType);
        if (StringUtils.hasText(typeText)) {
            return typeText;
        }
        return mergedText;
    }

    private String getTextByDocumentType(Map<String, StringBuilder> extractionTextByType, String documentType) {
        if (extractionTextByType == null || !StringUtils.hasText(documentType)) {
            return "";
        }
        StringBuilder text = extractionTextByType.get(documentType.toUpperCase(Locale.ROOT));
        if (text == null) {
            return "";
        }
        return text.toString();
    }

    private void appendByDocumentType(Map<String, StringBuilder> extractionTextByType,
                                      String documentType,
                                      String text) {
        if (!StringUtils.hasText(documentType) || !StringUtils.hasText(text)) {
            return;
        }
        String normalizedType = documentType.toUpperCase(Locale.ROOT);
        StringBuilder sb = extractionTextByType.computeIfAbsent(normalizedType, key -> new StringBuilder());
        if (!sb.isEmpty()) {
            sb.append("\n\n");
        }
        sb.append(text.trim());
    }

    private String detectDocumentType(String filename, String defaultDocumentType) {
        String base = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        if (base.contains("acta")) {
            return DOC_TYPE_ACTA;
        }
        if (base.contains("asamblea")) {
            return DOC_TYPE_ASAMBLEA;
        }
        if (base.contains("constancia") || base.contains("fiscal") || base.contains("situacion")) {
            return DOC_TYPE_CONSTANCIA;
        }
        return StringUtils.hasText(defaultDocumentType) ? defaultDocumentType : DOC_TYPE_ACTA;
    }

    private Integer resolvePageLimitByDocumentType(String documentType) {
        if (!StringUtils.hasText(documentType)) {
            return null;
        }
        if (DOC_TYPE_ACTA.equalsIgnoreCase(documentType)) {
            return PAGE_LIMIT_ACTA;
        }
        if (DOC_TYPE_ASAMBLEA.equalsIgnoreCase(documentType)) {
            return PAGE_LIMIT_ASAMBLEA;
        }
        if (DOC_TYPE_CONSTANCIA.equalsIgnoreCase(documentType)) {
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

    private String normalizeRfc(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String normalized = sanitizeExtractedText(text)
            .toUpperCase(Locale.ROOT)
            .replaceAll("[^A-Z0-9Ñ&]", "");
        if (normalized.length() < 12 || normalized.length() > 13) {
            return "";
        }
        return normalized;
    }

    private String normalizeAddressComponent(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return sanitizeExtractedText(text)
            .replaceAll("^[,;:\\-\\s]+", "")
            .trim();
    }

    private String toTitleCase(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String cleaned = sanitizeExtractedText(text).trim();
        if (cleaned.isEmpty()) {
            return "";
        }
        return java.util.Arrays.stream(cleaned.split("\\s+"))
            .map(this::normalizeTokenCase)
            .reduce((a, b) -> a + " " + b)
            .orElse(cleaned);
    }

    private String normalizeTokenCase(String token) {
        if (!StringUtils.hasText(token)) {
            return "";
        }
        String preserved = token.trim();
        String alnumOnly = PATTERN_NON_WORD_OR_SPACE.matcher(preserved).replaceAll("");
        if (alnumOnly.matches("\\d+")) {
            return preserved;
        }
        return preserved.substring(0, 1).toUpperCase(Locale.ROOT) + preserved.substring(1).toLowerCase(Locale.ROOT);
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
