package com.example.nom035.repository;

import com.example.nom035.entity.SurveyApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SurveyApplicationRepository extends JpaRepository<SurveyApplication, Long> {
    List<SurveyApplication> findByCompanySurveyId(Long companySurveyId);
    List<SurveyApplication> findByEmployeeId(Long employeeId);
    @Query("SELECT sa.status, COUNT(sa) FROM SurveyApplication sa " +
            "WHERE sa.employee.company.id = :companyId GROUP BY sa.status")
     List<Object[]> countByStatusAndCompanyId(@Param("companyId") Long companyId);

     List<SurveyApplication> findByCompanySurvey_CompanyId(Long companyId);

     @Query("SELECT sa FROM SurveyApplication sa WHERE sa.employee.id = :employeeId AND sa.companySurvey.survey.id = :surveyId ORDER BY sa.id DESC")
     List<SurveyApplication> findByEmployeeIdAndSurveyIdOrderByIdDesc(@Param("employeeId") Long employeeId, @Param("surveyId") Long surveyId);
}