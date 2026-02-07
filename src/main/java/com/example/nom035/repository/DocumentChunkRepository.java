package com.example.nom035.repository;

import com.example.nom035.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    List<DocumentChunk> findByJob_IdOrderByChunkIndexAsc(Long jobId);
    void deleteByJob_Id(Long jobId);
}
