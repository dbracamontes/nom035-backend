package com.example.nom035.controller;

import com.example.nom035.entity.CompanySurvey;
import com.example.nom035.entity.Company;
import com.example.nom035.entity.Survey;
import com.example.nom035.service.CompanySurveyService;
import com.example.nom035.service.CompanyService;
import com.example.nom035.service.SurveyService;
import com.example.nom035.dto.CompanySurveyDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class CompanySurveyController {
    private final CompanySurveyService companySurveyService;
    private final CompanyService companyService;
    private final SurveyService surveyService;

    public CompanySurveyController(CompanySurveyService companySurveyService, CompanyService companyService, SurveyService surveyService) {
        this.companySurveyService = companySurveyService;
        this.companyService = companyService;
        this.surveyService = surveyService;
    }

    @GetMapping("/company-surveys")
    public List<CompanySurveyDto> getAll() {
        return companySurveyService.getAllCompanySurveys()
            .stream()
            .map(CompanySurveyDto::fromEntity)
            .collect(Collectors.toList());
    }

    @GetMapping("/company-surveys/{id}")
    public CompanySurveyDto getById(@PathVariable Long id) {
        return companySurveyService.getCompanySurveyById(id)
            .map(CompanySurveyDto::fromEntity)
            .orElse(null);
    }

    @GetMapping("/company-surveys/company/{companyId}")
    public List<CompanySurveyDto> getByCompany(@PathVariable Long companyId) {
        return companySurveyService.getByCompanyId(companyId)
            .stream()
            .map(CompanySurveyDto::fromEntity)
            .collect(Collectors.toList());
    }

    @GetMapping("/company-surveys/survey/{surveyId}")
    public List<CompanySurveyDto> getBySurvey(@PathVariable Long surveyId) {
        return companySurveyService.getBySurveyId(surveyId)
            .stream()
            .map(CompanySurveyDto::fromEntity)
            .collect(Collectors.toList());
    }

    @PostMapping("/company-surveys")
    public ResponseEntity<CompanySurveyDto> createCompanySurvey(@RequestBody CreateCompanySurveyRequest request) {
        // Validar que company existe
        Company company = companyService.getCompanyById(request.getCompanyId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company not found"));

        // Validar que survey existe
        Survey survey = surveyService.getSurveyById(request.getSurveyId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found"));

        // Crear nueva instancia de CompanySurvey
        CompanySurvey companySurvey = new CompanySurvey();
        companySurvey.setCompany(company);
        companySurvey.setSurvey(survey);
        companySurvey.setAssignedAt(LocalDate.parse(request.getAssignedAt()));
        companySurvey.setNotes(request.getNotes());
        companySurvey.setStatus(CompanySurvey.SurveyStatus.activo);

        // Lógica para crear la encuesta de empresa
        CompanySurvey saved = companySurveyService.saveCompanySurvey(companySurvey);
        return ResponseEntity.status(HttpStatus.CREATED).body(CompanySurveyDto.fromEntity(saved));
    }

    // Clase interna para el request
    public static class CreateCompanySurveyRequest {
        public Long companyId;
        public Long surveyId;
        public String assignedAt;  // Cambié a String para evitar problemas de serialización
        public String notes;

        // Constructor vacío necesario para Jackson
        public CreateCompanySurveyRequest() {}

        // Getters y setters
        public Long getCompanyId() { return companyId; }
        public void setCompanyId(Long companyId) { this.companyId = companyId; }
        
        public Long getSurveyId() { return surveyId; }
        public void setSurveyId(Long surveyId) { this.surveyId = surveyId; }
        
        public String getAssignedAt() { return assignedAt; }
        public void setAssignedAt(String assignedAt) { this.assignedAt = assignedAt; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    @PutMapping("/company-surveys/{id}")
    public CompanySurveyDto update(@PathVariable Long id, @RequestBody CompanySurvey companySurvey) {
        companySurvey.setId(id);
        return CompanySurveyDto.fromEntity(companySurveyService.saveCompanySurvey(companySurvey));
    }

    @DeleteMapping("/company-surveys/{id}")
    public void delete(@PathVariable Long id) {
        companySurveyService.deleteCompanySurvey(id);
    }
}