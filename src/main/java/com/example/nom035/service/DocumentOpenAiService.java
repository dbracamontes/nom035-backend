package com.example.nom035.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class DocumentOpenAiService {

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final Logger log = LoggerFactory.getLogger(DocumentOpenAiService.class);

    private final RestClient restClient;
    private final String model;
    private final String apiKey;
    private final String systemPrompt;

    public DocumentOpenAiService(@org.springframework.beans.factory.annotation.Value("${OPENAI_API_KEY:}") String apiKey,
                                 @org.springframework.beans.factory.annotation.Value("${docai.openai.model:gpt-4.1-mini}") String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.systemPrompt = buildPrompt();
        this.restClient = RestClient.builder().build();
        log.info("DocumentOpenAiService inicializado con modelo '{}'", this.model);
    }

    public String interpret(String chunkText, String documentType) {
        if (!StringUtils.hasText(chunkText)) {
            return "";
        }
        if (!StringUtils.hasText(apiKey)) {
            return chunkText;
        }

        String prompt = systemPrompt;
        if (documentType != null && documentType.equalsIgnoreCase("ASAMBLEA")) {
            prompt = buildPromptForAsamblea();
        }

        Map<String, Object> request = Map.of(
            "model", model,
            "temperature", 0.2,
            "messages", List.of(
                Map.of("role", "system", "content", prompt),
                Map.of("role", "user", "content", chunkText)
            )
        );

        OpenAiChatResponse response = restClient.post()
            .uri(OPENAI_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .body(request)
            .retrieve()
            .body(OpenAiChatResponse.class);

        if (response == null || response.choices == null || response.choices.isEmpty()) {
            throw new IllegalStateException("OpenAI returned no choices");
        }
        OpenAiChatChoice choice = response.choices.get(0);
        if (choice.message == null || !StringUtils.hasText(choice.message.content)) {
            throw new IllegalStateException("OpenAI returned empty content");
        }
        return choice.message.content;
    }

    private String buildPrompt() {
        return "Eres un abogado jurídico senior especializado en actas constitutivas mexicanas.\n" +
            "Voy a enviarte texto obtenido por OCR de un documento notarial.\n" +
            "El texto puede contener errores visuales, palabras cortadas o caracteres incorrectos.\n\n" +
            "REGLAS OBLIGATORIAS:\n" +
            "1. Corrige ortografía, acentos y mayúsculas SOLO cuando el texto sea evidente.\n" +
            "2. NO inventes información legal.\n" +
            "3. Cualquier palabra, nombre, fecha, número o dato dudoso debe ir entre corchetes [ ].\n" +
            "4. Mantén estructura jurídica exacta: capítulos, artículos, numeración.\n" +
            "5. Usa redacción notarial formal en español de México.\n\n" +
            "Salida requerida (IMPORTANTE): Devuelve UNICAMENTE un objeto JSON válido (sin texto suplementario) con la siguiente estructura:\n" +
            "{\n" +
            "  \"title\": \"Título corto del documento o sección principal\",\n" +
            "  \"sections\": [ { \"heading\": \"Encabezado\", \"text\": \"Contenido del párrafo\" } ],\n" +
            "  \"plain_text\": \"(opcional) versión en texto plano del contenido\"\n" +
            "}\n" +
            "El campo 'sections' debe ser un array; cada elemento tiene 'heading' y 'text'. El campo 'plain_text' puede contener la representación completa en texto plano. NO añadas explicaciones ni envíes ningún otro contenido fuera del objeto JSON.";
    }

    // Prompt específico para actas de Asamblea
    private String buildPromptForAsamblea() {
        return "Eres un abogado jurídico senior experto en actas de asamblea y actas corporativas.\n" +
            "Voy a enviarte texto obtenido por OCR de un acta de asamblea.\n" +
            "El texto puede contener errores visuales, palabras cortadas o caracteres incorrectos.\n\n" +
            "REGLAS OBLIGATORIAS:\n" +
            "1. Corrige ortografía, acentos y mayúsculas SOLO cuando el texto sea evidente.\n" +
            "2. NO inventes información de asistentes, votos o acuerdos.\n" +
            "3. Marca entre corchetes [ ] cualquier nombre, fecha, número o dato dudoso.\n" +
            "4. Mantén la estructura del acta: lista de asistentes, orden del día, acuerdos, firmas.\n" +
            "5. Usa redacción notarial formal en español de México.\n\n" +
            "Salida requerida (IMPORTANTE): Devuelve UNICAMENTE un objeto JSON válido (sin texto suplementario) con la siguiente estructura:\n" +
            "{\n" +
            "  \"title\": \"Título corto del acta (ej. Acta de Asamblea Ordinaria)\",\n" +
            "  \"sections\": [ { \"heading\": \"Asistentes\", \"text\": \"Lista de asistentes...\" } ],\n" +
            "  \"plain_text\": \"(opcional) versión en texto plano del acta\"\n" +
            "}\n" +
            "El campo 'sections' debe ser un array; cada elemento tiene 'heading' y 'text'. El campo 'plain_text' puede contener la representación completa en texto plano. NO añadas explicaciones ni envíes ningún otro contenido fuera del objeto JSON.";
    }

    private static class OpenAiChatResponse {
        public List<OpenAiChatChoice> choices;
    }

    private static class OpenAiChatChoice {
        public OpenAiMessage message;
    }

    private static class OpenAiMessage {
        public String content;
    }
}
