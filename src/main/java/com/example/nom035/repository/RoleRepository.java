package com.example.nom035.repository;

import com.example.nom035.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;


import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
