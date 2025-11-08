package com.example.nom035.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nom035.dto.GeneratedPasswordResponse;
import com.example.nom035.dto.PasswordResetRequestResponse;
import com.example.nom035.dto.RoleDto;
import com.example.nom035.dto.UserRoleUpdateRequest;
import com.example.nom035.dto.UserWithRolesDto;
import com.example.nom035.entity.Employee;
import com.example.nom035.entity.PasswordResetToken;
import com.example.nom035.entity.Company;
import com.example.nom035.entity.Role;
import com.example.nom035.entity.User;
import com.example.nom035.repository.CompanyRepository;
import com.example.nom035.repository.EmployeeRepository;
import com.example.nom035.repository.PasswordResetTokenRepository;
import com.example.nom035.repository.RoleRepository;
import com.example.nom035.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private static final int TEMP_PASSWORD_LENGTH = 12;
    private static final String UPPERCASE = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijkmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIALS = "@#$%&*?";
    private static final String ALL_ALLOWED = UPPERCASE + LOWERCASE + DIGITS + SPECIALS;

    private final SecureRandom secureRandom = new SecureRandom();

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User saveUser(User user) {
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            if (user.getId() != null) {
                Optional<User> existing = userRepository.findById(user.getId());
                if (existing.isPresent()) {
                    String currentPassword = existing.get().getPassword();
                    if (!user.getPassword().equals(currentPassword)) {
                        user.setPassword(passwordEncoder.encode(user.getPassword()));
                    }
                } else {
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                }
            } else {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        }
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public List<UserWithRolesDto> getAllUsersWithRoles() {
        synchronizeEmployeeUsers();
        List<User> users = userRepository.findAll();
        Map<Long, String> companyNames = buildCompanyNameMap(users);
    Map<Long, String> employeeNames = buildEmployeeNameMap(users);
    return users.stream()
        .map(user -> UserWithRolesDto.fromEntity(
            user,
            companyNames.get(user.getCompanyId()),
            employeeNames.get(user.getEmployeeId())))
        .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<UserWithRolesDto> getUserWithRoles(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        Map<Long, String> companyNames = buildCompanyNameMap(List.of(userOpt.get()));
    String employeeName = resolveEmployeeName(userOpt.get().getEmployeeId());
    return Optional.of(UserWithRolesDto.fromEntity(
        userOpt.get(),
        companyNames.get(userOpt.get().getCompanyId()),
        employeeName));
    }

    @Transactional(readOnly = true)
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(RoleDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserWithRolesDto updateUserRoles(Long userId, UserRoleUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + userId));

        if (request.getRoleIds() != null) {
            List<Role> roles = roleRepository.findAllById(request.getRoleIds());
            if (roles.size() != request.getRoleIds().size()) {
                throw new IllegalArgumentException("Al menos uno de los roles proporcionados no existe");
            }
            user.setRoles(new HashSet<>(roles));
        }

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        if (request.isCompanyIdSpecified()) {
            if (request.getCompanyId() != null) {
                companyRepository.findById(request.getCompanyId())
                        .orElseThrow(() -> new IllegalArgumentException("La empresa proporcionada no existe"));
                user.setCompanyId(request.getCompanyId());
            } else {
                user.setCompanyId(null);
            }
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User saved = userRepository.save(user);
        String companyName = resolveCompanyName(saved.getCompanyId());
        String employeeName = resolveEmployeeName(saved.getEmployeeId());
        return UserWithRolesDto.fromEntity(saved, companyName, employeeName);
    }

    @Transactional
    public GeneratedPasswordResponse generateTemporaryPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + userId));
        String temporaryPassword = generateSecurePassword(TEMP_PASSWORD_LENGTH);
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        userRepository.save(user);
        return new GeneratedPasswordResponse(user.getId(), temporaryPassword);
    }

    @Transactional
    public PasswordResetRequestResponse createPasswordResetToken(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es requerido");
        }
        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("No existe un usuario con el correo proporcionado"));

        passwordResetTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());

    passwordResetTokenRepository.findByUserAndUsedFalse(user).forEach(existing -> {
        existing.setUsed(true);
        existing.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(existing);
    });

        PasswordResetToken token = new PasswordResetToken();
        token.setToken(generateResetToken());
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusHours(2));
        token.setUsed(false);
        token.setUsedAt(null);
        PasswordResetToken savedToken = passwordResetTokenRepository.save(token);

        return new PasswordResetRequestResponse(savedToken.getToken(), savedToken.getExpiresAt());
    }

    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {
        if (tokenValue == null || tokenValue.isBlank()) {
            throw new IllegalArgumentException("El token es requerido");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("La nueva contraseña es requerida");
        }

        PasswordResetToken token = passwordResetTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Token de recuperación inválido"));

        if (token.isUsed()) {
            throw new IllegalArgumentException("El token de recuperación ya fue utilizado");
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("El token de recuperación ha expirado");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
        token.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(token);
    }

    private Map<Long, String> buildCompanyNameMap(List<User> users) {
        Set<Long> companyIds = users.stream()
                .map(User::getCompanyId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (companyIds.isEmpty()) {
            return Map.of();
        }
        List<Company> companies = new ArrayList<>();
        companyRepository.findAllById(companyIds).forEach(companies::add);
        return companies.stream()
                .collect(Collectors.toMap(Company::getId, Company::getName));
    }

    private Map<Long, String> buildEmployeeNameMap(List<User> users) {
        Set<Long> employeeIds = users.stream()
                .map(User::getEmployeeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (employeeIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> namesById = new HashMap<>();
        employeeRepository.findAllById(employeeIds).forEach(employee -> {
            if (employee != null && employee.getId() != null) {
                namesById.put(employee.getId(), employee.getName());
            }
        });
        return namesById;
    }

    private void synchronizeEmployeeUsers() {
        List<Employee> employees = employeeRepository.findAll();
        Optional<Role> defaultRoleOpt = roleRepository.findByName("ROLE_EMPLOYEE");
        Role defaultRole = defaultRoleOpt.orElse(null);

        for (Employee employee : employees) {
            if (employee.getEmail() == null || employee.getEmail().isBlank()) {
                continue;
            }

            String trimmedEmail = employee.getEmail().trim();
            Long employeeId = employee.getId();
            Optional<User> existingByEmployee = (employeeId != null)
                    ? userRepository.findByEmployeeId(employeeId)
                    : Optional.empty();
            Optional<User> existingByEmail = userRepository.findByEmail(trimmedEmail);
            Long employeeCompanyId = employee.getCompany() != null ? employee.getCompany().getId() : null;

            if (existingByEmployee.isPresent()) {
                User existing = existingByEmployee.get();
                boolean requiresUpdate = false;
                String existingEmail = existing.getEmail() != null ? existing.getEmail().trim() : "";
                if (!trimmedEmail.equalsIgnoreCase(existingEmail)) {
                    existing.setEmail(trimmedEmail);
                    requiresUpdate = true;
                }
                if (employeeCompanyId != null && (existing.getCompanyId() == null
                        || !employeeCompanyId.equals(existing.getCompanyId()))) {
                    existing.setCompanyId(employeeCompanyId);
                    requiresUpdate = true;
                }
                if (existing.getEmployeeId() == null || !existing.getEmployeeId().equals(employeeId)) {
                    existing.setEmployeeId(employeeId);
                    requiresUpdate = true;
                }
                if (requiresUpdate) {
                    userRepository.save(existing);
                }
                continue;
            }

            if (existingByEmail.isPresent()) {
                User existing = existingByEmail.get();
                boolean requiresUpdate = false;
                if (employeeCompanyId != null && (existing.getCompanyId() == null
                        || !employeeCompanyId.equals(existing.getCompanyId()))) {
                    existing.setCompanyId(employeeCompanyId);
                    requiresUpdate = true;
                }
                if (employeeId != null && (existing.getEmployeeId() == null
                        || !employeeId.equals(existing.getEmployeeId()))) {
                    existing.setEmployeeId(employeeId);
                    requiresUpdate = true;
                }
                String existingEmail = existing.getEmail() != null ? existing.getEmail().trim() : "";
                if (!trimmedEmail.equalsIgnoreCase(existingEmail)) {
                    existing.setEmail(trimmedEmail);
                    requiresUpdate = true;
                }
                if (requiresUpdate) {
                    userRepository.save(existing);
                }
                continue;
            }

            User newUser = new User();
            newUser.setUsername(generateUniqueUsername(employee));
            newUser.setEmail(trimmedEmail);
            newUser.setCompanyId(employeeCompanyId);
            newUser.setEmployeeId(employeeId);
            newUser.setEnabled(true);
            newUser.setPassword(passwordEncoder.encode(generateSecurePassword(TEMP_PASSWORD_LENGTH)));

            if (defaultRole != null) {
                newUser.setRoles(new HashSet<>(Collections.singleton(defaultRole)));
            } else {
                newUser.setRoles(new HashSet<>());
            }

            userRepository.save(newUser);
        }
    }

    private String generateUniqueUsername(Employee employee) {
        String base = null;
        if (employee.getName() != null && !employee.getName().isBlank()) {
            base = employee.getName().trim().toLowerCase();
        }
        if (base == null || base.isBlank()) {
            if (employee.getEmail() != null && !employee.getEmail().isBlank()) {
                String email = employee.getEmail().trim().toLowerCase();
                int atIndex = email.indexOf('@');
                base = (atIndex > 0) ? email.substring(0, atIndex) : email;
            }
        }
        if (base == null || base.isBlank()) {
            base = "usuario";
        }
    base = base.replaceAll("[^a-z0-9]", ".");
    base = base.replaceAll("\\.+", ".").replaceAll("^\\.|\\.$", "");
        if (base.isBlank()) {
            base = "usuario";
        }

        String candidate = base;
        int counter = 1;
        while (userRepository.findByUsername(candidate).isPresent()) {
            candidate = base + counter;
            counter++;
        }
        return candidate;
    }

    private String resolveCompanyName(Long companyId) {
        if (companyId == null) {
            return null;
        }
        return companyRepository.findById(companyId)
                .map(Company::getName)
                .orElse(null);
    }

    private String resolveEmployeeName(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        return employeeRepository.findById(employeeId)
                .map(Employee::getName)
                .orElse(null);
    }

    private String generateSecurePassword(int length) {
        List<Character> characters = new ArrayList<>();
        characters.add(pickRandomChar(UPPERCASE));
        characters.add(pickRandomChar(LOWERCASE));
        characters.add(pickRandomChar(DIGITS));
        characters.add(pickRandomChar(SPECIALS));

        for (int i = characters.size(); i < length; i++) {
            characters.add(pickRandomChar(ALL_ALLOWED));
        }

        Collections.shuffle(characters, secureRandom);

        StringBuilder sb = new StringBuilder();
        characters.forEach(sb::append);
        return sb.toString();
    }

    private char pickRandomChar(String source) {
        return source.charAt(secureRandom.nextInt(source.length()));
    }

    private String generateResetToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
