package com.example.nom035.service;

import com.example.nom035.entity.ConsultoriaDraft;
import com.example.nom035.repository.ConsultoriaDraftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ConsultoriaDraftService {

    private final ConsultoriaDraftRepository consultoriaDraftRepository;

    public ConsultoriaDraftService(ConsultoriaDraftRepository consultoriaDraftRepository) {
        this.consultoriaDraftRepository = consultoriaDraftRepository;
    }

    @Transactional(readOnly = true)
    public Optional<ConsultoriaDraft> getByCompanyId(Long companyId) {
        return consultoriaDraftRepository.findByCompanyId(companyId);
    }

    @Transactional
    public ConsultoriaDraft upsertByCompanyId(Long companyId, String payload) {
        ConsultoriaDraft draft = consultoriaDraftRepository.findByCompanyId(companyId)
                .orElseGet(ConsultoriaDraft::new);

        draft.setCompanyId(companyId);
        draft.setPayload(payload);
        return consultoriaDraftRepository.save(draft);
    }
}
