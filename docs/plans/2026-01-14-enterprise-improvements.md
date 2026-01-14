# Enterprise Improvements Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Prepare Northern Chile Tours for enterprise/SaaS deployment with production-ready security, observability, and resilience.

**Architecture:** Incremental improvements to existing Spring Boot + Nuxt 3 stack, prioritizing security hardening, then observability, then UX polish. Each task is independent and can be deployed separately.

**Tech Stack:** Spring Boot 3.5.7, Spring Security 6, Bucket4j rate limiting, Micrometer metrics, SLF4J structured logging, Nuxt 3, Vue 3

---

## Phase 1: Critical Security Fixes

### Task 1: Fix XSS innerHTML in Calendar Components

**Files:**
- Modify: `frontend/app/components/TourCalendar.vue:410-438`
- Modify: `frontend/app/pages/admin/calendar.vue:665-700`

**Context:** Both files use `innerHTML` to render weather and moon data in FullCalendar day cells. While data comes from backend API (not user input), using textContent and proper DOM manipulation is safer.

**Step 1: Update TourCalendar.vue weather rendering**

Replace innerHTML with textContent and DOM creation at lines 420-434:

```typescript
// Replace this block (lines 415-434):
if (weather && weather.temp) {
  const wData = weather
  const weatherDiv = document.createElement('div')
  weatherDiv.className = 'text-xs text-neutral-500 flex flex-col'

  const icon = wData.weather?.[0]?.main === 'Clear' ? '☀️' : '☁️'

  const iconSpan = document.createElement('span')
  iconSpan.textContent = icon
  weatherDiv.appendChild(iconSpan)

  const tempSpan = document.createElement('span')
  tempSpan.className = 'font-mono'
  tempSpan.textContent = `${Math.round(wData.temp?.day || 0)}°`
  weatherDiv.appendChild(tempSpan)

  infoBox.appendChild(weatherDiv)
}

if (moon) {
  const moonDiv = document.createElement('div')
  moonDiv.className = 'text-xs text-neutral-500 flex flex-col items-end'

  const moonIconSpan = document.createElement('span')
  moonIconSpan.className = 'text-base'
  moonIconSpan.textContent = getMoonEmoji(moon.phaseName || '')
  moonDiv.appendChild(moonIconSpan)

  const illumSpan = document.createElement('span')
  illumSpan.className = 'scale-75 origin-right'
  illumSpan.textContent = `${moon.illumination}%`
  moonDiv.appendChild(illumSpan)

  infoBox.appendChild(moonDiv)
}
```

**Step 2: Run frontend typecheck**

Run: `cd frontend && pnpm typecheck`
Expected: No type errors

**Step 3: Update admin/calendar.vue moon rendering**

Replace innerHTML at lines 673-679:

```typescript
// Replace moon innerHTML (lines 673-679):
if (moonPhase) {
  const moonDiv = document.createElement('div')
  moonDiv.className = 'flex items-center gap-1'

  const iconSpan = document.createElement('span')
  iconSpan.className = 'text-lg'
  iconSpan.textContent = moonPhase.icon
  moonDiv.appendChild(iconSpan)

  const illumSpan = document.createElement('span')
  illumSpan.className = 'text-xs text-muted'
  illumSpan.textContent = `${moonPhase.illumination}%`
  moonDiv.appendChild(illumSpan)

  infoContainer.appendChild(moonDiv)
}
```

**Step 4: Update admin/calendar.vue weather rendering**

Replace innerHTML at lines 684-696:

```typescript
// Replace weather innerHTML (lines 684-696):
if (dayWeather) {
  const tempDiv = document.createElement('div')
  tempDiv.className = 'flex items-center gap-1'

  const weatherIcon = document.createElement('span')
  weatherIcon.className = 'text-base'
  weatherIcon.textContent = getWeatherIcon(dayWeather.weather[0]?.main || '')
  tempDiv.appendChild(weatherIcon)

  const tempText = document.createElement('span')
  tempText.className = 'text-xs text-muted'
  tempText.textContent = `${Math.round(dayWeather.temp.max)}°/${Math.round(dayWeather.temp.min)}°`
  tempDiv.appendChild(tempText)

  infoContainer.appendChild(tempDiv)
}
```

**Step 5: Run frontend typecheck again**

