package com.example.nom035.repository;


import com.example.nom035.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByCompanyId(Long companyId);
    List<Employee> findByCompanyIdAndStatus(Long companyId, com.example.nom035.entity.Employee.EmployeeStatus status);
    Optional<Employee> findByEmail(String email);
}