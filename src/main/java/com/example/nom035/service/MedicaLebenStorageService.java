package com.example.nom035.service;

import com.example.nom035.entity.Company;
import com.example.nom035.entity.MedicaLebenCompanyDocs;
import com.example.nom035.entity.MedicaLebenCompanyWorkPhoto;
import com.example.nom035.repository.CompanyRepository;
import com.example.nom035.repository.MedicaLebenCompanyDocsRepository;
import com.example.nom035.repository.MedicaLebenCompanyWorkPhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicaLebenStorageService {

    private final MedicaLebenCompanyDocsRepository docsRepository;
    private final MedicaLebenCompanyWorkPhotoRepository photoRepository;
    private final CompanyRepository companyRepository;

    @Value("${medica.leben.upload.base-path}")
    private String basePath;

    private String resolveCompanyFolderName(Company company) {
        String taxId = company.getTaxId();
        if (taxId != null && !taxId.isBlank()) {
            // sanitize taxId to be safe for filesystem paths
            return taxId.replaceAll("[^a-zA-Z0-9_-]", "_");
        }
        // fallback when there is no taxId
        return "company-" + company.getId();
    }

    private Path resolveCompanyDir(Company company) {
        String folderName = resolveCompanyFolderName(company);
        return Path.of(basePath, folderName);
    }

    private Path resolveDocsDir(Company company) {
        return resolveCompanyDir(company).resolve("docs");
    }

    private Path resolvePhotosDir(Company company) {
        return resolveCompanyDir(company).resolve("photos");
    }

    private String buildDocsUrl(Company company, String storedFilename) {
        // Now we treat docs like simple stored filenames as well
        return storedFilename;
    }

    private String buildPhotoUrl(Company company, String storedFilename) {
        // Work photos should store only the filename (document name), same as docs
        return storedFilename;
    }

    private String storeFile(MultipartFile file, Path targetDir, String targetFilename) throws IOException {
        Files.createDirectories(targetDir);
        Path target = targetDir.resolve(targetFilename);
        file.transferTo(target.toFile());
        // Still only return the filename so DB stores just the document name
        return targetFilename;
    }

    @Transactional
    public MedicaLebenCompanyDocs uploadDocs(Company company,
                                             MultipartFile actaConstitutiva,
                                             MultipartFile constanciaSituacionFiscal,
                                             MultipartFile poderNotarial,
                                             MultipartFile identificacionRepresentante,
                                             MultipartFile comprobanteDomicilio,
                                             MultipartFile estadoCuentaBancaria,
                                             MultipartFile comprobanteEmaEba) throws IOException {

        MedicaLebenCompanyDocs docs = docsRepository
                .findByCompany(company)
                .orElseGet(() -> MedicaLebenCompanyDocs.builder().company(company).build());

        Path docsDir = resolveDocsDir(company);

        if (actaConstitutiva != null && !actaConstitutiva.isEmpty()) {
            String filename = "acta_constitutiva_" + actaConstitutiva.getOriginalFilename();
            storeFile(actaConstitutiva, docsDir, filename);
            docs.setActaConstitutiva(buildDocsUrl(company, filename));
        }
        if (constanciaSituacionFiscal != null && !constanciaSituacionFiscal.isEmpty()) {
            String filename = "constancia_situacion_fiscal_" + constanciaSituacionFiscal.getOriginalFilename();
            storeFile(constanciaSituacionFiscal, docsDir, filename);
            docs.setConstanciaSituacionFiscal(buildDocsUrl(company, filename));
        }
        if (poderNotarial != null && !poderNotarial.isEmpty()) {
            String filename = "poder_notarial_" + poderNotarial.getOriginalFilename();
            storeFile(poderNotarial, docsDir, filename);
            docs.setPoderNotarial(buildDocsUrl(company, filename));
        }
        if (identificacionRepresentante != null && !identificacionRepresentante.isEmpty()) {
            String filename = "identificacion_representante_" + identificacionRepresentante.getOriginalFilename();
            storeFile(identificacionRepresentante, docsDir, filename);
            docs.setIdentificacionRepresentante(buildDocsUrl(company, filename));
        }
        if (comprobanteDomicilio != null && !comprobanteDomicilio.isEmpty()) {
            String filename = "comprobante_domicilio_" + comprobanteDomicilio.getOriginalFilename();
            storeFile(comprobanteDomicilio, docsDir, filename);
            docs.setComprobanteDomicilio(buildDocsUrl(company, filename));
        }
        if (estadoCuentaBancaria != null && !estadoCuentaBancaria.isEmpty()) {
            String filename = "estado_cuenta_bancaria_" + estadoCuentaBancaria.getOriginalFilename();
            storeFile(estadoCuentaBancaria, docsDir, filename);
            docs.setEstadoCuentaBancaria(buildDocsUrl(company, filename));
        }
        if (comprobanteEmaEba != null && !comprobanteEmaEba.isEmpty()) {
            String filename = "comprobante_ema_eba_" + comprobanteEmaEba.getOriginalFilename();
            storeFile(comprobanteEmaEba, docsDir, filename);
            docs.setComprobanteEmaEba(buildDocsUrl(company, filename));
        }

        MedicaLebenCompanyDocs savedDocs = docsRepository.save(docs);

        // Marca a nivel company que ya tiene documentos Médica LEBEN
        if (!company.isHasMedicaLebenDocs()) {
            company.setHasMedicaLebenDocs(true);
            companyRepository.save(company);
        }

        return savedDocs;
    }

    @Transactional
    public MedicaLebenCompanyWorkPhoto uploadPhoto(MedicaLebenCompanyDocs docs,
                                                   MultipartFile photo,
                                                   String description,
                                                   int sortOrder) throws IOException {
        Path photosDir = resolvePhotosDir(docs.getCompany());
        String filename = "foto_" + System.currentTimeMillis() + "_" + photo.getOriginalFilename();
        storeFile(photo, photosDir, filename);

        // Store only the filename (document name) in the DB, just like docs
        String url = buildPhotoUrl(docs.getCompany(), filename);

        MedicaLebenCompanyWorkPhoto entity = MedicaLebenCompanyWorkPhoto.builder()
                .companyDocs(docs)
                .url(url)
                .description(description)
                .sortOrder(sortOrder)
                .build();

        return photoRepository.save(entity);
    }

    public List<MedicaLebenCompanyWorkPhoto> listPhotos(MedicaLebenCompanyDocs docs) {
        // workPhotos already store just the document name in url field
        return photoRepository.findByCompanyDocsOrderBySortOrderAsc(docs);
    }
}