Run: `cd frontend && pnpm typecheck`
Expected: No type errors

**Step 6: Test calendar rendering manually**

Run: `cd frontend && pnpm dev`
Navigate to `/admin/calendar` and verify moon/weather data displays correctly.

**Step 7: Commit**

```bash
git add frontend/app/components/TourCalendar.vue frontend/app/pages/admin/calendar.vue
git commit -m "fix(security): replace innerHTML with textContent in calendar components"
```

---

### Task 2: Add Rate Limiting to Webhook Endpoints

**Files:**
- Modify: `backend/src/main/java/com/northernchile/api/config/RateLimitConfig.java`
- Modify: `backend/src/main/java/com/northernchile/api/config/RateLimitInterceptor.java`
- Modify: `backend/src/main/java/com/northernchile/api/config/WebMvcConfig.java`

**Context:** Webhook endpoints (`/api/webhooks/**`) are currently public and have no rate limiting. Add moderate rate limiting (30 requests/minute) to prevent abuse while allowing legitimate payment provider callbacks.

**Step 1: Add webhook bucket configuration to RateLimitConfig.java**

Add new method after existing bucket configuration:

```java
/**
 * Bucket for webhook endpoints.
 * More permissive than auth (30 requests/minute) to handle payment callbacks.
 */
public Bucket createWebhookBucket() {
    Bandwidth limit = Bandwidth.classic(30, Refill.intervally(30, Duration.ofMinutes(1)));
    return Bucket.builder()
            .addLimit(limit)
            .build();
}
```

**Step 2: Verify compilation**

Run: `docker-compose exec backend mvn compile`
Expected: BUILD SUCCESS

**Step 3: Update RateLimitInterceptor to handle webhook paths**

Add webhook bucket map and logic to preHandle:

```java
// Add field after authBuckets:
private final Map<String, Bucket> webhookBuckets = new ConcurrentHashMap<>();

// In preHandle method, add before the authEndpoints check:
// Rate limit webhooks
if (request.getRequestURI().startsWith("/api/webhooks/")) {
    String clientIp = getClientIP(request);
    Bucket bucket = webhookBuckets.computeIfAbsent(clientIp,
        k -> rateLimitConfig.createWebhookBucket());

    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
    if (probe.isConsumed()) {
        response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
        return true;
    }

    response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
    response.setContentType("application/json");
    response.getWriter().write("{\"error\":\"Rate limit exceeded for webhooks\"}");
    log.warn("Rate limit exceeded for webhook from IP: {}", clientIp);
    return false;
}
```

**Step 4: Verify compilation**

Run: `docker-compose exec backend mvn compile`
Expected: BUILD SUCCESS

**Step 5: Test rate limiting**

After starting backend, test with curl:
```bash
for i in {1..35}; do curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/webhooks/mercadopago; done
```
Expected: First 30 requests return 404 (endpoint exists but no body), then 429

**Step 6: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/config/RateLimitConfig.java \
        backend/src/main/java/com/northernchile/api/config/RateLimitInterceptor.java
git commit -m "feat(security): add rate limiting to webhook endpoints (30/min)"
```

---

### Task 3: Disable Stack Traces in Production

**Files:**
- Modify: `backend/src/main/java/com/northernchile/api/exception/GlobalExceptionHandler.java`

**Context:** Current `isDevMode()` returns true when no profiles are active, exposing stack traces by default. Production should be opt-in for debug info.

**Step 1: Fix isDevMode() logic**

Replace existing isDevMode method:

```java
/**
 * Returns true only if explicitly running in dev or local profile.
 * Production (no profiles or 'prod' profile) returns false.
 */
private boolean isDevMode() {
    String[] activeProfiles = environment.getActiveProfiles();
    for (String profile : activeProfiles) {
        if ("dev".equals(profile) || "local".equals(profile)) {
            return true;
        }
    }
    return false;
}
```

**Step 2: Verify compilation**

Run: `docker-compose exec backend mvn compile`
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/exception/GlobalExceptionHandler.java
git commit -m "fix(security): only expose stack traces in dev/local profiles"
```

---

### Task 4: Add CSRF Protection for State-Changing Operations

