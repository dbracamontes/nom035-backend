package com.example.nom035.service;

import com.example.nom035.entity.Company;
import com.example.nom035.entity.DocumentJob;
import com.example.nom035.entity.MedicaLebenCompanyDocs;
import com.example.nom035.entity.MedicaLebenCompanyWorkPhoto;
import com.example.nom035.repository.CompanyRepository;
import com.example.nom035.repository.DocumentJobRepository;
import com.example.nom035.repository.MedicaLebenCompanyDocsRepository;
import com.example.nom035.repository.MedicaLebenCompanyWorkPhotoRepository;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.poi.xwpf.usermodel.TableRowAlign;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.List;
import java.util.UUID;

@Service
public class MaterialidadService {

    private static final String SOURCE_MODULE = "MATERIALIDAD_MEDICA_LEBEN";
    private static final int PHOTO_WIDTH_EMU = 5 * 914400;
    private static final int PHOTO_HEIGHT_EMU = 3 * 914400;
        private static final Map<String, Integer> PHOTO_REQUIREMENT_ORDER = Map.of(
            "Fotos del área en donde se encuentran realizando las actividades los trabajadores.", 1,
            "Fotos de las salidas de emergencia.", 2,
            "Fotos del área de comida.", 3,
            "Fotos de las instalaciones de la empresa (entrada).", 4,
            "Fotos de las instalaciones de la empresa (salida).", 5,
            "Fotos de las instalaciones de la empresa (escaleras).", 6,
            "Foto de los equipos de seguridad con que cuentan.", 7
        );

    private final CompanyRepository companyRepository;
    private final MedicaLebenCompanyDocsRepository docsRepository;
    private final MedicaLebenCompanyWorkPhotoRepository photoRepository;
    private final DocumentJobRepository documentJobRepository;
    private static final String TEMPLATE_CLASSPATH = "templates/docgen/1/fotos.docx";

    private final Resource templateResource;
    private final Path storageBasePath;
    private final Path photosBasePath;

    public MaterialidadService(
            CompanyRepository companyRepository,
            MedicaLebenCompanyDocsRepository docsRepository,
            MedicaLebenCompanyWorkPhotoRepository photoRepository,
            DocumentJobRepository documentJobRepository,
            @Value("${docgen.storage-base-path:uploads/doc-generator/tmp}") String storageBasePath,
            @Value("${medica.leben.upload.base-path:uploads/medica-leben}") String photosBasePath) {
        this.companyRepository = companyRepository;
        this.docsRepository = docsRepository;
        this.photoRepository = photoRepository;
        this.documentJobRepository = documentJobRepository;
        this.templateResource = new ClassPathResource(TEMPLATE_CLASSPATH);
        this.storageBasePath = resolvePath(storageBasePath);
        this.photosBasePath = resolvePath(photosBasePath);
    }

    public DocumentJob generate(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        MedicaLebenCompanyDocs docs = docsRepository.findByCompany(company)
                .orElseThrow(() -> new IllegalArgumentException("La empresa no tiene información de Médica LEBEN"));
        List<MedicaLebenCompanyWorkPhoto> photos = photoRepository.findByCompanyDocsOrderBySortOrderAsc(docs)
                .stream()
                .filter(photo -> photo.getUrl() != null && !photo.getUrl().isBlank())
            .sorted(Comparator
                .comparingInt(this::resolveRequirementOrder)
                .thenComparingInt(MedicaLebenCompanyWorkPhoto::getSortOrder))
                .toList();
        if (photos.isEmpty()) {
            throw new IllegalArgumentException("La empresa no tiene fotografías del área de trabajo");
        }
        if (!templateResource.exists()) {
            throw new IllegalStateException("No se encontró la plantilla " + TEMPLATE_CLASSPATH);
        }

        Path jobDir = storageBasePath.resolve("materialidad-" + UUID.randomUUID());
        try {
            Files.createDirectories(jobDir);
            Path output = jobDir.resolve("materialidad-" + company.getId() + "-" + System.currentTimeMillis() + ".docx");
            buildDocument(company, photos, output);

            DocumentJob job = new DocumentJob();
            job.setOriginalFilename("fotos.docx");
            job.setStoredPath("classpath:/" + TEMPLATE_CLASSPATH);
            job.setOutputDocxPath(output.toAbsolutePath().toString());
            job.setStatus(DocumentJob.Status.DONE);
            job.setOcrProvider("materialidad");
            job.setModelUsed("template-fotos-docx");
            job.setTotalPages(1);
            job.setProcessedPages(1);
            job.setFileSizeBytes(Files.size(output));
            job.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            job.setTemplateType("MATERIALIDAD_MEDICA_LEBEN");
            job.setSourceModule(SOURCE_MODULE);
            job.setClientName(company.getName());
            job.setCompletedAt(LocalDateTime.now());
            return documentJobRepository.save(job);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo generar el documento de materialidad", ex);
        }
    }

