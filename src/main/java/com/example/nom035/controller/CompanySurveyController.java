package com.example.nom035.controller;

import com.example.nom035.entity.CompanySurvey;
import com.example.nom035.service.CompanySurveyService;
import com.example.nom035.dto.CompanySurveyDto;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/company-surveys")
public class CompanySurveyController {
    private final CompanySurveyService companySurveyService;

    public CompanySurveyController(CompanySurveyService companySurveyService) {
        this.companySurveyService = companySurveyService;
    }

    @GetMapping
    public List<CompanySurveyDto> getAll() {
        return companySurveyService.getAllCompanySurveys()
            .stream()
            .map(CompanySurveyDto::fromEntity)
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public CompanySurveyDto getById(@PathVariable Long id) {
        return companySurveyService.getCompanySurveyById(id)
            .map(CompanySurveyDto::fromEntity)
            .orElse(null);
    }

    @GetMapping("/company/{companyId}")
    public List<CompanySurveyDto> getByCompany(@PathVariable Long companyId) {
        return companySurveyService.getByCompanyId(companyId)
            .stream()
            .map(CompanySurveyDto::fromEntity)
            .collect(Collectors.toList());
    }

    @GetMapping("/survey/{surveyId}")
    public List<CompanySurveyDto> getBySurvey(@PathVariable Long surveyId) {
        return companySurveyService.getBySurveyId(surveyId)
            .stream()
            .map(CompanySurveyDto::fromEntity)
            .collect(Collectors.toList());
    }

    @PostMapping
    public CompanySurveyDto create(@RequestBody CompanySurvey companySurvey) {
        return CompanySurveyDto.fromEntity(companySurveyService.saveCompanySurvey(companySurvey));
    }

    @PutMapping("/{id}")
    public CompanySurveyDto update(@PathVariable Long id, @RequestBody CompanySurvey companySurvey) {
        companySurvey.setId(id);
        return CompanySurveyDto.fromEntity(companySurveyService.saveCompanySurvey(companySurvey));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        companySurveyService.deleteCompanySurvey(id);
    }
}