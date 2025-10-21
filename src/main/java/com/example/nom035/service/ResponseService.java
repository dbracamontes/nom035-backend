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

    public ResponseDto createResponse(ResponseCreateDto responseCreateDto) {
        Response response = new Response();
        
        // Buscar y asignar SurveyApplication
        SurveyApplication surveyApplication = surveyApplicationRepository.findById(responseCreateDto.getSurveyApplicationId())
                .orElseThrow(() -> new RuntimeException("SurveyApplication not found with id: " + responseCreateDto.getSurveyApplicationId()));
        response.setSurveyApplication(surveyApplication);
        
        // Buscar y asignar Question
        Question question = questionRepository.findById(responseCreateDto.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + responseCreateDto.getQuestionId()));
        response.setQuestion(question);
        
        // Asignar OptionAnswer si existe
        if (responseCreateDto.getOptionAnswerId() != null) {
            OptionAnswer optionAnswer = optionAnswerRepository.findById(responseCreateDto.getOptionAnswerId())
                    .orElseThrow(() -> new RuntimeException("OptionAnswer not found with id: " + responseCreateDto.getOptionAnswerId()));
            response.setOptionAnswer(optionAnswer);
        }
        
        // Asignar respuesta de texto si existe
        response.setFreeText(responseCreateDto.getTextAnswer());
        
        Response savedResponse = responseRepository.save(response);
        return convertToDto(savedResponse);
    }

    public ResponseDto updateResponse(Long id, ResponseCreateDto responseCreateDto) {
        Response response = responseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Response not found with id: " + id));
        
        // Actualizar SurveyApplication
        if (responseCreateDto.getSurveyApplicationId() != null) {
            SurveyApplication surveyApplication = surveyApplicationRepository.findById(responseCreateDto.getSurveyApplicationId())
                    .orElseThrow(() -> new RuntimeException("SurveyApplication not found with id: " + responseCreateDto.getSurveyApplicationId()));
            response.setSurveyApplication(surveyApplication);
        }
        
        // Actualizar Question
        if (responseCreateDto.getQuestionId() != null) {
            Question question = questionRepository.findById(responseCreateDto.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question not found with id: " + responseCreateDto.getQuestionId()));
            response.setQuestion(question);
        }
        
        // Actualizar OptionAnswer
        if (responseCreateDto.getOptionAnswerId() != null) {
            OptionAnswer optionAnswer = optionAnswerRepository.findById(responseCreateDto.getOptionAnswerId())
                    .orElseThrow(() -> new RuntimeException("OptionAnswer not found with id: " + responseCreateDto.getOptionAnswerId()));
            response.setOptionAnswer(optionAnswer);
        } else {
            response.setOptionAnswer(null);
        }
        
        // Actualizar texto
        response.setFreeText(responseCreateDto.getTextAnswer());
        
        Response updatedResponse = responseRepository.save(response);
        return convertToDto(updatedResponse);
    }

    public void deleteResponse(Long id) {
        if (!responseRepository.existsById(id)) {
            throw new RuntimeException("Response not found with id: " + id);
        }
        responseRepository.deleteById(id);
    }

    private ResponseDto convertToDto(Response response) {
        return new ResponseDto(
                response.getId(),
                response.getSurveyApplication() != null ? response.getSurveyApplication().getId() : null,
                response.getQuestion() != null ? response.getQuestion().getId() : null,
                response.getOptionAnswer() != null ? response.getOptionAnswer().getId() : null,
                response.getFreeText()
        );
    }
}