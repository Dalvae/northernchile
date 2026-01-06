package com.northernchile.api.config;

import com.northernchile.api.config.security.JsonAccessDeniedHandler;
import com.northernchile.api.config.security.JsonAuthenticationEntryPoint;
import com.northernchile.api.config.security.JwtAuthenticationFilter;
import com.northernchile.api.security.Role;
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

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                         JsonAuthenticationEntryPoint authenticationEntryPoint,
                         JsonAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers
                        .contentTypeOptions(contentTypeOptions -> contentTypeOptions.disable())
                        .xssProtection(xss -> xss.headerValue(org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .frameOptions(frameOptions -> frameOptions.deny())
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'"))
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)) // 1 year
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                 .authorizeHttpRequests(auth -> auth
                         .requestMatchers(
                                 "/api-docs",
                                 "/api-docs/**",
                                 "/v3/api-docs",
                                 "/v3/api-docs/**",
                                 "/swagger-ui/**",
                                 "/swagger-ui.html"
                         ).permitAll()

                        // Permitir GET a tours para todo el mundo (búsqueda pública)
                        .requestMatchers(HttpMethod.GET, "/api/tours/**").permitAll()
                        // Permitir GET a disponibilidad para todo el mundo
                        .requestMatchers(HttpMethod.GET, "/api/availability/**").permitAll()
                        // Permitir acceso público a datos del calendario (fases lunares + weather)
                        .requestMatchers(HttpMethod.GET, "/api/calendar/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/lunar/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/weather/**").permitAll()
                        // Proteger operaciones de storage (solo admins)
                        .requestMatchers("/api/storage/**").hasAnyAuthority(Role.SUPER_ADMIN.getRoleName(), Role.PARTNER_ADMIN.getRoleName())
                        // Proteger las operaciones de escritura en tours
                        .requestMatchers(HttpMethod.POST, "/api/tours/**").hasAnyAuthority(Role.SUPER_ADMIN.getRoleName(), Role.PARTNER_ADMIN.getRoleName())
                        .requestMatchers(HttpMethod.PUT, "/api/tours/**").hasAnyAuthority(Role.SUPER_ADMIN.getRoleName(), Role.PARTNER_ADMIN.getRoleName())
                        .requestMatchers(HttpMethod.DELETE, "/api/tours/**").hasAnyAuthority(Role.SUPER_ADMIN.getRoleName(), Role.PARTNER_ADMIN.getRoleName())
                        // Permitir rutas de autenticación y documentación
                        .requestMatchers(
                                "/api/auth/**",
                                "/actuator/health/**",
                                "/actuator/info",
                                "/api-docs/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/error"
                        ).permitAll()
                        // Permitir acceso público a webhooks de payment providers
                        // Nota: La seguridad se maneja mediante verificación de firma en WebhookSecurityService
                        .requestMatchers("/api/webhooks/**").permitAll()
                        // Permitir acceso público al carrito (guest carts)
                        .requestMatchers("/api/cart/**").permitAll()
                        // Permitir confirmación de pagos (callbacks de Transbank/MercadoPago)
                        .requestMatchers(HttpMethod.GET, "/api/payment-sessions/confirm/**").permitAll()
                        // Proteger todas las rutas de administración
                        .requestMatchers("/api/admin/**").hasAnyAuthority(Role.SUPER_ADMIN.getRoleName(), Role.PARTNER_ADMIN.getRoleName())
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
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Cookie"));
        configuration.setExposedHeaders(Arrays.asList("Set-Cookie"));
        configuration.setAllowCredentials(true); // Required for cookies
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
