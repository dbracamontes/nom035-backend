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

        /**
         * Recalcula y actualiza el completionRate de una CompanySurvey
         */
        @org.springframework.beans.factory.annotation.Autowired
        private com.example.nom035.repository.SurveyApplicationRepository surveyApplicationRepository;

        public void recalculateCompletionRate(Long companySurveyId) {
            Optional<CompanySurvey> opt = companySurveyRepository.findById(companySurveyId);
            if (opt.isEmpty()) return;
            CompanySurvey cs = opt.get();
            List<com.example.nom035.entity.SurveyApplication> apps = surveyApplicationRepository.findByCompanySurveyId(companySurveyId);
            if (apps == null || apps.isEmpty()) {
                cs.setCompletionRate(java.math.BigDecimal.ZERO);
                companySurveyRepository.save(cs);
                return;
            }
            // Calcular empleados únicos asignados
            java.util.Set<Long> empleadosAsignados = new java.util.HashSet<>();
            java.util.Set<Long> empleadosCompletados = new java.util.HashSet<>();
            for (com.example.nom035.entity.SurveyApplication app : apps) {
                if (app.getEmployee() != null && app.getEmployee().getId() != null) {
                    empleadosAsignados.add(app.getEmployee().getId());
                    if (app.getCompletedAt() != null) {
                        empleadosCompletados.add(app.getEmployee().getId());
                    }
                }
            }
            int totalAsignados = empleadosAsignados.size();
            int totalCompletados = empleadosCompletados.size();
            java.math.BigDecimal rate = java.math.BigDecimal.ZERO;
            if (totalAsignados > 0) {
                rate = java.math.BigDecimal.valueOf((double) totalCompletados / totalAsignados);
            }
            cs.setCompletionRate(rate);
            companySurveyRepository.save(cs);
        }
}