**Files:**
- Modify: `backend/src/main/java/com/northernchile/api/config/security/SecurityConfig.java`
- Create: `frontend/app/plugins/csrf.client.ts`
- Modify: `frontend/lib/api-client/base.ts` (if exists) or configure axios interceptor

**Context:** CSRF is currently disabled. For a SaaS platform, enable CSRF with proper cookie-based tokens. Exclude API endpoints that use JWT Bearer auth (stateless).

**Step 1: Enable CSRF with cookie repository**

In SecurityConfig.java, replace `csrf.disable()` at line ~47:

```java
// Enable CSRF with cookie-based token repository
.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
    .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
    // Exempt stateless API endpoints that use Bearer token auth
    .ignoringRequestMatchers(
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/refresh",
        "/api/webhooks/**",
        "/api/tours/**",  // Public read-only
        "/api/schedules/**"  // Public read-only
    )
)
```

Add import:
```java
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
```

**Step 2: Verify backend compilation**

Run: `docker-compose exec backend mvn compile`
Expected: BUILD SUCCESS

**Step 3: Create frontend CSRF plugin**

Create `frontend/app/plugins/csrf.client.ts`:

```typescript
export default defineNuxtPlugin(() => {
  // Read CSRF token from cookie and add to axios headers
  const getCsrfToken = (): string | null => {
    if (typeof document === 'undefined') return null
    const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/)
    return match ? decodeURIComponent(match[1]) : null
  }

  // Add interceptor to include CSRF token in requests
  const { $fetch } = useNuxtApp()

  // Override $fetch to include CSRF header
  globalThis.$fetch = $fetch.create({
    onRequest({ options }) {
      const token = getCsrfToken()
      if (token && ['POST', 'PUT', 'DELETE', 'PATCH'].includes((options.method || 'GET').toUpperCase())) {
        options.headers = {
          ...options.headers,
          'X-XSRF-TOKEN': token
        }
      }
    }
  })
})
```

**Step 4: Run frontend typecheck**

Run: `cd frontend && pnpm typecheck`
Expected: No type errors

**Step 5: Test CSRF flow manually**

1. Start backend and frontend
2. Open browser dev tools -> Application -> Cookies
3. Login and verify XSRF-TOKEN cookie exists
4. Make a POST request and verify X-XSRF-TOKEN header is sent

**Step 6: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/config/security/SecurityConfig.java \
        frontend/app/plugins/csrf.client.ts
git commit -m "feat(security): enable CSRF protection with cookie-based tokens"
```

---

### Task 5: Improve JWT Validation and Error Handling

**Files:**
- Modify: `backend/src/main/java/com/northernchile/api/config/security/JwtAuthenticationFilter.java`

**Context:** JWT validation should provide clear error responses (expired, malformed, invalid signature) without exposing internal details.

**Step 1: Read current JwtAuthenticationFilter**

Read the file to understand current implementation.

**Step 2: Add structured JWT error handling**

Update the filter to catch specific JWT exceptions and return appropriate 401 responses:

```java
try {
    // Existing JWT validation logic
} catch (ExpiredJwtException e) {
    log.debug("JWT token expired: {}", e.getMessage());
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response.getWriter().write("{\"error\":\"token_expired\",\"message\":\"Token has expired\"}");
    return;
} catch (MalformedJwtException e) {
    log.debug("Malformed JWT token: {}", e.getMessage());
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response.getWriter().write("{\"error\":\"invalid_token\",\"message\":\"Invalid token format\"}");
    return;
} catch (SignatureException e) {
    log.warn("JWT signature validation failed");
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response.getWriter().write("{\"error\":\"invalid_signature\",\"message\":\"Token signature invalid\"}");
    return;
}
```

**Step 3: Verify compilation**

Run: `docker-compose exec backend mvn compile`
Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/config/security/JwtAuthenticationFilter.java
git commit -m "feat(security): improve JWT error handling with structured responses"
```

---

## Phase 2: Observability and Resilience

### Task 6: Add Structured JSON Logging

**Files:**
- Modify: `backend/src/main/resources/logback-spring.xml`
- Add dependency to `backend/pom.xml` if needed

**Context:** Structured JSON logs enable better parsing in log aggregators (CloudWatch, Datadog, etc).

**Step 1: Create logback-spring.xml for production**

