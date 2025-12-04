package com.example.nom035.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@RequestMapping("/api/medica-leben")
public class MedicaLebenFileController {

    private static final Logger log = LoggerFactory.getLogger(MedicaLebenFileController.class);

    /**
     * Base path where Medica LEBEN documents are stored on the backend.
     * Matches medica.leben.upload.base-path in application.properties.
     * Example default: /uploads/medica-leben
     */
    @Value("${medica.leben.upload.base-path:/uploads/medica-leben}")
    private String basePath;

    /**
     * Serve a specific document for a company.
     * Example URL:
     *   GET /api/medica-leben/companies/1/docs/acta_constitutiva_vuelos.png
     */
    @GetMapping("/companies/{companyId}/docs/{filename}")
    @Secured({"ROLE_ADMIN", "ROLE_COMPANY"})
    public ResponseEntity<Resource> getCompanyDoc(
            @PathVariable("companyId") Long companyId,
            @PathVariable("filename") String filename
    ) {
        // Build path: {basePath}/company-{id}/docs/{filename} without String.format flags
        File file = new File(
                basePath,
                "company-" + companyId + File.separator + "docs" + File.separator + filename
        );

        if (!file.exists() || !file.isFile()) {
            log.warn("Medica LEBEN doc not found: {}", file.getAbsolutePath());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Resource resource = new FileSystemResource(file);

        // Basic content type detection by file extension
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        String nameLower = filename.toLowerCase();
        if (nameLower.endsWith(".png")) {
            mediaType = MediaType.IMAGE_PNG;
        } else if (nameLower.endsWith(".jpg") || nameLower.endsWith(".jpeg")) {
            mediaType = MediaType.IMAGE_JPEG;
        } else if (nameLower.endsWith(".pdf")) {
            mediaType = MediaType.APPLICATION_PDF;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDispositionFormData("inline", filename);

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    /**
     * Serve a specific photo for a company.
     * Example URL:
     *   GET /api/medica-leben/companies/1/photos/logo.png
     *
     * Files are expected under: {basePath}/company-{id}/photos/{filename}
     */
    @GetMapping("/companies/{companyId}/photos/{filename}")
    @Secured({"ROLE_ADMIN", "ROLE_COMPANY"})
    public ResponseEntity<Resource> getCompanyPhoto(
            @PathVariable("companyId") Long companyId,
            @PathVariable("filename") String filename
    ) {
        // Build path: {basePath}/company-{id}/photos/{filename}
        File file = new File(
                basePath,
                "company-" + companyId + File.separator + "photos" + File.separator + filename
        );

        if (!file.exists() || !file.isFile()) {
            log.warn("Medica LEBEN photo not found: {}", file.getAbsolutePath());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Resource resource = new FileSystemResource(file);

        // Basic content type detection by file extension (images only)
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        String nameLower = filename.toLowerCase();
        if (nameLower.endsWith(".png")) {
            mediaType = MediaType.IMAGE_PNG;
        } else if (nameLower.endsWith(".jpg") || nameLower.endsWith(".jpeg")) {
            mediaType = MediaType.IMAGE_JPEG;
        } else if (nameLower.endsWith(".gif")) {
            mediaType = MediaType.IMAGE_GIF;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDispositionFormData("inline", filename);

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}