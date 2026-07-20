package com.example.nom035.service;

import com.example.nom035.entity.ConsultoriaDraft;
import com.example.nom035.repository.ConsultoriaDraftRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultoriaDraftServiceTest {

    @Mock
    private ConsultoriaDraftRepository consultoriaDraftRepository;

    @InjectMocks
    private ConsultoriaDraftService consultoriaDraftService;

    @Test
    void upsertByCompanyIdCreatesDraftWhenMissing() {
        when(consultoriaDraftRepository.findByCompanyId(10L)).thenReturn(Optional.empty());
        when(consultoriaDraftRepository.save(any(ConsultoriaDraft.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConsultoriaDraft saved = consultoriaDraftService.upsertByCompanyId(10L, "{\"a\":1}");

        assertEquals(10L, saved.getCompanyId());
        assertEquals("{\"a\":1}", saved.getPayload());
        verify(consultoriaDraftRepository).save(any(ConsultoriaDraft.class));
    }

    @Test
    void upsertByCompanyIdUpdatesExistingDraft() {
        ConsultoriaDraft existing = new ConsultoriaDraft();
        existing.setId(77L);
        existing.setCompanyId(10L);
        existing.setPayload("{\"old\":true}");

        when(consultoriaDraftRepository.findByCompanyId(10L)).thenReturn(Optional.of(existing));
        when(consultoriaDraftRepository.save(any(ConsultoriaDraft.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConsultoriaDraft saved = consultoriaDraftService.upsertByCompanyId(10L, "{\"old\":false}");

        assertEquals(77L, saved.getId());
        assertEquals(10L, saved.getCompanyId());
        assertEquals("{\"old\":false}", saved.getPayload());
        verify(consultoriaDraftRepository).save(existing);
    }
}
