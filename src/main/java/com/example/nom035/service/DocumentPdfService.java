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
            float pageWidth = page.getMediaBox().getWidth();
            // Print title only when it's not the original PDF filename
            if (title != null && !title.isBlank() && !title.toLowerCase().endsWith(".pdf")) {
                cs.beginText();
                cs.setFont(PDType1Font.TIMES_BOLD, 14);
                float titleWidth = PDType1Font.TIMES_BOLD.getStringWidth(title) / 1000f * 14f;
                float titleX = (pageWidth - titleWidth) / 2f;
                cs.newLineAtOffset(titleX, y);
                cs.showText(title);
                cs.endText();
            }

            if (subtitle != null && !subtitle.isBlank()) {
                // center subtitle under title
                y -= 18;
                cs.beginText();
                cs.setFont(PDType1Font.TIMES_ITALIC, 11);
                float subWidth = PDType1Font.TIMES_ITALIC.getStringWidth(subtitle) / 1000f * 11f;
                float subX = (pageWidth - subWidth) / 2f;
                cs.newLineAtOffset(subX, y);
                cs.showText(subtitle);
                cs.endText();
            }

            // leave a single blank line before content (professional spacing)
            y -= 14;

            boolean firstChunk = true;
            PDType1Font bodyFont = PDType1Font.TIMES_ROMAN;
            float bodyFontSize = 12f;
            float usableWidth = pageWidth - (margin * 2);

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
                    cs.setFont(PDType1Font.TIMES_BOLD, 11);
                    float headerWidth = PDType1Font.TIMES_BOLD.getStringWidth(header) / 1000f * 11f;
                    float headerX = (pageWidth - headerWidth) / 2f;
                    cs.newLineAtOffset(headerX, y);
                    cs.showText(header);
                    cs.endText();
                }
                y -= 16;

                String text = chunk.getInterpretedText() != null ? chunk.getInterpretedText() : chunk.getRawText();
                if (text == null) text = "";

                // Basic cleaning of excessive blank lines
                String[] paragraphs = text.split("(?:\r?\n){2,}");

                // For the first chunk, attempt to skip common leading noise (filename, section markers)
                int startParagraph = 0;
                if (firstChunk && paragraphs.length > 0) {
                    // inspect first paragraph lines
                    String[] firstLines = paragraphs[0].split("\r?\n");
                    int skip = 0;
                    for (String l : firstLines) {
                        String candidate = l.trim();
                        if (candidate.isEmpty()) { skip++; continue; }
                        String lower = candidate.toLowerCase();
                        if (lower.endsWith(".pdf") || candidate.matches("^Secci[oó]n\\s+\\d+.*") || candidate.matches("^\\[P[aá]gina.*\\]$")) { skip++; continue; }
                        break;
                    }
                    if (skip >= firstLines.length) startParagraph = 1; // skip whole first paragraph
                }

                for (int p = startParagraph; p < paragraphs.length; p++) {
                    String para = paragraphs[p].trim();
                    if (para.isEmpty()) {
                        y -= 8;
                        continue;
                    }

                    // wrap paragraph into lines by words, then justify
                    String[] words = para.split("\\s+");
                    int wi = 0;
                    while (wi < words.length) {
                        // build line
                        float spaceWidth = bodyFont.getStringWidth(" ") / 1000f * bodyFontSize;
                        float accWidth = 0f;
                        int startWi = wi;
                        // greedily add words until exceed usableWidth
                        while (wi < words.length) {
                            String w = words[wi];
                            float wWidth = bodyFont.getStringWidth(w) / 1000f * bodyFontSize;
                            float nextWidth = (startWi == wi) ? wWidth : (accWidth + spaceWidth + wWidth);
                            if (nextWidth > usableWidth) {
                                if (startWi == wi) {
                                    // single very long word: break it
                                    int maxChars = Math.max(1, (int)((usableWidth / (bodyFont.getStringWidth("M")/1000f * bodyFontSize)))-1);
                                    String part = w.substring(0, Math.min(w.length(), maxChars));
                                    words[wi] = w.substring(part.length());
                                    w = part;
                                    wWidth = bodyFont.getStringWidth(w) / 1000f * bodyFontSize;
                                    wi = startWi + 1; // consumed
                                    accWidth = wWidth;
                                    break;
                                }
                                break;
                            }
                            accWidth = nextWidth;
                            wi++;
                        }

                        // prepare the actual words for this line: words[startWi .. wi-1]
                        int endWi = Math.max(startWi, wi) - 1;
                        if (endWi < startWi) endWi = startWi;
                        int tokens = endWi - startWi + 1;

                        // if last line of paragraph -> left align normally
                        boolean isLastLine = (wi >= words.length);

                        if (y < 60) {
                            cs.close();
                            doc.addPage(page);
                            page = new PDPage(PDRectangle.LETTER);
                            cs = new PDPageContentStream(doc, page);
                            y = page.getMediaBox().getHeight() - margin;
                        }

                        if (isLastLine || tokens == 1) {
                            // draw left aligned
                            cs.beginText();
                            cs.setFont(bodyFont, bodyFontSize);
                            cs.newLineAtOffset(margin, y);
                            StringBuilder sb = new StringBuilder();
                            for (int k = startWi; k <= endWi; k++) {
                                if (k > startWi) sb.append(' ');
                                sb.append(words[k]);
                            }
                            String lineText = sb.toString();
                            cs.showText(lineText);
                            cs.endText();
                        } else {
                            // justify line by distributing extra space
                            float wordsWidth = 0f;
                            for (int k = startWi; k <= endWi; k++) {
                                wordsWidth += bodyFont.getStringWidth(words[k]) / 1000f * bodyFontSize;
                            }
                            int gaps = tokens - 1;
                            float extra = (usableWidth - wordsWidth) / gaps;

                            float x = margin;
                            for (int k = startWi; k <= endWi; k++) {
                                String w = words[k];
                                cs.beginText();
                                cs.setFont(bodyFont, bodyFontSize);
                                cs.newLineAtOffset(x, y);
                                cs.showText(w);
                                cs.endText();
                                float wWidth = bodyFont.getStringWidth(w) / 1000f * bodyFontSize;
                                x += wWidth + extra;
                            }
                        }

                        y -= (bodyFontSize + 2);
                    }

                    // paragraph spacing
                    y -= 6;
                }

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
