package com.northernchile.api.config;

import com.northernchile.api.model.User;
import com.northernchile.api.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Configuration for multiple admin users
    @Value("${admin.users.config:}")
    private String adminUsersConfig;

    @Override
    public void run(String... args) {
        System.out.println("=== INICIALIZACIÓN DE DATOS ===");
        initializeAdminUsers();
        System.out.println("=== FIN INICIALIZACIÓN ===");
    }

    private void initializeAdminUsers() {
        System.out.println("--- Inicializando Usuarios Administradores ---");

        if (adminUsersConfig == null || adminUsersConfig.isEmpty()) {
            System.out.println("No se encontró configuración de usuarios admin (admin.users.config). Omitiendo.");
            return;
        }

        // Parse configuration: email:password:role;email2:password2:role2;...
        String[] userConfigs = adminUsersConfig.split(";");

        for (String userConfig : userConfigs) {
            String[] parts = userConfig.split(":");
            if (parts.length != 3) {
                System.err.println("Configuración de usuario inválida (debe ser email:password:role): " + userConfig);
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
                System.out.println("✓ Usuario admin creado: " + email + " con rol " + role);
            } else {
                System.out.println("○ Usuario admin ya existe: " + email);
            }
        }
    }
}
