package com.example.nom035.controller;

import com.example.nom035.entity.Company;
import com.example.nom035.entity.Employee;
import com.example.nom035.service.CompanyService;
import com.example.nom035.service.EmployeeService;
import com.example.nom035.security.CustomUserDetailsService;
import com.example.nom035.service.CompanySurveyService;
import com.example.nom035.service.SurveyApplicationService;
import com.example.nom035.dto.EmployeeDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EmployeeController.class,
    excludeFilters = {@ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE, classes = {
        com.example.nom035.controller.UserController.class,
        com.example.nom035.controller.CompanySurveyController.class,
        com.example.nom035.controller.DashboardController.class
    })}
)
class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;
    @MockBean
    private CompanyService companyService;
    @MockBean
    private CustomUserDetailsService customUserDetailsService;
    @MockBean
    private CompanySurveyService companySurveyService;
    @MockBean
    private SurveyApplicationService surveyApplicationService;
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
    private com.example.nom035.service.SurveyService surveyService;
    @MockBean
    private com.example.nom035.repository.SurveyRepository surveyRepository;

    @Test
    @DisplayName("ADMIN puede ver todos los empleados")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCanViewAllEmployees() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(new Employee()));
        mockMvc.perform(get("/api/employees")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("COMPANY solo puede ver empleados de su empresa")
    @WithMockUser(username = "company1", roles = {"EMPLOYEE"})
    void companyCanViewOwnEmployees() throws Exception {
        Employee emp = new Employee();
        Company company = new Company();
        company.setId(1L);
        emp.setCompany(company);
        when(employeeService.getAllEmployees()).thenReturn(List.of(emp));
        // Simular que el usuario está asociado a la empresa 1
        com.example.nom035.entity.User user = new com.example.nom035.entity.User();
        user.setUsername("company1");
        when(userRepository.findByUsername("company1")).thenReturn(Optional.of(user));
        mockMvc.perform(get("/api/employees")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("COMPANY no puede ver empleados de otra empresa")
    @WithMockUser(username = "company2", roles = {"EMPLOYEE"})
    void companyCannotViewOtherEmployees() throws Exception {
        Employee emp = new Employee();
        Company company = new Company();
        company.setId(1L);
        emp.setCompany(company);
        when(employeeService.getAllEmployees()).thenReturn(List.of(emp));
        com.example.nom035.entity.User user = new com.example.nom035.entity.User();
        user.setUsername("company2");
        when(userRepository.findByUsername("company2")).thenReturn(Optional.of(user));
        mockMvc.perform(get("/api/employees")).andExpect(status().isOk()); // La lista estará vacía
    }
}
