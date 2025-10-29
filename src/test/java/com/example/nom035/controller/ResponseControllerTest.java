package com.example.nom035.controller;

import com.example.nom035.dto.ResponseCreateDto;
import com.example.nom035.dto.ResponseDto;
import com.example.nom035.service.ResponseService;
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

@WebMvcTest(controllers = ResponseController.class,
    excludeFilters = {@ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE, classes = {
        com.example.nom035.controller.UserController.class,
        com.example.nom035.controller.CompanySurveyController.class,
        com.example.nom035.controller.DashboardController.class
    })}
)
class ResponseControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResponseService responseService;
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
    @DisplayName("ADMIN puede ver todas las respuestas")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCanViewAllResponses() throws Exception {
        when(responseService.getAllResponses()).thenReturn(List.of(new ResponseDto()));
        mockMvc.perform(get("/api/responses")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("EMPLOYEE puede ver todas las respuestas")
    @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
    void employeeCanViewAllResponses() throws Exception {
        when(responseService.getAllResponses()).thenReturn(List.of(new ResponseDto()));
        mockMvc.perform(get("/api/responses")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("COMPANY no puede ver respuestas")
    @WithMockUser(username = "company", roles = {"COMPANY"})
    void companyCannotViewResponses() throws Exception {
        mockMvc.perform(get("/api/responses")).andExpect(status().isForbidden());
    }
}
