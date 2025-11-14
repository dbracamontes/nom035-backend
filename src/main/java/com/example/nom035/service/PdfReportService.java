package com.example.nom035.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.nom035.dto.CategoryScoreDto;
import com.example.nom035.dto.DictamenDto;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class PdfReportService {

    // --- Branded footer event (page number + optional footer text) ---
    private static class BrandedFooterEvent extends PdfPageEventHelper {
        private final PdfBrandingConfig brand;
        private final Font footerFont;
        BrandedFooterEvent(PdfBrandingConfig brand) {
            this.brand = brand;
            this.footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        }
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            String left = brand != null && brand.getFooterText() != null ? brand.getFooterText() : "";
            String right = "Página " + writer.getPageNumber();
            float y = document.bottom() - 10f;
            // Left footer text
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase(left, footerFont), document.left(), y, 0);
            // Right page number
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase(right, footerFont), document.right(), y, 0);
        }
    }

    // --- Helpers for branding header ---
    private void addBrandedHeader(Document doc, PdfBrandingConfig brand) throws DocumentException {
        if (brand == null) return; // no-op if no branding provided
        try {
            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setWidths(new float[]{1.0f, 3.0f});

            PdfPCell left = new PdfPCell();
            left.setBorder(Rectangle.NO_BORDER);
            // Optional logo
            if (brand.getLogoClasspath() != null) {
                try (InputStream is = getClass().getResourceAsStream(brand.getLogoClasspath())) {
                    if (is != null) {
                        Image logo = Image.getInstance(is.readAllBytes());
                        float targetW = brand.getLogoWidth() > 0 ? brand.getLogoWidth() : 64f;
                        float scale = targetW / logo.getWidth();
                        logo.scalePercent(scale * 100f);
                        logo.setAlignment(Image.ALIGN_LEFT);
                        left.addElement(logo);
                    }
                } catch (Exception ignored) { /* logo optional */ }
            }
            header.addCell(left);

            PdfPCell right = new PdfPCell();
            right.setBorder(Rectangle.NO_BORDER);
            Font t1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font t2 = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font t3 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            // Colorize main title with primary color if provided
            if (brand.getPrimaryHex() != null) {
                java.awt.Color c = parseHex(brand.getPrimaryHex(), new java.awt.Color(33,150,243));
                t1.setColor(c);
            }
            String title = brand.getTitle() != null ? brand.getTitle() : "NOM-035";
            String subtitle = brand.getSubtitle() != null ? brand.getSubtitle() : "Reporte";
            String company = brand.getCompanyName() != null ? brand.getCompanyName() : "";
            Paragraph pTitle = new Paragraph(title, t1);
            Paragraph pSub = new Paragraph(subtitle, t2);
            Paragraph pCompany = company.isBlank() ? null : new Paragraph(company, t3);
            pTitle.setSpacingAfter(2f);
            pSub.setSpacingAfter(2f);
            pTitle.setAlignment(Element.ALIGN_LEFT);
            pSub.setAlignment(Element.ALIGN_LEFT);
            right.addElement(pTitle);
            right.addElement(pSub);
            if (pCompany != null) right.addElement(pCompany);
            header.addCell(right);

            doc.add(header);
            // Spacer
            doc.add(new Paragraph(" "));
        } catch (Exception ex) {
            // If anything fails, continue without branded header
        }
    }

    private java.awt.Color parseHex(String hex, java.awt.Color fallback) {
        try {
            String h = hex.startsWith("#") ? hex.substring(1) : hex;
            int r = Integer.valueOf(h.substring(0,2), 16);
            int g = Integer.valueOf(h.substring(2,4), 16);
            int b = Integer.valueOf(h.substring(4,6), 16);
            return new java.awt.Color(r,g,b);
        } catch (Exception e) {
            return fallback;
        }
    }

    // --- Existing unbranded method (kept) ---
    public byte[] buildApplicationDictamenPdf(DictamenDto dto) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document();
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            writer.setPageEvent(new BrandedFooterEvent(new PdfBrandingConfig().setFooterText("NOM-035")));
            doc.open();

            Font h1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font h2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph title = new Paragraph("Dictamen NOM-035 - Aplicación individual", h1);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            doc.add(new Paragraph("Generado: " + LocalDateTime.now(), normal));
            doc.add(new Paragraph(" "));

            PdfPTable meta = new PdfPTable(2);
            meta.setWidthPercentage(100);
            addCell(meta, "Application ID", h2);
            addCell(meta, String.valueOf(dto.getApplicationId()), normal);
            addCell(meta, "Employee ID", h2);
            addCell(meta, String.valueOf(dto.getEmployeeId()), normal);
            addCell(meta, "Company ID", h2);
            addCell(meta, String.valueOf(dto.getCompanyId()), normal);
            addCell(meta, "Survey ID", h2);
            addCell(meta, String.valueOf(dto.getSurveyId()), normal);
            doc.add(meta);
            doc.add(new Paragraph(" "));

            PdfPTable global = new PdfPTable(2);
            global.setWidthPercentage(100);
            addCell(global, "Puntaje Global", h2);
            addCell(global, String.valueOf(dto.getGlobalScore()), normal);
            addCell(global, "Nivel Global", h2);
            addCell(global, String.valueOf(dto.getGlobalLevel()), normal);
            addCell(global, "ATS (Acontecimientos Traumáticos Severos)", h2);
            String ats = dto.isTraumaticAlert() ? ("Alerta: " + dto.getTraumaticEventsCount()) : "Sin alertas";
            addCell(global, ats, normal);
            doc.add(global);
            doc.add(new Paragraph(" "));

            Paragraph catTitle = new Paragraph("Resultados por categoría", h2);
            doc.add(catTitle);

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            addHeader(table, "Categoría");
            addHeader(table, "Puntaje");
            addHeader(table, "Nivel");
            List<CategoryScoreDto> cats = dto.getCategories();
            if (cats != null) {
                for (CategoryScoreDto c : cats) {
                    addCell(table, c.getCategory(), normal);
                    addCell(table, String.valueOf(c.getScore()), normal);
                    addCell(table, c.getLevel(), normal);
                }
            }
            doc.add(table);

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Conclusión", h2));
            doc.add(new Paragraph(dto.getConclusion() != null ? dto.getConclusion() : "", normal));

            doc.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    // --- Branded overload ---
    public byte[] buildApplicationDictamenPdf(DictamenDto dto, PdfBrandingConfig brand) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document();
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            writer.setPageEvent(new BrandedFooterEvent(brand));
            doc.open();

            addBrandedHeader(doc, brand);

            Font h2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);

            PdfPTable meta = new PdfPTable(2);
            meta.setWidthPercentage(100);
            addCell(meta, "Application ID", h2);
            addCell(meta, String.valueOf(dto.getApplicationId()), normal);
            addCell(meta, "Employee ID", h2);
            addCell(meta, String.valueOf(dto.getEmployeeId()), normal);
            addCell(meta, "Company ID", h2);
            addCell(meta, String.valueOf(dto.getCompanyId()), normal);
            addCell(meta, "Survey ID", h2);
            addCell(meta, String.valueOf(dto.getSurveyId()), normal);
            doc.add(meta);
            doc.add(new Paragraph(" "));

            PdfPTable global = new PdfPTable(2);
            global.setWidthPercentage(100);
            addCell(global, "Puntaje Global", h2);
            addCell(global, String.valueOf(dto.getGlobalScore()), normal);
            addCell(global, "Nivel Global", h2);
            addCell(global, String.valueOf(dto.getGlobalLevel()), normal);
            addCell(global, "ATS (Acontecimientos Traumáticos Severos)", h2);
            String ats = dto.isTraumaticAlert() ? ("Alerta: " + dto.getTraumaticEventsCount()) : "Sin alertas";
            addCell(global, ats, normal);
            doc.add(global);
            doc.add(new Paragraph(" "));

            Paragraph catTitle = new Paragraph("Resultados por categoría", h2);
            doc.add(catTitle);

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            addHeader(table, "Categoría");
            addHeader(table, "Puntaje");
            addHeader(table, "Nivel");
            List<CategoryScoreDto> cats = dto.getCategories();
            if (cats != null) {
                for (CategoryScoreDto c : cats) {
                    addCell(table, c.getCategory(), normal);
                    addCell(table, String.valueOf(c.getScore()), normal);
                    addCell(table, c.getLevel(), normal);
                }
            }
            doc.add(table);

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Conclusión", h2));
            doc.add(new Paragraph(dto.getConclusion() != null ? dto.getConclusion() : "", normal));

            doc.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    // --- Existing unbranded method (kept) ---
    public byte[] buildCompanySummaryPdf(Map<String, Object> summary) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document();
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            writer.setPageEvent(new BrandedFooterEvent(new PdfBrandingConfig().setFooterText("NOM-035")));
            doc.open();

            Font h1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font h2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph title = new Paragraph("Dictamen NOM-035 - Resumen por empresa", h1);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            doc.add(new Paragraph("Generado: " + LocalDateTime.now(), normal));
            doc.add(new Paragraph(" "));

            PdfPTable global = new PdfPTable(2);
            global.setWidthPercentage(100);
            addCell(global, "Aplicaciones", h2);
            addCell(global, String.valueOf(summary.getOrDefault("applicationsCount", 0)), normal);
            addCell(global, "ATS afirmativos", h2);
            addCell(global, String.valueOf(summary.getOrDefault("traumaticYesCount", 0)), normal);
            doc.add(global);
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Distribución por nivel global", h2));
            @SuppressWarnings("unchecked")
            Map<String, Integer> dist = (Map<String, Integer>) summary.get("globalLevelDistribution");
            if (dist != null && !dist.isEmpty()) {
                PdfPTable distTable = new PdfPTable(2);
                distTable.setWidthPercentage(100);
                addHeader(distTable, "Nivel");
                addHeader(distTable, "Cantidad");
                for (Map.Entry<String, Integer> e : dist.entrySet()) {
                    addCell(distTable, e.getKey(), normal);
                    addCell(distTable, String.valueOf(e.getValue()), normal);
                }
                doc.add(distTable);
            }

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Promedios por categoría", h2));
            @SuppressWarnings("unchecked")
            Map<String, Object> avgs = (Map<String, Object>) summary.get("avgCategoryScores");
            if (avgs != null && !avgs.isEmpty()) {
                PdfPTable avgTable = new PdfPTable(2);
                avgTable.setWidthPercentage(100);
                addHeader(avgTable, "Categoría");
                addHeader(avgTable, "Puntaje promedio");
                for (Map.Entry<String, Object> e : avgs.entrySet()) {
                    addCell(avgTable, e.getKey(), normal);
                    try {
                        double v = e.getValue() instanceof Number ? ((Number) e.getValue()).doubleValue() : Double.parseDouble(String.valueOf(e.getValue()));
                        addCell(avgTable, String.valueOf(Math.round(v)), normal);
                    } catch (Exception ex) {
                        addCell(avgTable, String.valueOf(e.getValue()), normal);
                    }
                }
                doc.add(avgTable);
            }

            doc.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    // --- Branded overload ---
    public byte[] buildCompanySummaryPdf(Map<String, Object> summary, PdfBrandingConfig brand) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document();
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            writer.setPageEvent(new BrandedFooterEvent(brand));
            doc.open();

            addBrandedHeader(doc, brand);

            Font h2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);

            PdfPTable global = new PdfPTable(2);
            global.setWidthPercentage(100);
            addCell(global, "Aplicaciones", h2);
            addCell(global, String.valueOf(summary.getOrDefault("applicationsCount", 0)), normal);
            addCell(global, "ATS afirmativos", h2);
            addCell(global, String.valueOf(summary.getOrDefault("traumaticYesCount", 0)), normal);
            doc.add(global);
            doc.add(new Paragraph(" "));

            Paragraph distTitle = new Paragraph("Distribución por nivel global", h2);
            doc.add(distTitle);
            @SuppressWarnings("unchecked")
            Map<String, Integer> dist = (Map<String, Integer>) summary.get("globalLevelDistribution");
            if (dist != null && !dist.isEmpty()) {
                PdfPTable distTable = new PdfPTable(2);
                distTable.setWidthPercentage(100);
                addHeader(distTable, "Nivel");
                addHeader(distTable, "Cantidad");
                for (Map.Entry<String, Integer> e : dist.entrySet()) {
                    addCell(distTable, e.getKey(), normal);
                    addCell(distTable, String.valueOf(e.getValue()), normal);
                }
                doc.add(distTable);
            }

            doc.add(new Paragraph(" "));
            Paragraph avgTitle = new Paragraph("Promedios por categoría", h2);
            doc.add(avgTitle);
            @SuppressWarnings("unchecked")
            Map<String, Object> avgs = (Map<String, Object>) summary.get("avgCategoryScores");
            if (avgs != null && !avgs.isEmpty()) {
                PdfPTable avgTable = new PdfPTable(2);
                avgTable.setWidthPercentage(100);
                addHeader(avgTable, "Categoría");
                addHeader(avgTable, "Puntaje promedio");
                for (Map.Entry<String, Object> e : avgs.entrySet()) {
                    addCell(avgTable, e.getKey(), normal);
                    try {
                        double v = e.getValue() instanceof Number ? ((Number) e.getValue()).doubleValue() : Double.parseDouble(String.valueOf(e.getValue()));
                        addCell(avgTable, String.valueOf(Math.round(v)), normal);
                    } catch (Exception ex) {
                        addCell(avgTable, String.valueOf(e.getValue()), normal);
                    }
                }
                doc.add(avgTable);
            }

            doc.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    private void addHeader(PdfPTable table, String text) {
        Font header = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        PdfPCell cell = new PdfPCell(new Phrase(text, header));
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        table.addCell(cell);
    }
}