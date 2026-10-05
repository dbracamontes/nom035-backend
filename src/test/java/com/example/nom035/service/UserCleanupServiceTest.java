package com.example.nom035.service;

import com.example.nom035.entity.Role;
import com.example.nom035.entity.User;
import com.example.nom035.repository.PasswordResetTokenRepository;
import com.example.nom035.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCleanupServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @InjectMocks
    private UserCleanupService service;

    private User user(Long id, String roleName) {
        Role role = new Role();
        role.setName(roleName);
        User user = new User();
        user.setId(id);
        user.setRoles(Set.of(role));
        user.setCompanyId(5L);
        user.setEmployeeId(id);
        return user;
    }

    @Test
    void deletesNonAdminUsersOfCompany() {
        User employeeUser = user(1L, "ROLE_EMPLOYEE");
        when(userRepository.findByCompanyId(5L)).thenReturn(List.of(employeeUser));

        service.deleteUsersForCompany(5L);

        verify(passwordResetTokenRepository).deleteByUserIn(List.of(employeeUser));
        verify(userRepository).deleteAll(List.of(employeeUser));
    }

    @Test
    void unlinksAdminInsteadOfDeleting() {
        User admin = user(2L, "ROLE_ADMIN");
        when(userRepository.findByEmployeeIdIn(List.of(2L))).thenReturn(List.of(admin));

        service.deleteUsersForEmployees(List.of(2L));

        verify(userRepository).save(admin);
        verify(userRepository, never()).deleteAll(any());
        org.junit.jupiter.api.Assertions.assertNull(admin.getEmployeeId());
        org.junit.jupiter.api.Assertions.assertNull(admin.getCompanyId());
    }
}
