package com.example.nom035.repository;

import com.example.nom035.entity.Privilege;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {
    // Métodos personalizados si se requieren
}
