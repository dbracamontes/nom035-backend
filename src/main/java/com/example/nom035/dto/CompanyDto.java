package com.example.nom035.dto;

import com.example.nom035.entity.Company;
import java.time.LocalDateTime;

public class CompanyDto {
    private Long id;
    private String name;
    private String taxId;
    private LocalDateTime createdAt;
    // Optionally, add employee count or summary fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Mapping method
    public static CompanyDto fromEntity(Company company) {
        CompanyDto dto = new CompanyDto();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setTaxId(company.getTaxId());
        dto.setCreatedAt(company.getCreatedAt());
        return dto;
    }
}