    private void buildDocument(Company company, List<MedicaLebenCompanyWorkPhoto> photos, Path output) throws Exception {
        try (InputStream template = templateResource.getInputStream();
             XWPFDocument document = new XWPFDocument(template);
             OutputStream stream = Files.newOutputStream(output)) {
            XWPFParagraph pageBreak = document.createParagraph();
            pageBreak.setPageBreak(true);

            XWPFParagraph heading = document.createParagraph();
            heading.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun headingRun = heading.createRun();
            headingRun.setBold(true);
            headingRun.setFontSize(16);
            headingRun.setFontFamily("Aptos Display");
            headingRun.setText(company.getName());

            XWPFParagraph intro = document.createParagraph();
            intro.setAlignment(ParagraphAlignment.CENTER);
            intro.createRun().setText("Evidencia fotográfica del área de trabajo");

            XWPFTable table = document.createTable(photos.size(), 1);
            table.setWidth("100%");
            table.setTableAlignment(TableRowAlign.CENTER);
            for (int index = 0; index < photos.size(); index++) {
                MedicaLebenCompanyWorkPhoto photo = photos.get(index);
                XWPFTableRow row = table.getRow(index);
                XWPFTableCell cell = row.getCell(0);
                cell.setWidth("100%");
                addPhoto(cell, photo, index + 1);
            }
            document.write(stream);
        }
    }

    private void addPhoto(XWPFTableCell cell, MedicaLebenCompanyWorkPhoto photo, int index) throws Exception {
        cell.removeParagraph(0);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        XWPFParagraph paragraph = cell.addParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        paragraph.setIndentationLeft(0);
        paragraph.setIndentationRight(0);
        paragraph.setSpacingBefore(0);
        paragraph.setSpacingAfter(0);
        Path imagePath = photosBasePath.resolve("company-" + photo.getCompanyDocs().getCompany().getId())
                .resolve("photos").resolve(photo.getUrl()).normalize();
        if (!imagePath.startsWith(photosBasePath.normalize()) || !Files.isRegularFile(imagePath)) {
            throw new IllegalStateException("No se encontró la fotografía " + photo.getUrl());
        }
        try (InputStream image = Files.newInputStream(imagePath)) {
            paragraph.createRun().addPicture(image, XWPFDocument.PICTURE_TYPE_JPEG,
                    photo.getUrl(), PHOTO_WIDTH_EMU, PHOTO_HEIGHT_EMU);
        }
        XWPFParagraph caption = cell.addParagraph();
        caption.setAlignment(ParagraphAlignment.CENTER);
        caption.setIndentationLeft(0);
        caption.setIndentationRight(0);
        caption.createRun().setText(index + ". " + (photo.getDescription() == null ? "Fotografía del área de trabajo" : photo.getDescription()));
    }

    private int resolveRequirementOrder(MedicaLebenCompanyWorkPhoto photo) {
        if (photo == null || photo.getDescription() == null) {
            return Integer.MAX_VALUE;
        }
        return PHOTO_REQUIREMENT_ORDER.getOrDefault(photo.getDescription().trim(), Integer.MAX_VALUE);
    }

    private Path resolvePath(String configuredPath) {
        Path path = Paths.get(configuredPath);
        return path.isAbsolute() ? path : Paths.get(System.getProperty("user.dir"), configuredPath);
    }
}