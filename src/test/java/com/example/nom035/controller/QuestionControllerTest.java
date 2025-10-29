package com.example.nom035.controller;

import com.example.nom035.entity.Question;
import com.example.nom035.service.QuestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = QuestionController.class,
    excludeFilters = {@ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE, classes = {
        com.example.nom035.controller.UserController.class,
        com.example.nom035.controller.CompanySurveyController.class,
        com.example.nom035.controller.DashboardController.class
    })}
)
class QuestionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestionService questionService;
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
    @MockBean
    private com.example.nom035.service.CompanyService companyService;
    @MockBean
    private com.example.nom035.repository.CompanyRepository companyRepository;

    @Test
    @DisplayName("ADMIN puede ver preguntas de un survey")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCanViewQuestionsBySurvey() throws Exception {
        when(questionService.getQuestionsBySurveyId(1L)).thenReturn(List.of(new Question()));
        mockMvc.perform(get("/api/surveys/1/questions")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("EMPLOYEE no puede ver preguntas de un survey")
    @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
    void employeeCannotViewQuestionsBySurvey() throws Exception {
        mockMvc.perform(get("/api/surveys/1/questions")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("COMPANY no puede ver preguntas de un survey")
    @WithMockUser(username = "company", roles = {"COMPANY"})
    void companyCannotViewQuestionsBySurvey() throws Exception {
        mockMvc.perform(get("/api/surveys/1/questions")).andExpect(status().isForbidden());
    }
}
