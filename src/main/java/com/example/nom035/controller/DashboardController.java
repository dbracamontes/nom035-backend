package com.example.nom035.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.nom035.dto.CompanySurveyDto;
import com.example.nom035.dto.EmployeeDto;
import com.example.nom035.entity.CompanySurvey;
import com.example.nom035.entity.Employee;
import com.example.nom035.entity.Response;
import com.example.nom035.entity.SurveyApplication.ApplicationStatus;
import com.example.nom035.repository.CompanySurveyRepository;
import com.example.nom035.repository.EmployeeRepository;
import com.example.nom035.repository.QuestionRepository;
import com.example.nom035.repository.ResponseRepository;
import com.example.nom035.repository.SurveyApplicationRepository;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SurveyApplicationRepository surveyAppRepository;

    @Autowired
    private CompanySurveyRepository companySurveyRepository;

    @Autowired
    private ResponseRepository responseRepository;

    @Autowired
    private QuestionRepository questionRepository;

    // -----------------------------
    // 1. Dashboard general por empresa
    // -----------------------------
    @GetMapping("/company/{companyId}")
    public ResponseEntity<?> getCompanyDashboard(@PathVariable Long companyId) {
        List<EmployeeDto> employees = employeeRepository.findByCompanyId(companyId)
            .stream()
            .map(EmployeeDto::fromEntity)
            .collect(Collectors.toList());

        List<Object[]> statusCounts = surveyAppRepository.countByStatusAndCompanyId(companyId);

        List<CompanySurveyDto> surveys = companySurveyRepository.findByCompanyId(companyId)
            .stream()
            .map(CompanySurveyDto::fromEntity)
            .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("employees", employees);
        result.put("surveyStatusCounts", statusCounts);
        result.put("surveys", surveys);

        return ResponseEntity.ok(result);
    }

    // -----------------------------
    // 2. Promedio de riesgo por factor
    // -----------------------------
    @GetMapping("/company/{companyId}/risk")
    public ResponseEntity<?> getRiskByFactor(@PathVariable Long companyId) {

        // Obtener todas las respuestas de empleados de la empresa
        List<Response> responses = responseRepository.findAllByCompanyId(companyId);

        // Agrupar por factor y calcular promedio
        Map<String, Double> avgRiskByFactor = responses.stream()
                .filter(r -> r.getValue() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getQuestion().getRiskFactor(),
                        Collectors.averagingInt(Response::getValue)
                ));

        return ResponseEntity.ok(avgRiskByFactor);
    }

    // -----------------------------
    // 3. Resumen de participación por encuesta
    // -----------------------------
    @GetMapping("/company/{companyId}/participation")
    public ResponseEntity<?> getParticipation(@PathVariable Long companyId) {

        List<CompanySurvey> surveys = companySurveyRepository.findByCompanyId(companyId);

        List<Map<String, Object>> participation = new ArrayList<>();

        for (CompanySurvey cs : surveys) {
            int totalEmployees = cs.getCompany().getEmployees().size();
            long completed = cs.getSurveyApplications().stream()
                    .filter(sa -> sa.getStatus() == ApplicationStatus.completado)
                    .count();

            Map<String, Object> map = new HashMap<>();
            map.put("surveyTitle", cs.getSurvey().getTitle());
            map.put("completionRate", totalEmployees > 0 ? (completed * 100.0 / totalEmployees) : 0);
            participation.add(map);
        }

        return ResponseEntity.ok(participation);
    }
}