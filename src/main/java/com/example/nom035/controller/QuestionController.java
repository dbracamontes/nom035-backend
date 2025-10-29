package com.example.nom035.controller;

import com.example.nom035.entity.Question;
import com.example.nom035.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.security.access.annotation.Secured;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Para evitar CORS issues
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    // El endpoint principal que mencionaste
    @GetMapping("/surveys/{surveyId}/questions")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<List<Question>> getQuestionsBySurveyId(@PathVariable Long surveyId) {
        List<Question> questions = questionService.getQuestionsBySurveyId(surveyId);
        return ResponseEntity.ok(questions);
    }
    
    // Endpoint adicional por si lo necesitas
    @GetMapping("/questions")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<List<Question>> getAllQuestions() {
        return ResponseEntity.ok(questionService.getAllQuestions());
    }
    
    // Endpoint por guide type
    @GetMapping("/questions/guide/{guideType}")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<List<Question>> getQuestionsByGuideType(@PathVariable String guideType) {
        List<Question> questions = questionService.getQuestionsByGuideType(guideType);
        return ResponseEntity.ok(questions);
    }
}