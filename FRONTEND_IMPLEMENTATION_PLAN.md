# Plan de Implementación Frontend - Northern Chile

## Objetivo
Completar la implementación del frontend (admin + público) siguiendo las mejores prácticas de Nuxt 3 y Nuxt UI v4.

---

## FASE 1: ADMIN PANEL COMPLETO

### 1.1 Layout y Navegación Admin ✅ PARCIAL
**Estado actual:** Existe pero usa componentes desactualizados

**Tareas:**
- [ ] Actualizar `layouts/admin.vue` para usar Nuxt UI Dashboard components
  - Migrar a `UDashboardGroup` + `UDashboardSidebar` + `UDashboardNavbar`
  - Implementar sidebar colapsable y responsive
  - Agregar breadcrumbs con `UBreadcrumb`
- [ ] Agregar dropdown de usuario con opciones:
  - Ver perfil
  - Configuración
  - Cerrar sesión
- [ ] Implementar indicador de rol (SUPER_ADMIN vs PARTNER_ADMIN)
- [ ] Agregar color mode toggle en navbar

**Componentes Nuxt UI necesarios:**
- `UDashboardGroup`
- `UDashboardSidebar` 
- `UDashboardNavbar`
- `UBreadcrumb`
- `UDropdownMenu`
- `UColorModeButton`

---

### 1.2 Dashboard (index.vue) ✅ PARCIAL
**Estado actual:** Muestra stats básicas, falta funcionalidad

**Tareas:**
- [ ] Implementar carga real de estadísticas desde API
  - Total de reservas (hoy, semana, mes)
  - Ingresos (hoy, semana, mes)
  - Tours activos
  - Alertas pendientes
- [ ] Agregar gráficos con vue-chartjs o similar:
  - Reservas por día (últimos 30 días)
  - Ingresos por tour
  - Ocupación promedio
- [ ] Tabla de reservas recientes (últimas 10)
- [ ] Lista de alertas climáticas pendientes
- [ ] Filtros por rango de fecha
- [ ] Comparación con período anterior (% cambio)

**API Endpoints:**
- `GET /api/admin/stats/bookings`
- `GET /api/admin/stats/revenue`
- `GET /api/admin/bookings?limit=10&sort=createdAt`
- `GET /api/admin/alerts?status=PENDING`

**Componentes:**
- `UCard` para stats
- `UTable` para reservas recientes
- `UAlert` para notificaciones
- Chart component (decidir librería)

---

### 1.3 Gestión de Tours (tours.vue) ✅ PARCIAL
**Estado actual:** Lista tours, modal de creación/edición funcional

**Mejoras pendientes:**
- [ ] Agregar filtros avanzados:
  - Por categoría (ASTRONOMICAL, REGULAR, etc.)
  - Por estado (DRAFT, PUBLISHED, ARCHIVED)
  - Por sensibilidad climática
  - Búsqueda por nombre
- [ ] Implementar acciones en masa:
  - Publicar/despublicar múltiples tours
  - Archivar múltiples tours
  - Exportar selección a CSV
- [ ] Agregar vista de detalle (drawer o página separada)
  - Ver todos los schedules del tour
  - Ver estadísticas de reservas
  - Ver ingresos generados
- [ ] Implementar duplicar tour
- [ ] Agregar paginación
- [ ] Agregar ordenamiento por columnas

**API Endpoints existentes:**
- `GET /api/admin/tours`
- `POST /api/admin/tours`
- `PUT /api/admin/tours/{id}`
- `DELETE /api/admin/tours/{id}`
- `POST /api/admin/tours/{id}/duplicate` (verificar si existe)

---

### 1.4 Calendario de Schedules (calendar.vue) ✅ PARCIAL
**Estado actual:** FullCalendar implementado, muestra eventos básicos

**Mejoras pendientes:**
- [ ] Implementar modal de creación de schedule:
  - Seleccionar tour
  - Fecha y hora
  - Precio custom (opcional)
  - Max participantes custom (opcional)
  - Guía asignado
  - Notas internas
