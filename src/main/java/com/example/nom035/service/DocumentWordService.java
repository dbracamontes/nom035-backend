package com.example.nom035.service;

import com.example.nom035.entity.DocumentChunk;
import com.example.nom035.entity.DocumentJob;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Service
public class DocumentWordService {

    public Path buildWord(DocumentJob job, List<DocumentChunk> chunks, Path jobDir) {
        try {
            Files.createDirectories(jobDir);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create storage directory: " + e.getMessage(), e);
        }

        Path output = jobDir.resolve("interpreted-document.docx");
        try (XWPFDocument doc = new XWPFDocument(); OutputStream os = Files.newOutputStream(output)) {
            addTitle(doc, "Interpretación de Documento", job.getOriginalFilename());
            for (DocumentChunk chunk : chunks) {
                addChunk(doc, chunk);
            }
            doc.write(os);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate Word: " + e.getMessage(), e);
        }
        return output;
    }

    private void addTitle(XWPFDocument doc, String title, String subtitle) {
        XWPFParagraph titleParagraph = doc.createParagraph();
        titleParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = titleParagraph.createRun();
        run.setBold(true);
        run.setFontSize(14);
        run.setFontFamily("Times New Roman");
        run.setText(title);
        if (subtitle != null && !subtitle.isBlank()) {
            XWPFParagraph sub = doc.createParagraph();
            sub.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun runSub = sub.createRun();
            runSub.setItalic(true);
            runSub.setFontSize(11);
            runSub.setFontFamily("Times New Roman");
            runSub.setText(subtitle);
        }
    }

    private void addChunk(XWPFDocument doc, DocumentChunk chunk) {
        XWPFParagraph header = doc.createParagraph();
        header.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = header.createRun();
        run.setBold(true);
        run.setFontFamily("Times New Roman");
        run.setFontSize(12);
        run.setText("Sección " + (chunk.getChunkIndex() + 1) + " (pág. " + chunk.getPageStart() + "-" + chunk.getPageEnd() + ")");
        String interpreted = chunk.getInterpretedText();
        if (interpreted != null && interpreted.trim().startsWith("{")) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                Map<?, ?> root = mapper.readValue(interpreted, Map.class);
                Object sectionsObj = root.get("sections");
                if (sectionsObj instanceof List) {
                    List<?> sections = (List<?>) sectionsObj;
                    for (Object s : sections) {
                        if (s instanceof Map) {
                            Map<?, ?> sec = (Map<?, ?>) s;
                            Object heading = sec.get("heading");
                            Object textObj = sec.get("text");
                            if (heading != null) {
                                XWPFParagraph h = doc.createParagraph();
                                h.setAlignment(ParagraphAlignment.CENTER);
                                XWPFRun hr = h.createRun();
                                hr.setBold(true);
                                hr.setFontSize(12);
                                hr.setFontFamily("Times New Roman");
                                hr.setText(heading.toString());
                            }
                            if (textObj != null) {
                                String text = textObj.toString();
                                String[] paragraphs = text.split("(?:\\r?\\n){2,}");
                                for (String para : paragraphs) {
                                    XWPFParagraph paragraph = doc.createParagraph();
                                    paragraph.setAlignment(ParagraphAlignment.BOTH);
                                    paragraph.setSpacingBetween(1.15);
                                    paragraph.setSpacingBefore(120); // 6pt
                                    paragraph.setSpacingAfter(120);  // 6pt
                                    paragraph.setFirstLineIndent(340); // ~6mm
                                    XWPFRun lineRun = paragraph.createRun();
                                    lineRun.setFontFamily("Times New Roman");
                                    lineRun.setFontSize(12);
                                    lineRun.setText(para.trim());
                                }
                            }
                        }
                    }
                    return;
                }
            } catch (Exception e) {
                // If parsing fails, fall through to plain-text behavior
            }
        }

        String text = interpreted != null ? interpreted : chunk.getRawText();
        if (text == null) {
            text = "";
        }
        String[] paragraphs = text.split("(?:\r?\n){2,}");
        for (String para : paragraphs) {
            XWPFParagraph paragraph = doc.createParagraph();
            paragraph.setAlignment(ParagraphAlignment.BOTH);
            paragraph.setSpacingBetween(1.15);
            paragraph.setSpacingBefore(120);
            paragraph.setSpacingAfter(120);
            paragraph.setFirstLineIndent(340);
            XWPFRun lineRun = paragraph.createRun();
            lineRun.setFontFamily("Times New Roman");
            lineRun.setFontSize(12);
            lineRun.setText(para.trim());
        }
    }
}
