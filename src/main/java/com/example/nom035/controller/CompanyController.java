package com.example.nom035.controller;

import com.example.nom035.entity.Company;
import com.example.nom035.service.CompanyService;
import com.example.nom035.dto.CompanyDto;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.access.annotation.Secured;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.nom035.repository.UserRepository;
import com.example.nom035.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyService companyService;

    @Autowired
    private UserRepository userRepository;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    // Helper para obtener el usuario autenticado
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    // Helper para saber si es ADMIN
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // Helper para obtener el id de la empresa asociada al usuario COMPANY
    private Long getCompanyIdForCurrentUser() {
        User user = getCurrentUser();
        if (user == null) return null;
        // TODO: Relacionar User con Company directamente para robustez
        // Por ahora, se asume que el username es el email de la empresa y hay coincidencia 1:1
        // Si tienes un campo companyId en User, úsalo aquí
        return null; // Implementar lógica real según tu modelo
    }

    @GetMapping
    @Secured("ROLE_COMPANY")
    public List<CompanyDto> getAll() {
        if (isAdmin()) {
            return companyService.getAllCompanies()
                .stream()
                .map(CompanyDto::fromEntity)
                .collect(Collectors.toList());
        } else {
            Long companyId = getCompanyIdForCurrentUser();
            if (companyId == null) return List.of();
            return companyService.getAllCompanies().stream()
                .filter(c -> c.getId().equals(companyId))
                .map(CompanyDto::fromEntity)
                .collect(Collectors.toList());
        }
    }

    @GetMapping("/{id}")
    @Secured("ROLE_COMPANY")
    public CompanyDto getById(@PathVariable Long id) {
        if (!isAdmin()) {
            Long companyId = getCompanyIdForCurrentUser();
            if (companyId == null || !companyId.equals(id)) {
                return null;
            }
        }
        return companyService.getCompanyById(id)
            .map(CompanyDto::fromEntity)
            .orElse(null);
    }

    @PostMapping
    @Secured("ROLE_COMPANY")
    public CompanyDto create(@RequestBody Company company) {
        if (!isAdmin()) {
            Long companyId = getCompanyIdForCurrentUser();
            if (companyId == null || !company.getId().equals(companyId)) {
                return null;
            }
        }
        return CompanyDto.fromEntity(companyService.saveCompany(company));
    }

    @PutMapping("/{id}")
    @Secured("ROLE_COMPANY")
    public CompanyDto update(@PathVariable Long id, @RequestBody Company company) {
        if (!isAdmin()) {
            Long companyId = getCompanyIdForCurrentUser();
            if (companyId == null || !companyId.equals(id)) {
                return null;
            }
        }
        company.setId(id);
        return CompanyDto.fromEntity(companyService.saveCompany(company));
    }

    @DeleteMapping("/{id}")
    @Secured("ROLE_COMPANY")
    public void delete(@PathVariable Long id) {
        if (!isAdmin()) {
            Long companyId = getCompanyIdForCurrentUser();
            if (companyId == null || !companyId.equals(id)) {
                return;
            }
        }
        companyService.deleteCompany(id);
    }
}