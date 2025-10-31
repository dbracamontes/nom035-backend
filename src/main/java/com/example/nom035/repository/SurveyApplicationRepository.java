package com.example.nom035.repository;


import com.example.nom035.entity.SurveyApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyApplicationRepository extends JpaRepository<SurveyApplication, Long> {
    List<SurveyApplication> findByCompanySurveyId(Long companySurveyId);
    List<SurveyApplication> findByEmployeeId(Long employeeId);
    @Query(value = "SELECT sa.status, COUNT(*) FROM survey_application sa JOIN employee e ON sa.employee_id = e.id WHERE e.company_id = :companyId GROUP BY sa.status", nativeQuery = true)
    List<Object[]> countByStatusAndCompanyId(@Param("companyId") Long companyId);

     List<SurveyApplication> findByCompanySurvey_CompanyId(Long companyId);

     @Query("SELECT sa FROM SurveyApplication sa WHERE sa.employee.id = :employeeId AND sa.companySurvey.survey.id = :surveyId ORDER BY sa.id DESC")
     List<SurveyApplication> findByEmployeeIdAndSurveyIdOrderByIdDesc(@Param("employeeId") Long employeeId, @Param("surveyId") Long surveyId);
}