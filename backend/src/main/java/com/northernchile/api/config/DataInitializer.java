package com.northernchile.api.config;

import com.northernchile.api.external.LunarService;
import com.northernchile.api.model.Tour;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.model.User;
import com.northernchile.api.tour.TourRepository;
import com.northernchile.api.tour.TourScheduleRepository;
import com.northernchile.api.user.UserRepository;
import com.northernchile.api.util.DateTimeUtils;
import com.northernchile.api.util.SlugGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private TourScheduleRepository tourScheduleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LunarService lunarService;

    @Autowired
    private SlugGenerator slugGenerator;

    // Configuration for multiple admin users
    @Value("${admin.users.config:}")
    private String adminUsersConfig;

    // Flag to enable/disable data seeding
    @Value("${seed.data:false}")
    private boolean shouldSeedData;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== INICIALIZACIÓN DE DATOS ===");

        // Step 1: Initialize admin users
        initializeAdminUsers();

        // Step 2: Seed synthetic data if enabled
        if (shouldSeedData) {
            seedSyntheticData();
        }

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

    private void seedSyntheticData() {
        System.out.println("--- Sembrando Datos Sintéticos ---");

        // Only seed if there are no tours yet
        if (tourRepository.count() > 0) {
            System.out.println("○ Los datos sintéticos ya existen (tours encontrados). Omitiendo seeding.");
            return;
        }

        // Find admin users to assign as tour owners
        User firstAdmin = userRepository.findAll().stream()
                .filter(user -> user.getRole().contains("ADMIN"))
                .findFirst()
                .orElse(null);

        if (firstAdmin == null) {
            System.err.println("✗ No se encontraron usuarios administradores para asignar como propietarios de tours. Abortando seeding.");
            return;
        }

        System.out.println("Usando usuario '" + firstAdmin.getEmail() + "' como propietario de los tours de prueba.");

        // Create sample tours
        Tour tourAstro = createTour(
                firstAdmin,
                "Tour Astronómico",
                "Observa las estrellas del desierto más claro del mundo",
                "ASTRONOMICAL",
                50000,
                true
        );

        Tour tourValle = createTour(
                firstAdmin,
                "Valle de la Luna",
                "Explora las formaciones rocosas más espectaculares del Atacama",
                "REGULAR",
                35000,
                false
        );

        Tour tourFoto = createTour(
                firstAdmin,
                "Astrofotografía Nocturna",
                "Aprende a capturar la Vía Láctea con tu cámara",
                "SPECIAL",
                80000,
                true
        );

        Tour tourGeiser = createTour(
                firstAdmin,
                "Géiseres del Tatio",
                "Amanecer en los géiseres más altos del mundo",
                "REGULAR",
                45000,
                false
        );

        // Generate schedules for the next 30 days
        generateSchedulesForTour(tourAstro, 30);
        generateSchedulesForTour(tourValle, 30);
        generateSchedulesForTour(tourFoto, 15); // Less frequent
        generateSchedulesForTour(tourGeiser, 20);

        System.out.println("✓ Datos sintéticos creados exitosamente.");
    }

    private Tour createTour(User owner, String name, String description, String category, double price, boolean isRecurring) {
        Tour tour = new Tour();
        tour.setOwner(owner);

        // Multi-language support
        tour.setNameTranslations(Map.of(
                "es", name,
                "en", name + " Tour",
                "pt", "Passeio " + name
        ));

        tour.setDescriptionTranslations(Map.of(
                "es", description,
                "en", "Experience " + description.toLowerCase(),
                "pt", "Experimente " + description.toLowerCase()
        ));

        tour.setCategory(category);
        tour.setPrice(BigDecimal.valueOf(price));
        tour.setDefaultMaxParticipants(15);
        tour.setDurationHours(category.equals("REGULAR") ? 4 : 3);
        tour.setStatus("PUBLISHED");
        tour.setRecurring(isRecurring);

        // Generate slug from Spanish name
        tour.setSlug(slugGenerator.generateSlug(name));

        // Set weather sensitivities based on tour category
        if (category.equals("ASTRONOMICAL")) {
            tour.setMoonSensitive(true);
            tour.setCloudSensitive(true);
        }
        if (category.equals("REGULAR") && name.contains("Géiseres")) {
            tour.setWindSensitive(true);
        }

        Tour savedTour = tourRepository.save(tour);
        System.out.println("  ✓ Tour creado: " + name + " (" + category + ")");
        return savedTour;
    }

    private void generateSchedulesForTour(Tour tour, int days) {
        // Use Chile timezone for generating schedules
        LocalDate today = DateTimeUtils.todayInChile();
        int schedulesCreated = 0;

        for (int i = 1; i <= days; i++) {
            LocalDate date = today.plusDays(i);

            // Skip full moon dates for astronomical tours (use real lunar service)
            if (tour.isMoonSensitive() && lunarService.isFullMoon(date)) {
                System.out.println("      ⊘ Día " + date + " omitido (luna llena)");
                continue;
            }

            // Different start times based on category (in Chile local time)
            LocalTime startTime = switch (tour.getCategory()) {
                case "ASTRONOMICAL", "SPECIAL" -> LocalTime.of(20, 0); // 8 PM Chile time
                case "REGULAR" -> date.getDayOfWeek().getValue() <= 5
                    ? LocalTime.of(15, 0)  // 3 PM weekdays
                    : LocalTime.of(9, 0);   // 9 AM weekends
                default -> LocalTime.of(14, 0);
            };

            TourSchedule schedule = new TourSchedule();
            schedule.setTour(tour);
            // Convert Chile local time to UTC Instant for storage
            schedule.setStartDatetime(DateTimeUtils.toInstant(date, startTime));
            schedule.setMaxParticipants(tour.getDefaultMaxParticipants());
            schedule.setStatus("OPEN");

            tourScheduleRepository.save(schedule);
            schedulesCreated++;
        }

        System.out.println("    ✓ " + schedulesCreated + " horarios generados para " + tour.getNameTranslations().get("es"));
        System.out.println("      (Horario Chile - maneja cambio horario automáticamente)");
    }
}
