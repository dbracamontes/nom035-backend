package com.example.nom035.repository;

import com.example.nom035.entity.DocumentJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentJobRepository extends JpaRepository<DocumentJob, Long> {
	List<DocumentJob> findTop200BySourceModuleOrderByCreatedAtDesc(String sourceModule);
}
