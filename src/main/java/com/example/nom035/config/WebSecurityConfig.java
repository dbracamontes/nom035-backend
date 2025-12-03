package com.example.nom035.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de seguridad principal.
 * Mantengo CORS y un PasswordEncoder "dev" para que las cuentas seeded funcionen.
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class WebSecurityConfig implements WebMvcConfigurer {

    @Autowired
    private UserDetailsService userDetailsService;


    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:3000");
        configuration.addAllowedOrigin("http://localhost:3001");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        // Dev encoder: plain-text. Reemplazar por BCryptPasswordEncoder en producción.
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword == null ? null : rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                if (rawPassword == null && encodedPassword == null) return true;
                if (rawPassword == null || encodedPassword == null) return false;
                return rawPassword.toString().equals(encodedPassword);
            }
        };
    }

    @Bean
    org.springframework.security.authentication.dao.DaoAuthenticationProvider authenticationProvider() {
        org.springframework.security.authentication.dao.DaoAuthenticationProvider authProvider = new org.springframework.security.authentication.dao.DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Acceso denegado: no tienes permisos suficientes para esta acción.\"}");
        };
    }

    @Bean
    org.springframework.security.authentication.AuthenticationManager authenticationManager(org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mapear /uploads/** a la carpeta "uploads" que está un nivel arriba del directorio del proyecto backend
        // Estructura esperada: <workspace-root>/uploads/...
        String uploadsPath = System.getProperty("user.dir") + "/../uploads/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadsPath)
                .setCachePeriod(0);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/static/**",
                    "/public/**",
                    "/api/auth/**",
                    "/api/public/**",
                    "/favicon.ico",
                    "/uploads/**"    // permitir acceso público a archivos subidos
                ).permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                // ADMIN y COMPANY pueden crear, editar o eliminar empleados (el controller valida el alcance por empresa)
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/employees", "/api/employees/**").hasAnyRole("ADMIN", "COMPANY")
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/employees", "/api/employees/**").hasAnyRole("ADMIN", "COMPANY")
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/employees", "/api/employees/**").hasAnyRole("ADMIN", "COMPANY")
                // ADMIN, COMPANY (y opcionalmente EMPLOYEE) pueden consultar empleados; el controller aplica filtros
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/employees", "/api/employees/**").hasAnyRole("ADMIN", "COMPANY", "EMPLOYEE")
                 .requestMatchers("/api/companies/**", "/api/dashboard/**").hasAnyRole("ADMIN", "COMPANY")
                 .requestMatchers("/api/**").authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e.accessDeniedHandler(accessDeniedHandler()));

        return http.build();
    }
}