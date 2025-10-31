package com.example.nom035.service;

import com.example.nom035.dto.SurveyApplicationCheckDto;
import com.example.nom035.dto.SurveyApplicationCreateDto;
import com.example.nom035.entity.CompanySurvey;
import com.example.nom035.entity.Employee;
import com.example.nom035.entity.Survey;
import com.example.nom035.entity.SurveyApplication;
import com.example.nom035.repository.CompanySurveyRepository;
import com.example.nom035.repository.EmployeeRepository;
import com.example.nom035.repository.SurveyApplicationRepository;
import com.example.nom035.repository.SurveyRepository;
import com.example.nom035.repository.ResponseRepository;
import com.example.nom035.entity.ApplicationStatus;
import com.example.nom035.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class SurveyApplicationService {

    private final SurveyApplicationRepository surveyApplicationRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanySurveyRepository companySurveyRepository;
    private final SurveyRepository surveyRepository;
    private final ResponseRepository responseRepository;
    private final QuestionRepository questionRepository;

    public SurveyApplicationService(SurveyApplicationRepository surveyApplicationRepository,
                                    EmployeeRepository employeeRepository,
                                    CompanySurveyRepository companySurveyRepository,
                                    SurveyRepository surveyRepository,
                                    ResponseRepository responseRepository,
                                    QuestionRepository questionRepository) {
        this.surveyApplicationRepository = surveyApplicationRepository;
        this.employeeRepository = employeeRepository;
        this.companySurveyRepository = companySurveyRepository;
        this.surveyRepository = surveyRepository;
        this.responseRepository = responseRepository;
        this.questionRepository = questionRepository;
    }

    public List<SurveyApplication> getAll() {
        return surveyApplicationRepository.findAll();
    }

    public SurveyApplication getById(Long id) {
        return surveyApplicationRepository.findById(id).orElse(null);
    }

    public SurveyApplication create(SurveyApplicationCreateDto dto) {
        if (dto.getEmployeeId() == null || dto.getSurveyId() == null) {
            throw new IllegalArgumentException("employeeId and surveyId are required");
        }

        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found: " + dto.getEmployeeId()));
        Survey survey = surveyRepository.findById(dto.getSurveyId())
                .orElseThrow(() -> new RuntimeException("Survey not found: " + dto.getSurveyId()));

        Long companyId = employee.getCompany() != null ? employee.getCompany().getId() : null;
        if (companyId == null) {
            throw new RuntimeException("Employee has no company associated");
        }

        // Try to find existing CompanySurvey, or create one if it doesn't exist
        CompanySurvey cs = companySurveyRepository
                .findByCompanyIdAndSurveyId(companyId, survey.getId())
                .orElseGet(() -> {
                    // Create a new CompanySurvey automatically
                    CompanySurvey newCs = new CompanySurvey();
                    newCs.setCompany(employee.getCompany());
                    newCs.setSurvey(survey);
                    newCs.setStatus(CompanySurvey.SurveyStatus.activo);
                    newCs.setCompanyVersion("v1");
                    newCs.setCompletionRate(java.math.BigDecimal.ZERO);
                    newCs.setNotes("Auto-created for survey application");
                    newCs.setAssignedAt(java.time.LocalDate.now());
                    // Set due date to 1 year from now
                    newCs.setDueDate(java.time.LocalDate.now().plusYears(1));
                    return companySurveyRepository.save(newCs);
                });

        SurveyApplication sa = new SurveyApplication();
        sa.setEmployee(employee);
        sa.setCompanySurvey(cs);

        // Dates
        LocalDateTime startedAt = tryParseDateTime(dto.getStartDate());
        LocalDateTime completedAt = tryParseDateTime(dto.getEndDate());
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
        sa.setStartedAt(startedAt);
        sa.setCompletedAt(completedAt);

        // Status
        ApplicationStatus status = mapStatus(dto.getStatus());
        sa.setStatusEnum(status);

        // Initial score/risk left null; can be calculated later
        return surveyApplicationRepository.save(sa);
    }

    public SurveyApplication updateStatusAndDates(Long id, String statusStr, String startDate, String endDate) {
        SurveyApplication sa = surveyApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SurveyApplication not found: " + id));

    if (statusStr != null) sa.setStatusEnum(mapStatus(statusStr));
        if (startDate != null) sa.setStartedAt(tryParseDateTime(startDate));
        if (endDate != null) sa.setCompletedAt(tryParseDateTime(endDate));

        // Calculate risk level if status is completed
        if (sa.getStatus() == SurveyApplication.ApplicationStatus.COMPLETADA) {
            calculateAndSetRiskLevel(sa);
        }

        return surveyApplicationRepository.save(sa);
    }

    /**
     * Calculate the total score and risk level for a survey application
     */
    public void calculateAndSetRiskLevel(SurveyApplication sa) {
        if (sa == null || sa.getId() == null) return;

        // Get all responses for this application
        List<com.example.nom035.entity.Response> responses = responseRepository.findBySurveyApplicationId(sa.getId());
        
        if (responses.isEmpty()) {
            sa.setScore(0);
            sa.setRiskLevel(SurveyApplication.RiskLevel.Bajo);
            return;
        }

        // Calculate total score
        int totalScore = 0;
        for (com.example.nom035.entity.Response response : responses) {
            if (response.getValue() != null) {
                totalScore += response.getValue();
            }
        }

        sa.setScore(totalScore);

        // Determine risk level based on score
        // These thresholds can be adjusted based on NOM-035 requirements
        int numQuestions = responses.size();
        double averageScore = numQuestions > 0 ? (double) totalScore / numQuestions : 0;

        // Risk level based on average score per question
        // Assuming scale: 1=Siempre (worst), 5=Nunca (best)
        // Lower average = higher risk
        if (averageScore <= 2.0) {
            sa.setRiskLevel(SurveyApplication.RiskLevel.Alto);
        } else if (averageScore <= 3.5) {
            sa.setRiskLevel(SurveyApplication.RiskLevel.Medio);
        } else {
            sa.setRiskLevel(SurveyApplication.RiskLevel.Bajo);
        }
    }

    /**
     * Recalculate risk level for an existing application
     */
    public SurveyApplication recalculateRiskLevel(Long applicationId) {
        SurveyApplication sa = surveyApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("SurveyApplication not found: " + applicationId));
        
        calculateAndSetRiskLevel(sa);
        return surveyApplicationRepository.save(sa);
    }

    public SurveyApplicationCheckDto check(Long employeeId, Long surveyId) {
        SurveyApplicationCheckDto dto = new SurveyApplicationCheckDto();
        dto.setFound(false);
        dto.setCompleted(false);
        dto.setResponsesCount(0);
        dto.setQuestionsCount(0);

        if (employeeId == null || surveyId == null) return dto;

        List<SurveyApplication> list = surveyApplicationRepository
                .findByEmployeeIdAndSurveyIdOrderByIdDesc(employeeId, surveyId);
        if (list == null || list.isEmpty()) return dto;

        SurveyApplication latest = list.get(0);
        dto.setFound(true);
        dto.setApplicationId(latest.getId());
    dto.setStatus(latest.getStatusEnum() != null ? latest.getStatusEnum().name() : null);
        dto.setCompletedAt(latest.getCompletedAt());

        int questions = questionRepository.findBySurveyId(surveyId).size();
        dto.setQuestionsCount(questions);
        long responses = responseRepository.countBySurveyApplicationId(latest.getId());
        dto.setResponsesCount((int) responses);

    boolean statusCompleted = latest.getStatusEnum() == ApplicationStatus.COMPLETADO;
        boolean completedAt = latest.getCompletedAt() != null;
        boolean allAnswered = questions > 0 && responses >= questions;

        if (statusCompleted || completedAt || allAnswered) dto.setCompleted(true);
        return dto;
    }

    private ApplicationStatus mapStatus(String s) {
        if (s == null) return ApplicationStatus.PENDIENTE;
        String v = s.trim().toLowerCase();
        if (v.contains("progres")) return ApplicationStatus.EN_PROCESO;
        if (v.contains("complet") || v.contains("finaliz") || v.equals("done") || v.equals("closed"))
            return ApplicationStatus.COMPLETADO;
        return ApplicationStatus.PENDIENTE;
    }

    private LocalDateTime tryParseDateTime(String iso) {
        if (iso == null || iso.isBlank()) return null;
        try {
            return LocalDateTime.parse(iso);
        } catch (DateTimeParseException ex) {
            try {
                // Try parsing a date-only value
                LocalDate d = LocalDate.parse(iso);
                return d.atStartOfDay();
            } catch (DateTimeParseException ignored) {}
        }
        return null;
    }
}