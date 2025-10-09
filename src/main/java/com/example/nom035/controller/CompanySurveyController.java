package com.example.nom035.controller;

import com.example.nom035.entity.CompanySurvey;
import com.example.nom035.service.CompanySurveyService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/company-surveys")
public class CompanySurveyController {
    private final CompanySurveyService companySurveyService;

    public CompanySurveyController(CompanySurveyService companySurveyService) {
        this.companySurveyService = companySurveyService;
    }

    @GetMapping
    public List<CompanySurvey> getAll() {
        return companySurveyService.getAllCompanySurveys();
    }

    @GetMapping("/{id}")
    public Optional<CompanySurvey> getById(@PathVariable Long id) {
        return companySurveyService.getCompanySurveyById(id);
    }

    @GetMapping("/company/{companyId}")
    public List<CompanySurvey> getByCompany(@PathVariable Long companyId) {
        return companySurveyService.getByCompanyId(companyId);
    }

    @GetMapping("/survey/{surveyId}")
    public List<CompanySurvey> getBySurvey(@PathVariable Long surveyId) {
        return companySurveyService.getBySurveyId(surveyId);
    }

    @PostMapping
    public CompanySurvey create(@RequestBody CompanySurvey companySurvey) {
        return companySurveyService.saveCompanySurvey(companySurvey);
    }

    @PutMapping("/{id}")
    public CompanySurvey update(@PathVariable Long id, @RequestBody CompanySurvey companySurvey) {
        companySurvey.setId(id);
        return companySurveyService.saveCompanySurvey(companySurvey);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        companySurveyService.deleteCompanySurvey(id);
    }
}