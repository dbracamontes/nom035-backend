package com.example.nom035.repository;

import com.example.nom035.entity.Response;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResponseRepository extends JpaRepository<Response, Long> {
    List<Response> findBySurveyApplicationId(Long surveyApplicationId);
    List<Response> findByQuestionId(Long questionId);   
    @Query("SELECT r FROM Response r WHERE r.surveyApplication.employee.company.id = :companyId")
    List<Response> findAllByCompanyId(@Param("companyId") Long companyId);
}