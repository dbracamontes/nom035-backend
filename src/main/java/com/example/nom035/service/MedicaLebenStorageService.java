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
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MedicaLebenStorageService {

    private final MedicaLebenCompanyDocsRepository docsRepository;
    private final MedicaLebenCompanyWorkPhotoRepository photoRepository;
    private final CompanyRepository companyRepository;

    @Value("${medica.leben.upload.base-path}")
    private String basePath;

    public void setBasePathForTesting(String basePath) {
        this.basePath = basePath;
    }

    private Path resolveBaseDirectory() {
        String effectiveBasePath = (basePath == null || basePath.isBlank())
                ? "uploads/medica-leben"
                : basePath.trim();
        Path configured = Path.of(effectiveBasePath);
        return configured.isAbsolute() ? configured : Path.of(System.getProperty("user.dir"), effectiveBasePath);
    }

    private String resolveCompanyFolderName(Company company) {
        if (company == null || company.getId() == null) {
            throw new IllegalArgumentException("Company y company.id son requeridos para guardar documentos.");
        }
        // Use a stable path format that matches the public file-controller lookup.
        // This keeps uploads consistent in production and avoids files being written to
        // a taxId-based directory that can never be served back by /api/medica-leben/companies/{id}/...
        return "company-" + company.getId();
    }

    private Path resolveCompanyDir(Company company) {
        String folderName = resolveCompanyFolderName(company);
        return resolveBaseDirectory().resolve(folderName);
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

    private void deletePhysicalFileIfExists(Path dir, String filename) {
        if (filename == null || filename.isBlank()) {
            return;
        }
        try {
            Path target = dir.resolve(filename);
            Files.deleteIfExists(target);
        } catch (IOException ex) {
            // swallow exception to avoid breaking API if disk delete fails
        }
    }

    @Transactional
    public MedicaLebenCompanyDocs uploadDocs(Company company,
                                             MultipartFile actaConstitutiva,
                                             MultipartFile asamblea,
                                             MultipartFile constanciaSituacionFiscal,
                                             MultipartFile poderNotarial,
                                             MultipartFile identificacionRepresentante,
                                             MultipartFile comprobanteDomicilio,
                                             MultipartFile estadoCuentaBancaria,
                                             MultipartFile comprobanteEmaEba) throws IOException {

        List<MultipartFile> incomingFiles = Arrays.asList(
                actaConstitutiva,
                asamblea,
                constanciaSituacionFiscal,
                poderNotarial,
                identificacionRepresentante,
                comprobanteDomicilio,
                estadoCuentaBancaria,
                comprobanteEmaEba
        );

        boolean hasAnyRealFile = incomingFiles.stream()
                .filter(Objects::nonNull)
                .anyMatch(file -> file.getSize() > 0 && !file.isEmpty());

        if (!hasAnyRealFile) {
            throw new IllegalArgumentException("No se seleccionó ningún documento para guardar.");
        }

        MedicaLebenCompanyDocs docs = docsRepository
                .findByCompany(company)
                .orElseGet(() -> MedicaLebenCompanyDocs.builder()
                        .company(company)
                        .status(MedicaLebenCompanyDocs.DocumentStatus.PENDING)
                        .build());

        docs.setStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
        docs.setActaConstitutivaStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
        docs.setAsambleaStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
        docs.setConstanciaSituacionFiscalStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
        docs.setPoderNotarialStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
        docs.setIdentificacionRepresentanteStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
        docs.setComprobanteDomicilioStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
        docs.setEstadoCuentaBancariaStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
        docs.setComprobanteEmaEbaStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);

        // Re-uploading any company document should always restart review from Pending.
        // A stale approved/rejected aggregate or field state must never survive a fresh upload.
        docs.setStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);

        Path docsDir = resolveDocsDir(company);

        if (actaConstitutiva != null && !actaConstitutiva.isEmpty()) {
            String filename = "acta_constitutiva_" + actaConstitutiva.getOriginalFilename();
            storeFile(actaConstitutiva, docsDir, filename);
            docs.setActaConstitutiva(buildDocsUrl(company, filename));
            docs.setActaConstitutivaStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
        }
        if (asamblea != null && !asamblea.isEmpty()) {
            String filename = "asamblea_" + asamblea.getOriginalFilename();
            storeFile(asamblea, docsDir, filename);
            docs.setAsamblea(buildDocsUrl(company, filename));
            docs.setAsambleaStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
        }
        if (constanciaSituacionFiscal != null && !constanciaSituacionFiscal.isEmpty()) {
            String filename = "constancia_situacion_fiscal_" + constanciaSituacionFiscal.getOriginalFilename();
            storeFile(constanciaSituacionFiscal, docsDir, filename);
            docs.setConstanciaSituacionFiscal(buildDocsUrl(company, filename));
            docs.setConstanciaSituacionFiscalStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
        }
        if (poderNotarial != null && !poderNotarial.isEmpty()) {
            String filename = "poder_notarial_" + poderNotarial.getOriginalFilename();
            storeFile(poderNotarial, docsDir, filename);
            docs.setPoderNotarial(buildDocsUrl(company, filename));
            docs.setPoderNotarialStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
        }
        if (identificacionRepresentante != null && !identificacionRepresentante.isEmpty()) {
            String filename = "identificacion_representante_" + identificacionRepresentante.getOriginalFilename();
            storeFile(identificacionRepresentante, docsDir, filename);
            docs.setIdentificacionRepresentante(buildDocsUrl(company, filename));
            docs.setIdentificacionRepresentanteStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
        }
        if (comprobanteDomicilio != null && !comprobanteDomicilio.isEmpty()) {
            String filename = "comprobante_domicilio_" + comprobanteDomicilio.getOriginalFilename();
            storeFile(comprobanteDomicilio, docsDir, filename);
            docs.setComprobanteDomicilio(buildDocsUrl(company, filename));
            docs.setComprobanteDomicilioStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
        }
        if (estadoCuentaBancaria != null && !estadoCuentaBancaria.isEmpty()) {
            String filename = "estado_cuenta_bancaria_" + estadoCuentaBancaria.getOriginalFilename();
            storeFile(estadoCuentaBancaria, docsDir, filename);
            docs.setEstadoCuentaBancaria(buildDocsUrl(company, filename));
            docs.setEstadoCuentaBancariaStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
        }
        if (comprobanteEmaEba != null && !comprobanteEmaEba.isEmpty()) {
            String filename = "comprobante_ema_eba_" + comprobanteEmaEba.getOriginalFilename();
            storeFile(comprobanteEmaEba, docsDir, filename);
            docs.setComprobanteEmaEba(buildDocsUrl(company, filename));
            docs.setComprobanteEmaEbaStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
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
                .status(MedicaLebenCompanyWorkPhoto.PhotoStatus.PENDING)
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

    @Transactional
    public MedicaLebenCompanyDocs deleteDoc(Company company, String fieldName) throws IOException {
        MedicaLebenCompanyDocs docs = docsRepository
                .findByCompany(company)
                .orElseThrow(() -> new IllegalArgumentException("Medica LEBEN docs not found for company"));

        Path docsDir = resolveDocsDir(company);

        switch (fieldName) {
            case "acta_constitutiva" -> {
                deletePhysicalFileIfExists(docsDir, docs.getActaConstitutiva());
                docs.setActaConstitutiva(null);
                docs.setActaConstitutivaStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
            }
            case "asamblea" -> {
                deletePhysicalFileIfExists(docsDir, docs.getAsamblea());
                docs.setAsamblea(null);
                docs.setAsambleaStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
            }
            case "constancia_situacion_fiscal" -> {
                deletePhysicalFileIfExists(docsDir, docs.getConstanciaSituacionFiscal());
                docs.setConstanciaSituacionFiscal(null);
                docs.setConstanciaSituacionFiscalStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
            }
            case "poder_notarial" -> {
                deletePhysicalFileIfExists(docsDir, docs.getPoderNotarial());
                docs.setPoderNotarial(null);
                docs.setPoderNotarialStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
            }
            case "identificacion_representante" -> {
                deletePhysicalFileIfExists(docsDir, docs.getIdentificacionRepresentante());
                docs.setIdentificacionRepresentante(null);
                docs.setIdentificacionRepresentanteStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
            }
            case "comprobante_domicilio" -> {
                deletePhysicalFileIfExists(docsDir, docs.getComprobanteDomicilio());
                docs.setComprobanteDomicilio(null);
                docs.setComprobanteDomicilioStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
            }
            case "estado_cuenta_bancaria" -> {
                deletePhysicalFileIfExists(docsDir, docs.getEstadoCuentaBancaria());
                docs.setEstadoCuentaBancaria(null);
                docs.setEstadoCuentaBancariaStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
            }
            case "comprobante_ema_eba" -> {
                deletePhysicalFileIfExists(docsDir, docs.getComprobanteEmaEba());
                docs.setComprobanteEmaEba(null);
                docs.setComprobanteEmaEbaStatus(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
            }
            default -> throw new IllegalArgumentException("Unknown document field: " + fieldName);
        }

        return docsRepository.save(docs);
    }

    @Transactional
    public void deletePhotoById(Long photoId) throws IOException {
        MedicaLebenCompanyWorkPhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("Photo not found"));

        Company company = photo.getCompanyDocs().getCompany();
        Path photosDir = resolvePhotosDir(company);
        deletePhysicalFileIfExists(photosDir, photo.getUrl());

        photoRepository.delete(photo);
    }
}