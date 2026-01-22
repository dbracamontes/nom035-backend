package com.example.nom035.service;

import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.nom035.dto.CategoryScoreDto;
import com.example.nom035.dto.DictamenDto;

import jakarta.annotation.PostConstruct;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 * Generates the ponderaciones PDF using JasperReports and the ponderaciones.jrxml template.
 */
@Service
public class JasperPonderacionReportService {
    
    // Logger kept out in case it's useful later
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JasperPonderacionReportService.class);

    private JasperReport ponderacionesTemplate;
    private JasperReport categoriesSubreport;

    @PostConstruct
    public void loadTemplate() {
        try (InputStream is = getClass().getResourceAsStream("/reports/ponderaciones.jrxml")) {
            if (is == null) {
                System.err.println("No se encontró la plantilla ponderaciones.jrxml en classpath; se omitirá la funcionalidad de reports");
            } else {
                try {
                    ponderacionesTemplate = JasperCompileManager.compileReport(is);
                } catch (Exception e) {
                    System.err.println("Error al compilar la plantilla de ponderaciones, se omitirá la funcionalidad de reports: " + e.getMessage());
                    e.printStackTrace();
                    ponderacionesTemplate = null;
                }
            }
        } catch (Exception e) {
            // Seguridad: no detener toda la aplicación por un fallo en templates
            System.err.println("Error inesperado al intentar leer ponderaciones.jrxml: " + e.getMessage());
            e.printStackTrace();
            ponderacionesTemplate = null;
        }
        
        // Compilar subreport
        try (InputStream subIs = getClass().getResourceAsStream("/reports/categories_subreport.jrxml")) {
            if (subIs == null) {
                System.err.println("No se encontró el subreport categories_subreport.jrxml; se omitirá la funcionalidad de reports");
            } else {
                try {
                    categoriesSubreport = JasperCompileManager.compileReport(subIs);
                } catch (Exception e) {
                    System.err.println("Error al compilar el subreport de categorías, se omitirá la funcionalidad de reports: " + e.getMessage());
                    e.printStackTrace();
                    categoriesSubreport = null;
                }
            }
        } catch (Exception e) {
            System.err.println("Error inesperado al intentar leer categories_subreport.jrxml: " + e.getMessage());
            e.printStackTrace();
            categoriesSubreport = null;
        }
    }

    public byte[] buildPonderaciones(DictamenDto dto, PdfBrandingConfig brand) {
        ensureTemplate();
        Map<String, Object> params = new HashMap<>();

        params.put("REPORT_TITLE", brand != null && brand.getTitle() != null && !brand.getTitle().isBlank() ? brand.getTitle() : "Ponderación Médica Leben");
        params.put("REPORT_SUBTITLE", brand != null && brand.getSubtitle() != null && !brand.getSubtitle().isBlank() ? brand.getSubtitle() : "Resultados por categoría");
        params.put("COMPANY_NAME", dto.getCompanyName());
        params.put("EMPLOYEE_NAME", dto.getEmployeeName());
        params.put("EMPLOYEE_EMAIL", dto.getEmployeeEmail());
        params.put("EMPLOYEE_DEPARTMENT", dto.getDepartment());
        params.put("EMPLOYEE_POSITION", dto.getPosition());
        params.put("EMPLOYEE_AGE", dto.getAge());
        params.put("EMPLOYEE_MARITAL", dto.getMaritalStatus());
        params.put("EMPLOYEE_GENDER", dto.getGender());
        params.put("EMPLOYEE_STUDIES", dto.getStudies());
        params.put("EMPLOYEE_SENIORITY", dto.getSeniority());
        params.put("EMPLOYEE_SAME_ACTIVITY", dto.getSameActivity());
        params.put("EMPLOYEE_WORKING_DAYS", dto.getWorkingDays());
        params.put("EMPLOYEE_HOURS_PER_DAY", dto.getHoursPerDay());
        params.put("EMPLOYEE_TRANSPORT", dto.getTransportType());
        params.put("EMPLOYEE_WEEKLY_GAS", dto.getWeeklyGasoline());
        params.put("EMPLOYEE_COMMUTE_TIME", dto.getCommuteTime());
        params.put("EMPLOYEE_TRANSPORT_COST", dto.getTransportCost());
        params.put("EMPLOYEE_HOUSING", dto.getHousing());
        params.put("APPLICATION_DATE", dto.getApplicationDate());
        params.put("COMPLETED_DATE", dto.getCompletedDate());
        params.put("GENERATED_DATE", DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(LocalDateTime.now()));
        // Usar el valor calculado en el DictamenDto si está presente. Formatear a 2 decimales para el PDF.
        Double factorAjuste = dto.getAdjustmentFactor() != null ? Double.valueOf(Math.round(dto.getAdjustmentFactor() * 100.0) / 100.0) : null;
        params.put("FACTOR_AJUSTE", factorAjuste);
        params.put("GLOBAL_SCORE", dto.getGlobalScore());
        params.put("GLOBAL_MIN", dto.getGlobalMin());
        params.put("GLOBAL_MAX", dto.getGlobalMax());
        params.put("TOTAL_RESPONSES", dto.getTotalResponses());
        params.put("GLOBAL_LEVEL", dto.getGlobalLevel());
        params.put("TRAUMATIC_ALERT", dto.isTraumaticAlert());
        params.put("TRAUMATIC_COUNT", dto.getTraumaticEventsCount());
        params.put("CONCLUSION", dto.getConclusion());
        params.put("GENERATED_AT", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(LocalDateTime.now()));
        
        // Cargar el logo como ByteArrayInputStream para que JasperReports pueda leerlo múltiples veces
        if (brand != null && brand.getLogoClasspath() != null) {
            try {
                String path = brand.getLogoClasspath();
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
                InputStream logoStream = getClass().getResourceAsStream(path);
                if (logoStream != null) {
                    byte[] logoBytes = logoStream.readAllBytes();
                    logoStream.close();
                    if (logoBytes.length > 0) {
                        // Crear ByteArrayInputStream que se puede leer múltiples veces
                        params.put("LOGO_IMAGE", new java.io.ByteArrayInputStream(logoBytes));
                    } else {
                        System.err.println("Logo vacío: " + path);
                        params.put("LOGO_IMAGE", null);
                    }
                } else {
                    System.err.println("Logo NO encontrado: " + path);
                    params.put("LOGO_IMAGE", null);
                }
            } catch (Exception e) {
                System.err.println("Error al cargar logo: " + e.getMessage());
                e.printStackTrace();
                params.put("LOGO_IMAGE", null);
            }
        } else {
            params.put("LOGO_IMAGE", null);
        }

        Color primary = toColor(brand != null ? brand.getPrimaryHex() : null, new Color(33, 150, 243));
        Color secondary = toColor(brand != null ? brand.getSecondaryHex() : null, new Color(156, 39, 176));
        params.put("PRIMARY_COLOR", primary);
        params.put("SECONDARY_COLOR", secondary);

        List<CategoryScoreDto> categories = dto.getCategories() != null ? dto.getCategories() : List.of();
        
        // Pasar las 5 categorías como parámetros individuales (jr:list NO funciona después de 7 intentos)
        for (int i = 0; i < 5; i++) {
            CategoryScoreDto cat = i < categories.size() ? categories.get(i) : null;
            params.put("CAT" + (i+1), cat);
            params.put("CAT" + (i+1) + "_NAME", cat != null ? cat.getCategory() : "");
            params.put("CAT" + (i+1) + "_LEVEL", cat != null ? cat.getLevel() : "");

            if (cat != null) {
                int score = cat.getScore();
                int min = cat.getMin() != null ? cat.getMin() : 0;
                int max = cat.getMax() != null ? cat.getMax() : 0;
                int responses = cat.getResponsesCount() != null ? cat.getResponsesCount() : 0;
                int questions = cat.getQuestionsCount() != null ? cat.getQuestionsCount() : 0;

                params.put("CAT" + (i+1) + "_SCORE", "Puntaje: " + score + " / " + max + " (mín " + min + " · máx " + max + ")");
                String avg = "Promedio: " + String.format("%.2f", responses > 0 ? (double)score / responses : 0.0) + " · Respuestas: " + responses + " / " + questions;
                params.put("CAT" + (i+1) + "_AVG", avg);
            } else {
                params.put("CAT" + (i+1) + "_SCORE", "");
                params.put("CAT" + (i+1) + "_AVG", "");
            }
        }

        try {
            // Parameters prepared; generate the report

            // Escribir un log temporal en target/last_report_title.log para confirmar el valor del parámetro
            try {
                Object rt = params.get("REPORT_TITLE");
                Object fa = params.get("FACTOR_AJUSTE");
                String line = java.time.LocalDateTime.now().toString() + " REPORT_TITLE=" + (rt == null ? "<null>" : rt.toString()) + " FACTOR_AJUSTE=" + (fa == null ? "<null>" : fa.toString()) + System.lineSeparator();
                Path out = Path.of("target", "last_report_title.log");
                Files.createDirectories(out.getParent());
                Files.writeString(out, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception ex) {
                // no detener generación por fallo en escritura de log
            }

            JasperPrint jp = JasperFillManager.fillReport(ponderacionesTemplate, params, new JREmptyDataSource(1));
            return JasperExportManager.exportReportToPdf(jp);
        } catch (JRException e) {
            throw new IllegalStateException("No se pudo generar el PDF de ponderaciones", e);
        }
    }

    private void ensureTemplate() {
        if (ponderacionesTemplate == null) {
            throw new IllegalStateException("La plantilla de ponderaciones no está cargada");
        }
    }

    private String resolveLogoUrl(PdfBrandingConfig brand) {
        if (brand == null || brand.getLogoClasspath() == null) return null;
        try {
            // Asegurar que la ruta empiece con /
            String path = brand.getLogoClasspath();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            var url = getClass().getResource(path);
            if (url != null) {
                return url.toString();
            } else {
                System.err.println("Logo NO encontrado en classpath: " + path);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error al buscar logo: " + e.getMessage());
            return null; // logo es opcional
        }
    }

    private Color toColor(String hex, Color fallback) {
        if (hex == null || hex.isBlank()) return fallback;
        try {
            String h = hex.startsWith("#") ? hex.substring(1) : hex;
            int r = Integer.valueOf(h.substring(0, 2), 16);
            int g = Integer.valueOf(h.substring(2, 4), 16);
            int b = Integer.valueOf(h.substring(4, 6), 16);
            return new Color(r, g, b);
        } catch (Exception e) {
            return fallback;
        }
    }
}
