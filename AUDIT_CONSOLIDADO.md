# Auditoría Consolidada - Northern Chile
**Fecha:** Noviembre 2025
**Alcance:** Backend (Spring Boot 3), Frontend (Nuxt 3), Configuración, Seguridad, i18n

---

## RESUMEN EJECUTIVO

Este documento consolida todas las auditorías técnicas del proyecto NorthernChile. Se enfoca en **problemas pendientes** que requieren atención, eliminando items ya resueltos.

**Estado actual del proyecto:**
- ✅ Base sólida con Spring Boot 3 + Nuxt 3
- ✅ Sistema de autenticación JWT funcional
- ✅ Logging robusto con persistencia
- ✅ Validación de participantes completa
- ✅ Accesibilidad (ARIA labels) implementada
- ✅ Títulos de página apropiados en admin
- ✅ Stacktrace en desarrollo para debugging
- ✅ Password exposure en toString() CORREGIDO
- ✅ @Valid annotations agregadas
- ✅ Payment token logging ELIMINADO
- ✅ Security headers implementados
- ✅ Cart cookie security CORREGIDO

**Problemas pendientes críticos:**
- ❌ Seguridad: Rate limiting, webhook signatures
- ❌ i18n: Cart.vue sin traducciones, locale hardcodeado en admin pages
- ❌ Configuración: Variables de entorno faltantes en docker-compose
- ❌ Backend: Errores sin i18n, falta LocaleResolver

### DISTRIBUCIÓN DE SEVERIDAD (Pendientes)

**CRÍTICO:** 1 issue  → Rate limiting (2-4 horas)
**HIGH:**    6 issues → Webhook security, i18n backend, cart i18n, admin dates, etc. (~12 horas)
**MEDIUM:**  8 issues → Token refresh, logout, password complexity, etc. (~16 horas)

**Total estimado:** ~30-35 horas de trabajo

### IMPLEMENTACIONES SEGURAS ENCONTRADAS ✓

- ✓ BCrypt password hashing (buena práctica)
- ✓ JWT authentication con HMAC-SHA256
- ✓ JPA parameterized queries (previene SQL injection)
- ✓ Email verification tokens con expiración
- ✓ Password reset tokens con 2 horas de expiración
- ✓ Role-based access control (RBAC)
- ✓ Multi-tenant tour ownership validation
- ✓ File upload size validation (10MB límite)
- ✓ File type validation (solo imágenes)
- ✓ CSRF disabled para JWT stateless auth (apropiado)

### COMPLIANCE ASSESSMENT

**OWASP Top 10 2021:**
- A01 Broken Access Control........... PARTIAL (requiere testing)
- A02 Cryptographic Failures.......... GOOD (BCrypt + JWT)
- A03 Injection...................... GOOD (JPA protege)
- A04 Insecure Design................ NEEDS WORK (falta rate limiting)
- A05 Security Misconfiguration....... GOOD (headers implementados)
- A06 Vulnerable Components........... GOOD (mantener actualizado)
- A07 Authentication Failures......... NEEDS WORK (falta logout, rate limit)
- A08 Software/Data Integrity......... GOOD
- A09 Logging & Monitoring............ GOOD (token logging corregido)
- A10 SSRF........................... LOW RISK

**PCI DSS (para procesamiento de pagos):**
- Status: PARCIALMENTE COMPLIANT
- Requiere: Webhook signature verification
- Requiere: Audit logging para operaciones de pago
- Requiere: HTTPS/TLS enforcement (solo producción)

**GDPR (manejo de datos de usuario):**
- Status: NEEDS ASSESSMENT
- Falta: Mecanismo de eliminación de datos
- Falta: Mecanismo de exportación de datos de usuario
- OK: Email verification con tracking de consentimiento

---

## 1. SEGURIDAD - ISSUES PENDIENTES

### CRÍTICO - Implementar rate limiting en auth endpoints
**Impacto:** Ataques de fuerza bruta sin restricción
**Archivos:** `AuthController.java`
**Tiempo estimado:** 2-4 horas (incluye dependencias)

**Solución recomendada:** Usar Bucket4j
```xml
<dependency>
    <groupId>io.github.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>7.6.0</version>
</dependency>
```

```java
@Configuration
public class RateLimitConfig {
    @Bean
    public Bucket loginBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
            .build();
    }
}
```

---

