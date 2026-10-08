package com.example.nom035.service;

import com.example.nom035.entity.Company;
import com.example.nom035.entity.Employee;
import com.example.nom035.entity.User;
import com.example.nom035.repository.CompanyRepository;
import com.example.nom035.repository.EmployeeRepository;
import com.example.nom035.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class CompanyDeletionIntegrationTest {

    @Autowired
    private CompanyService companyService;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void deletingCompanyRemovesEmployeesAndTheirUsers() {
        Company company = new Company();
        company.setName("Empresa Borrar");
        company.setTaxId("DEL123456");
        company.setFolioMercantil("DEL-FOLIO-1");
        company = companyRepository.save(company);

        Employee employee = new Employee();
        employee.setName("Empleado Borrar");
        employee.setCompany(company);
        employee = employeeRepository.save(employee);

        User user = new User();
        user.setUsername("borrar.user");
        user.setPassword("x");
        user.setEmployeeId(employee.getId());
        user.setCompanyId(company.getId());
        user = userRepository.save(user);

        Long companyId = company.getId();
        Long employeeId = employee.getId();
        Long userId = user.getId();

        companyService.deleteCompany(companyId);

        assertFalse(companyRepository.existsById(companyId));
        assertFalse(employeeRepository.existsById(employeeId));
        assertTrue(userRepository.findById(userId).isEmpty());
    }
}
