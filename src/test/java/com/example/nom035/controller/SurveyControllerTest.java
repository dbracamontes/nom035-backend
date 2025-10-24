// ...existing code...
package com.example.nom035.controller;

import com.example.nom035.entity.Survey;
import com.example.nom035.service.SurveyService;
import com.example.nom035.dto.SurveyDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SurveyController.class,
    excludeFilters = {@ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE, classes = {
        com.example.nom035.controller.UserController.class,
        com.example.nom035.controller.CompanySurveyController.class,
        com.example.nom035.controller.DashboardController.class
    })}
)
class SurveyControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.example.nom035.service.CompanyService companyService;
    @MockBean
    private com.example.nom035.repository.CompanyRepository companyRepository;

    @MockBean
    private SurveyService surveyService;
    @MockBean
    private com.example.nom035.security.CustomUserDetailsService customUserDetailsService;
    @MockBean
    private com.example.nom035.service.CompanySurveyService companySurveyService;
    @MockBean
    private com.example.nom035.service.SurveyApplicationService surveyApplicationService;
    @MockBean
    private com.example.nom035.service.RoleService roleService;
    @MockBean
    private com.example.nom035.repository.RoleRepository roleRepository;
    @MockBean
    private com.example.nom035.repository.EmployeeRepository employeeRepository;
    @MockBean
    private com.example.nom035.repository.SurveyApplicationRepository surveyApplicationRepository;
    @MockBean
    private com.example.nom035.repository.CompanySurveyRepository companySurveyRepository;
    @MockBean
    private com.example.nom035.repository.ResponseRepository responseRepository;
    @MockBean
    private com.example.nom035.repository.QuestionRepository questionRepository;
    @MockBean
    private com.example.nom035.repository.PrivilegeRepository privilegeRepository;
    @MockBean
    private com.example.nom035.repository.OptionAnswerRepository optionAnswerRepository;
    @MockBean
    private com.example.nom035.repository.UserRepository userRepository;
    @MockBean
    private com.example.nom035.repository.SurveyRepository surveyRepository;

    @Test
    @DisplayName("ADMIN puede ver todas las encuestas")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCanViewAllSurveys() throws Exception {
        when(surveyService.getAllSurveys()).thenReturn(List.of(new Survey()));
        mockMvc.perform(get("/api/surveys")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("COMPANY puede ver todas las encuestas")
    @WithMockUser(username = "company", roles = {"COMPANY"})
    void companyCanViewAllSurveys() throws Exception {
        when(surveyService.getAllSurveys()).thenReturn(List.of(new Survey()));
        mockMvc.perform(get("/api/surveys")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("EMPLOYEE no puede ver encuestas")
    @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
    void employeeCannotViewSurveys() throws Exception {
        mockMvc.perform(get("/api/surveys")).andExpect(status().isForbidden());
    }
}
