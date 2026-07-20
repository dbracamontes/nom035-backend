package com.example.nom035.repository;

import com.example.nom035.entity.ConsultoriaDraft;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsultoriaDraftRepository extends JpaRepository<ConsultoriaDraft, Long> {
    Optional<ConsultoriaDraft> findByCompanyId(Long companyId);
}
