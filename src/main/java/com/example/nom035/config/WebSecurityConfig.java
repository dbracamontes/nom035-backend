package com.example.nom035.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
    @Autowired
    private UserDetailsService userDetailsService;
    @Bean
    public org.springframework.security.authentication.dao.DaoAuthenticationProvider authenticationProvider() {
        org.springframework.security.authentication.dao.DaoAuthenticationProvider authProvider = new org.springframework.security.authentication.dao.DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
        return authProvider;
    }
    @Bean
    public org.springframework.security.web.access.AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Access Denied");
        };
    }
    @Bean
    public org.springframework.security.authentication.AuthenticationManager authenticationManager(org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .requestMatchers("/api/companies/**").hasRole("COMPANY")
                .requestMatchers("/api/company-surveys/**").hasAnyRole("ADMIN", "COMPANY")
                .requestMatchers("/api/company-surveys").hasAnyRole("ADMIN", "COMPANY")
                .requestMatchers("/api/employees/**").hasAnyRole("ADMIN", "EMPLOYEE")
                .requestMatchers("/api/surveys/**/questions").hasRole("ADMIN")
                .requestMatchers("/api/questions/**").hasRole("ADMIN")
                .requestMatchers("/api/surveys/**").hasAnyRole("ADMIN", "COMPANY")
                .requestMatchers("/api/responses/**").hasAnyRole("ADMIN", "EMPLOYEE")
                .requestMatchers("/api/survey-applications/**").hasAnyRole("ADMIN", "EMPLOYEE")
                .anyRequest().denyAll()
            )
            .exceptionHandling(exception -> exception.accessDeniedHandler(accessDeniedHandler()))
            .httpBasic(org.springframework.security.config.Customizer.withDefaults());
        return http.build();
    }
}
