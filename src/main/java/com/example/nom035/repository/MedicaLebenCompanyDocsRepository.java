package com.example.nom035.repository;

import com.example.nom035.entity.Company;
import com.example.nom035.entity.MedicaLebenCompanyDocs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicaLebenCompanyDocsRepository extends JpaRepository<MedicaLebenCompanyDocs, Long> {
    Optional<MedicaLebenCompanyDocs> findByCompany(Company company);
}
