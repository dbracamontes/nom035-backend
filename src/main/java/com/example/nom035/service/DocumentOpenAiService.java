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

        Map<String, Object> request = Map.of(
            "model", model,
            "temperature", 0.2,
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
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
        return "Actua como un abogado corporativo experto en derecho mercantil y contratacion en Mexico, con experiencia en contratos de prestacion de servicios.\n" +
            "Recibiras texto OCR proveniente del paquete documental empresarial (ACTA CONSTITUTIVA, ACTA DE ASAMBLEA y CONSTANCIA DE SITUACION FISCAL), posiblemente en fragmentos.\n" +
            "Analiza de forma juridica y extrae solo informacion verificable para llenar campos de plantilla contractual.\n\n" +
            "OBJETIVO:\n" +
            "1. Identificar datos relevantes de las partes y facultades de representacion.\n" +
            "2. Validar coherencia basica entre documentos (ejemplo: representante con facultades vigentes).\n" +
            "3. Proponer candidatos para campos de contrato sin inventar datos.\n\n" +
            "REGLAS OBLIGATORIAS:\n" +
            "1. Corrige ortografia solo cuando sea evidente.\n" +
            "2. NO inventes datos legales, fechas, RFC, nombres o numeros notariales.\n" +
            "3. Si un dato es dudoso, colocalo entre corchetes [ ].\n" +
            "4. Si no hay evidencia suficiente, devuelve string vacio en ese campo.\n" +
            "5. Devuelve UNICAMENTE JSON valido, sin texto adicional.\n\n" +
            "SALIDA REQUERIDA (JSON):\n" +
            "{\n" +
            "  \"document_scope\": \"CONTRACT_PACKAGE\",\n" +
            "  \"title\": \"Resumen juridico de extraccion\",\n" +
            "  \"consistency_notes\": [\"nota breve 1\", \"nota breve 2\"],\n" +
            "  \"sections\": [ { \"heading\": \"Fuente\", \"text\": \"Hallazgos relevantes\" } ],\n" +
            "  \"plain_text\": \"(opcional) resumen consolidado\",\n" +
            "  \"contract_field_candidates\": {\n" +
            "    \"EL_CLIENTE\": \"\",\n" +
            "    \"REPRESENTANTE_DE\": \"\",\n" +
            "    \"RFC\": \"\",\n" +
            "    \"DOMICILIO\": \"\",\n" +
            "    \"ESCRITURA_PUBLICA_ACTA_NUMERO\": \"\",\n" +
            "    \"FECHA_ACTA\": \"\",\n" +
            "    \"LICENCIADO_ACTA_DA_FE\": \"\",\n" +
            "    \"CORREDURIA_PUBLICA_NO\": \"\",\n" +
            "    \"ESCRITURA_PUBLICA_ASAMBLEA_NO\": \"\",\n" +
            "    \"FECHA_ASAMBLEA\": \"\",\n" +
            "    \"LICENCIADO_ASAMBLEA_DA_FE\": \"\",\n" +
            "    \"NOTARIA_PUBLICA_NO\": \"\",\n" +
            "    \"CIUDAD_ASAMBLEA\": \"\",\n" +
            "    \"CIUDADANO\": \"\"\n" +
            "  }\n" +
            "}\n" +
            "El objeto contract_field_candidates debe contener SOLO valores con evidencia textual clara; en ausencia de evidencia, deja string vacio.";
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
