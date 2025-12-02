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

    private String storeFile(MultipartFile file, Path targetDir, String targetFilename) throws IOException {
        Files.createDirectories(targetDir);
        Path target = targetDir.resolve(targetFilename);
        file.transferTo(target.toFile());
        return target.toString().replace('\\', '/');
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
            String path = storeFile(actaConstitutiva, docsDir, "acta_constitutiva_" + actaConstitutiva.getOriginalFilename());
            docs.setActaConstitutiva(path);
        }
        if (constanciaSituacionFiscal != null && !constanciaSituacionFiscal.isEmpty()) {
            String path = storeFile(constanciaSituacionFiscal, docsDir, "constancia_situacion_fiscal_" + constanciaSituacionFiscal.getOriginalFilename());
            docs.setConstanciaSituacionFiscal(path);
        }
        if (poderNotarial != null && !poderNotarial.isEmpty()) {
            String path = storeFile(poderNotarial, docsDir, "poder_notarial_" + poderNotarial.getOriginalFilename());
            docs.setPoderNotarial(path);
        }
        if (identificacionRepresentante != null && !identificacionRepresentante.isEmpty()) {
            String path = storeFile(identificacionRepresentante, docsDir, "identificacion_representante_" + identificacionRepresentante.getOriginalFilename());
            docs.setIdentificacionRepresentante(path);
        }
        if (comprobanteDomicilio != null && !comprobanteDomicilio.isEmpty()) {
            String path = storeFile(comprobanteDomicilio, docsDir, "comprobante_domicilio_" + comprobanteDomicilio.getOriginalFilename());
            docs.setComprobanteDomicilio(path);
        }
        if (estadoCuentaBancaria != null && !estadoCuentaBancaria.isEmpty()) {
            String path = storeFile(estadoCuentaBancaria, docsDir, "estado_cuenta_bancaria_" + estadoCuentaBancaria.getOriginalFilename());
            docs.setEstadoCuentaBancaria(path);
        }
        if (comprobanteEmaEba != null && !comprobanteEmaEba.isEmpty()) {
            String path = storeFile(comprobanteEmaEba, docsDir, "comprobante_ema_eba_" + comprobanteEmaEba.getOriginalFilename());
            docs.setComprobanteEmaEba(path);
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
        String path = storeFile(photo, photosDir, "foto_" + System.currentTimeMillis() + "_" + photo.getOriginalFilename());

        MedicaLebenCompanyWorkPhoto entity = MedicaLebenCompanyWorkPhoto.builder()
                .companyDocs(docs)
                .url(path)
                .description(description)
                .sortOrder(sortOrder)
                .build();

        return photoRepository.save(entity);
    }

    public List<MedicaLebenCompanyWorkPhoto> listPhotos(MedicaLebenCompanyDocs docs) {
        return photoRepository.findByCompanyDocsOrderBySortOrderAsc(docs);
    }
}