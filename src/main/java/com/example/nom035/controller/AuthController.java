package com.example.nom035.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private com.example.nom035.repository.UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Map<String, Object> response = new HashMap<>();
            response.put("username", userDetails.getUsername());
            // Roles
            response.put("roles", userDetails.getAuthorities());

            // Privilegios
            com.example.nom035.entity.User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                java.util.Set<String> privileges = new java.util.HashSet<>();
                if (user.getRoles() != null) {
                    for (com.example.nom035.entity.Role role : user.getRoles()) {
                        if (role.getPrivileges() != null) {
                            for (com.example.nom035.entity.Privilege priv : role.getPrivileges()) {
                                privileges.add(priv.getName());
                            }
                        }
                    }
                }
                response.put("privileges", privileges);
            } else {
                response.put("privileges", java.util.Collections.emptySet());
            }
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }
    }
}
