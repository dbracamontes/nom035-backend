package com.example.nom035.service;

import com.example.nom035.entity.Company;
import com.example.nom035.entity.MedicaLebenCompanyDocs;
import com.example.nom035.entity.MedicaLebenCompanyWorkPhoto;
import com.example.nom035.repository.CompanyRepository;
import com.example.nom035.repository.MedicaLebenCompanyDocsRepository;
import com.example.nom035.repository.MedicaLebenCompanyWorkPhotoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MedicaLebenStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void uploadDocsOnlyResetsStatusForTheUploadedField() throws Exception {
        Company company = new Company();
        company.setId(7L);
        MedicaLebenCompanyDocs docs = MedicaLebenCompanyDocs.builder()
                .company(company)
                .status(MedicaLebenCompanyDocs.DocumentStatus.APPROVED)
                .actaConstitutiva("acta.pdf")
                .actaConstitutivaStatus(MedicaLebenCompanyDocs.DocumentStatus.APPROVED)
                .asamblea("asamblea.pdf")
                .asambleaStatus(MedicaLebenCompanyDocs.DocumentStatus.APPROVED)
                .constanciaSituacionFiscal("constancia.pdf")
                .constanciaSituacionFiscalStatus(MedicaLebenCompanyDocs.DocumentStatus.APPROVED)
                .poderNotarial("poder.pdf")
                .poderNotarialStatus(MedicaLebenCompanyDocs.DocumentStatus.APPROVED)
                .identificacionRepresentante("identificacion.pdf")
                .identificacionRepresentanteStatus(MedicaLebenCompanyDocs.DocumentStatus.APPROVED)
                .comprobanteDomicilio("domicilio.pdf")
                .comprobanteDomicilioStatus(MedicaLebenCompanyDocs.DocumentStatus.APPROVED)
                .estadoCuentaBancaria("cuenta.pdf")
                .estadoCuentaBancariaStatus(MedicaLebenCompanyDocs.DocumentStatus.APPROVED)
                .comprobanteEmaEba("ema.pdf")
                .comprobanteEmaEbaStatus(MedicaLebenCompanyDocs.DocumentStatus.APPROVED)
                .build();

        MedicaLebenCompanyDocsRepository docsRepository = mock(MedicaLebenCompanyDocsRepository.class);
        when(docsRepository.findByCompany(company)).thenReturn(java.util.Optional.of(docs));
        when(docsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        MedicaLebenStorageService service = new MedicaLebenStorageService(
                docsRepository,
                mock(MedicaLebenCompanyWorkPhotoRepository.class),
                mock(CompanyRepository.class));
        service.setBasePathForTesting(tempDir.toString());

        MedicaLebenCompanyDocs result = service.uploadDocs(
                company,
                null,
                new MockMultipartFile("asamblea", "nueva-asamblea.pdf", "application/pdf", "contenido".getBytes()),
                null,
                null,
                null,
                null,
                null,
                null);

        assertThat(result.getActaConstitutivaStatus()).isEqualTo(MedicaLebenCompanyDocs.DocumentStatus.APPROVED);
        assertThat(result.getAsambleaStatus()).isEqualTo(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
        assertThat(result.getConstanciaSituacionFiscalStatus()).isEqualTo(MedicaLebenCompanyDocs.DocumentStatus.APPROVED);
        assertThat(result.getPoderNotarialStatus()).isEqualTo(MedicaLebenCompanyDocs.DocumentStatus.APPROVED);
        assertThat(result.getIdentificacionRepresentanteStatus()).isEqualTo(MedicaLebenCompanyDocs.DocumentStatus.APPROVED);
        assertThat(result.getComprobanteDomicilioStatus()).isEqualTo(MedicaLebenCompanyDocs.DocumentStatus.APPROVED);
        assertThat(result.getEstadoCuentaBancariaStatus()).isEqualTo(MedicaLebenCompanyDocs.DocumentStatus.APPROVED);
        assertThat(result.getComprobanteEmaEbaStatus()).isEqualTo(MedicaLebenCompanyDocs.DocumentStatus.APPROVED);
        assertThat(result.getStatus()).isEqualTo(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
    }

    @Test
    void deleteDocOnlyResetsTheDeletedFieldAndRecalculatesAggregateStatus() throws Exception {
        Company company = new Company();
        company.setId(8L);
        MedicaLebenCompanyDocs docs = MedicaLebenCompanyDocs.builder()
                .company(company)
                .status(MedicaLebenCompanyDocs.DocumentStatus.APPROVED)
                .actaConstitutiva("acta.pdf")
                .actaConstitutivaStatus(MedicaLebenCompanyDocs.DocumentStatus.APPROVED)
                .asamblea("asamblea.pdf")
                .asambleaStatus(MedicaLebenCompanyDocs.DocumentStatus.APPROVED)
                .constanciaSituacionFiscal("constancia.pdf")
                .constanciaSituacionFiscalStatus(MedicaLebenCompanyDocs.DocumentStatus.APPROVED)
                .poderNotarial("poder.pdf")
                .poderNotarialStatus(MedicaLebenCompanyDocs.DocumentStatus.APPROVED)
                .identificacionRepresentante("identificacion.pdf")
                .identificacionRepresentanteStatus(MedicaLebenCompanyDocs.DocumentStatus.APPROVED)
                .comprobanteDomicilio("domicilio.pdf")
                .comprobanteDomicilioStatus(MedicaLebenCompanyDocs.DocumentStatus.APPROVED)
                .estadoCuentaBancaria("cuenta.pdf")
                .estadoCuentaBancariaStatus(MedicaLebenCompanyDocs.DocumentStatus.APPROVED)
                .comprobanteEmaEba("ema.pdf")
                .comprobanteEmaEbaStatus(MedicaLebenCompanyDocs.DocumentStatus.APPROVED)
                .build();

        MedicaLebenCompanyDocsRepository docsRepository = mock(MedicaLebenCompanyDocsRepository.class);
        when(docsRepository.findByCompany(company)).thenReturn(java.util.Optional.of(docs));
        when(docsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        MedicaLebenStorageService service = new MedicaLebenStorageService(
                docsRepository,
                mock(MedicaLebenCompanyWorkPhotoRepository.class),
                mock(CompanyRepository.class));
        service.setBasePathForTesting(tempDir.toString());

        MedicaLebenCompanyDocs result = service.deleteDoc(company, "asamblea");

        assertThat(result.getActaConstitutivaStatus()).isEqualTo(MedicaLebenCompanyDocs.DocumentStatus.APPROVED);
        assertThat(result.getAsamblea()).isNull();
        assertThat(result.getAsambleaStatus()).isEqualTo(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
        assertThat(result.getConstanciaSituacionFiscalStatus()).isEqualTo(MedicaLebenCompanyDocs.DocumentStatus.APPROVED);
        assertThat(result.getPoderNotarialStatus()).isEqualTo(MedicaLebenCompanyDocs.DocumentStatus.APPROVED);
        assertThat(result.getIdentificacionRepresentanteStatus()).isEqualTo(MedicaLebenCompanyDocs.DocumentStatus.APPROVED);
        assertThat(result.getComprobanteDomicilioStatus()).isEqualTo(MedicaLebenCompanyDocs.DocumentStatus.APPROVED);
        assertThat(result.getEstadoCuentaBancariaStatus()).isEqualTo(MedicaLebenCompanyDocs.DocumentStatus.APPROVED);
        assertThat(result.getComprobanteEmaEbaStatus()).isEqualTo(MedicaLebenCompanyDocs.DocumentStatus.APPROVED);
        assertThat(result.getStatus()).isEqualTo(MedicaLebenCompanyDocs.DocumentStatus.PENDING);
    }

    @Test
    void storesWorkPhotosWithUniformDimensions() throws Exception {
        Company company = new Company();
        company.setId(42L);
        MedicaLebenCompanyDocs docs = MedicaLebenCompanyDocs.builder().company(company).build();

        BufferedImage source = new BufferedImage(1600, 400, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D graphics = source.createGraphics();
        graphics.setColor(Color.BLUE);
        graphics.fillRect(0, 0, source.getWidth(), source.getHeight());
        graphics.dispose();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(source, "png", bytes);

        MedicaLebenCompanyWorkPhotoRepository photoRepository = mock(MedicaLebenCompanyWorkPhotoRepository.class);
        when(photoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        MedicaLebenStorageService service = new MedicaLebenStorageService(
                mock(MedicaLebenCompanyDocsRepository.class),
                photoRepository,
                mock(CompanyRepository.class));
        service.setBasePathForTesting(tempDir.toString());

        MedicaLebenCompanyWorkPhoto saved = service.uploadPhoto(
                docs,
                new MockMultipartFile("photo", "original.png", "image/png", bytes.toByteArray()),
                "Foto de prueba",
                1);

        Path standardized = tempDir.resolve("company-42").resolve("photos").resolve(saved.getUrl());
        BufferedImage result = ImageIO.read(standardized.toFile());
        assertThat(saved.getUrl()).endsWith(".jpg");
        assertThat(result.getWidth()).isEqualTo(1200);
        assertThat(result.getHeight()).isEqualTo(800);
        assertThat(Files.size(standardized)).isGreaterThan(0);
    }
}