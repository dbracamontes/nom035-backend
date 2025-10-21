package com.example.nom035.service;

import com.example.nom035.entity.Question;
import com.example.nom035.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class QuestionService {
    
    @Autowired
    private QuestionRepository questionRepository;
    
    public List<Question> getQuestionsBySurveyId(Long surveyId) {
        return questionRepository.findBySurveyId(surveyId);
    }
    
    public List<Question> getQuestionsByGuideType(String guideType) {
        return questionRepository.findByGuideType(guideType);
    }
    
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }
}