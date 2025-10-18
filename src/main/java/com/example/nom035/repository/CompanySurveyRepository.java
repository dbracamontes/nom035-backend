package com.example.nom035.repository;

import com.example.nom035.entity.CompanySurvey;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CompanySurveyRepository extends JpaRepository<CompanySurvey, Long> {
    List<CompanySurvey> findByCompanyId(Long companyId);
    List<CompanySurvey> findBySurveyId(Long surveyId);
    Optional<CompanySurvey> findByCompanyIdAndSurveyId(Long companyId, Long surveyId);
}