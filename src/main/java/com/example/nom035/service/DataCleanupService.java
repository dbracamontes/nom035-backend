package com.example.nom035.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * Explicitly removes the data that hangs from an employee or a company before the owner row is
 * deleted. This does not rely on ON DELETE CASCADE, which only exists in databases created from
 * schema.sql; databases built by Hibernate (ddl-auto=update) have plain foreign keys and reject the delete.
 */
@Service
public class DataCleanupService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void deleteEmployeeDependencies(Collection<Long> employeeIds) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            return;
        }
        execute("DELETE FROM Response r WHERE r.surveyApplication.id IN "
                + "(SELECT sa.id FROM SurveyApplication sa WHERE sa.employee.id IN :ids)", employeeIds);
        execute("DELETE FROM SurveyApplication sa WHERE sa.employee.id IN :ids", employeeIds);
        execute("DELETE FROM EmployeeDocs d WHERE d.employee.id IN :ids", employeeIds);
        entityManager.flush();
        entityManager.clear();
    }

    @Transactional
    public void deleteCompanyDependencies(Long companyId) {
        if (companyId == null) {
            return;
        }
        execute("DELETE FROM Employee e WHERE e.company.id = :id", companyId);
        execute("DELETE FROM Response r WHERE r.surveyApplication.id IN "
                + "(SELECT sa.id FROM SurveyApplication sa WHERE sa.companySurvey.id IN "
                + "(SELECT cs.id FROM CompanySurvey cs WHERE cs.company.id = :id))", companyId);
        execute("DELETE FROM SurveyApplication sa WHERE sa.companySurvey.id IN "
                + "(SELECT cs.id FROM CompanySurvey cs WHERE cs.company.id = :id)", companyId);
        execute("DELETE FROM CompanySurvey cs WHERE cs.company.id = :id", companyId);
        execute("DELETE FROM MedicaLebenCompanyWorkPhoto p WHERE p.companyDocs.id IN "
                + "(SELECT d.id FROM MedicaLebenCompanyDocs d WHERE d.company.id = :id)", companyId);
        execute("DELETE FROM MedicaLebenCompanyDocs d WHERE d.company.id = :id", companyId);
        execute("DELETE FROM ConsultoriaDraft c WHERE c.companyId = :id", companyId);
        entityManager.flush();
        entityManager.clear();
    }

    private void execute(String jpql, Collection<Long> ids) {
        entityManager.createQuery(jpql).setParameter("ids", ids).executeUpdate();
    }

    private void execute(String jpql, Long id) {
        entityManager.createQuery(jpql).setParameter("id", id).executeUpdate();
    }
}
