package com.example.nom035.service;

import com.example.nom035.entity.Company;
import com.example.nom035.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
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

    public void deleteCompany(Long id) {
        companyRepository.deleteById(id);
    }
}