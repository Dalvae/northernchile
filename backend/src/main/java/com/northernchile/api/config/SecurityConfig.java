package com.northernchile.api.config;

import com.northernchile.api.config.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Permitir GET a tours para todo el mundo (búsqueda pública)
                        .requestMatchers(HttpMethod.GET, "/api/tours/**").permitAll()
                        // Permitir GET a disponibilidad para todo el mundo
                        .requestMatchers(HttpMethod.GET, "/api/availability/**").permitAll()
                        // Permitir acceso público a datos del calendario (fases lunares + weather)
                        .requestMatchers(HttpMethod.GET, "/api/calendar/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/lunar/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/weather/**").permitAll()
                        // Proteger operaciones de storage (solo admins)
                        .requestMatchers("/api/storage/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_PARTNER_ADMIN")
                        // Proteger las operaciones de escritura en tours
                        .requestMatchers(HttpMethod.POST, "/api/tours/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_PARTNER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/tours/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_PARTNER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/tours/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_PARTNER_ADMIN")
                        // Permitir rutas de autenticación y documentación
                        .requestMatchers(
                                "/api/auth/**",
                                "/api-docs/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/error"
                        ).permitAll()
                        // Proteger todas las rutas de administración
                        .requestMatchers("/api/admin/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_PARTNER_ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "https://northernchile.com",
                "https://www.northernchile.com"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }



    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
