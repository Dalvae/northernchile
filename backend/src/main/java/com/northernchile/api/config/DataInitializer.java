package com.northernchile.api.config;

import com.northernchile.api.model.User;
import com.northernchile.api.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.fullName}")
    private String adminFullName;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== INICIALIZACIÓN DE DATOS ===");
        System.out.println("Email admin configurado: " + adminEmail);
        System.out.println("Password admin configurado: " + adminPassword);
        System.out.println("Nombre admin: " + adminFullName);
        
        List<User> superAdmins = userRepository.findAll().stream()
                .filter(user -> "ROLE_SUPER_ADMIN".equals(user.getRole()))
                .toList();

        if (superAdmins.isEmpty()) {
            System.out.println("No SUPER_ADMIN found, creating initial admin user...");

            User adminUser = new User();
            adminUser.setEmail(adminEmail);
            String encodedPassword = passwordEncoder.encode(adminPassword);
            adminUser.setPasswordHash(encodedPassword);
            adminUser.setFullName(adminFullName);
            adminUser.setRole("ROLE_SUPER_ADMIN");
            adminUser.setAuthProvider("LOCAL");

            userRepository.save(adminUser);
            System.out.println("Initial SUPER_ADMIN created with email: " + adminEmail);
            System.out.println("Password sin encriptar: " + adminPassword);
            System.out.println("Password encriptado: " + encodedPassword);
        } else {
            System.out.println("SUPER_ADMIN user(s) already exist. Count: " + superAdmins.size());
            superAdmins.forEach(user -> 
                System.out.println("Admin user: " + user.getEmail())
            );
        }
        System.out.println("=== FIN INICIALIZACIÓN ===");
    }
}
