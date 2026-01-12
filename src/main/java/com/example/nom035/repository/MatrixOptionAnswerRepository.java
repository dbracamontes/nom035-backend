package com.example.nom035.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.nom035.entity.MatrixOptionAnswer;

public interface MatrixOptionAnswerRepository extends JpaRepository<MatrixOptionAnswer, Long> {

    List<MatrixOptionAnswer> findByQuestionId(Long questionId);

    Optional<MatrixOptionAnswer> findByQuestionIdAndCategoryAndText(Long questionId, String category, String text);

    @Query("SELECT MIN(m.value) FROM MatrixOptionAnswer m WHERE m.questionId = :questionId")
    Integer findMinValueByQuestionId(@Param("questionId") Long questionId);

    @Query("SELECT MAX(m.value) FROM MatrixOptionAnswer m WHERE m.questionId = :questionId")
    Integer findMaxValueByQuestionId(@Param("questionId") Long questionId);
}
