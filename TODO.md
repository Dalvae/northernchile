# TODO - Northern Chile Platform

**Última Actualización:** 26 de Noviembre, 2025

---

## 🔴 CRÍTICO - BLOQUEA PRODUCCIÓN (11 issues)

### Backend (5 issues)

- [ ] **DataInitializer.java:50** - Remover logging de contraseñas en texto plano
- [ ] **PrivateTourRequestController** - Agregar @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PARTNER_ADMIN')") a endpoints admin
- [ ] **StorageController** - Agregar @PreAuthorize y validación de path traversal (líneas 31-108)
- [ ] **BookingSecurityService & TourSecurityService** - Reemplazar `.orElse(null)` con `.orElseThrow()` para evitar NPE
- [ ] **WebhookController.java:135** - Remover logging de payload completo de webhooks

### Frontend (6 issues)

- [ ] **Remover authStore.token** - 8 archivos afectados (payment.ts, useAdminData.ts, checkout.vue, etc.)
- [ ] **Remover localStorage.getItem('auth_token')** - 3 archivos (useS3Upload.ts, useCalendarData.ts, private-requests.vue)
- [ ] **Migrar Cart Store a cookies** - cart.ts:148,160
- [ ] **Mejorar CSP** - nuxt.config.ts - Remover unsafe-inline
- [ ] **Implementar Contact Form backend** - contact.vue:80-104 - Crear POST /api/contact
- [ ] **Fix hardcoded colors** - OgImage/Tour.vue - Usar semantic colors

---

## 🟠 ALTA PRIORIDAD (34 issues)

### Backend

- [ ] **Agregar @Valid a 8 endpoints** (WeatherAlertController, TourScheduleAdminController, SystemSettingsController, etc.)
- [ ] **DTOs sin validaciones** - CartItemReq, UserUpdateReq, MediaUpdateReq
- [ ] **N+1 Queries** - TourService.getPublishedTours(), PrivateTourRequestController, UserService
- [ ] **TODOs críticos** - SystemSettingsController:79, AuthService:74, EmailService:115,123, TourCreateReq:165
- [ ] **Índices de BD** - Crear V4__add_indexes.sql con 6 índices

### Frontend

- [ ] **Console.log en producción** - admin/media/[slug]/index.vue (5 statements)
- [ ] **TODO sin implementar** - admin/index.vue:146 (click handler)
- [ ] **Migrar useLocalStorage** - Cambiar a useCookie()
- [ ] **Validación Zod** - Contact form, private tours form
- [ ] **Textos sin i18n** - checkout.vue, otros componentes

---

## 🟡 MEDIA PRIORIDAD

### Backend

- [ ] Duplicación de código - getMoonIcon() en LunarController y CalendarDataController
- [ ] Lógica en controllers - Mover a service layer
- [ ] Error handling incompleto - Mejorar GlobalExceptionHandler
- [ ] Configuración hardcoded - Timezone, constantes
- [ ] Transacciones faltantes - DataInitializer, AuthService.login

### Frontend

- [ ] Loading states inconsistentes
- [ ] Mensajes de error genéricos
- [ ] Performance issues - Lazy loading

---

## 📝 NOTAS

### Issues Resueltos (ya NO aparecen en TODO)

✅ **Password en toString()** - LoginReq, RegisterReq, User - RESUELTO
✅ **@Valid en AuthController** - login, register - RESUELTO
✅ **Payment token en logs** - PaymentService - RESUELTO
✅ **RateLimitInterceptor** - Implementado y registrado - RESUELTO

### Media Management - Future Optimizations (LOW PRIORITY)

**Gallery Pagination:**
- **Estado Actual:** Endpoints retornan todas las fotos sin paginación
- **Funciona bien:** Para galerías típicas (5-50 fotos)
- **Decisión:** Monitorear en producción, implementar solo si hay slowness

**TourModal Rendering (FIXED):**
- **Problema:** UTabs slots no renderizaban en primer load
- **Solución:** Usar v-show en lugar de slots
- **Estado:** ✅ Funcionando con workaround

---

## 📊 Próximos Pasos

1. **Semana 1:** Implementar fixes críticos (16-24 horas)
2. **Semana 2:** Alta prioridad (12-16 horas)
3. **Semana 3:** Testing exhaustivo
4. **Semana 4:** Deploy a producción

Ver **AUDITORIA_COMPLETA_2025.md** para detalles completos.
