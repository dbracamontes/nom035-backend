package com.example.nom035.repository;

import com.example.nom035.entity.Question;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @EntityGraph(attributePaths = "options")
    List<Question> findBySurveyId(Long surveyId);

    @EntityGraph(attributePaths = "options")
    List<Question> findByGuideType(String guideType);
}