### HIGH - Proteger webhooks de replay attacks
**Impacto:** Pagos pueden confirmarse múltiples veces
**Archivos:** `WebhookController.java`
**Tiempo estimado:** 2-3 horas

**Faltante:**
- Verificación de firma Mercado Pago
- Verificación de firma Transbank
- Deduplicación de request IDs
- Validación de timestamp

**Solución:**
```java
private boolean verifyMercadoPagoSignature(String body, String signature) {
    String secret = mercadoPagoSecret;
    String computed = HmacUtils.hmacSha256Hex(secret, body);
    return computed.equals(signature);
}

@PostMapping("/mercadopago")
public ResponseEntity<Void> handleMercadoPagoWebhook(
    @RequestBody String body,
    @RequestHeader("X-Signature") String signature) {

    if (!verifyMercadoPagoSignature(body, signature)) {
        return ResponseEntity.status(403).build();
    }
    // Process webhook...
}
```

---

### MEDIUM - Validar URLs de redirect en payment flow
**Impacto:** Open redirect vulnerability - phishing
**Archivos:** `PaymentInitReq.java`, `PaymentController.java`
**Tiempo estimado:** 1 hora

**Solución:**
```java
private static final List<String> ALLOWED_DOMAINS = Arrays.asList(
    "https://www.northernchile.com",
    "http://localhost:3000"
);

private boolean isValidReturnUrl(String url) {
    return ALLOWED_DOMAINS.stream().anyMatch(url::startsWith);
}
```

---

### MEDIUM - Implementar logout con token blacklist (OPCIONAL)
**Archivos:** Falta crear `LogoutController.java`, `TokenBlacklistRepository.java`
**Tiempo estimado:** 2-3 horas
**NOTA:** Sin Redis en el proyecto - usar PostgreSQL o skip

**Opción A: PostgreSQL-based (más lento pero funcional)**
```sql
CREATE TABLE token_blacklist (
    token VARCHAR(512) PRIMARY KEY,
    blacklisted_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_expires_at ON token_blacklist(expires_at);
```

```java
@Service
public class JwtBlacklistService {
    private final TokenBlacklistRepository repository;

    @Transactional
    public void blacklistToken(String token) {
        Instant expiresAt = jwtUtil.getExpirationTime(token);
        TokenBlacklist blacklist = new TokenBlacklist(token, Instant.now(), expiresAt);
        repository.save(blacklist);
    }

    public boolean isBlacklisted(String token) {
        return repository.existsByToken(token);
    }

    @Scheduled(cron = "0 0 */6 * * *") // Each 6 hours
    public void cleanupExpiredTokens() {
        repository.deleteByExpiresAtBefore(Instant.now());
    }
}
```

**Opción B: Skip logout (recomendado)**
- Los tokens expiran naturalmente en 24h
- Usuarios simplemente eliminan el token del localStorage
- Más simple, sin overhead de base de datos

---

### MEDIUM - Implementar token refresh mechanism
**Impacto:** Usuarios deben hacer login cada 24h
**Tiempo estimado:** 2 horas

**Solución:**
1. Agregar refresh token a LoginRes
2. Endpoint `/auth/refresh` que acepta refresh token
3. Access token de corta duración (15min), refresh token largo (7 días)

---

### MEDIUM - Enforcer complejidad de passwords
**Archivo:** `RegisterReq.java`
**Tiempo estimado:** 30 minutos

**Actual:**
```java
@Size(min = 8)
private String password;
```

**Mejorado:**
```java
@Pattern(
    regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
    message = "Password debe tener al menos 1 mayúscula, 1 minúscula, 1 número y 1 carácter especial"
)
private String password;
```

---

## 2. INTERNACIONALIZACIÓN (i18n) - PENDIENTE

### CRÍTICO - Admin panel sin traducciones (40+ strings hardcodeadas)

**Páginas afectadas:**
- `admin/index.vue`: "Total Reservas", "Ingresos del Mes", "Tours Activos"
- `admin/audit-logs.vue`: "Registro de Auditoría", "Total", "Acción"
- `admin/users.vue`, `admin/reports.vue`, `admin/alerts.vue`, `admin/private-requests.vue`

**NOTA:** El usuario especifica que **admin está en español y no importa traducirlo**.

**Acción:** SKIP - No traducir admin panel (queda en español)

---

### HIGH - Cart.vue tiene 17+ strings hardcodeadas

**Archivo:** `frontend/app/pages/cart.vue`
**Tiempo estimado:** 1 hora

