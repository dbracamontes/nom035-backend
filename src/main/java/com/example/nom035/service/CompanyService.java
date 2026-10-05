package com.example.nom035.service;

import com.example.nom035.entity.Company;
import com.example.nom035.entity.Employee;
import com.example.nom035.repository.CompanyRepository;
import com.example.nom035.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;

    private final EmployeeRepository employeeRepository;
    private final UserCleanupService userCleanupService;

    public CompanyService(CompanyRepository companyRepository,
                          EmployeeRepository employeeRepository,
                          UserCleanupService userCleanupService) {
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
        this.userCleanupService = userCleanupService;
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Optional<Company> getCompanyById(Long id) {
        return companyRepository.findById(id);
    }

    /**
     * Save a company. If another company with the same taxId already exists,
     * update that record instead of trying to insert a new one, in order to
     * honor the unique constraint on tax_id (company.uq_company_tax_id).
     */
    public Company saveCompany(Company company) {
        String taxId = company.getTaxId();

        if (taxId != null && !taxId.isBlank()) {
            Optional<Company> existing = companyRepository.findByTaxId(taxId);
            if (existing.isPresent()) {
                Company current = existing.get();
                // preserve the existing id and createdAt
                company.setId(current.getId());
                if (company.getCreatedAt() == null) {
                    company.setCreatedAt(current.getCreatedAt());
                }
            }
        }

        return companyRepository.save(company);
    }

    @Transactional
    public void deleteCompany(Long id) {
        List<Long> employeeIds = employeeRepository.findByCompanyId(id).stream()
                .map(Employee::getId)
                .collect(Collectors.toList());
        userCleanupService.deleteUsersForEmployees(employeeIds);
        userCleanupService.deleteUsersForCompany(id);
        companyRepository.deleteById(id);
    }
}