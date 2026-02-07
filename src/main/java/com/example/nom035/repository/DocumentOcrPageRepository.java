package com.example.nom035.repository;

import com.example.nom035.entity.DocumentOcrPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentOcrPageRepository extends JpaRepository<DocumentOcrPage, Long> {
    List<DocumentOcrPage> findByJob_IdOrderByPageNumberAsc(Long jobId);
    void deleteByJob_Id(Long jobId);
}
