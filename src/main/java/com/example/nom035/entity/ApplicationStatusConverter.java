package com.example.nom035.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Converter JPA para ApplicationStatus.
 * - Persiste el name() de la constante en la DB (ej. PENDIENTE)
 * - A la lectura normaliza el texto de la DB y lo mapea al enum,
 *   aceptando variantes como "pendiente", "PENDIENTE", "pendiénte", "en_proceso", "en-proceso", etc.
 *
 * AutoApply = true para que JPA lo aplique automáticamente donde haya columnas del tipo ApplicationStatus.
 */
@Converter(autoApply = true)
public class ApplicationStatusConverter implements AttributeConverter<ApplicationStatus, String> {

    @Override
    public String convertToDatabaseColumn(ApplicationStatus attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public ApplicationStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        // Normalizar: reemplazar guiones por guiones bajos, espacios por underscores, eliminar acentos
        String normalized = dbData.trim().replace('-', '_').replaceAll("\\s+", "_");

        String ascii = Normalizer.normalize(normalized, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toUpperCase(Locale.ROOT);

        // Intento directo por nombre de constante (PENDIENTE, EN_PROCESO, ...)
        try {
            return ApplicationStatus.valueOf(ascii);
        } catch (IllegalArgumentException e) {
            // Intentar por 'value' definido en el enum (si existe)
            for (ApplicationStatus s : ApplicationStatus.values()) {
                String enumValue = s.getValue();
                if (enumValue != null && enumValue.equalsIgnoreCase(dbData)) {
                    return s;
                }

                // comparar sin underscores para coincidencias flexibles
                String candidate = s.name().replaceAll("_", "");
                if (candidate.equalsIgnoreCase(ascii.replaceAll("_", ""))) {
                    return s;
                }

                if (enumValue != null) {
                    String vNoUnderscore = enumValue.replaceAll("_", "");
                    if (vNoUnderscore.equalsIgnoreCase(ascii.replaceAll("_", ""))) {
                        return s;
                    }
                }
            }
            // No encontrado -> lanzar con mensaje claro
            throw new IllegalArgumentException("Unknown ApplicationStatus from DB: " + dbData);
        }
    }
}
