package com.example.nom035.repository;

import com.example.nom035.entity.EmployeeDocs;
import com.example.nom035.entity.EmployeeDocs.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeDocsRepository extends JpaRepository<EmployeeDocs, Long> {
    List<EmployeeDocs> findByEmployeeId(Long employeeId);
    List<EmployeeDocs> findByStatus(DocumentStatus status);
    Optional<EmployeeDocs> findByIdAndEmployeeId(Long id, Long employeeId);
}
