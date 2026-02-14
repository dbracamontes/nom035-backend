package com.example.nom035.service;

import com.example.nom035.dto.DocumentTemplateDto;
import com.example.nom035.dto.DocumentTemplateFieldDto;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DocumentTemplateCatalogService {

    public enum TemplateType {
        PROPUESTA_NEGOCIACION(
            "PROPUESTA_NEGOCIACION",
            "1.- PROPUESTA DE NEGOCIACIÓN DE CONTRATO COLECTIVO DE TRABAJO",
            "1.- PROPUESTA DE NEGOCIACIÓN DE CONTRATO COLECTIVO DE TRABAJO.docx",
            "propuesta_negociacion.py",
            true
        ),
        CLAUSULA_CONTRATO_PATRON_NOM2U(
            "CLAUSULA_CONTRATO_PATRON_NOM2U",
            "2.1.- CLAUSULA CONTRATO PATRÓN NOM2U",
            "2.1/2.1.- CLAUSULA CONTRATO PATRÓN NOM2U.docx",
            "2.1/patron_nom2u.py",
            true
        ),
        DOCUMENTO_02(
            "DOCUMENTO_02",
            "2.- COLECTIVO HANFU",
            "2/2.-Colectivo HANFU.docx",
            null,
            true
        ),
        DOCUMENTO_03(
            "DOCUMENTO_03",
            "3.- FORMATO PROPUESTA DE SERVICIOS NOMS",
            "3/3.-FORMATO PROPUESTA DE SERVICIOS NOMS.docx",
            null,
            true
        ),
        DOCUMENTO_04(
            "DOCUMENTO_04",
            "4.- CONTRATO DE PRESTACIÓN DE SERVICIOS",
            "4/4.-CONTRATO DE PRESTACIÓN DE SERVICIOS.docx",
            null,
            true
        ),
        DOCUMENTO_05(
            "DOCUMENTO_05",
            "5.- BIENVENIDO",
            "5/5.-BIENVENIDO.docx",
            null,
            true
        ),
        DOCUMENTO_06(
            "DOCUMENTO_06",
            "6.- REQUISITOS ALTA",
            "6/6.-REQUISITOS ALTA.docx",
            null,
            true
        ),
        DOCUMENTO_06_1(
            "DOCUMENTO_06_1",
            "6.1.- PREDICTAMEN PARA EL PAGO PROVISIONAL DE LA INDEMNIZACIÓN",
            "6.1/6.1.-PREDICTAMEN PARA EL PAGO PROVISIONAL DE LA INDEMNIZACIÓN.docx",
            null,
            true
        ),
        DOCUMENTO_07(
            "DOCUMENTO_07",
            "7.- CERTIFICADO MÉDICO DE NIVEL DE RIESGO Y POSIBLES ENFERMEDADES OCUPACIONALES",
            "7/7.-CERTIFICADO MÉDICO DE NIVEL DE RIESGO Y POSIBLES ENFERMEDADES OCUPACIONALES.docx",
            null,
            true
        ),
        DOCUMENTO_08(
            "DOCUMENTO_08",
            "8.- DICTAMEN DE NIVEL DE CUMPLIMIENTO NORMATIVO",
            "8/8.-DICTAMEN DE NIVEL DE CUMPLIMIENTO NORMATIVO.docx",
            null,
            true
        ),
        DOCUMENTO_09(
            "DOCUMENTO_09",
            "9.- DICTAMEN DE NIVEL DE RIESGO DE TRABAJO",
            "9/9.-DICTAMEN DE NIVEL DE RIESGO DE TRABAJO.docx",
            null,
            true
        ),
        DOCUMENTO_10("DOCUMENTO_10", "Documento 10 (Pendiente)", null, null, false),
        DOCUMENTO_11(
            "DOCUMENTO_11",
            "11.- CONTRATO DE PRESTACIÓN DE SERVICIOS",
            "11/11.-CONTRATO DE PRESTACIÓN DE SERVICIOS.docx",
            null,
            true
        ),
        DOCUMENTO_12(
            "DOCUMENTO_12",
            "12.- MANDATO PARA EL PAGO DE LAS INDEMNIZACIONES POR RIESGO DE TRABAJO A LOS TRABAJADORES",
            "12/12.-MANDATO PARA EL PAGO DE LAS INDEMINZACIONES POR RIESGO DE TRABAJO A LOS TRABAJADORES.docx",
            null,
            true
        );

        private final String code;
        private final String displayName;
        private final String docxFilename;
        private final String scriptFilename;
        private final boolean enabled;

        TemplateType(String code, String displayName, String docxFilename, String scriptFilename, boolean enabled) {
            this.code = code;
            this.displayName = displayName;
            this.docxFilename = docxFilename;
            this.scriptFilename = scriptFilename;
            this.enabled = enabled;
        }

        public String getCode() {
            return code;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDocxFilename() {
            return docxFilename;
        }

        public String getScriptFilename() {
            return scriptFilename;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }

    private static final Pattern FIELD_PATTERN = Pattern.compile("\\[sg\\.Text\\(\"([^\"]+)\"\\),\\s*sg\\.Input\\(key=\"([^\"]+)\"");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\{\\{([\\p{L}\\p{N}_]+)}}|\\$\\{([\\p{L}\\p{N}_]+)}");
    private static final Map<String, String> FIELD_LABEL_OVERRIDES = Map.of(
        "VID", "VIGENCIA CONTRATO INICIA EL DIA",
        "VIM", "VIGENCIA CONTRATO INICIA EL MES",
        "VIA", "VIGENCIA CONTRATO INICIA EL AÑO",
        "VTD", "VIGENCIA CONTRATO TERMINA EL DIA",
        "VTM", "VIGENCIA CONTRATO TERMINA EL MES",
        "VTA", "VIGENCIA CONTRATO TERMINA EL AÑO",
        "APODERADO_LEGAL", "APODERADO LEGAL DE"
    );
    private static final List<DocumentTemplateFieldDto> FALLBACK_PROPUESTA_FIELDS = List.of(
        new DocumentTemplateFieldDto("EN_CONTRAPOSICION_A", "En contraposición a", true),
        new DocumentTemplateFieldDto("CONSEDE_EN", "Con sede en", true),
        new DocumentTemplateFieldDto("SE_DIRIGE_A", "Se dirige a", true),
        new DocumentTemplateFieldDto("NUM_TRAB", "Número de trabajadores", true),
        new DocumentTemplateFieldDto("CUOTA_SINDICAL_ANUAL", "Cuota Sindical anual", true),
        new DocumentTemplateFieldDto("CUOTA_SINDICAL_MENSUAL", "Cuota Sindical mensual", true),
        new DocumentTemplateFieldDto("PROPUESTA_PARA_LA_EMPRESA", "Propuesta para la empresa", true),
        new DocumentTemplateFieldDto("TELEFONO", "Teléfono", true),
        new DocumentTemplateFieldDto("CORREO_ELECTRONICO", "Correo electrónico", true),
        new DocumentTemplateFieldDto("DIA", "Día", true),
        new DocumentTemplateFieldDto("MES", "Mes", true),
        new DocumentTemplateFieldDto("AÑO", "Año", true),
        new DocumentTemplateFieldDto("NOMBRE_FIRMA", "Nombre Firma", true),
        new DocumentTemplateFieldDto("APODERADO_LEGAL", "Apoderado Legal", true)
    );
    private static final List<DocumentTemplateFieldDto> FALLBACK_PATRON_NOM2U_FIELDS = List.of(
        new DocumentTemplateFieldDto("EL_PRESTADOR_DE_SERVICIOS", "El prestador de servicios", true),
        new DocumentTemplateFieldDto("EL_CLIENTE", "EL cliente", true)
    );

    private final Path templatesBasePath;
    private final Path scriptsBasePath;

    public DocumentTemplateCatalogService(@Value("${docgen.templates-base-path:Genera Documento}") String templatesBasePath,
                                          @Value("${docgen.scripts-base-path:Genera Documento}") String scriptsBasePath) {
        this.templatesBasePath = Paths.get(templatesBasePath);
        this.scriptsBasePath = Paths.get(scriptsBasePath);
    }

    public List<DocumentTemplateDto> listTemplates() {
        List<DocumentTemplateDto> list = new ArrayList<>();
        for (TemplateType templateType : TemplateType.values()) {
            DocumentTemplateDto dto = new DocumentTemplateDto(templateType.getCode(), templateType.getDisplayName(), templateType.isEnabled());
            if (templateType.isEnabled()) {
                dto.setFields(getFieldsByType(templateType.getCode()));
            }
            list.add(dto);
        }
        return list;
    }

    public TemplateType resolve(String typeCode) {
        if (typeCode == null) {
            throw new IllegalArgumentException("templateType es requerido");
        }
        String normalized = typeCode.trim().toUpperCase(Locale.ROOT);
        for (TemplateType value : TemplateType.values()) {
            if (value.getCode().equals(normalized)) {
                return value;
            }
        }
        throw new IllegalArgumentException("templateType no soportado: " + typeCode);
    }

    public List<DocumentTemplateFieldDto> getFieldsByType(String typeCode) {
        TemplateType template = resolve(typeCode);
        if (!template.isEnabled()) {
            return List.of();
        }

        List<DocumentTemplateFieldDto> extractedFromScript = parseFieldsFromScript(template);
        if (!extractedFromScript.isEmpty()) {
            return extractedFromScript;
        }

        List<DocumentTemplateFieldDto> extractedFromDocx = parseFieldsFromTemplateDocx(template);
        if (!extractedFromDocx.isEmpty()) {
            return extractedFromDocx;
        }

        if (template == TemplateType.PROPUESTA_NEGOCIACION) {
            return FALLBACK_PROPUESTA_FIELDS;
        }
        if (template == TemplateType.CLAUSULA_CONTRATO_PATRON_NOM2U) {
            return FALLBACK_PATRON_NOM2U_FIELDS;
        }

        return List.of();
    }

    public Path resolveTemplatePath(TemplateType templateType) {
        if (templateType.getDocxFilename() == null) {
            throw new IllegalArgumentException("La plantilla seleccionada aún no está disponible");
        }

        Path path = templatesBasePath.resolve(templateType.getDocxFilename());
        if (Files.exists(path)) {
            return path;
        }

        Path legacyPath = Paths.get("Genera Documento").resolve(templateType.getDocxFilename());
        if (Files.exists(legacyPath)) {
            return legacyPath;
        }

        ClassPathResource classPathResource = new ClassPathResource("templates/docgen/" + templateType.getDocxFilename());
        if (classPathResource.exists()) {
            try (InputStream in = classPathResource.getInputStream()) {
                Path temp = Files.createTempFile("docgen-template-", ".docx");
                Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
                return temp;
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo cargar la plantilla DOCX desde classpath", e);
            }
        }

        throw new IllegalArgumentException("No se encontró la plantilla DOCX: " + path);
    }

    private List<DocumentTemplateFieldDto> parseFieldsFromScript(TemplateType template) {
        if (template.getScriptFilename() == null) {
            return List.of();
        }
        Path scriptPath = scriptsBasePath.resolve(template.getScriptFilename());
        if (!Files.exists(scriptPath)) {
            return List.of();
        }

        List<DocumentTemplateFieldDto> fields = new ArrayList<>();
        try {
            String script = Files.readString(scriptPath, StandardCharsets.UTF_8);
            String[] lines = script.split("\\r?\\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.startsWith("#")) {
                    continue;
                }
                Matcher matcher = FIELD_PATTERN.matcher(line);
                if (!matcher.find()) {
                    continue;
                }
                String label = matcher.group(1);
                String key = matcher.group(2);
                if ("-PATH-".equalsIgnoreCase(key)) {
                    continue;
                }
                fields.add(new DocumentTemplateFieldDto(key, toFieldLabel(key), true));
            }
        } catch (IOException e) {
            return List.of();
        }
        return fields;
    }

    private List<DocumentTemplateFieldDto> parseFieldsFromTemplateDocx(TemplateType template) {
        if (template.getDocxFilename() == null) {
            return List.of();
        }

        Set<String> orderedKeys = new LinkedHashSet<>();
        try (InputStream in = openTemplateInputStream(template);
             XWPFDocument document = new XWPFDocument(in);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            String content = extractor.getText();
            Matcher matcher = TOKEN_PATTERN.matcher(content == null ? "" : content);
            while (matcher.find()) {
                String key = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                if (key != null && !key.isBlank()) {
                    orderedKeys.add(key);
                }
            }
        } catch (Exception e) {
            return List.of();
        }

        List<DocumentTemplateFieldDto> fields = new ArrayList<>();
        for (String key : orderedKeys) {
            fields.add(new DocumentTemplateFieldDto(key, toFieldLabel(key), true));
        }
        return fields;
    }

    private InputStream openTemplateInputStream(TemplateType templateType) throws IOException {
        Path path = templatesBasePath.resolve(templateType.getDocxFilename());
        if (Files.exists(path)) {
            return Files.newInputStream(path);
        }

        Path legacyPath = Paths.get("Genera Documento").resolve(templateType.getDocxFilename());
        if (Files.exists(legacyPath)) {
            return Files.newInputStream(legacyPath);
        }

        ClassPathResource classPathResource = new ClassPathResource("templates/docgen/" + templateType.getDocxFilename());
        if (classPathResource.exists()) {
            return classPathResource.getInputStream();
        }

        throw new IOException("No se encontró plantilla: " + templateType.getDocxFilename());
    }

    private String toFieldLabel(String key) {
        if (FIELD_LABEL_OVERRIDES.containsKey(key)) {
            return FIELD_LABEL_OVERRIDES.get(key);
        }
        return key.replace('_', ' ').toUpperCase(Locale.ROOT);
    }
}
