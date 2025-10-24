package com.northernchile.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitamos CSRF, común en APIs REST
            .authorizeHttpRequests(auth -> auth
                // 1. Permitimos acceso PÚBLICO a estos endpoints específicos
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/api-docs/**"
                ).permitAll()
                // 2. Para CUALQUIER OTRA petición, requerimos autenticación
                .anyRequest().authenticated()
            )
            // 3. Habilitamos un formulario de login básico por ahora
            .formLogin(withDefaults());

        return http.build();
    }
}