- [ ] Implementar modal de edición de schedule:
  - Cancelar schedule
  - Cambiar guía
  - Modificar capacidad
  - Ver reservas del schedule
- [ ] Agregar vista de lista alternativa al calendario
- [ ] Filtros por:
  - Tour específico
  - Estado (OPEN, CLOSED, CANCELLED)
  - Rango de fechas
  - Alertas climáticas
- [ ] Indicadores visuales mejorados:
  - Color por tipo de tour
  - Icono de alerta si tiene condiciones adversas
  - Mostrar cupos disponibles en el evento
- [ ] Exportar calendario a ICS

**API Endpoints:**
- `GET /api/admin/schedules/calendar?start=X&end=Y`
- `POST /api/admin/schedules`
- `PUT /api/admin/schedules/{id}`
- `DELETE /api/admin/schedules/{id}`
- `POST /api/admin/schedules/generate` ✅ (ya existe)

---

### 1.5 Gestión de Reservas (bookings.vue) ❌ IMPLEMENTAR
**Estado actual:** Solo estructura vacía

**Implementación completa:**
- [ ] Tabla de reservas con columnas:
  - ID / Referencia
  - Fecha de creación
  - Cliente (nombre + email)
  - Tour + Fecha del schedule
  - N° participantes (adultos/niños)
  - Total pagado
  - Estado (PENDING, CONFIRMED, CANCELLED, COMPLETED)
  - Método de pago
  - Acciones
- [ ] Filtros:
  - Por estado
  - Por tour
  - Por rango de fechas
  - Por cliente (búsqueda)
  - Por método de pago
- [ ] Búsqueda global
- [ ] Modal de detalle de reserva:
  - Información del cliente
  - Lista de participantes
  - Historial de pagos
  - Historial de cambios
  - Opciones de cancelación
  - Enviar email de confirmación
  - Descargar comprobante PDF
- [ ] Acciones:
  - Confirmar reserva (si PENDING)
  - Cancelar reserva
  - Modificar reserva
  - Reenviar confirmación
  - Procesar reembolso
- [ ] Exportar a CSV/Excel
- [ ] Paginación

**API Endpoints:**
- `GET /api/admin/bookings`
- `GET /api/admin/bookings/{id}`
- `PUT /api/admin/bookings/{id}`
- `POST /api/admin/bookings/{id}/cancel`
- `POST /api/admin/bookings/{id}/confirm`
- `POST /api/admin/bookings/{id}/refund`
- `POST /api/admin/bookings/{id}/resend-confirmation`

**Componentes:**
- `UTable` con columnas definidas
- `UModal` para detalle
- `UBadge` para estados
- `UInput` para búsqueda
- `USelectMenu` para filtros
- `UPagination`

---

### 1.6 Gestión de Usuarios (users.vue) ❌ IMPLEMENTAR
**Estado actual:** Solo estructura vacía

**Implementación completa:**
- [ ] Tabla de usuarios con columnas:
  - Avatar
  - Nombre completo
  - Email
  - Rol (SUPER_ADMIN, PARTNER_ADMIN, CLIENT)
  - Provider (LOCAL, GOOGLE)
  - Fecha de registro
  - Estado (ACTIVE, SUSPENDED)
  - Acciones
- [ ] Filtros:
  - Por rol
  - Por provider
  - Por estado
  - Búsqueda por nombre/email
- [ ] Modal de creación de usuario admin:
  - Email
  - Nombre completo
  - Password (solo para LOCAL)
  - Rol
  - Enviar email de bienvenida
- [ ] Modal de edición:
  - Cambiar rol
  - Suspender/activar cuenta
  - Resetear contraseña (enviar email)
  - Ver historial de reservas del usuario
  - Ver tours creados (si es PARTNER_ADMIN)
- [ ] Estadísticas por usuario (para PARTNER_ADMIN):
  - Total de tours creados
  - Total de reservas
  - Ingresos generados
- [ ] Exportar lista de usuarios

