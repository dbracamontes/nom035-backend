package com.example.nom035.repository;

import com.example.nom035.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findBySurveyId(Long surveyId);
}