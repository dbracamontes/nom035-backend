package com.example.nom035.controller;

import com.example.nom035.entity.Company;
import com.example.nom035.service.CompanyService;
import com.example.nom035.dto.CompanyDto;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public List<CompanyDto> getAll() {
        return companyService.getAllCompanies()
            .stream()
            .map(CompanyDto::fromEntity)
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public CompanyDto getById(@PathVariable Long id) {
        return companyService.getCompanyById(id)
            .map(CompanyDto::fromEntity)
            .orElse(null);
    }

    @PostMapping
    public CompanyDto create(@RequestBody Company company) {
        return CompanyDto.fromEntity(companyService.saveCompany(company));
    }

    @PutMapping("/{id}")
    public CompanyDto update(@PathVariable Long id, @RequestBody Company company) {
        company.setId(id);
        return CompanyDto.fromEntity(companyService.saveCompany(company));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        companyService.deleteCompany(id);
    }
}