**API Endpoints:**
- `GET /api/admin/users`
- `GET /api/admin/users/{id}`
- `POST /api/admin/users`
- `PUT /api/admin/users/{id}`
- `PUT /api/admin/users/{id}/role`
- `PUT /api/admin/users/{id}/suspend`
- `POST /api/admin/users/{id}/reset-password`

**Restricciones:**
- PARTNER_ADMIN solo ve sus propias reservas y tours
- SUPER_ADMIN ve todo

---

### 1.7 Gestión de Alertas Climáticas ❌ NUEVO
**Nueva página:** `/admin/alerts`

**Implementación:**
- [ ] Crear página `pages/admin/alerts.vue`
- [ ] Tabla de alertas con:
  - Fecha del schedule afectado
  - Tour
  - Tipo de alerta (WIND, CLOUDS, RAIN, FULL_MOON)
  - Severidad (INFO, WARNING, CRITICAL)
  - Estado (PENDING, ACKNOWLEDGED, RESOLVED)
  - Acciones
- [ ] Filtros:
  - Por tipo
  - Por severidad
  - Por estado
  - Por rango de fechas
- [ ] Acciones:
  - Marcar como reconocida
  - Cancelar schedule asociado
  - Notificar a clientes
  - Resolver alerta
- [ ] Sistema de notificaciones push (futuro)

**API Endpoints:**
- `GET /api/admin/alerts`
- `PUT /api/admin/alerts/{id}/acknowledge`
- `PUT /api/admin/alerts/{id}/resolve`
- `POST /api/admin/alerts/{id}/notify-clients`

---

### 1.8 Solicitudes de Tours Privados ❌ NUEVO
**Nueva página:** `/admin/private-requests`

**Implementación:**
- [ ] Crear página `pages/admin/private-requests.vue`
- [ ] Tabla de solicitudes:
  - ID
  - Cliente (nombre + email)
  - Teléfono
  - Fecha preferida
  - N° personas
  - Presupuesto estimado
  - Descripción
  - Estado (NEW, CONTACTED, QUOTED, CONFIRMED, REJECTED)
  - Fecha de solicitud
  - Acciones
- [ ] Modal de detalle:
  - Información completa del cliente
  - Historial de comunicación
  - Agregar notas internas
  - Cambiar estado
  - Enviar cotización
  - Convertir a reserva
- [ ] Filtros por estado
- [ ] Responder desde el panel (enviar email)

**API Endpoints:**
- `GET /api/admin/private-tour-requests`
- `GET /api/admin/private-tour-requests/{id}`
- `PUT /api/admin/private-tour-requests/{id}/status`
- `POST /api/admin/private-tour-requests/{id}/quote`
- `POST /api/admin/private-tour-requests/{id}/convert-to-booking`

---

### 1.9 Reportes y Análisis ❌ NUEVO
**Nueva página:** `/admin/reports`

**Implementación:**
- [ ] Crear página `pages/admin/reports.vue`
- [ ] Secciones:
  
  **A. Reporte de Ventas:**
  - Gráfico de ingresos por período
  - Comparación con períodos anteriores
  - Desglose por tour
  - Desglose por método de pago
  - Exportar a PDF/Excel
  
  **B. Reporte de Ocupación:**
  - % ocupación promedio
  - Tours con mejor ocupación
  - Tours con peor ocupación
  - Días/horarios más populares
  
  **C. Reporte de Clientes:**
  - Nuevos clientes por período
  - Clientes recurrentes
  - Origen geográfico (si se captura)
  - Satisfacción (si hay encuestas)
  
  **D. Reporte de Cancelaciones:**
  - Tasa de cancelación
  - Motivos principales
  - Tours más cancelados
  - Impacto climático

- [ ] Filtros por rango de fechas
- [ ] Comparación de períodos
- [ ] Exportación a múltiples formatos

**API Endpoints:**
- `GET /api/admin/reports/sales`
- `GET /api/admin/reports/occupancy`
- `GET /api/admin/reports/clients`
- `GET /api/admin/reports/cancellations`

---

### 1.10 Configuración del Sistema ❌ NUEVO
**Nueva página:** `/admin/settings`

