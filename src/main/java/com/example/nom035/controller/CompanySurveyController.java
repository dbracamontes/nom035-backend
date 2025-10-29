package com.example.nom035.controller;

import com.example.nom035.entity.CompanySurvey;
import com.example.nom035.service.CompanySurveyService;
import com.example.nom035.dto.CompanySurveyDto;
import com.example.nom035.dto.CompanySurveyCreateDto;
import com.example.nom035.dto.SurveyApplicationCreateDto;
import com.example.nom035.dto.SurveyApplicationDto;
import com.example.nom035.service.SurveyApplicationService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.annotation.Secured;

@RestController
@RequestMapping("/api/company-surveys")
public class CompanySurveyController {
    private final CompanySurveyService companySurveyService;
    private final SurveyApplicationService surveyApplicationService;

    public CompanySurveyController(CompanySurveyService companySurveyService, SurveyApplicationService surveyApplicationService) {
        this.companySurveyService = companySurveyService;
        this.surveyApplicationService = surveyApplicationService;
    }

    @GetMapping
    @Secured({"ROLE_COMPANY", "ROLE_ADMIN"})
    public List<CompanySurveyDto> getAll() {
        return companySurveyService.getAllCompanySurveys()
            .stream()
            .map(CompanySurveyDto::fromEntity)
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Secured({"ROLE_COMPANY", "ROLE_ADMIN"})
    public CompanySurveyDto getById(@PathVariable Long id) {
        return companySurveyService.getCompanySurveyById(id)
            .map(CompanySurveyDto::fromEntity)
            .orElse(null);
    }

    @GetMapping("/company/{companyId}")
    @Secured({"ROLE_COMPANY", "ROLE_ADMIN"})
    public List<CompanySurveyDto> getByCompany(@PathVariable Long companyId) {
        return companySurveyService.getByCompanyId(companyId)
            .stream()
            .map(CompanySurveyDto::fromEntity)
            .collect(Collectors.toList());
    }

    @GetMapping("/survey/{surveyId}")
    @Secured({"ROLE_COMPANY", "ROLE_ADMIN"})
    public List<CompanySurveyDto> getBySurvey(@PathVariable Long surveyId) {
        return companySurveyService.getBySurveyId(surveyId)
            .stream()
            .map(CompanySurveyDto::fromEntity)
            .collect(Collectors.toList());
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    @Secured({"ROLE_COMPANY", "ROLE_ADMIN"})
    public CompanySurveyDto create(@RequestBody CompanySurveyCreateDto createDto) {
        return CompanySurveyDto.fromEntity(companySurveyService.createCompanySurvey(createDto));
    }

    @PutMapping("/{id}")
    public CompanySurveyDto update(@PathVariable Long id, @RequestBody CompanySurvey companySurvey) {
        companySurvey.setId(id);
        return CompanySurveyDto.fromEntity(companySurveyService.saveCompanySurvey(companySurvey));
    }

    // Endpoint para asignar encuesta a empleado (empresa)
    @PostMapping("/assign")
    public SurveyApplicationDto assignSurveyToEmployee(@RequestBody SurveyApplicationCreateDto dto) {
        return SurveyApplicationDto.fromEntity(surveyApplicationService.create(dto));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        companySurveyService.deleteCompanySurvey(id);
    }
}