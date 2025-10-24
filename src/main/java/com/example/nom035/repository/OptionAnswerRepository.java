package com.example.nom035.repository;


import com.example.nom035.entity.OptionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OptionAnswerRepository extends JpaRepository<OptionAnswer, Long> {
    List<OptionAnswer> findByQuestionId(Long questionId);
}