**Implementación:**
- [ ] Crear página `pages/admin/settings.vue`
- [ ] Tabs/secciones:
  
  **A. Configuración General:**
  - Nombre de la empresa
  - Email de contacto
  - Teléfonos
  - Dirección
  - Redes sociales
  - Logo (upload)
  
  **B. Configuración de Reservas:**
  - Horas mínimas para cancelación gratuita
  - Porcentaje de anticipo requerido
  - Tiempo máximo para confirmar pago
  - Email automático de recordatorio (días antes)
  
  **C. Configuración de Pagos:**
  - Métodos habilitados
  - Credenciales de pasarelas (encriptadas)
  - Comisiones por método
  
  **D. Configuración de Notificaciones:**
  - Email de administrador para alertas
  - Plantillas de emails
  - Notificaciones push
  
  **E. Configuración Climática:**
  - API key de WeatherAPI
  - Umbrales de alerta (viento, nubes, lluvia)
  - Umbral de luna llena

- [ ] Validación de formularios
- [ ] Encriptación de datos sensibles
- [ ] Logs de cambios

**API Endpoints:**
- `GET /api/admin/settings`
- `PUT /api/admin/settings`
- `POST /api/admin/settings/logo`
- `PUT /api/admin/settings/email-templates`

---

## FASE 2: FRONTEND PÚBLICO

### 2.1 Homepage (index.vue) ❌ IMPLEMENTAR
**Secciones:**
- [ ] Hero section con imagen del desierto de Atacama
  - Título principal
  - Subtítulo
  - CTA "Ver Tours" / "Reservar Ahora"
  - Video de fondo (opcional)
- [ ] Tours destacados (carrousel o grid)
  - 4-6 tours más populares
  - Imagen, título, precio, duración
  - CTA por tour
- [ ] ¿Por qué elegirnos?
  - Experiencia
  - Guías certificados
  - Equipamiento profesional
  - Cancelación flexible
- [ ] Galería de fotos
- [ ] Testimonios de clientes
- [ ] Call to action final
- [ ] Footer con links

**Componentes Nuxt UI:**
- `UPageHero`
- `UPageFeature`
- `UCarousel`
- `UCard`
- `UButton`

---

### 2.2 Listado de Tours (tours/index.vue) ❌ IMPLEMENTAR
**Implementación:**
- [ ] Grid de tours con cards
  - Imagen principal
  - Categoría badge
  - Título
  - Descripción corta
  - Duración
  - Precio desde
  - Rating (si existe)
  - Botón "Ver detalles"
- [ ] Sidebar con filtros:
  - Por categoría
  - Por duración
  - Por precio (rango)
  - Por fecha disponible
  - Por características (moon-sensitive, etc.)
- [ ] Ordenamiento:
  - Más populares
  - Menor precio
  - Mayor precio
  - Más recientes
- [ ] Búsqueda
- [ ] Paginación
- [ ] Vista lista/grid toggle

**API Endpoints:**
- `GET /api/tours?status=PUBLISHED`
- `GET /api/tours?category=X&minPrice=Y&maxPrice=Z`

---

### 2.3 Detalle de Tour (tours/[id].vue) ❌ IMPLEMENTAR
**Implementación:**
- [ ] Galería de imágenes (carousel principal + thumbnails)
- [ ] Información del tour:
  - Título
  - Categoría
  - Rating y reviews
  - Descripción completa
  - Qué incluye
  - Qué NO incluye
  - Duración
  - Nivel de dificultad
  - Idiomas disponibles
  - Punto de encuentro
- [ ] Calendario de disponibilidad:
  - Mostrar próximos 60 días
  - Indicar fechas disponibles
  - Precio por fecha (si varía)
  - Click en fecha para reservar
- [ ] Sección de precios:
  - Precio adulto
  - Precio niño (si aplica)
  - Descuentos por grupo (si aplica)
- [ ] Política de cancelación
- [ ] FAQ del tour
- [ ] Reviews de clientes
- [ ] Tours relacionados
- [ ] Botón flotante de "Reservar Ahora"