**Reemplazar:**
```vue
<!-- ❌ Actual -->
<h1>Carrito de Compras</h1>
<p>Tu carrito está vacío</p>
<UButton>Proceder al Pago</UButton>

<!-- ✅ Correcto -->
<h1>{{ t('cart.title') }}</h1>
<p>{{ t('cart.empty_message') }}</p>
<UButton>{{ t('cart.proceed_to_payment') }}</UButton>
```

**Las claves ya existen en es.json** - solo falta usarlas.

---

### HIGH - Páginas admin usan `toLocaleDateString('es-CL')` hardcodeado

**Tiempo estimado:** 1-2 horas (7 archivos)

**Archivos afectados:**
- `admin/audit-logs.vue` (línea 561, 569)
- `admin/users.vue` (línea 124)
- `admin/private-requests.vue` (línea 195)
- `admin/index.vue` (línea 115)
- `admin/alerts.vue` (línea 647)
- `admin/reports.vue` (línea 125)
- `payment/callback.vue` (línea 194)

**Solución:** Usar composable `useDateTime()` (ya arreglado):
```vue
<script setup>
const { formatDate, formatDateTime } = useDateTime()
</script>

<template>
  <!-- ❌ Actual -->
  {{ new Date(booking.date).toLocaleDateString('es-CL') }}

  <!-- ✅ Correcto -->
  {{ formatDate(booking.date) }}
</template>
```

---

### HIGH - Backend sin i18n para mensajes de error

**Tiempo estimado:** 3-4 horas

**Faltante:**
1. Mensajes de validación localizados
2. Mensajes de error de negocio localizados
3. LocaleResolver configurado
4. Procesamiento de `Accept-Language` header

**Solución:**

**1. Agregar a messages_*.properties:**
```properties
# messages_es.properties
error.booking.overbooking=Tour completamente reservado
error.booking.invalid_participant_count=Número de participantes inválido
error.tour.not_found=Tour no encontrado
error.schedule.not_found=Horario de tour no encontrado
error.cancellation.too_late=La cancelación no está permitida dentro de 24 horas de inicio
error.payment.failed=Error al procesar el pago
error.authentication.invalid_credentials=Credenciales inválidas
```

**2. Configurar LocaleResolver:**
```java
@Configuration
public class LocaleConfiguration {
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(new Locale("es", "CL"));
        resolver.setSupportedLocales(Arrays.asList(
            new Locale("es", "CL"),
            new Locale("en", "US"),
            new Locale("pt", "BR")
        ));
        return resolver;
    }
}
```

**3. Crear LocalizedMessageProvider:**
```java
@Component
public class LocalizedMessageProvider {
    private final MessageSource messageSource;

    public LocalizedMessageProvider(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String key, Object[] args, Locale locale) {
        return messageSource.getMessage(key, args, locale);
    }

    public String getMessage(String key, Locale locale) {
        return getMessage(key, null, locale);
    }
}
```

**4. Usar en GlobalExceptionHandler:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final LocalizedMessageProvider messageProvider;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            WebRequest request,
            Locale locale) {

        String message = messageProvider.getMessage(
            "error." + ex.getResourceType() + ".not_found",
            new Object[]{ex.getResourceId()},
            locale
        );

        ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            message,
            request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}
```

---

### MEDIUM - Currency formatting en schedule page
**Archivo:** `tours/[slug]/schedule.vue`

**Actual:**
```vue
${{ (tour.price || 0)?.toLocaleString() }}
```

**Correcto:**
```vue
<script setup>
const { formatPrice } = useCurrency()
</script>

<template>
  {{ formatPrice(tour.price) }}
</template>
```

---

### MEDIUM - Falta keys de traducción
**Keys referenciadas pero no existen:**
- `schedule.error_description`
- `schedule.back_to_tours`
- `schedule.title`

**Acción:** Agregar a es.json, en.json, pt.json

---

## 3. CONFIGURACIÓN - VARIABLES DE ENTORNO FALTANTES

### CRÍTICO - docker-compose.yml incompleto

**Faltante en environment del backend:**
```yaml
# Email Configuration
- MAIL_HOST=${MAIL_HOST:-smtp.gmail.com}
- MAIL_PORT=${MAIL_PORT:-587}
- MAIL_USERNAME=${MAIL_USERNAME}
- MAIL_PASSWORD=${MAIL_PASSWORD}
- MAIL_FROM_EMAIL=${MAIL_FROM_EMAIL:-noreply@northernchile.cl}
- MAIL_FROM_NAME=${MAIL_FROM_NAME:-Northern Chile Tours}
- MAIL_ENABLED=${MAIL_ENABLED:-false}

