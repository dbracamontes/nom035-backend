package com.example.nom035.service;

import com.example.nom035.dto.ResponseCreateDto;
import com.example.nom035.dto.ResponseDto;
import com.example.nom035.entity.Response;
import com.example.nom035.entity.SurveyApplication;
import com.example.nom035.entity.Question;
import com.example.nom035.entity.OptionAnswer;
import com.example.nom035.repository.ResponseRepository;
import com.example.nom035.repository.SurveyApplicationRepository;
import com.example.nom035.repository.QuestionRepository;
import com.example.nom035.repository.OptionAnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ResponseService {

    @Autowired
    private ResponseRepository responseRepository;
    
    @Autowired
    private SurveyApplicationRepository surveyApplicationRepository;
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private OptionAnswerRepository optionAnswerRepository;

    @Autowired
    private SurveyApplicationService surveyApplicationService;

    public List<ResponseDto> getAllResponses() {
        return responseRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<ResponseDto> getResponseById(Long id) {
        return responseRepository.findById(id)
                .map(this::convertToDto);
    }

    public List<ResponseDto> getResponsesBySurveyApplication(Long surveyApplicationId) {
        return responseRepository.findBySurveyApplicationId(surveyApplicationId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ResponseDto> getFilteredResponses(Long employeeId, Long surveyId) {
        if (employeeId != null && surveyId != null) {
            // Get responses by employee and survey
            return responseRepository.findByEmployeeIdAndSurveyId(employeeId, surveyId).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else if (employeeId != null) {
            // Get all responses by employee (across all surveys)
            return responseRepository.findAll().stream()
                    .filter(r -> r.getSurveyApplication() != null && 
                                r.getSurveyApplication().getEmployee() != null &&
                                r.getSurveyApplication().getEmployee().getId().equals(employeeId))
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else if (surveyId != null) {
            // Get all responses for a survey (across all employees)
            return responseRepository.findAll().stream()
                    .filter(r -> r.getQuestion() != null && 
                                r.getQuestion().getSurvey() != null &&
                                r.getQuestion().getSurvey().getId().equals(surveyId))
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }
        // No filters, return all
        return getAllResponses();
    }

    public ResponseDto createResponse(ResponseCreateDto responseCreateDto) {
        // Validate SurveyApplication
        SurveyApplication surveyApplication = surveyApplicationRepository.findById(responseCreateDto.getSurveyApplicationId())
                .orElseThrow(() -> new RuntimeException("SurveyApplication not found with id: " + responseCreateDto.getSurveyApplicationId()));
        
        // Validate Question
        Question question = questionRepository.findById(responseCreateDto.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + responseCreateDto.getQuestionId()));

        // Idempotent upsert by (surveyApplication, question)
        Response response = responseRepository
                .findBySurveyApplicationIdAndQuestionId(surveyApplication.getId(), question.getId())
                .orElse(new Response());

        response.setSurveyApplication(surveyApplication);
        response.setQuestion(question);

        // OptionAnswer (optional)
        OptionAnswer optionAnswer = null;
        if (responseCreateDto.getOptionAnswerId() != null) {
            optionAnswer = optionAnswerRepository.findById(responseCreateDto.getOptionAnswerId())
                    .orElseThrow(() -> new RuntimeException("OptionAnswer not found with id: " + responseCreateDto.getOptionAnswerId()));
            response.setOptionAnswer(optionAnswer);
        } else {
            response.setOptionAnswer(null);
        }
        
        // Free text (support both freeText and textAnswer)
        String freeText = responseCreateDto.getFreeText() != null ? responseCreateDto.getFreeText() : responseCreateDto.getTextAnswer();
        response.setFreeText(freeText);

        // Numeric value
        if (responseCreateDto.getValue() != null) {
            response.setValue(responseCreateDto.getValue());
        } else if (optionAnswer != null && optionAnswer.getValue() != null) {
            response.setValue(optionAnswer.getValue());
        } else {
            // No value provided; leave null to not affect scoring
            response.setValue(null);
        }

        // Timestamp
        response.setAnsweredAt(LocalDateTime.now());
        
        Response savedResponse = responseRepository.save(response);
        
        // Recalculate the application's score and risk level
        try {
            surveyApplicationService.calculateAndSetRiskLevel(surveyApplication);
            surveyApplicationRepository.save(surveyApplication);
        } catch (Exception e) {
            System.err.println("Error calculating risk level: " + e.getMessage());
        }
        
        return convertToDto(savedResponse);
    }

    public ResponseDto updateResponse(Long id, ResponseCreateDto responseCreateDto) {
        Response response = responseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Response not found with id: " + id));
        
        SurveyApplication surveyApplication = response.getSurveyApplication();
        
        // Update SurveyApplication
        if (responseCreateDto.getSurveyApplicationId() != null) {
            surveyApplication = surveyApplicationRepository.findById(responseCreateDto.getSurveyApplicationId())
                    .orElseThrow(() -> new RuntimeException("SurveyApplication not found with id: " + responseCreateDto.getSurveyApplicationId()));
            response.setSurveyApplication(surveyApplication);
        }
        
        // Update Question
        if (responseCreateDto.getQuestionId() != null) {
            Question question = questionRepository.findById(responseCreateDto.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question not found with id: " + responseCreateDto.getQuestionId()));
            response.setQuestion(question);
        }
        
        // Update OptionAnswer
        if (responseCreateDto.getOptionAnswerId() != null) {
            OptionAnswer optionAnswer = optionAnswerRepository.findById(responseCreateDto.getOptionAnswerId())
                    .orElseThrow(() -> new RuntimeException("OptionAnswer not found with id: " + responseCreateDto.getOptionAnswerId()));
            response.setOptionAnswer(optionAnswer);
            // If no explicit value provided, mirror optionAnswer's numeric value
            if (responseCreateDto.getValue() == null) {
                response.setValue(optionAnswer.getValue());
            }
        } else {
            response.setOptionAnswer(null);
        }
        
        // Update numeric value if provided
        if (responseCreateDto.getValue() != null) {
            response.setValue(responseCreateDto.getValue());
        }
        
        // Update text
        String freeText = responseCreateDto.getFreeText() != null ? responseCreateDto.getFreeText() : responseCreateDto.getTextAnswer();
        response.setFreeText(freeText);
        
        Response updatedResponse = responseRepository.save(response);
        
        // Recalculate the application's score and risk level
        try {
            surveyApplicationService.calculateAndSetRiskLevel(surveyApplication);
            surveyApplicationRepository.save(surveyApplication);
        } catch (Exception e) {
            System.err.println("Error calculating risk level: " + e.getMessage());
        }
        
        return convertToDto(updatedResponse);
    }

    public void deleteResponse(Long id) {
        if (!responseRepository.existsById(id)) {
            throw new RuntimeException("Response not found with id: " + id);
        }
        responseRepository.deleteById(id);
    }

    private ResponseDto convertToDto(Response response) {
        ResponseDto dto = new ResponseDto(
                response.getId(),
                response.getSurveyApplication() != null ? response.getSurveyApplication().getId() : null,
                response.getQuestion() != null ? response.getQuestion().getId() : null,
                response.getOptionAnswer() != null ? response.getOptionAnswer().getId() : null,
                response.getFreeText()
        );
        // Enrich with numeric value and freeText fields for clients expecting them
        dto.setValue(response.getValue());
        dto.setFreeText(response.getFreeText());
        return dto;
    }
}