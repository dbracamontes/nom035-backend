package com.example.nom035.service;

import java.awt.Color;
import java.io.InputStream;
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

    private JasperReport ponderacionesTemplate;

    @PostConstruct
    public void loadTemplate() {
        try (InputStream is = getClass().getResourceAsStream("/reports/ponderaciones.jrxml")) {
            if (is == null) {
                throw new IllegalStateException("No se encontró la plantilla ponderaciones.jrxml en classpath");
            }
            ponderacionesTemplate = JasperCompileManager.compileReport(is);
        } catch (Exception e) {
            throw new IllegalStateException("Error al compilar la plantilla de ponderaciones", e);
        }
    }

    public byte[] buildPonderaciones(DictamenDto dto, PdfBrandingConfig brand) {
        ensureTemplate();
        Map<String, Object> params = new HashMap<>();

        params.put("REPORT_TITLE", brand != null && brand.getTitle() != null ? brand.getTitle() : "Ponderación Medica Leben");
        params.put("REPORT_SUBTITLE", brand != null && brand.getSubtitle() != null ? brand.getSubtitle() : "Resultados por categoría");
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
        params.put("FACTOR_AJUSTE", "1.12"); // TODO: calcular si aplica
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
                        System.out.println("Logo cargado como ByteArrayInputStream desde: " + path + " (" + logoBytes.length + " bytes)");
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
        params.put("CATEGORIES_DS", new JRBeanCollectionDataSource(categories));

        try {
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
                System.out.println("Logo encontrado: " + url.toString());
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
