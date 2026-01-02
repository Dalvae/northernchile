# Auditoría de Código - Issues Encontrados

**Fecha:** 2026-01-01

---

## 🔴 CRÍTICOS (Deben arreglarse)

### 1. Credenciales Transbank hardcodeadas como defaults
**Archivo:** `backend/src/main/java/com/northernchile/api/payment/RefundService.java:43-49`

```java
@Value("${transbank.commerce-code:597055555532}")
private String transbankCommerceCode;

@Value("${transbank.api-key:579B532A7440BB0C9079DED94D31EA1615BACEB56610332264630D42D0A36B1C}")
private String transbankApiKey;
```

**Problema:** Si las credenciales no están configuradas en producción, la app usa silenciosamente keys de integración/test.

**Solución:** Quitar defaults y fallar si no están configuradas.

---

### 2. Catch de NullPointerException para control de flujo
**Archivo:** `backend/src/main/java/com/northernchile/api/payment/PaymentService.java:123-133`

```java
try {
    Instant tourStart = payment.getBooking().getSchedule().getStartDatetime();
    long hoursUntilTour = ChronoUnit.HOURS.between(Instant.now(), tourStart);
} catch (NullPointerException e) {
    throw new IllegalStateException("Cannot process refund - tour schedule information is missing", e);
}
```

**Problema:** Mala práctica - oculta dónde está el null real en la cadena.

**Solución:** Usar null checks explícitos u Optional.

---

### 3. Deduplicación de webhooks en memoria
**Archivo:** `backend/src/main/java/com/northernchile/api/payment/WebhookSecurityService.java:42-44`

```java
private final Map<String, Instant> processedRequests = new ConcurrentHashMap<>();
```

**Problema:** Si hay múltiples instancias del backend, el mismo webhook puede procesarse múltiples veces causando:
- Bookings duplicados
- Emails duplicados
- Estado inconsistente

**Solución:** Implementar deduplicación en base de datos con tabla `processed_webhooks`.

---

### 4. Errores TypeScript en página de alertas
**Archivo:** `frontend/app/pages/admin/alerts.vue`

**Problema:** 20+ errores TypeScript - faltan propiedades en tipo `AlertWithDetails`:
- `id`, `severity`, `alertType`, `status`, `windSpeed`, `cloudCoverage`, `createdAt`, `resolvedAt`, `resolution`

**Solución:** Definir interface correcta que coincida con respuesta del backend.

---

### 5. Memory leak - onUnmounted dentro de onMounted
**Archivo:** `frontend/app/pages/tours/[slug]/index.vue:227-235`

```typescript
onMounted(async () => {
  window.addEventListener('scroll', handleScroll)
  onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll)
  })
})
```

**Problema:** `onUnmounted` no debe estar dentro de `onMounted`.

**Solución:** Mover `onUnmounted` fuera de `onMounted`.

---

## 🟠 MEDIOS (Deberían arreglarse)

### 6. Falta validación de status en booking
**Archivo:** `backend/src/main/java/com/northernchile/api/booking/BookingService.java:292-311`

**Problema:** `newStatus` no valida que sea un valor válido (PENDING, CONFIRMED, COMPLETED, CANCELLED) antes de usarse.

**Solución:** Agregar validación de valores permitidos.

---

### 7. Método deprecated sigue usando patrón viejo
**Archivo:** `backend/src/main/java/com/northernchile/api/media/MediaService.java:231-262`

**Problema:** `createMedia` deprecated usa `media.setTour(tour)` directamente en vez de join tables.

**Solución:** Eliminar método o actualizar para usar join tables.

---

### 8. SUPER_ADMIN no puede buscar media globalmente
**Archivo:** `backend/src/main/java/com/northernchile/api/media/MediaService.java:319-331`

```java
if (isSuperAdmin) {
    // TODO: Need a repository method for global search
    mediaPage = mediaRepository.searchByFilename(requesterId, search, pageable);
} else {
    mediaPage = mediaRepository.searchByFilename(requesterId, search, pageable);
}
```

**Problema:** Ambas ramas hacen lo mismo - SUPER_ADMIN no puede ver media de otros usuarios.

**Solución:** Implementar método de búsqueda global en repository.

---

### 9. SecurityException en vez de AccessDeniedException
**Archivo:** `backend/src/main/java/com/northernchile/api/booking/BookingService.java:371-373`

```java
throw new SecurityException("You can only update your own bookings");
```

**Problema:** Spring Security espera `AccessDeniedException` para errores de autorización.

**Solución:** Cambiar a `new AccessDeniedException(...)`.

---

### 10. Imports rotos en frontend
**Archivos:**
- `app/components/profile/BookingsList.vue:2`
- `app/components/profile/EditBookingModal.vue:2`
- `app/layouts/admin.vue:2`
- `app/pages/admin/alerts.vue:439`
- `app/pages/admin/calendar.vue:272`

**Problema:** Importan de `~/lib/api-client/api` que no existe.

**Solución:** Importar desde `api-client`.

---

### 11. Polling sin backoff
**Archivo:** `frontend/app/stores/payment.ts:71-86`

```typescript
startPolling(paymentId: string, intervalMs: number = 5000) {
  this.pollingInterval = setInterval(async () => { ... }, intervalMs)
}
```

