# Lista TODO Robusta para la Aplicación Northern Chile

**I. Frontend UI/UX & Características (Prioridad Alta)**

1.  **Completar Funcionalidad del Calendario (`frontend/app/pages/admin/calendar.vue`):**
    *   [ ] Implementar el formulario dentro del modal para crear/editar schedules. Esto implica:
        *   [ ] Crear DTOs `TourScheduleCreateReq` y `TourScheduleRes` (si no están ya en frontend).
        *   [ ] Conectar el formulario a los endpoints de la API de backend (`POST /api/admin/schedules`, `PATCH /api/admin/schedules/{id}`).
        *   [ ] Implementar validación para los campos del formulario.
    *   [ ] Implementar funcionalidad de arrastrar y soltar (drag & drop) para schedules en FullCalendar.
    *   [ ] Asegurar que todos los indicadores visuales (fases lunares, alertas climáticas) estén completamente integrados y se muestren correctamente.

2.  **Implementar Frontend para Alertas Climáticas:**
    *   [ ] **Dashboard de Alertas** (`frontend/app/pages/admin/alerts.vue`):
        *   [ ] Crear la página para mostrar una lista de alertas pendientes/resueltas.
        *   [ ] Implementar filtrado por tipo, severidad y fecha.
        *   [ ] Desarrollar un modal para resolver alertas (CANCELLED, KEPT, RESCHEDULED).
    *   [ ] **Widget de Alertas** (Componente global):
        *   [ ] Crear un badge en la barra de navegación mostrando el conteo de alertas pendientes.
        *   [ ] Implementar un menú desplegable (dropdown) mostrando las últimas 5 alertas.
        *   [ ] Añadir un enlace al dashboard completo de alertas.

3.  **Implementar Calendario Lunar Público (`frontend/app/pages/moon-calendar.vue`):**
    *   [ ] Crear una nueva página para mostrar una vista de mes completo con fases lunares, iluminación y nombres de fase.
    *   [ ] Integrar con la API de backend `LunarController` (`GET /api/lunar/calendar`).

4.  **Completar Traducciones Faltantes:**
    *   [ ] Revisar todos los componentes y páginas de frontend para asegurar que todo el texto visible para el usuario esté correctamente internacionalizado usando `useI18n`.
    *   [ ] Añadir cualquier clave/valor faltante a `frontend/locales/en.json`, `es.json`, `pt.json`.

5.  **Corregir Problema de Gestión de Temas/Modo Claro y Oscuro:**
    *   [ ] Investigar la configuración actual de temas y Nuxt UI para resolver el problema del modo claro/oscuro.

**II. Backend Mejoras & Verificación (Prioridad Media)**

1.  **Verificar Métodos de `TourScheduleAdminController`:**
    *   [ ] Confirmar que los métodos `create`, `update` y `delete` en `TourScheduleAdminController` (y su lógica de capa de servicio correspondiente) estén completamente implementados y funcionales.
2.  **Crear DTOs de `TourSchedule`:**
    *   [ ] Asegurar que los DTOs `TourScheduleCreateReq` y `TourScheduleRes` estén correctamente definidos y se utilicen para las interacciones de la API.
3.  **Implementar Filtro `owner_id` para `PARTNER_ADMIN`:**
    *   [ ] Asegurar que todos los endpoints de la API relevantes filtren correctamente los datos por `owner_id` cuando sean accedidos por un `PARTNER_ADMIN` para aplicar la segregación de datos.
4.  **Añadir Contador de Bookings a `TourScheduleRes`:**
    *   [ ] Modificar `TourScheduleRes` para incluir un conteo de las reservas actuales para un schedule dado.

**III. Integraciones (Prioridad Baja)**

1.  **Integrar Proveedores de Pago:**
    *   [ ] Transbank (Webpay Plus/REST) para el mercado chileno.
    *   [ ] Mercado Pago (API v2 con PIX) para el mercado brasileño.
    *   [ ] Stripe para pagos internacionales.
2.  **Integrar Servicios de Correo:**
    *   [ ] Implementar correos electrónicos automatizados para confirmación de reserva, recordatorios y notas de agradecimiento.
    *   [ ] Asegurar soporte multilingüe para los correos electrónicos.