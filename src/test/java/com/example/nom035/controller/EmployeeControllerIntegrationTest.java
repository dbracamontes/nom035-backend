package com.example.nom035.controller;

import com.example.nom035.entity.Company;
import com.example.nom035.repository.CompanyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
public class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldCreateEmployeeWithNewFields() throws Exception {
        Company company = new Company();
        company.setName("Test Company X");
        company.setTaxId("TST123456");
        company.setFolioMercantil("TEST-FOLIO-123456");
        companyRepository.save(company);

        String payload = "{"
            + "\"name\": \"Empleado Prueba\","
            + "\"email\": \"test.employee@example.com\","
            + "\"position\": \"Analista\","
            + "\"department\": \"Riesgos\","
            + "\"dateOfBirth\": \"1990-05-12\","
            + "\"maritalStatus\": \"Soltero\","
            + "\"gender\": \"M\","
            + "\"education\": \"Licenciatura\","
            + "\"companyCategory\": \"Operativo\","
            + "\"seniorityYears\": 3,"
            + "\"company\": { \"id\": " + company.getId() + " }"
            + "}";

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Empleado Prueba")))
                .andExpect(content().string(containsString("1990-05-12")))
                .andExpect(content().string(containsString("Soltero")))
                .andExpect(content().string(containsString("Licenciatura")))
                .andExpect(content().string(containsString("Operativo")));
    }
}
