package com.example.nom035.security;

import com.example.nom035.entity.User;
import com.example.nom035.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("[CustomUserDetailsService] Buscando usuario: {}", username);
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            logger.warn("[CustomUserDetailsService] Usuario no encontrado: {}", username);
            throw new UsernameNotFoundException("Usuario no encontrado: " + username);
        }
        User user = userOpt.get();
        logger.info("[CustomUserDetailsService] Usuario cargado: {} (enabled: {})", user.getUsername(), user.isEnabled());
        logger.warn("[DEBUG] Contraseña leída de BD para {}: {}", user.getUsername(), user.getPassword());
        logger.info("[CustomUserDetailsService] Contraseña leída de BD: {}", user.getPassword());
        if (user.getRoles() != null) {
            logger.info("[CustomUserDetailsService] Roles: {}", user.getRoles().stream().map(r -> r.getName()).toList());
        } else {
            logger.info("[CustomUserDetailsService] Roles: null");
        }
        return new CustomUserDetails(user);
    }
}
