package com.example.nom035.service;

import com.example.nom035.entity.CompanySurvey;
import com.example.nom035.entity.Company;
import com.example.nom035.entity.Survey;
import com.example.nom035.dto.CompanySurveyCreateDto;
import com.example.nom035.repository.CompanySurveyRepository;
import com.example.nom035.repository.CompanyRepository;
import com.example.nom035.repository.SurveyRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CompanySurveyService {
    private final CompanySurveyRepository companySurveyRepository;
    private final CompanyRepository companyRepository;
    private final SurveyRepository surveyRepository;

    public CompanySurveyService(CompanySurveyRepository companySurveyRepository,
                               CompanyRepository companyRepository,
                               SurveyRepository surveyRepository) {
        this.companySurveyRepository = companySurveyRepository;
        this.companyRepository = companyRepository;
        this.surveyRepository = surveyRepository;
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

    public CompanySurvey createCompanySurvey(CompanySurveyCreateDto createDto) {
        // Cargar entidades desde la base de datos
        Company company = companyRepository.findById(createDto.getCompanyId())
            .orElseThrow(() -> new RuntimeException("Company not found with id: " + createDto.getCompanyId()));
        
        Survey survey = surveyRepository.findById(createDto.getSurveyId())
            .orElseThrow(() -> new RuntimeException("Survey not found with id: " + createDto.getSurveyId()));

        // Verificar si ya existe una combinación company_id + survey_id
        Optional<CompanySurvey> existingCompanySurvey = companySurveyRepository
                .findByCompanyIdAndSurveyId(createDto.getCompanyId(), createDto.getSurveyId());
        
        CompanySurvey companySurvey;
        if (existingCompanySurvey.isPresent()) {
            // Actualizar el registro existente
            companySurvey = existingCompanySurvey.get();
            // Solo actualizar los campos que vienen en el DTO
            if (createDto.getDueDate() != null) {
                companySurvey.setDueDate(createDto.getDueDate());
            }
            if (createDto.getCompanyVersion() != null) {
                companySurvey.setCompanyVersion(createDto.getCompanyVersion());
            }
            if (createDto.getStatus() != null) {
                companySurvey.setStatus(CompanySurvey.SurveyStatus.valueOf(createDto.getStatus()));
            }
            if (createDto.getCompletionRate() != null) {
                companySurvey.setCompletionRate(createDto.getCompletionRate());
            }
            if (createDto.getNotes() != null) {
                companySurvey.setNotes(createDto.getNotes());
            }
            // No actualizar assignedAt para mantener la fecha original de asignación
        } else {
            // Crear nueva entidad CompanySurvey
            companySurvey = new CompanySurvey();
            companySurvey.setCompany(company);
            companySurvey.setSurvey(survey);
            // Si no se proporciona assignedAt, usar el día actual
            companySurvey.setAssignedAt(createDto.getAssignedAt() != null ? 
                createDto.getAssignedAt() : LocalDate.now());
            companySurvey.setDueDate(createDto.getDueDate());
            companySurvey.setCompanyVersion(createDto.getCompanyVersion());
            
            // Convertir string a enum
            if (createDto.getStatus() != null) {
                companySurvey.setStatus(CompanySurvey.SurveyStatus.valueOf(createDto.getStatus()));
            }
            
            companySurvey.setCompletionRate(createDto.getCompletionRate());
            companySurvey.setNotes(createDto.getNotes());
        }

        return companySurveyRepository.save(companySurvey);
    }

    public void deleteCompanySurvey(Long id) {
        companySurveyRepository.deleteById(id);
    }
}