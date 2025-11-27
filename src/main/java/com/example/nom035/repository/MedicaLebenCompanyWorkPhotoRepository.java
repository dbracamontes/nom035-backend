package com.example.nom035.repository;

import com.example.nom035.entity.MedicaLebenCompanyDocs;
import com.example.nom035.entity.MedicaLebenCompanyWorkPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicaLebenCompanyWorkPhotoRepository extends JpaRepository<MedicaLebenCompanyWorkPhoto, Long> {
    List<MedicaLebenCompanyWorkPhoto> findByCompanyDocsOrderBySortOrderAsc(MedicaLebenCompanyDocs docs);
}
