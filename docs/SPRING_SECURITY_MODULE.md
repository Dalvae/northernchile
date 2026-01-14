# Spring Boot Security Module Guide

## ¿Qué es el Módulo de Seguridad de Spring Boot?

Sí, Spring Boot tiene un módulo de seguridad completo llamado **Spring Security** que proporciona autenticación, autorización, y protección contra ataques comunes. Es el framework de seguridad estándar de la industria para aplicaciones Java.

## Spring Security en Northern Chile

### ¿Qué ya tenemos implementado?

El proyecto Northern Chile ya utiliza Spring Security con las siguientes características:

```xml
<!-- En pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

**Características actuales:**
1. ✅ Autenticación JWT (JSON Web Tokens)
2. ✅ Control de acceso basado en roles (RBAC)
3. ✅ OAuth2 para login con Google
4. ✅ Protección CSRF
5. ✅ Headers de seguridad (XSS, Content Security Policy)
6. ✅ Seguridad a nivel de método con `@EnableMethodSecurity`
7. ✅ Filtros personalizados (JwtAuthenticationFilter)
8. ✅ Manejo de errores de autenticación/autorización

### Estructura Actual de Seguridad

```
backend/src/main/java/com/northernchile/api/config/security/
├── SecurityConfig.java                    # Configuración principal
├── JwtAuthenticationFilter.java           # Filtro para validar JWT
├── JwtUtil.java                          # Utilidad para manejar JWT
├── UserDetailsServiceImpl.java           # Carga de usuarios
├── CustomOAuth2UserService.java          # OAuth2 (Google)
├── JsonAuthenticationEntryPoint.java     # Manejo de errores 401
├── JsonAccessDeniedHandler.java          # Manejo de errores 403
├── AuthenticationExceptionHandler.java   # Excepciones de autenticación
├── BookingSecurityService.java           # Seguridad de reservas
├── TourSecurityService.java              # Seguridad de tours
└── annotation/
    └── CurrentUser.java                  # Anotación para obtener usuario actual
```

## Componentes Principales de Spring Security

### 1. SecurityConfig - Configuración Principal

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // ← Habilita seguridad a nivel de método
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Configuración de autorización por URL
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas
                .requestMatchers("/api/tours/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                
                // Rutas protegidas por rol
                .requestMatchers("/api/admin/**")
                    .hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_PARTNER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/tours/**")
                    .hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_PARTNER_ADMIN")
                
                // Todas las demás requieren autenticación
                .anyRequest().authenticated()
            )
            // Sin estado (stateless) para API REST
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
            
        return http.build();
    }
}
```

### 2. Autenticación JWT

**Flujo de autenticación:**

1. Usuario se registra o inicia sesión → `/api/auth/login`
2. Backend valida credenciales
3. Backend genera JWT token
4. Cliente guarda token (localStorage, cookie)
5. Cliente envía token en cada request: `Authorization: Bearer <token>`
6. JwtAuthenticationFilter valida el token
7. Spring Security carga el usuario y sus roles
8. Request procede si tiene permisos

```java
// JwtAuthenticationFilter.java
@Override
protected void doFilterInternal(HttpServletRequest request, 
                                HttpServletResponse response, 
                                FilterChain filterChain) {
    String token = extractToken(request);
    
    if (token != null && jwtUtil.validateToken(token)) {
        String email = jwtUtil.getEmailFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        
        // Crear objeto de autenticación
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
            );
        
        // Establecer en contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    filterChain.doFilter(request, response);
}
```

### 3. Control de Acceso Basado en Roles (RBAC)

**Roles actuales en Northern Chile:**

```java
// Tres roles principales
ROLE_SUPER_ADMIN      // Acceso completo al sistema
ROLE_PARTNER_ADMIN    // Puede gestionar solo sus propios tours
ROLE_CLIENT           // Usuario cliente, puede hacer reservas
```

**Uso en controladores:**

```java
@RestController
@RequestMapping("/api/tours")
public class TourController {
    
    // Cualquiera puede ver tours
    @GetMapping
    public ResponseEntity<List<TourRes>> getAllTours() {
        return ResponseEntity.ok(tourService.findAll());
    }
    
    // Solo admins pueden crear tours
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_PARTNER_ADMIN')")
    public ResponseEntity<TourRes> createTour(@RequestBody TourCreateReq req) {
        return ResponseEntity.ok(tourService.create(req));
    }
    
    // Solo el dueño o super admin pueden actualizar
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or @tourSecurityService.isOwner(#id)")
    public ResponseEntity<TourRes> updateTour(@PathVariable UUID id, 
                                              @RequestBody TourUpdateReq req) {
        return ResponseEntity.ok(tourService.update(id, req));
    }
}
```

