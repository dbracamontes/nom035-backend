package com.example.nom035.service;

import com.example.nom035.entity.DocumentJob;
import com.example.nom035.entity.DocumentOcrPage;
import com.example.nom035.repository.DocumentOcrPageRepository;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentOcrService {

    private final DocumentOcrPageRepository documentOcrPageRepository;

    public DocumentOcrService(DocumentOcrPageRepository documentOcrPageRepository) {
        this.documentOcrPageRepository = documentOcrPageRepository;
    }

    public List<DocumentOcrPage> runOcr(Path pdfPath, DocumentJob job, int pageLimit) {
        List<DocumentOcrPage> pages = new ArrayList<>();
        try (PDDocument document = PDDocument.load(pdfPath.toFile())) {
            PDFRenderer renderer = new PDFRenderer(document);
            int count = Math.min(document.getNumberOfPages(), Math.max(1, pageLimit));
            Tesseract tesseract = buildTesseract();
            for (int i = 0; i < count; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 400);
                String text = normalize(tesseract.doOCR(image));
                DocumentOcrPage page = new DocumentOcrPage();
                page.setJob(job);
                page.setPageNumber(i + 1);
                page.setText(text);
                pages.add(page);
            }
        } catch (IOException e) {
            throw new IllegalStateException("OCR failed reading PDF: " + e.getMessage(), e);
        } catch (TesseractException e) {
            throw new IllegalStateException("OCR engine error: " + e.getMessage(), e);
        }
        return documentOcrPageRepository.saveAll(pages);
    }

    private Tesseract buildTesseract() {
        Tesseract tesseract = new Tesseract();
        String dataPath = System.getenv("TESSDATA_PREFIX");
        if (dataPath != null && !dataPath.isBlank()) {
            tesseract.setDatapath(dataPath);
        }
        tesseract.setLanguage("spa");
        tesseract.setOcrEngineMode(1); // LSTM only
        tesseract.setPageSegMode(1);   // Automatic page segmentation with OSD
        tesseract.setTessVariable("preserve_interword_spaces", "1");
        return tesseract;
    }

    private String normalize(String text) {
        if (text == null) return "";
        String out = text;
        // Replace common ligatures
        out = out.replace("\uFB01", "fi").replace("\uFB02", "fl");
        out = out.replace("ﬁ", "fi").replace("ﬂ", "fl");
        // Remove soft-hyphens
        out = out.replace("\u00AD", "");
        // Normalize smart quotes to ASCII
        out = out.replace("“", "\"").replace("”", "\"");
        out = out.replace("‘", "'").replace("’", "'");
        // Normalize various dashes to simple hyphen then collapse sequences
        out = out.replaceAll("[\\u2014\\u2013]", "-");
        out = out.replaceAll("[\\-]{2,}", "—");
        // Remove hyphenation that splits words at line breaks: "exam-\nple" -> "example"
        out = out.replaceAll("-\\s*\\r?\\n\\s*", "");
        // Preserve paragraph breaks: convert 2+ newlines to a paragraph marker
        out = out.replaceAll("\\r?\\n\\r?\\n+", "\n\n__PARA__\n\n");
        // Replace remaining single newlines with a space to join broken lines
        out = out.replaceAll("\\r?\\n", " ");
        // Restore paragraph separators
        out = out.replace("__PARA__", "\n\n");
        // Collapse multiple spaces
        out = out.replaceAll("[ ]{2,}", " ");
        // Tidy spacing before punctuation
        out = out.replaceAll("\\s+([,;:.)])", "$1");
        out = out.replaceAll("(\\()\\s+", "$1");
        return out.trim();
    }

    /**
     * Limpiado adicional local previo al chunking:
     * - elimina numeración de páginas y separadores
     * - elimina líneas con solo dígitos
     * - colapsa saltos de línea excesivos
     */
    public String cleanText(String text) {
        if (text == null) return "";
        String t = normalize(text);
        // Remove page numbers like "Página 1 de 3" or "Page 1/3"
        t = t.replaceAll("(?mi)^\\s*pag\\.?\\s+\\d+(\\s+de\\s+\\d+)?\\s*$", "");
        t = t.replaceAll("(?mi)^\\s*page\\s+\\d+(\\s*/\\s*\\d+)?\\s*$", "");
        // Remove separator lines (----, ___)
        t = t.replaceAll("(?m)^\\s*[-_]{3,}\\s*$", "");
        // Remove lines that are only numbers (possible footer)
        t = t.replaceAll("(?m)^\\s*\\d+\\s*$", "");
        // Collapse 3+ newlines into two (paragraph break)
        t = t.replaceAll("\\n{3,}", "\\n\\n");
        // Trim and return
        return t.trim();
    }
}
