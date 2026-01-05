package com.example.nom035.repository;


import com.example.nom035.entity.OptionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OptionAnswerRepository extends JpaRepository<OptionAnswer, Long> {

    List<OptionAnswer> findByQuestionId(Long questionId);
    
    @Query("SELECT MIN(oa.value) FROM OptionAnswer oa WHERE oa.question. id = :questionId AND oa.value IS NOT NULL")
    Integer findMinValueByQuestionId(@Param("questionId") Long questionId);
    
    @Query("SELECT MAX(oa. value) FROM OptionAnswer oa WHERE oa.question.id = :questionId AND oa.value IS NOT NULL")
    Integer findMaxValueByQuestionId(@Param("questionId") Long questionId);
}