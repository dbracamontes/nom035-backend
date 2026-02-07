package com.example.nom035.service;

import com.example.nom035.dto.DocumentPreviewChunkDto;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class DocumentPdfService {

    public Path buildPdfFromPreview(String title, String subtitle, List<DocumentPreviewChunkDto> chunks, Path outputDir) {
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create output directory: " + e.getMessage(), e);
        }

        Path out = outputDir.resolve("interpreted-document.pdf");

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float margin = 50;
            float y = page.getMediaBox().getHeight() - margin;
            // Print title only when it's not the original PDF filename
            if (title != null && !title.isBlank() && !title.toLowerCase().endsWith(".pdf")) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
                float titleWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(title) / 1000f * 14f;
                float pageWidth = page.getMediaBox().getWidth();
                float titleX = (pageWidth - titleWidth) / 2f;
                cs.newLineAtOffset(titleX, y);
                cs.showText(title);
                cs.endText();
            }

            if (subtitle != null && !subtitle.isBlank()) {
                y -= 20;
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 11);
                cs.newLineAtOffset(margin, y);
                cs.showText(subtitle);
                cs.endText();
            }


            y -= 30;

            boolean firstChunk = true;
            for (DocumentPreviewChunkDto chunk : chunks) {
                String header = "Sección " + (chunk.getChunkIndex() + 1) + " (pág. " + chunk.getPageStart() + "-" + chunk.getPageEnd() + ")";
                if (y < 80) {
                    cs.close();
                    doc.addPage(page);
                    page = new PDPage(PDRectangle.LETTER);
                    cs = new PDPageContentStream(doc, page);
                    y = page.getMediaBox().getHeight() - margin;
                }
                // Skip header for the very first chunk to remove duplicate top lines
                if (!firstChunk) {
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
                    float headerWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(header) / 1000f * 11f;
                    float pageWidth = page.getMediaBox().getWidth();
                    float headerX = (pageWidth - headerWidth) / 2f;
                    cs.newLineAtOffset(headerX, y);
                    cs.showText(header);
                    cs.endText();
                }
                y -= 16;

                String text = chunk.getInterpretedText() != null ? chunk.getInterpretedText() : chunk.getRawText();
                if (text == null) text = "";
                String[] lines = text.split("\r?\n");
                int lineIndex = 0;
                // For the first chunk, skip common leading lines: filename (.pdf), our own "Sección ..." header, and page marker like "[Página 1]"
                if (firstChunk) {
                    while (lineIndex < lines.length) {
                        String candidate = lines[lineIndex].trim();
                        if (candidate.isEmpty()) {
                            lineIndex++;
                            continue;
                        }
                        String lower = candidate.toLowerCase();
                        if (lower.endsWith(".pdf")) {
                            lineIndex++;
                            continue;
                        }
                        if (candidate.matches("^Secci[oó]n\\s+\\d+.*")) {
                            lineIndex++;
                            continue;
                        }
                        if (candidate.matches("^\\[P[aá]gina.*\\]$")) {
                            lineIndex++;
                            continue;
                        }
                        break;
                    }
                }
                int lineCount = lines.length;
                for (; lineIndex < lineCount; lineIndex++) {
                    if (y < 60) {
                        cs.close();
                        doc.addPage(page);
                        page = new PDPage(PDRectangle.LETTER);
                        cs = new PDPageContentStream(doc, page);
                        y = page.getMediaBox().getHeight() - margin;
                    }
                    String line = lines[lineIndex];
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA, 10);
                    cs.newLineAtOffset(margin, y);
                    String toShow = line.length() > 120 ? line.substring(0, 120) : line;
                    cs.showText(toShow);
                    cs.endText();
                    y -= 12;
                }
                y -= 8;
                firstChunk = false;
            }

            cs.close();
            doc.addPage(page);
            doc.save(out.toFile());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate PDF: " + e.getMessage(), e);
        }

        return out;
    }
}
