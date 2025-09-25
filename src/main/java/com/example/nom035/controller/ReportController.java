package com.example.nom035.controller;

import com.example.nom035.entity.SurveyResponse;
import com.example.nom035.service.SurveyResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private SurveyResponseService surveyResponseService;

    @GetMapping("/employee/{employeeId}")
    public List<SurveyResponse> reportByEmployee(@PathVariable Long employeeId) {
        return surveyResponseService.findByEmployeeId(employeeId);
    }

    @GetMapping("/department/{department}")
    public List<SurveyResponse> reportByDepartment(@PathVariable String department) {
        return surveyResponseService.findByDepartment(department);
    }

    @GetMapping("/risk/{riskLevel}")
    public List<SurveyResponse> reportByRisk(@PathVariable String riskLevel) {
        return surveyResponseService.findByRiskLevel(riskLevel);
    }

    @GetMapping("/survey/{surveyId}")
    public List<SurveyResponse> reportBySurvey(@PathVariable Long surveyId) {
        return surveyResponseService.findBySurveyId(surveyId);
    }

    @GetMapping("/risk-summary")
    public Map<String, Object> riskSummary() {
        List<SurveyResponse> all = surveyResponseService.findAll();
        Map<String, Long> counts = all.stream()
            .collect(Collectors.groupingBy(SurveyResponse::getRiskLevel, Collectors.counting()));
        double avgScore = all.stream()
            .mapToInt(r -> surveyResponseService.calculateRawScore(r))
            .average().orElse(0);
        Map<String, Object> result = new HashMap<>();
        result.put("counts", counts);
        result.put("averageScore", avgScore);
        return result;
    }
}