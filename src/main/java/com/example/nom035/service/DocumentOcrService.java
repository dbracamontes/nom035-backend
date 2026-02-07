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
        out = out.replaceAll("[\\u2014\\u2013\\-]{2,}", "—"); // collapse repeated dashes
        out = out.replaceAll("[ ]{2,}", " ");
        out = out.replaceAll("\n{3,}", "\n\n");
        return out.trim();
    }
}
