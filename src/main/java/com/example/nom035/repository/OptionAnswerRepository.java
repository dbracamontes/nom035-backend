package com.example.nom035.repository;

import com.example.nom035.entity.OptionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OptionAnswerRepository extends JpaRepository<OptionAnswer, Long> {
    List<OptionAnswer> findByQuestionId(Long questionId);
}