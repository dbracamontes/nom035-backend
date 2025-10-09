package com.example.nom035.service;

import com.example.nom035.entity.CompanySurvey;
import com.example.nom035.repository.CompanySurveyRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CompanySurveyService {
    private final CompanySurveyRepository companySurveyRepository;

    public CompanySurveyService(CompanySurveyRepository companySurveyRepository) {
        this.companySurveyRepository = companySurveyRepository;
    }

    public List<CompanySurvey> getAllCompanySurveys() {
        return companySurveyRepository.findAll();
    }

    public Optional<CompanySurvey> getCompanySurveyById(Long id) {
        return companySurveyRepository.findById(id);
    }

    public List<CompanySurvey> getByCompanyId(Long companyId) {
        return companySurveyRepository.findByCompanyId(companyId);
    }

    public List<CompanySurvey> getBySurveyId(Long surveyId) {
        return companySurveyRepository.findBySurveyId(surveyId);
    }

    public CompanySurvey saveCompanySurvey(CompanySurvey companySurvey) {
        return companySurveyRepository.save(companySurvey);
    }

    public void deleteCompanySurvey(Long id) {
        companySurveyRepository.deleteById(id);
    }
}