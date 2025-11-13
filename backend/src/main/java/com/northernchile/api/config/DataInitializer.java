package com.northernchile.api.config;

import com.northernchile.api.model.User;
import com.northernchile.api.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Configuration for multiple admin users
    @Value("${admin.users.config:}")
    private String adminUsersConfig;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("=== INICIALIZACIÓN DE DATOS ===");
        initializeAdminUsers();
        log.info("=== FIN INICIALIZACIÓN ===");
    }

    private void initializeAdminUsers() {
        log.info("--- Inicializando Usuarios Administradores ---");

        if (adminUsersConfig == null || adminUsersConfig.isEmpty()) {
            log.info("No se encontró configuración de usuarios admin (admin.users.config). Omitiendo.");
            return;
        }

        // Parse configuration: email:password:role;email2:password2:role2;...
        String[] userConfigs = adminUsersConfig.split(";");

        for (String userConfig : userConfigs) {
            String[] parts = userConfig.split(":");
            if (parts.length != 3) {
                log.error("Configuración de usuario inválida (debe ser email:password:role): {}", userConfig);
                continue;
            }

            String email = parts[0].trim();
            String password = parts[1].trim();
            String role = parts[2].trim();

            // Create user only if doesn't exist
            if (userRepository.findByEmail(email).isEmpty()) {
                User adminUser = new User();
                adminUser.setEmail(email);
                adminUser.setPasswordHash(passwordEncoder.encode(password));
                // Extract name from email (before @) as default
                adminUser.setFullName(email.split("@")[0]);
                adminUser.setRole(role);
                adminUser.setAuthProvider("LOCAL");
                userRepository.save(adminUser);
                log.info("✓ Usuario admin creado: {} con rol {}", email, role);
            } else {
                log.info("○ Usuario admin ya existe: {}", email);
            }
        }
    }
}
