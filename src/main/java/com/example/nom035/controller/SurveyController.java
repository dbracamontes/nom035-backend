package com.example.nom035.controller;

import com.example.nom035.entity.Survey;
import com.example.nom035.service.SurveyService;
import com.example.nom035.dto.SurveyDto;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/surveys")
public class SurveyController {
    private final SurveyService surveyService;

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @GetMapping
    public List<SurveyDto> getAll() {
        return surveyService.getAllSurveys()
            .stream()
            .map(SurveyDto::fromEntity)
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public SurveyDto getById(@PathVariable Long id) {
        return surveyService.getSurveyById(id)
            .map(SurveyDto::fromEntity)
            .orElse(null);
    }

    @PostMapping
    public SurveyDto create(@RequestBody Survey survey) {
        return SurveyDto.fromEntity(surveyService.saveSurvey(survey));
    }

    @PutMapping("/{id}")
    public SurveyDto update(@PathVariable Long id, @RequestBody Survey survey) {
        survey.setId(id);
        return SurveyDto.fromEntity(surveyService.saveSurvey(survey));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        surveyService.deleteSurvey(id);
    }
}