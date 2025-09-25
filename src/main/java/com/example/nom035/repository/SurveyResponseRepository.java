package com.example.nom035.repository;

import com.example.nom035.entity.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
    List<SurveyResponse> findByEmployeeId(Long employeeId);
    List<SurveyResponse> findByEmployeeDepartment(String department);
    List<SurveyResponse> findByRiskLevel(String riskLevel);
    List<SurveyResponse> findBySurveyId(Long surveyId);
    List<SurveyResponse> findBySubmittedAtBetween(LocalDateTime start, LocalDateTime end);
    List<SurveyResponse> findByRiskLevelIn(List<String> riskLevels);
}