package com.example.nom035.controller;

import com.example.nom035.entity.Survey;
import com.example.nom035.service.SurveyService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/surveys")
public class SurveyController {
    private final SurveyService surveyService;

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @GetMapping
    public List<Survey> getAll() {
        return surveyService.getAllSurveys();
    }

    @GetMapping("/{id}")
    public Optional<Survey> getById(@PathVariable Integer id) {
        return surveyService.getSurveyById(id);
    }

    @PostMapping
    public Survey create(@RequestBody Survey survey) {
        return surveyService.saveSurvey(survey);
    }

    @PutMapping("/{id}")
    public Survey update(@PathVariable Long id, @RequestBody Survey survey) {
        survey.setId(id);
        return surveyService.saveSurvey(survey);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        surveyService.deleteSurvey(id);
    }
}