**Componentes:**
- `UCarousel` para galería
- `UCalendar` o custom date picker
- `UCard` para información
- `UAccordion` para FAQ
- `UBadge` para categorías
- `UButton` sticky para reserva

---

### 2.4 Proceso de Reserva (booking/) ❌ IMPLEMENTAR

**Estructura de páginas:**
```
/booking/
  [tourId]/
    [scheduleId]/
      index.vue          # Step 1: Seleccionar participantes
      participants.vue   # Step 2: Datos de participantes
      payment.vue        # Step 3: Pago
      confirmation.vue   # Step 4: Confirmación
```

**Step 1: Seleccionar participantes**
- [ ] Resumen del tour y fecha seleccionada
- [ ] Selector de cantidad de adultos
- [ ] Selector de cantidad de niños (si aplica)
- [ ] Precio total calculado dinámicamente
- [ ] Verificar disponibilidad en tiempo real
- [ ] Botón "Continuar"

**Step 2: Datos de participantes**
- [ ] Formulario para cada participante:
  - Nombre completo
  - Documento de identidad
  - Nacionalidad
  - Edad (si es niño)
  - Requisitos especiales (alergias, movilidad, etc.)
- [ ] Datos de contacto del responsable:
  - Email
  - Teléfono
  - País
- [ ] Checkbox de términos y condiciones
- [ ] Botón "Ir a pago"

**Step 3: Pago**
- [ ] Resumen de la reserva
- [ ] Seleccionar método de pago:
  - Tarjeta (Transbank/Stripe)
  - Transferencia bancaria
  - PIX (MercadoPago Brasil)
  - PayPal
- [ ] Formulario según método
- [ ] Aplicar cupón de descuento (si aplica)
- [ ] Botón "Pagar"
- [ ] Indicador de procesamiento

**Step 4: Confirmación**
- [ ] Mensaje de éxito
- [ ] Número de reserva
- [ ] Resumen completo
- [ ] Botón para descargar comprobante PDF
- [ ] Email de confirmación enviado
- [ ] Opciones:
  - Ver mi reserva
  - Reservar otro tour
  - Volver al inicio

**API Endpoints:**
- `POST /api/bookings` (crear reserva)
- `GET /api/schedules/{id}/availability`
- `POST /api/payments/process`
- `GET /api/bookings/{id}/confirmation`

---

### 2.5 Perfil de Usuario (profile/) ❌ IMPLEMENTAR

**Páginas:**
- `/profile` - Información personal
- `/profile/bookings` - Mis reservas
- `/profile/settings` - Configuración

**profile/index.vue:**
- [ ] Ver y editar información:
  - Nombre completo
  - Email (no editable si es Google)
  - Teléfono
  - País
  - Preferencias de idioma
- [ ] Cambiar contraseña (solo LOCAL)
- [ ] Eliminar cuenta

**profile/bookings.vue:**
- [ ] Lista de reservas del usuario:
  - Futuras
  - Pasadas
  - Canceladas
- [ ] Filtros por estado
- [ ] Card por reserva:
  - Tour
  - Fecha
  - Participantes
  - Estado
  - Acciones (ver detalle, cancelar, descargar comprobante)
- [ ] Modal de detalle de reserva
- [ ] Cancelación con política aplicada

**API Endpoints:**
- `GET /api/profile`
- `PUT /api/profile`
- `PUT /api/profile/password`
- `DELETE /api/profile`
- `GET /api/profile/bookings`
- `POST /api/bookings/{id}/cancel`

---

### 2.6 Carrito de Compras (cart) ❌ IMPLEMENTAR

**Implementación:**
- [ ] Botón de carrito en header con badge de cantidad
- [ ] Drawer o página `/cart` con:
  - Lista de items agregados
  - Tour, fecha, participantes
  - Subtotal por item
  - Eliminar item
  - Modificar cantidades
  - Total general
  - Aplicar cupón
  - Botón "Proceder al pago"
- [ ] Persistencia del carrito (localStorage + API)
- [ ] Sincronización entre sesiones
- [ ] Expiración de items (si el schedule se llena)