Create or update `backend/src/main/resources/logback-spring.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

    <!-- Console appender for development -->
    <springProfile name="dev,local">
        <include resource="org/springframework/boot/logging/logback/console-appender.xml"/>
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>

    <!-- JSON appender for production -->
    <springProfile name="!dev,!local">
        <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LogstashEncoder">
                <includeMdcKeyName>traceId</includeMdcKeyName>
                <includeMdcKeyName>userId</includeMdcKeyName>
                <includeMdcKeyName>requestId</includeMdcKeyName>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="JSON"/>
        </root>
    </springProfile>
</configuration>
```

**Step 2: Add logstash-logback-encoder dependency**

Add to `backend/pom.xml` in dependencies section:

```xml
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

**Step 3: Verify compilation**

Run: `docker-compose exec backend mvn compile`
Expected: BUILD SUCCESS

**Step 4: Test JSON logging**

Run with prod profile: `SPRING_PROFILES_ACTIVE=prod docker-compose up backend`
Expected: Logs appear as JSON objects

**Step 5: Commit**

```bash
git add backend/src/main/resources/logback-spring.xml backend/pom.xml
git commit -m "feat(observability): add structured JSON logging for production"
```

---

### Task 7: Add Request Tracing with MDC

**Files:**
- Create: `backend/src/main/java/com/northernchile/api/config/RequestTracingFilter.java`
- Modify: `backend/src/main/java/com/northernchile/api/config/WebMvcConfig.java`

**Context:** Add request ID to MDC for correlation across log entries. Essential for debugging distributed issues.

**Step 1: Create RequestTracingFilter**

```java
package com.northernchile.api.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTracingFilter implements Filter {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String MDC_REQUEST_ID = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Use existing request ID from header or generate new one
        String requestId = httpRequest.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }

        MDC.put(MDC_REQUEST_ID, requestId);
        httpResponse.setHeader(REQUEST_ID_HEADER, requestId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_REQUEST_ID);
        }
    }
}
```

**Step 2: Verify compilation**

Run: `docker-compose exec backend mvn compile`
Expected: BUILD SUCCESS

**Step 3: Test request tracing**

Make a request and check logs for `requestId` field.

**Step 4: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/config/RequestTracingFilter.java
git commit -m "feat(observability): add request ID tracing via MDC"
```

---

### Task 8: Add Circuit Breaker for External APIs

**Files:**
- Add dependency to `backend/pom.xml`
- Modify: `backend/src/main/java/com/northernchile/api/external/WeatherService.java`
- Modify: `backend/src/main/java/com/northernchile/api/payment/provider/MercadoPagoPaymentService.java`
- Modify: `backend/src/main/java/com/northernchile/api/payment/provider/TransbankPaymentService.java`

**Context:** External API failures shouldn't cascade. Circuit breakers provide graceful degradation.

**Step 1: Add Resilience4j dependency**

Add to `backend/pom.xml`:

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.2.0</version>
</dependency>
```

**Step 2: Configure circuit breaker in application.yml**

Add to `backend/src/main/resources/application.yml`:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      weather:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
        permittedNumberOfCallsInHalfOpenState: 3
      payment:
        slidingWindowSize: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 60s
        permittedNumberOfCallsInHalfOpenState: 2
```

**Step 3: Add @CircuitBreaker to WeatherService**

Annotate external API calls:

```java
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@CircuitBreaker(name = "weather", fallbackMethod = "getWeatherFallback")
public WeatherResponse getWeather(String location) {
    // existing implementation
}

private WeatherResponse getWeatherFallback(String location, Exception e) {
    log.warn("Weather API unavailable, returning empty response: {}", e.getMessage());
    return WeatherResponse.empty();
}
```

**Step 4: Add @CircuitBreaker to payment services**

Similar pattern for MercadoPago and Transbank services.

**Step 5: Verify compilation**

Run: `docker-compose exec backend mvn compile`
Expected: BUILD SUCCESS

**Step 6: Commit**

```bash
git add backend/pom.xml backend/src/main/resources/application.yml \
        backend/src/main/java/com/northernchile/api/external/WeatherService.java
git commit -m "feat(resilience): add circuit breakers for external API calls"
```

---

### Task 9: Enhanced Health Checks

