package com.example.nom035.service;

import com.example.nom035.dto.DocumentTemplateDto;
import com.example.nom035.dto.DocumentTemplateFieldDto;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DocumentTemplateCatalogService {

    public enum TemplateType {
        PROPUESTA_NEGOCIACION(
            "PROPUESTA_NEGOCIACION",
            "PROPUESTA DE NEGOCIACIÓN DE CONTRATO COLECTIVO DE TRABAJO",
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
        DOCUMENTO_03("DOCUMENTO_03", "Documento 3 (Pendiente)", null, null, false),
        DOCUMENTO_04("DOCUMENTO_04", "Documento 4 (Pendiente)", null, null, false),
        DOCUMENTO_05("DOCUMENTO_05", "Documento 5 (Pendiente)", null, null, false),
        DOCUMENTO_06("DOCUMENTO_06", "Documento 6 (Pendiente)", null, null, false),
        DOCUMENTO_07("DOCUMENTO_07", "Documento 7 (Pendiente)", null, null, false),
        DOCUMENTO_08("DOCUMENTO_08", "Documento 8 (Pendiente)", null, null, false),
        DOCUMENTO_09("DOCUMENTO_09", "Documento 9 (Pendiente)", null, null, false),
        DOCUMENTO_10("DOCUMENTO_10", "Documento 10 (Pendiente)", null, null, false),
        DOCUMENTO_11("DOCUMENTO_11", "Documento 11 (Pendiente)", null, null, false),
        DOCUMENTO_12("DOCUMENTO_12", "Documento 12 (Pendiente)", null, null, false);

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
        if (template == TemplateType.PROPUESTA_NEGOCIACION) {
            List<DocumentTemplateFieldDto> extracted = parseFieldsFromScript(template);
            return extracted.isEmpty() ? FALLBACK_PROPUESTA_FIELDS : extracted;
        }
        if (template == TemplateType.CLAUSULA_CONTRATO_PATRON_NOM2U) {
            List<DocumentTemplateFieldDto> extracted = parseFieldsFromScript(template);
            return extracted.isEmpty() ? FALLBACK_PATRON_NOM2U_FIELDS : extracted;
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
                fields.add(new DocumentTemplateFieldDto(key, label, true));
            }
        } catch (IOException e) {
            return List.of();
        }
        return fields;
    }
}