**Problema:** Polling fijo cada 5s sin backoff - puede sobrecargar servidor en errores.

**Solución:** Implementar exponential backoff en errores.

---

### 12. Strings hardcodeados sin i18n
**Archivo:** `frontend/app/pages/tours/[slug]/index.vue`

Strings en español sin traducción:
- Línea 499: "Horas"
- Línea 506: "Max {n} personas"
- Línea 513: "Desde $"
- Línea 621: "Galería"
- Línea 690: "Reserva tu Experiencia"
- Línea 700: "Fase Lunar (Hoy)"
- Línea 710: "Clima (Hoy)"

**Solución:** Usar `t()` con keys de traducción.

---

### 13. Console.log en producción
**Archivos:** 93 ocurrencias en:
- `app/stores/cart.ts:69,130,172`
- `app/stores/auth.ts:95,136`
- `app/pages/checkout.vue:299,306,496`
- Y muchos más...

**Problema:** Exponen detalles internos en consola del browser.

**Solución:** Eliminar o usar servicio de logging apropiado.

---

### 14. Credenciales Transbank en docker-compose
**Archivo:** `docker-compose.yml:65-66`

**Problema:** Credenciales de integración como defaults.

**Solución:** Usar defaults vacíos y requerir configuración explícita.

---

### 15. CORS origins hardcodeados
**Archivo:** `backend/src/main/java/com/northernchile/api/config/SecurityConfig.java:118-121`

**Problema:** Orígenes CORS hardcodeados en código.

**Solución:** Mover a variable de entorno para flexibilidad en producción.

---

### 16. Falta NOTIFICATION_ADMIN_EMAIL en .env.example
**Archivo:** `.env.example`

**Problema:** Variable usada en `application.properties:165` pero no documentada.

**Solución:** Agregar a `.env.example`.

---

### 17. innerHTML con contenido dinámico
**Archivos:**
- `app/components/TourCalendar.vue:428,438`
- `app/pages/admin/calendar.vue:688,699`

**Problema:** Potencial vector XSS si datos son controlados por usuario.

**Solución:** Validar que datos vienen de APIs confiables o sanitizar.

---

### 18. Error handling inconsistente en auth store
**Archivo:** `frontend/app/stores/auth.ts:58-62`

```typescript
} catch (error) {
  throw error  // Re-throws sin manejar
}
```

**Problema:** Errores suben sin contexto adicional.

**Solución:** Transformar errores antes de re-throw.

---

## 🟡 MENORES (Nice to fix)

### 19. Timezone hardcodeado en múltiples lugares
**Archivos:**
- `BookingService.java:132`
- `PaymentSessionService.java:355`
- `ReportsService.java:37`

**Problema:** `ZoneId.of("America/Santiago")` repetido.

**Solución:** Extraer a constante compartida o property de configuración.

---

### 20. Magic numbers en lógica de negocio
**Archivo:** `backend/src/main/java/com/northernchile/api/cart/CartService.java:69,132,152`

```java
cart.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
```

**Solución:** Extraer a `@Value("${cart.expiration.hours:1}")`.

---

### 21. Mensajes de error en idiomas mezclados
**Archivos varios:**
- `UserService.java:154`: "La contraseña actual es incorrecta." (español)
- `BookingService.java`: "You can only update your own bookings" (inglés)

**Solución:** Usar i18n consistentemente.

---

### 22. 'unsafe-eval' en CSP
**Archivo:** `frontend/nuxt.config.ts:101,113`

**Problema:** CSP permite `'unsafe-eval'` - revisar si es necesario.

**Solución:** Eliminar si no es estrictamente necesario.

---

### 23. Sin timeouts en API proxy
**Archivo:** `frontend/server/utils/apiProxy.ts:30,58,84`

**Problema:** Requests pueden quedar colgados indefinidamente.

**Solución:** Agregar opción `timeout` a llamadas `$fetch`.

---

### 24. Race condition en cleanup de carts
**Archivo:** `backend/src/main/java/com/northernchile/api/cart/CartService.java:230-243`

```java
if (!cartRepository.existsExpiredCarts(now)) {
    return;
}
int deletedCount = cartRepository.deleteByExpiresAtBefore(now);
```

**Problema:** Check antes de delete agrega complejidad sin beneficio real.

**Solución:** Ejecutar delete directamente - es un job programado cada 15 minutos.

---

### 25. Errores de tipo en DateInput
**Archivo:** `frontend/app/components/ui/DateInput.vue:10-94`

**Problema:** `CalendarDate | null` no asignable a `DateValue | undefined`.

**Solución:** Agregar type guards y null coalescing.

---

### 26. Variables no usadas
**Archivos:**
- `app/pages/tours/[slug]/index.vue:12` - `_localePath`
- `app/components/TourCalendar.vue:55` - `_router`

**Solución:** Eliminar o usar.

---

### 27. Código comentado
**Archivo:** `app/composables/useS3Upload.ts:167-172`

**Problema:** `onUploadProgress` comentado.

**Solución:** Eliminar o implementar.

---

## Resumen

| Severidad | Cantidad |
|-----------|----------|
| Críticos | 5 |
| Medios | 13 |
| Menores | 9 |
| **Total** | **27** |
