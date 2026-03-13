package com.example.nom035.service;

import com.example.nom035.dto.ContractPrepareResponseDto;
import com.example.nom035.dto.DocumentPreviewChunkDto;
import com.example.nom035.dto.DocumentTemplateFieldDto;
import com.example.nom035.entity.DocumentJob;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractGenerationServiceTest {

    @Mock
    private DocumentInterpretationService documentInterpretationService;

    @Mock
    private DocumentTemplateCatalogService documentTemplateCatalogService;

    @Mock
    private DocumentCreationService documentCreationService;

    @InjectMocks
    private ContractGenerationService contractGenerationService;

    @Test
    void prepareExtractsRfcAndDomicilioFromConstancia() {
        List<DocumentTemplateFieldDto> fields = List.of(
            new DocumentTemplateFieldDto("RFC", "RFC", true),
            new DocumentTemplateFieldDto("DOMICILIO", "DOMICILIO", true)
        );
        when(documentTemplateCatalogService.getFieldsByType("DOCUMENTO_04_1")).thenReturn(fields);

        DocumentJob actaJob = buildJob(101L);
        DocumentJob asambleaJob = buildJob(102L);
        DocumentJob constanciaJob = buildJob(103L);

        when(documentInterpretationService.process(any(MultipartFile.class), anyString(), any()))
            .thenReturn(actaJob, asambleaJob, constanciaJob);

        when(documentInterpretationService.getPreview(101L)).thenReturn(List.of(chunk("ACTA TEST")));
        when(documentInterpretationService.getPreview(102L)).thenReturn(List.of(chunk("ASAMBLEA TEST")));
        when(documentInterpretationService.getPreview(103L)).thenReturn(List.of(chunk("""
            Datos de Identificacion del Contribuyente
            Su representada se encuentra inscrita en el Registro Federal de Contribuyentes bajo la clave BPH210817RY1

            Datos del domicilio registrado
            Tipo de Vialidad: CALLE
            Nombre de Vialidad: CANGREJO
            Numero Exterior: 4875
            Numero Interior: J
            Nombre de la Colonia: LA CALMA
            Nombre de la Localidad: ZAPOPAN
            Nombre de la Entidad Federativa: JALISCO
            Codigo Postal: 45070
            """)));

        List<MultipartFile> files = List.of(
            pdf("acta.pdf"),
            pdf("asamblea.pdf"),
            pdf("constancia_situacion_fiscal.pdf")
        );

        ContractPrepareResponseDto response = contractGenerationService.prepare(files, null, "DOCUMENTO_04_1");

        assertEquals("BPH210817RY1", response.getSuggestedValues().get("RFC"));
        assertEquals(
            "Calle Cangrejo, numero 4875, Interior \"J\" Colonia La Calma, Zapopan, Jalisco, Codigo Postal 45070",
            response.getSuggestedValues().get("DOMICILIO")
        );
    }

    private static DocumentJob buildJob(Long id) {
        DocumentJob job = new DocumentJob();
        job.setId(id);
        return job;
    }

    private static DocumentPreviewChunkDto chunk(String rawText) {
        DocumentPreviewChunkDto dto = new DocumentPreviewChunkDto();
        dto.setRawText(rawText);
        dto.setInterpretedText(rawText);
        return dto;
    }

    private static MockMultipartFile pdf(String name) {
        return new MockMultipartFile(
            "files",
            name,
            "application/pdf",
            "fake-pdf".getBytes(StandardCharsets.UTF_8)
        );
    }
}
