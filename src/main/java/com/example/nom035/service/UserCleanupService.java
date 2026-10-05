package com.example.nom035.service;

import com.example.nom035.entity.User;
import com.example.nom035.repository.PasswordResetTokenRepository;
import com.example.nom035.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Removes the login accounts linked to an employee or company. user.employee_id and
 * user.company_id are plain columns, so deleting the owner does not remove the account.
 * Admin accounts are never deleted; they are only unlinked.
 */
@Service
public class UserCleanupService {
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public UserCleanupService(UserRepository userRepository,
                              PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Transactional
    public void deleteUsersForEmployees(Collection<Long> employeeIds) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            return;
        }
        removeOrUnlink(userRepository.findByEmployeeIdIn(employeeIds));
    }

    @Transactional
    public void deleteUsersForCompany(Long companyId) {
        if (companyId == null) {
            return;
        }
        removeOrUnlink(userRepository.findByCompanyId(companyId));
    }

    private void removeOrUnlink(List<User> candidates) {
        Map<Long, User> unique = new LinkedHashMap<>();
        candidates.forEach(user -> unique.put(user.getId(), user));

        List<User> toDelete = new ArrayList<>();
        for (User user : unique.values()) {
            boolean admin = user.getRoles() != null
                    && user.getRoles().stream().anyMatch(role -> ROLE_ADMIN.equals(role.getName()));
            if (admin) {
                user.setEmployeeId(null);
                user.setCompanyId(null);
                userRepository.save(user);
            } else {
                toDelete.add(user);
            }
        }

        if (!toDelete.isEmpty()) {
            passwordResetTokenRepository.deleteByUserIn(toDelete);
            userRepository.deleteAll(toDelete);
        }
    }
}