**API Endpoints:**
- `GET /api/cart`
- `POST /api/cart/items`
- `PUT /api/cart/items/{id}`
- `DELETE /api/cart/items/{id}`
- `POST /api/cart/apply-coupon`

---

### 2.7 Otras Páginas Públicas

**Contacto (contact.vue):**
- [ ] Formulario de contacto
- [ ] Información de contacto
- [ ] Mapa de ubicación
- [ ] Horarios de atención

**About (about.vue):**
- [ ] Historia de la empresa
- [ ] Misión y visión
- [ ] Equipo
- [ ] Certificaciones

**FAQ (faq.vue):**
- [ ] Preguntas frecuentes organizadas por categoría
- [ ] Accordion para cada pregunta

**Terms (terms.vue):**
- [ ] Términos y condiciones
- [ ] Política de privacidad
- [ ] Política de cancelación

---

## FASE 3: MEJORAS TRANSVERSALES

### 3.1 Internacionalización (i18n)
- [ ] Completar traducciones en todos los idiomas (ES, EN, PT)
- [ ] Agregar selector de idioma en header
- [ ] Persistir preferencia de idioma
- [ ] SEO para cada idioma (hreflang)
- [ ] Formateo de fechas por locale
- [ ] Formateo de moneda por locale

### 3.2 SEO y Performance
- [ ] Meta tags dinámicos por página
- [ ] Open Graph tags
- [ ] Twitter Card tags
- [ ] Structured data (JSON-LD)
- [ ] Sitemap XML
- [ ] robots.txt
- [ ] Lazy loading de imágenes
- [ ] Optimización de imágenes (Nuxt Image)
- [ ] Code splitting
- [ ] Critical CSS

### 3.3 Autenticación y Autorización
- [ ] Completar flujos de auth:
  - Login
  - Registro
  - Recuperar contraseña
  - Google OAuth
  - Email de verificación
- [ ] Middleware de autenticación
- [ ] Middleware de autorización por rol
- [ ] Refresh token automático
- [ ] Logout en todas las pestañas

### 3.4 Notificaciones y Feedback
- [ ] Sistema de toast notifications con UNotifications
- [ ] Mensajes de error consistentes
- [ ] Loading states en todas las acciones async
- [ ] Confirmaciones para acciones destructivas
- [ ] Notificaciones push (futuro)

### 3.5 Accesibilidad
- [ ] Navegación por teclado
- [ ] ARIA labels
- [ ] Contraste de colores (WCAG AA)
- [ ] Focus visible
- [ ] Screen reader support

### 3.6 Testing
- [ ] Unit tests con Vitest
- [ ] Component tests
- [ ] E2E tests con Playwright
- [ ] Visual regression tests

---

## STACK TECNOLÓGICO CONFIRMADO

### Core
- **Nuxt**: 3.x (latest)
- **Vue**: 3.x con Composition API
- **TypeScript**: Strict mode

### UI y Styling
- **Nuxt UI**: v4 (latest)
- **Tailwind CSS**: 3.x
- **Nuxt Icon**: Para iconos de Iconify
- **Nuxt Fonts**: Para optimización de fuentes
- **Nuxt Color Mode**: Para dark/light mode

### Estado y Datos
- **Pinia**: Para state management
- **@nuxtjs/i18n**: Para internacionalización
- **VueUse**: Utilidades de composables

### Formularios y Validación
- **Zod**: Para validación de schemas
- **@nuxt/ui Forms**: Formularios con validación integrada

### Calendario y Fechas
- **FullCalendar**: Para calendario admin (ya implementado)
- **@internationalized/date**: Para manejo de fechas (Nuxt UI Calendar)

### Gráficos (para reportes)
- **Chart.js** + **vue-chartjs**: O
- **Apache ECharts** + **vue-echarts**: (mejor para dashboards complejos)

### Peticiones HTTP
- **$fetch**: Nativo de Nuxt
- **useFetch**: Para SSR
- **Axios client**: Generado desde OpenAPI