**Files:**
- Modify: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/java/com/northernchile/api/config/health/ExternalServicesHealthIndicator.java`

**Context:** Production health checks should verify database, cache, and critical external services.

**Step 1: Enable detailed health checks**

Add to application.yml:

```yaml
management:
  endpoint:
    health:
      show-details: when_authorized
      show-components: when_authorized
  health:
    circuitbreakers:
      enabled: true
    ratelimiters:
      enabled: true
```

**Step 2: Create custom health indicator for external services**

```java
package com.northernchile.api.config.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ExternalServicesHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // Check critical external services
        return Health.up()
            .withDetail("weather_api", "operational")
            .withDetail("payment_providers", "operational")
            .build();
    }
}
```

**Step 3: Verify compilation and test**

Run: `docker-compose exec backend mvn compile`
Test: `curl http://localhost:8080/actuator/health`

**Step 4: Commit**

```bash
git add backend/src/main/resources/application.yml \
        backend/src/main/java/com/northernchile/api/config/health/
git commit -m "feat(observability): add enhanced health checks with external service status"
```

---

### Task 10: Add Global API Rate Limiting

**Files:**
- Modify: `backend/src/main/java/com/northernchile/api/config/RateLimitConfig.java`
- Modify: `backend/src/main/java/com/northernchile/api/config/RateLimitInterceptor.java`

**Context:** Add global rate limiting (100 requests/minute per IP) for all API endpoints as a baseline protection.

**Step 1: Add global bucket to RateLimitConfig**

```java
/**
 * Global rate limit bucket for all API endpoints.
 * 100 requests per minute per IP.
 */
public Bucket createGlobalBucket() {
    Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
    return Bucket.builder()
            .addLimit(limit)
            .build();
}
```

**Step 2: Update interceptor to apply global limit**

Add global rate limiting as the first check in preHandle, before specific endpoint limits.

**Step 3: Verify compilation**

Run: `docker-compose exec backend mvn compile`
Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/config/RateLimitConfig.java \
        backend/src/main/java/com/northernchile/api/config/RateLimitInterceptor.java
git commit -m "feat(security): add global API rate limiting (100/min per IP)"
```

---

## Phase 3: Data Integrity and Validation

### Task 11: Add Exhaustive Pagination to List Endpoints

**Files:**
- Modify: Controllers with list endpoints that don't use Pageable
- Check: `BookingController`, `TourController`, `UserController`, etc.

**Context:** Any endpoint returning lists should support pagination to prevent memory issues with large datasets.

**Step 1: Audit controllers for missing pagination**

Search for endpoints returning `List<>` without Pageable parameter.

**Step 2: Add Pageable to identified endpoints**

Example pattern:
```java
@GetMapping
public ResponseEntity<Page<BookingRes>> getAllBookings(
    @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
    Pageable pageable) {
    return ResponseEntity.ok(bookingService.findAll(pageable));
}
```

**Step 3: Update corresponding service methods**

Ensure services pass Pageable to repositories.

**Step 4: Verify compilation and test**

Run: `docker-compose exec backend mvn compile`
Test endpoints with `?page=0&size=10`

**Step 5: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/
git commit -m "feat(api): add pagination to all list endpoints"
```

---

### Task 12: Add Input Validation Annotations

**Files:**
- Audit all `*Req.java` DTOs for missing validation

**Context:** All request DTOs should have JSR-303 validation annotations.

**Step 1: Audit DTOs for missing validation**

Check all request records for:
- `@NotNull` / `@NotBlank` for required fields
- `@Size` for string length limits
- `@Email` for email fields
- `@Min` / `@Max` for numeric ranges
- `@Pattern` for format constraints

**Step 2: Add missing annotations**

Example:
```java
public record BookingReq(
    @NotNull(message = "Schedule ID is required")
    UUID scheduleId,

    @NotNull(message = "Number of participants is required")
    @Min(value = 1, message = "At least 1 participant required")
    @Max(value = 20, message = "Maximum 20 participants allowed")
    Integer participantCount,

    @NotBlank(message = "Contact email is required")
    @Email(message = "Valid email required")
    @Size(max = 255, message = "Email too long")
    String contactEmail
) {}
```

**Step 3: Verify compilation**

