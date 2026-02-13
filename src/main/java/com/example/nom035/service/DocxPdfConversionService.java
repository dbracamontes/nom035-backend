package com.example.nom035.service;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.concurrent.TimeUnit;

@Service
public class DocxPdfConversionService {

    private static final Logger log = LoggerFactory.getLogger(DocxPdfConversionService.class);

    public Path convertDocxToPdf(Path docxPath, Path outputDir) {
        return convertDocxToPdf(docxPath, outputDir, false);
    }

    public Path convertDocxToPdf(Path docxPath, Path outputDir, boolean forceBlackText) {
        try {
            Files.createDirectories(outputDir);
            Path sourceDocx = forceBlackText ? createBlackTextCopy(docxPath, outputDir) : docxPath;
            Path outputPdf = outputDir.resolve(replaceExtension(sourceDocx.getFileName().toString(), ".pdf"));

            if (convertWithLibreOffice(sourceDocx, outputDir, outputPdf)) {
                return outputPdf;
            }

            log.warn("Conversión LibreOffice no disponible; usando fallback docx4j para {}", sourceDocx.getFileName());
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(sourceDocx.toFile());
            try (OutputStream os = Files.newOutputStream(outputPdf)) {
                Docx4J.toPDF(wordMLPackage, os);
            }
            return outputPdf;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo convertir DOCX a PDF con formato", e);
        }
    }

    private Path createBlackTextCopy(Path originalDocx, Path outputDir) {
        try {
            Path copy = outputDir.resolve("pdf-black-" + originalDocx.getFileName().toString());
            Files.copy(originalDocx, copy, StandardCopyOption.REPLACE_EXISTING);
            try (InputStream in = Files.newInputStream(copy);
                 XWPFDocument document = new XWPFDocument(in);
                 OutputStream out = Files.newOutputStream(copy, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING)) {
                setBlackInParagraphs(document.getParagraphs());
                flattenTransparentImages(document);
                for (XWPFTable table : document.getTables()) {
                    for (XWPFTableRow row : table.getRows()) {
                        for (XWPFTableCell cell : row.getTableCells()) {
                            setBlackInParagraphs(cell.getParagraphs());
                        }
                    }
                }
                document.getHeaderList().forEach(header -> setBlackInParagraphs(header.getParagraphs()));
                document.getFooterList().forEach(footer -> setBlackInParagraphs(footer.getParagraphs()));
                document.write(out);
            }
            return copy;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo preparar DOCX en negro para PDF", e);
        }
    }

    private void flattenTransparentImages(XWPFDocument document) {
        for (XWPFPictureData pictureData : document.getAllPackagePictures()) {
            try {
                byte[] originalBytes = pictureData.getData();
                if (originalBytes == null || originalBytes.length == 0) {
                    continue;
                }

                BufferedImage src = ImageIO.read(new ByteArrayInputStream(originalBytes));
                if (src == null || !src.getColorModel().hasAlpha()) {
                    continue;
                }

                BufferedImage flattened = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = flattened.createGraphics();
                g2.setColor(java.awt.Color.WHITE);
                g2.fillRect(0, 0, src.getWidth(), src.getHeight());
                g2.drawImage(src, 0, 0, null);
                g2.dispose();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                String ext = pictureData.suggestFileExtension();
                String format = (ext == null || ext.isBlank()) ? "png" : ext.toLowerCase();
                if (!ImageIO.write(flattened, format, baos)) {
                    baos.reset();
                    ImageIO.write(flattened, "png", baos);
                }
                try (OutputStream imageOut = pictureData.getPackagePart().getOutputStream()) {
                    imageOut.write(baos.toByteArray());
                }
            } catch (Exception e) {
                log.debug("No se pudo normalizar imagen embebida para PDF: {}", e.getMessage());
            }
        }
    }

    private void setBlackInParagraphs(java.util.List<XWPFParagraph> paragraphs) {
        for (XWPFParagraph paragraph : paragraphs) {
            if (paragraph.getRuns() == null) {
                continue;
            }
            for (XWPFRun run : paragraph.getRuns()) {
                run.setColor("000000");
            }
        }
    }

    private boolean convertWithLibreOffice(Path docxPath, Path outputDir, Path expectedPdf) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                "soffice",
                "--headless",
                "--nologo",
                "--nodefault",
                "--nofirststartwizard",
                "--convert-to",
                "pdf:writer_pdf_Export",
                "--outdir",
                outputDir.toAbsolutePath().toString(),
                docxPath.toAbsolutePath().toString()
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            String processOutput = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            boolean finished = process.waitFor(120, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.warn("LibreOffice no terminó a tiempo para {}", docxPath.getFileName());
                return false;
            }
            if (process.exitValue() != 0) {
                log.warn("LibreOffice falló (exit={}): {}", process.exitValue(), processOutput);
                return false;
            }
            if (!Files.exists(expectedPdf)) {
                log.warn("LibreOffice terminó sin generar PDF esperado {}", expectedPdf);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("LibreOffice no disponible para conversión PDF: {}", e.getMessage());
            return false;
        }
    }

    private String replaceExtension(String filename, String newExtension) {
        int index = filename.lastIndexOf('.');
        if (index < 0) {
            return filename + newExtension;
        }
        return filename.substring(0, index) + newExtension;
    }
}