# Payment Providers
- TRANSBANK_COMMERCE_CODE=${TRANSBANK_COMMERCE_CODE:-597055555532}
- TRANSBANK_API_KEY=${TRANSBANK_API_KEY:-579B532A7440BB0C9079DED94D31EA1615BACEB56610332264630D42D0A36B1C}
- TRANSBANK_ENVIRONMENT=${TRANSBANK_ENVIRONMENT:-INTEGRATION}
- MERCADOPAGO_ACCESS_TOKEN=${MERCADOPAGO_ACCESS_TOKEN:-TEST-ACCESS-TOKEN}
- MERCADOPAGO_PUBLIC_KEY=${MERCADOPAGO_PUBLIC_KEY:-TEST-PUBLIC-KEY}

# OAuth (si se implementa)
- GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID}
- GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET}

# Development
- SPRING_REMOTE_SECRET=${SPRING_REMOTE_SECRET:-change-me-in-dev}
```

**Consecuencia actual:** Email, payment, y APIs externas fallan en Docker

---

### HIGH - .env.example incompleto

**Agregar:**
```bash
# Database (explícito)
SPRING_DATASOURCE_USERNAME=user
SPRING_DATASOURCE_PASSWORD=password

# AWS S3
AWS_S3_BUCKET_NAME=northern-chile-assets

# Frontend
NUXT_PUBLIC_BASE_URL=http://localhost:3000
```

**Eliminar** (herramientas de desarrollo local, no de proyecto):
```bash
# ❌ QUITAR ESTO:
AIDER_MODEL=gemini/gemini-2.5-pro-preview-03-25
OPENROUTER_API_KEY=
AIDER_ATTRIBUTE_COMMITTER=false
AIDER_AUTO_COMMITS=false
AIDER_DARK_MODE=true
```

---

### HIGH - GitHub Actions workflow incompleto

**Archivo:** `.github/workflows/deploy-backend.yml`

**Faltante en secrets:**
```yaml
TRANSBANK_COMMERCE_CODE=${{ secrets.TRANSBANK_COMMERCE_CODE }}
TRANSBANK_API_KEY=${{ secrets.TRANSBANK_API_KEY }}
TRANSBANK_ENVIRONMENT=${{ secrets.TRANSBANK_ENVIRONMENT }}
MERCADOPAGO_ACCESS_TOKEN=${{ secrets.MERCADOPAGO_ACCESS_TOKEN }}
MERCADOPAGO_PUBLIC_KEY=${{ secrets.MERCADOPAGO_PUBLIC_KEY }}
MAIL_HOST=${{ secrets.MAIL_HOST }}
MAIL_PORT=${{ secrets.MAIL_PORT }}
MAIL_USERNAME=${{ secrets.MAIL_USERNAME }}
MAIL_PASSWORD=${{ secrets.MAIL_PASSWORD }}
MAIL_FROM_EMAIL=${{ secrets.MAIL_FROM_EMAIL }}
MAIL_FROM_NAME=${{ secrets.MAIL_FROM_NAME }}
MAIL_ENABLED=${{ secrets.MAIL_ENABLED }}
GOOGLE_CLIENT_ID=${{ secrets.GOOGLE_CLIENT_ID }}
GOOGLE_CLIENT_SECRET=${{ secrets.GOOGLE_CLIENT_SECRET }}
SPRING_REMOTE_SECRET=${{ secrets.SPRING_REMOTE_SECRET }}
```

---

### MEDIUM - docker-compose.override.yml mismatch

**Actual:**
```yaml
environment:
  - SPRING_DEVTOOLS_REMOTE_SECRET=tu-secreto-aqui
```

**Correcto:**
```yaml
environment:
  - SPRING_DEVTOOLS_REMOTE_SECRET=${SPRING_REMOTE_SECRET:-change-me-in-dev}
