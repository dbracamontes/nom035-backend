package com.example.nom035.service;

import com.example.nom035.entity.DocumentChunk;
import com.example.nom035.entity.DocumentJob;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
        run.setText(title);
        if (subtitle != null && !subtitle.isBlank()) {
            XWPFParagraph sub = doc.createParagraph();
            sub.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun runSub = sub.createRun();
            runSub.setItalic(true);
            runSub.setFontSize(12);
            runSub.setText(subtitle);
        }
    }

    private void addChunk(XWPFDocument doc, DocumentChunk chunk) {
        XWPFParagraph header = doc.createParagraph();
        header.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun run = header.createRun();
        run.setBold(true);
        run.setText("Sección " + (chunk.getChunkIndex() + 1) + " (pág. " + chunk.getPageStart() + "-" + chunk.getPageEnd() + ")");

        String text = chunk.getInterpretedText() != null ? chunk.getInterpretedText() : chunk.getRawText();
        if (text == null) {
            text = "";
        }
        String[] lines = text.split("\r?\n");
        for (String line : lines) {
            XWPFParagraph paragraph = doc.createParagraph();
            paragraph.setAlignment(ParagraphAlignment.BOTH);
            XWPFRun lineRun = paragraph.createRun();
            lineRun.setText(line.trim());
        }
    }
}