### 4. Servicios de Seguridad Personalizados

**TourSecurityService** - Valida propiedad de recursos:

```java
@Service
public class TourSecurityService {
    
    private final TourRepository tourRepository;
    
    // Verifica si el usuario actual es dueño del tour
    public boolean isOwner(UUID tourId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        
        User user = (User) auth.getPrincipal();
        
        // Super admin puede todo
        if (user.getRole().equals("ROLE_SUPER_ADMIN")) {
            return true;
        }
        
        // Partner admin solo puede gestionar sus tours
        if (user.getRole().equals("ROLE_PARTNER_ADMIN")) {
            Tour tour = tourRepository.findById(tourId).orElse(null);
            return tour != null && tour.getOwnerId().equals(user.getId());
        }
        
        return false;
    }
}
```

### 5. Obtener Usuario Actual

**Opción 1: Anotación personalizada**

```java
@RestController
public class BookingController {
    
    @PostMapping("/api/bookings")
    public ResponseEntity<BookingRes> createBooking(
            @CurrentUser User user,  // ← Inyecta usuario actual
            @RequestBody BookingCreateReq req) {
        return ResponseEntity.ok(bookingService.create(user, req));
    }
}
```

**Opción 2: SecurityUtils**

```java
public class SecurityUtils {
    
    public static Optional<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
            return Optional.of((User) auth.getPrincipal());
        }
        return Optional.empty();
    }
    
    public static UUID getCurrentUserId() {
        return getCurrentUser()
            .map(User::getId)
            .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
    }
}
```

## Características Avanzadas de Spring Security

### 1. Seguridad a Nivel de Método

```java
@Service
public class TourService {
    
    // Verificar antes de ejecutar
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_PARTNER_ADMIN')")
    public Tour create(TourCreateReq req) {
        // ...
    }
    
    // Verificar después de ejecutar
    @PostAuthorize("returnObject.ownerId == authentication.principal.id")
    public Tour findById(UUID id) {
        // ...
    }
    
    // Filtrar colecciones
    @PostFilter("filterObject.status == 'PUBLISHED' or hasRole('ROLE_ADMIN')")
    public List<Tour> findAll() {
        // ...
    }
}
```

### 2. Expresiones de Seguridad Personalizadas

```java
@Component("customSecurity")
public class CustomSecurityExpressions {
    
    // Uso: @PreAuthorize("@customSecurity.canAccessTour(#tourId)")
    public boolean canAccessTour(UUID tourId) {
        // Lógica personalizada
        return true;
    }
    
    // Uso: @PreAuthorize("@customSecurity.hasPermission('tour:write')")
    public boolean hasPermission(String permission) {
        User user = SecurityUtils.getCurrentUser().orElse(null);
        if (user == null) return false;
        
        // Verificar permiso
        return user.getPermissions().contains(permission);
    }
}
```

### 3. Filtrado Automático por Tenant (Multi-tenancy)

**Opción A: Repository personalizado**

```java
@Repository
public class TourRepository extends JpaRepository<Tour, UUID> {
    
    default List<Tour> findAllForCurrentUser() {
        User user = SecurityUtils.getCurrentUser().orElseThrow();
        
        if (user.getRole().equals("ROLE_SUPER_ADMIN")) {
            return findAll();
        }
        
        return findByOwnerId(user.getId());
    }
}
```

**Opción B: Filtro JPA automático**

```java
@Entity
@Table(name = "tours")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "owner_id = :tenantId")
public class Tour {
    // ...
}

// Activar filtro automáticamente
@Component
public class TenantFilterAspect {
    
    @Before("execution(* com.northernchile.api..*Repository.*(..))")
    public void enableTenantFilter(JoinPoint joinPoint) {
        User user = SecurityUtils.getCurrentUser().orElse(null);
        if (user != null && user.getRole().equals("ROLE_PARTNER_ADMIN")) {
            // Activar filtro para este usuario
            Session session = entityManager.unwrap(Session.class);
            Filter filter = session.enableFilter("tenantFilter");
            filter.setParameter("tenantId", user.getId().toString());
        }
    }
}
```

### 4. Auditoría de Seguridad

```java
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {
    
    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return () -> SecurityUtils.getCurrentUser().map(User::getId);
    }
}

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Tour {
    
    @CreatedBy
    private UUID createdBy;
    
    @LastModifiedBy
    private UUID lastModifiedBy;
    
    @CreatedDate
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;
}
```