```

---

## 4. FEATURES PENDIENTES

### Email notifications

**Faltante:**
1. ❌ Payment confirmation email
2. ❌ Booking cancellation email
3. ❌ Admin notification emails

**Email audit logging:**
- Agregar tabla `email_logs` con estado (sent, failed, pending)
- Retry logic para emails fallidos
- Tracking de opens/clicks (opcional)

---

### Booking status transition validation

**Archivo:** Agregar a `BookingService.java`

**Validar transiciones permitidas:**
```
PENDING -> CONFIRMED (solo después de pago)
PENDING -> CANCELLED (solo antes de 24h)
CONFIRMED -> COMPLETED (solo después de fecha del tour)
CONFIRMED -> CANCELLED (solo antes de 24h, con refund)
```

**Implementación:**
```java
public void validateStatusTransition(String currentStatus, String newStatus) {
    Map<String, List<String>> allowedTransitions = Map.of(
        "PENDING", Arrays.asList("CONFIRMED", "CANCELLED"),
        "CONFIRMED", Arrays.asList("COMPLETED", "CANCELLED"),
        "CANCELLED", Collections.emptyList(),
        "COMPLETED", Collections.emptyList()
    );

    if (!allowedTransitions.get(currentStatus).contains(newStatus)) {
        throw new IllegalStateException(
            "Cannot transition from " + currentStatus + " to " + newStatus
        );
    }
}
```

---

### Booking cancellation con refund

**Faltante:**
- Endpoint `/api/bookings/{id}/cancel`
- Validación de política de 24h
- Integración con payment providers para refund
- Email de confirmación de cancelación

**Implementación:**
```java
@PostMapping("/{id}/cancel")
public ResponseEntity<?> cancelBooking(@PathVariable UUID id) {
    Booking booking = bookingRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Booking", id));

    // Validar 24h policy
    Instant tourStart = booking.getTourSchedule().getStartDatetime();
    if (Instant.now().isAfter(tourStart.minus(24, ChronoUnit.HOURS))) {
        throw new IllegalStateException("Cannot cancel within 24 hours of tour start");
    }

    // Process refund
    Payment payment = booking.getPayment();
    if (payment != null && payment.getStatus() == PaymentStatus.COMPLETED) {
        paymentService.refund(payment.getId());
    }

    // Update status
    booking.setStatus("CANCELLED");
    bookingRepository.save(booking);

    // Send email
    notificationService.sendCancellationEmail(booking);

    return ResponseEntity.ok(Map.of("message", "Booking cancelled successfully"));
}
```

---

### Schedule cancellation cascade

**Lógica:**
Cuando se cancela un `TourSchedule`, automáticamente:
1. Cancelar todas las bookings asociadas
2. Procesar refunds para bookings CONFIRMED
3. Enviar emails a todos los afectados
4. Marcar schedule como CANCELLED

**Implementación:**
```java
@Transactional
public void cancelSchedule(UUID scheduleId, String reason) {
    TourSchedule schedule = scheduleRepository.findById(scheduleId)
        .orElseThrow(() -> new ResourceNotFoundException("Schedule", scheduleId));

    // Get all confirmed bookings
    List<Booking> bookings = bookingRepository.findByScheduleId(scheduleId);

    for (Booking booking : bookings) {
        if ("CONFIRMED".equals(booking.getStatus())) {
            // Process refund
            if (booking.getPayment() != null) {
                paymentService.refund(booking.getPayment().getId());
            }

            // Cancel booking
            booking.setStatus("CANCELLED");
            booking.setCancellationReason(reason);
            bookingRepository.save(booking);

            // Send email
            notificationService.sendScheduleCancellationEmail(booking, reason);
        }
    }

    // Cancel schedule
    schedule.setStatus("CANCELLED");
    scheduleRepository.save(schedule);
}
```

---

## 5. TESTING PENDIENTE

### Integration tests críticos

**Faltante:**
1. Test de flujo de booking completo (create -> pay -> confirm)
2. Test de cancellation con refund
3. Test de webhook replay protection
4. Test de rate limiting
5. Test de token blacklist

**Framework:** Spring Boot Test + TestContainers para PostgreSQL

**Ejemplo:**
```java
@SpringBootTest
@Testcontainers
class BookingIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Test
    void shouldCompleteBookingFlow() {
        // 1. Create booking
        BookingReq req = new BookingReq(/*...*/);
        BookingRes booking = bookingController.createBooking(req).getBody();

        // 2. Initialize payment
        PaymentInitRes payment = paymentController.initPayment(booking.getId()).getBody();

        // 3. Simulate webhook confirmation
        webhookController.handleTransbankWebhook(payment.getToken());

        // 4. Verify booking confirmed
        BookingRes confirmed = bookingController.getBooking(booking.getId()).getBody();
        assertEquals("CONFIRMED", confirmed.getStatus());
    }
}
```

---

## 6. PRIORIZACIÓN DE TAREAS

### INMEDIATO (Esta semana):
1. ✅ Agregar variables faltantes a docker-compose.yml
2. ✅ Actualizar .env.example (agregar, quitar AIDER)
3. ✅ Fix cart.vue hardcoded strings
4. ✅ Fix admin pages `toLocaleDateString('es-CL')` → `useDateTime()`

### CORTO PLAZO (2 semanas):
5. ❌ Implementar rate limiting en auth
6. ❌ Backend error messages i18n
7. ❌ Webhook signature verification
8. ❌ Booking cancellation con refund
9. ❌ Email notifications (payment, cancellation)

### MEDIO PLAZO (1 mes):
10. ❌ Token refresh mechanism
11. ❌ Logout con blacklist
12. ❌ Schedule cancellation cascade
13. ❌ Integration tests
14. ❌ Password complexity enforcement

### LARGO PLAZO (Pre-producción):
15. ❌ Validate redirect URLs
16. ❌ Email audit logging
17. ❌ Booking status transition validation
18. ❌ Admin notification emails

---

## 7. CHECKLIST DE PRODUCCIÓN

### Seguridad
- [ ] Rate limiting implementado
- [ ] Webhook signatures verificadas
- [ ] Redirect URLs validadas
- [ ] Token blacklist funcional
- [ ] Token refresh implementado
- [ ] Password complexity enforced
- [ ] HTTPS/TLS configurado
- [ ] Security headers activos

### Configuración
- [x] Todas las variables en docker-compose.yml
- [x] .env.example completo y limpio
- [ ] GitHub Actions secrets configurados
- [ ] Production docker-compose.yml template
- [ ] Database SSL enabled

### i18n
- [x] useDateTime() usando locale dinámico
- [ ] Cart.vue traducido (usar claves existentes)
- [ ] Admin pages usando useDateTime()
- [ ] Backend LocaleResolver configurado
- [ ] Error messages localizados
- [ ] Missing translation keys agregados

### Features
- [ ] Payment confirmation email
- [ ] Booking cancellation email
- [ ] Admin notification emails
- [ ] Booking cancellation con refund
- [ ] Schedule cancellation cascade
- [ ] Booking status validation

### Testing
- [ ] Integration tests de booking flow
- [ ] Integration tests de payment
- [ ] Integration tests de cancellation
- [ ] Rate limiting tests
- [ ] Security tests (OWASP Top 10)

### Documentación
- [x] Logging guide (LOGS_GUIDE.md)
- [ ] Production deployment guide
- [ ] Environment variables reference
- [ ] Database migration guide
- [ ] Security hardening checklist

---

## APÉNDICE: ARCHIVOS A MODIFICAR

### Backend
1. `docker-compose.yml` - Agregar variables faltantes
2. `AuthController.java` - Rate limiting
3. `WebhookController.java` - Signature verification
4. `PaymentController.java` - Validate redirect URLs
5. `GlobalExceptionHandler.java` - i18n messages
6. `LocaleConfiguration.java` - CREAR
7. `LocalizedMessageProvider.java` - CREAR
8. `LogoutController.java` - CREAR
9. `JwtBlacklistService.java` - CREAR
10. `BookingService.java` - Cancellation + status validation
11. `ScheduleService.java` - Cascade cancellation
12. `messages_*.properties` - Error messages

### Frontend
1. `.env.example` - Limpiar y completar
2. `cart.vue` - Usar t() para strings
3. `admin/audit-logs.vue` - useDateTime()
4. `admin/users.vue` - useDateTime()
5. `admin/private-requests.vue` - useDateTime()
6. `admin/index.vue` - useDateTime()
7. `admin/alerts.vue` - useDateTime()
8. `admin/reports.vue` - useDateTime()
9. `payment/callback.vue` - useDateTime()
10. `tours/[slug]/schedule.vue` - useCurrency()
11. `i18n/locales/*.json` - Missing keys

### CI/CD
1. `.github/workflows/deploy-backend.yml` - Secrets completos
2. `docker-compose.override.yml` - Fix SPRING_REMOTE_SECRET

---

**Documento generado:** Noviembre 2025
**Próxima revisión:** Después de completar tareas inmediatas
**Autor:** Consolidación de auditorías de seguridad, configuración e i18n
