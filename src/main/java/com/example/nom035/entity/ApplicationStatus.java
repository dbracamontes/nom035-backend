package com.example.nom035.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.text.Normalizer;

/**
 * Enum para estados de aplicación/encuesta.
 *
 * - Mantén aquí todas las constantes que tu aplicación/DB pueda contener.
 * - getValue() devuelve la representación en JSON/DB en minúsculas con guion_bajo.
 * - from(...) permite crear la constante desde variantes (mayúsculas/minúsculas,
 *   con/sin acentos, guiones o espacios).
 */
public enum ApplicationStatus {
    PENDIENTE("pendiente"),
    EN_PROCESO("en_proceso"),
    COMPLETADO("completado"),
    CANCELADO("cancelado"),
    RECHAZADO("rechazado"),
    APROBADO("aprobado");

    private final String value;

    ApplicationStatus(String value) {
        this.value = value;
    }

    /**
     * Valor serializado en JSON (y útil para comparaciones flexibles).
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Creador flexible para JSON -> enum.
     * Acepta:
     *  - "pendiente", "PENDIENTE", "Pendiente"
     *  - "en_proceso", "en-proceso", "en proceso", "EN PROCESO"
     *  - cadenas con o sin acentos (normaliza)
     */
    @JsonCreator
    public static ApplicationStatus from(String input) {
        if (input == null) return null;

        String normalized = input.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toLowerCase();

        // eliminar diacríticos (acentos)
        String ascii = Normalizer.normalize(normalized, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // 1) intentar por nombre de la constante (PENDIENTE, EN_PROCESO, ...)
        try {
            return ApplicationStatus.valueOf(ascii.toUpperCase());
        } catch (IllegalArgumentException ignored) {}

        // 2) intentar por value() definido en el enum (por ejemplo "pendiente", "en_proceso")
        for (ApplicationStatus s : ApplicationStatus.values()) {
            if (s.value.equalsIgnoreCase(ascii)) {
                return s;
            }
        }

        // 3) comparar sin underscores (por ejemplo "enproceso" vs "en_proceso")
        String asciiNoUnderscore = ascii.replace("_", "");
        for (ApplicationStatus s : ApplicationStatus.values()) {
            String candidate = s.value.replace("_", "");
            if (candidate.equalsIgnoreCase(asciiNoUnderscore) ||
                s.name().replace("_", "").equalsIgnoreCase(asciiNoUnderscore)) {
                return s;
            }
        }

        // No se pudo mapear - dejar explícito el error para que sea detectable en runtime.
        throw new IllegalArgumentException("Unknown ApplicationStatus: " + input);
    }

    @Override
    public String toString() {
        return this.value;
    }
}