## Módulos Adicionales de Spring Security

### 1. Spring Security OAuth2 (Ya implementado)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

**Configuración para Google Login:**

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope:
              - email
              - profile
```

### 2. Spring Security ACL (Access Control Lists)

Para permisos muy granulares a nivel de objeto:

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-acl</artifactId>
</dependency>
```

**Ejemplo de uso:**

```java
// Verificar permiso específico en objeto específico
@PreAuthorize("hasPermission(#tourId, 'com.northernchile.api.model.Tour', 'WRITE')")
public void updateTour(UUID tourId, TourUpdateReq req) {
    // ...
}
```

### 3. Spring Security LDAP

Para integración con Active Directory:

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-ldap</artifactId>
</dependency>
```

### 4. Spring Security SAML

Para Single Sign-On empresarial:

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-saml2-service-provider</artifactId>
</dependency>
```

## Testing con Spring Security

```java
@SpringBootTest
@AutoConfigureMockMvc
public class TourControllerSecurityTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(username = "admin@test.com", roles = "SUPER_ADMIN")
    public void superAdminCanCreateTour() throws Exception {
        mockMvc.perform(post("/api/tours")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "Test Tour", "price": 50000}
                    """))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "client@test.com", roles = "CLIENT")
    public void clientCannotCreateTour() throws Exception {
        mockMvc.perform(post("/api/tours")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "Test Tour", "price": 50000}
                    """))
            .andExpect(status().isForbidden());
    }
    
    @Test
    public void unauthenticatedCanViewPublicTours() throws Exception {
        mockMvc.perform(get("/api/tours"))
            .andExpect(status().isOk());
    }
}
```

## Best Practices

### ✅ DO - Buenas Prácticas

1. **Usar constantes para roles**
```java
public class SecurityConstants {
    public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    public static final String ROLE_PARTNER_ADMIN = "ROLE_PARTNER_ADMIN";
    public static final String ROLE_CLIENT = "ROLE_CLIENT";
}
```

2. **Preferir seguridad a nivel de método sobre URL**
```java
// ✅ Mejor
@PreAuthorize("hasRole('ADMIN')")
public void sensitiveOperation() { }

// ⚠️ Solo usar en SecurityConfig para rutas públicas/estáticas
.requestMatchers("/api/admin/**").hasRole("ADMIN")
```

3. **Validar propiedad de recursos**
```java
@PreAuthorize("@tourSecurityService.isOwner(#tourId)")
public void updateTour(UUID tourId) { }
```

4. **Usar PasswordEncoder para contraseñas**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

5. **Stateless sessions para APIs REST**
```java
.sessionManagement(session -> 
    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)
```

### ❌ DON'T - Malas Prácticas

1. ❌ **No guardar contraseñas en texto plano**
2. ❌ **No desactivar CSRF sin razón válida**
3. ❌ **No exponer detalles de errores de seguridad**
4. ❌ **No validar solo en frontend**
5. ❌ **No usar roles hardcodeados como strings en múltiples lugares**

## Recursos Adicionales

### Documentación Oficial
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Spring Boot Security](https://docs.spring.io/spring-boot/docs/current/reference/html/web.html#web.security)
- [Method Security](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)

### Tutoriales
- [Baeldung Spring Security](https://www.baeldung.com/security-spring)
- [Spring Security Architecture](https://spring.io/guides/topicals/spring-security-architecture)
- [JWT with Spring Boot](https://www.baeldung.com/spring-security-oauth-jwt)

### Herramientas
- [JWT.io](https://jwt.io/) - Debug JWT tokens
- [OWASP Security Guide](https://owasp.org/www-project-web-security-testing-guide/)

## Conclusión

Spring Security es un framework completo y potente que ya está integrado en Northern Chile. La implementación actual es sólida y apropiada para la fase MVP del proyecto.

**Resumen de capacidades:**
- ✅ Autenticación (JWT, OAuth2)
- ✅ Autorización (Roles, Métodos)
- ✅ Protección contra ataques comunes
- ✅ Multi-tenancy básico (owner_id)
- ⏳ Permisos granulares (recomendado para futuro)
- ⏳ ACL para control fino (si se necesita)
- ⏳ Auditoría completa (parcialmente implementada)

Para evolucionar el sistema de seguridad, consulta:
- `SECURITY_RESEARCH.md` - Opciones de mejora
- `SIMILAR_REPOSITORIES.md` - Ejemplos de otros proyectos