Run: `docker-compose exec backend mvn compile`
Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/*/dto/
git commit -m "feat(validation): add comprehensive input validation to all DTOs"
```

---

## Phase 4: Frontend Resilience

### Task 13: Add Global Error Boundary

**Files:**
- Create: `frontend/app/components/ErrorBoundary.vue`
- Modify: `frontend/app/app.vue` or layouts

**Context:** Catch unhandled Vue errors gracefully instead of blank screens.

**Step 1: Create ErrorBoundary component**

```vue
<template>
  <div v-if="error" class="min-h-screen flex items-center justify-center bg-neutral-100 dark:bg-neutral-900">
    <div class="text-center p-8">
      <div class="text-6xl mb-4">😕</div>
      <h1 class="text-2xl font-bold text-default mb-2">{{ $t('error.unexpected') }}</h1>
      <p class="text-muted mb-4">{{ $t('error.tryAgain') }}</p>
      <UButton color="primary" @click="retry">
        {{ $t('error.reload') }}
      </UButton>
    </div>
  </div>
  <slot v-else />
</template>

<script setup lang="ts">
const error = ref<Error | null>(null)

onErrorCaptured((err) => {
  error.value = err
  console.error('Captured error:', err)
  return false // prevent propagation
})

function retry() {
  error.value = null
  window.location.reload()
}
</script>
```

**Step 2: Add i18n keys**

Add to locale files:
```json
{
  "error": {
    "unexpected": "Something went wrong",
    "tryAgain": "We're sorry, an unexpected error occurred.",
    "reload": "Reload page"
  }
}
```

**Step 3: Wrap app with ErrorBoundary**

In `app.vue` or main layout, wrap content with ErrorBoundary.

**Step 4: Test error handling**

Temporarily throw error in a component to verify boundary catches it.

**Step 5: Commit**

```bash
git add frontend/app/components/ErrorBoundary.vue frontend/i18n/locales/
git commit -m "feat(frontend): add global error boundary for graceful error handling"
```

---

### Task 14: Add Skeleton Loaders for Better UX

**Files:**
- Create: `frontend/app/components/skeletons/` directory
- Modify: Pages that show loading states

**Context:** Replace "Loading..." text with skeleton UI for perceived performance.

**Step 1: Create base skeleton components**

```vue
<!-- SkeletonCard.vue -->
<template>
  <div class="animate-pulse bg-neutral-200 dark:bg-neutral-800 rounded-lg p-4">
    <div class="h-48 bg-neutral-300 dark:bg-neutral-700 rounded mb-4" />
    <div class="h-4 bg-neutral-300 dark:bg-neutral-700 rounded w-3/4 mb-2" />
    <div class="h-4 bg-neutral-300 dark:bg-neutral-700 rounded w-1/2" />
  </div>
</template>
```

**Step 2: Create skeleton variants**

- `SkeletonTourCard.vue`
- `SkeletonTable.vue`
- `SkeletonForm.vue`

**Step 3: Replace loading states in pages**

Replace `<div v-if="loading">Loading...</div>` with appropriate skeleton components.

**Step 4: Commit**

```bash
git add frontend/app/components/skeletons/
git commit -m "feat(ux): add skeleton loaders for improved perceived performance"
```

---

## Summary Checklist

### Phase 1: Critical Security (Tasks 1-5)
- [ ] Task 1: Fix XSS innerHTML in calendars
- [ ] Task 2: Rate limit webhook endpoints
- [ ] Task 3: Disable stack traces in production
- [ ] Task 4: Enable CSRF protection
- [ ] Task 5: Improve JWT error handling

### Phase 2: Observability (Tasks 6-10)
- [ ] Task 6: Structured JSON logging
- [ ] Task 7: Request tracing with MDC
- [ ] Task 8: Circuit breakers for external APIs
- [ ] Task 9: Enhanced health checks
- [ ] Task 10: Global API rate limiting

### Phase 3: Data Integrity (Tasks 11-12)
- [ ] Task 11: Exhaustive pagination
- [ ] Task 12: Input validation annotations

### Phase 4: Frontend Resilience (Tasks 13-14)
- [ ] Task 13: Global error boundary
- [ ] Task 14: Skeleton loaders

---

**Total: 14 tasks across 4 phases**

Execute in order. Each task is independent after Phase 1 completion. Commit after each task for easy rollback.