### Desarrollo
- **ESLint**: Con @nuxt/eslint
- **Prettier**: Para formateo
- **TypeScript**: Con Nuxt type checking

---

## ORDEN DE IMPLEMENTACIÓN RECOMENDADO

### Sprint 1 (Admin Core)
1. Layout admin con Dashboard components
2. Dashboard con stats reales
3. Tours - mejoras y filtros
4. Calendario - modals de creación/edición

### Sprint 2 (Admin Gestión)
5. Gestión de Reservas completa
6. Gestión de Usuarios completa
7. Alertas Climáticas
8. Solicitudes de Tours Privados

### Sprint 3 (Admin Avanzado)
9. Reportes y Análisis
10. Configuración del Sistema

### Sprint 4 (Frontend Público Core)
11. Homepage
12. Listado de Tours
13. Detalle de Tour
14. Sistema de Autenticación

### Sprint 5 (Frontend Booking)
15. Proceso de Reserva completo (4 steps)
16. Carrito de Compras
17. Perfil de Usuario

### Sprint 6 (Frontend Páginas)
18. Contacto, About, FAQ, Terms
19. Completar i18n
20. SEO y performance

### Sprint 7 (Polish & Testing)
21. Accesibilidad
22. Tests
23. Bug fixes
24. Optimizaciones finales

---

## CONVENCIONES DE CÓDIGO

### Componentes
```vue
<script setup lang="ts">
// Imports
import { ref, computed } from 'vue'
import type { TourRes } from '~/lib/api-client'

// Props
const props = defineProps<{
  tour: TourRes
}>()

// Emits
const emit = defineEmits<{
  select: [id: string]
}>()

// State
const isLoading = ref(false)

// Computed
const formattedPrice = computed(() => {
  return new Intl.NumberFormat('es-CL', {
    style: 'currency',
    currency: 'CLP'
  }).format(props.tour.priceAdult)
})

// Methods
async function handleSelect() {
  emit('select', props.tour.id)
}
</script>

<template>
  <UCard>
    <!-- Template -->
  </UCard>
</template>
```

### Composables
- Usar prefijo `use`
- Retornar objetos con funciones y estado
- Tipado completo

### API Calls
- Usar `useFetch` para SSR
- Usar `$fetch` para CSR
- Manejo de errores consistente
- Loading states

### Colores
- **SIEMPRE usar colores semánticos** (ver THEMING.md)
- Nunca hardcodear colores como `red`, `blue`, `gray`
- Usar `primary`, `secondary`, `tertiary`, `success`, `error`, `warning`, `info`, `neutral`

### Componentes Nuxt UI
- Usar `#content` slot para modales y drawers
- Usar `v-model:open` para controlar apertura
- No usar `UCard` dentro de modales

---

## RECURSOS Y DOCUMENTACIÓN

### Oficial
- Nuxt 3: https://nuxt.com/docs
- Nuxt UI: https://ui.nuxt.com/
- Vue 3: https://vuejs.org/
- Tailwind: https://tailwindcss.com/

### MCP Server
- Usar el MCP de Nuxt UI para consultar docs actualizadas
- Comandos disponibles:
  - `mcp__nuxt-ui-remote__get_component`
  - `mcp__nuxt-ui-remote__list_components`
  - `mcp__nuxt-ui-remote__get_documentation_page`

### Internas
- `/CLAUDE.md` - Arquitectura y convenciones
- `/frontend/THEMING.md` - Guía de colores
- `/frontend/NUXT_UI_UPDATES.md` - Cambios recientes

---

## NOTAS FINALES

- **Prioridad**: Completar admin antes que frontend público
- **Calidad sobre velocidad**: Mejor implementar bien que rápido
- **Testing**: Agregar tests progresivamente, no al final
- **Documentación**: Actualizar docs conforme se avanza
- **Commits**: Commits atómicos y descriptivos en español
- **Code review**: Revisar cambios antes de merge

---

**Versión:** 1.0  
**Fecha:** 2025-10-30  
**Autor:** Claude Code con revisión de documentación actualizada
