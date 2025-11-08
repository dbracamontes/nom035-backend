package com.example.nom035.controller;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.access.annotation.Secured;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.example.nom035.dto.GeneratedPasswordResponse;
import com.example.nom035.dto.PasswordResetConfirmRequest;
import com.example.nom035.dto.PasswordResetRequest;
import com.example.nom035.dto.PasswordResetRequestResponse;
import com.example.nom035.dto.RoleDto;
import com.example.nom035.dto.UserRoleUpdateRequest;
import com.example.nom035.dto.UserWithRolesDto;
import com.example.nom035.entity.User;
import com.example.nom035.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private com.example.nom035.repository.UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
        }
        org.springframework.security.core.userdetails.UserDetails userDetails = (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
        java.util.Map<String, Object> userInfo = new java.util.HashMap<>();
        userInfo.put("username", userDetails.getUsername());
        userInfo.put("roles", userDetails.getAuthorities());
        // Enrich with persisted user info (companyId, id, email) so frontend can enforce permissions correctly
        try {
            java.util.Optional<User> uOpt = userRepository.findByUsername(userDetails.getUsername());
            if (uOpt.isPresent()) {
                User u = uOpt.get();
                userInfo.put("id", u.getId());
                userInfo.put("email", u.getEmail());
                userInfo.put("companyId", u.getCompanyId());
                userInfo.put("employeeId", u.getEmployeeId());
            }
        } catch (Exception ignored) { }
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping
    @Secured("ROLE_ADMIN")
    public List<UserWithRolesDto> getAllUsers() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Principal: " + authentication.getPrincipal());
            System.out.println("Authorities: ");
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                System.out.println(" - " + authority.getAuthority());
            }
        return userService.getAllUsersWithRoles();
    }

    @GetMapping("/roles")
    @Secured("ROLE_ADMIN")
    public List<RoleDto> getAllRoles() {
        return userService.getAllRoles();
    }

    @GetMapping("/{id}")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<UserWithRolesDto> getUserById(@PathVariable Long id) {
        return userService.getUserWithRoles(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Secured("ROLE_ADMIN")
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }

    @PutMapping("/{id}")
    @Secured("ROLE_ADMIN")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return userService.saveUser(user);
    }

    @DeleteMapping("/{id}")
    @Secured("ROLE_ADMIN")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @GetMapping("/{id}/roles")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<UserWithRolesDto> getUserRoles(@PathVariable Long id) {
        return userService.getUserWithRoles(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/roles")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<UserWithRolesDto> updateUserRoles(@PathVariable Long id, @RequestBody UserRoleUpdateRequest request) {
        try {
            UserWithRolesDto updated = userService.updateUserRoles(id, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/password/generate")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<GeneratedPasswordResponse> generateTemporaryPassword(@PathVariable Long id) {
        try {
            GeneratedPasswordResponse response = userService.generateTemporaryPassword(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<PasswordResetRequestResponse> requestPasswordReset(@RequestBody(required = false) PasswordResetRequest request) {
        try {
            if (request == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            PasswordResetRequestResponse response = userService.createPasswordResetToken(request.getEmail());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(@RequestBody(required = false) PasswordResetConfirmRequest request) {
        try {
            if (request == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            userService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}