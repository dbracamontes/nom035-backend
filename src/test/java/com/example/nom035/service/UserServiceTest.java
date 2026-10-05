package com.example.nom035.service;

import com.example.nom035.entity.User;
import com.example.nom035.repository.CompanyRepository;
import com.example.nom035.repository.EmployeeRepository;
import com.example.nom035.repository.PasswordResetTokenRepository;
import com.example.nom035.repository.RoleRepository;
import com.example.nom035.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmployeeService employeeService;
    @InjectMocks
    private UserService service;

    @Test
    void deletesLinkedEmployeeAndUserAccount() {
        User user = new User();
        user.setId(12L);
        user.setEmployeeId(34L);
        when(userRepository.findById(12L)).thenReturn(Optional.of(user));

        service.deleteUser(12L);

        verify(passwordResetTokenRepository).deleteByUserIn(List.of(user));
        verify(employeeService).deleteEmployee(34L);
        verify(userRepository).deleteById(12L);
    }

    @Test
    void deletesUserAccountWithoutDeletingAnUnlinkedEmployee() {
        User user = new User();
        user.setId(12L);
        when(userRepository.findById(12L)).thenReturn(Optional.of(user));

        service.deleteUser(12L);

        verify(passwordResetTokenRepository).deleteByUserIn(List.of(user));
        verify(employeeService, never()).deleteEmployee(anyLong());
        verify(userRepository).deleteById(12L);
    }
}
