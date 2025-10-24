// ...existing code...
package com.example.nom035.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.test.context.support.WithMockUser;
// ...existing code...
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.boot.test.mock.mockito.MockBean;
import com.example.nom035.service.UserService;
import com.example.nom035.security.CustomUserDetailsService;

import com.example.nom035.service.CompanyService;
import com.example.nom035.service.CompanySurveyService;
import com.example.nom035.service.SurveyApplicationService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class,
    excludeFilters = {@ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE, classes = {
        com.example.nom035.controller.CompanyController.class,
        com.example.nom035.controller.CompanySurveyController.class,
        com.example.nom035.controller.DashboardController.class
    })}
)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    // Mock para UserRepository (evita error de contexto por DashboardController)
    @MockBean
    private com.example.nom035.repository.UserRepository userRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

        @MockBean
        private CompanyService companyService;

        @MockBean
        private CompanySurveyService companySurveyService;

        @MockBean
        private SurveyApplicationService surveyApplicationService;

    // Mock para RoleService y RoleRepository (evita error de contexto)
    @MockBean
    private com.example.nom035.service.RoleService roleService;
    @MockBean
    private com.example.nom035.repository.RoleRepository roleRepository;

    // Mocks para los repositorios requeridos por DashboardController
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

    // Mocks para SurveyService y SurveyRepository (evita error de contexto por SurveyController)
    @MockBean
    private com.example.nom035.service.SurveyService surveyService;
    @MockBean
    private com.example.nom035.repository.SurveyRepository surveyRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCannotAccessUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }
}
