# Lista de Tareas Priorizada - Northern Chile

Este documento organiza las tareas pendientes en fases, desde lo más crítico para el lanzamiento hasta las mejoras finales.

---

## Fase 1: Flujo Crítico del Cliente (Prioridad Máxima) 🚀
**Objetivo: Permitir que un usuario complete una reserva de principio a fin con un pago simulado.**

- [x] **Finalizar el Flujo de Reserva (`frontend/app/pages/tours/[id]/book.vue`):**
    - [x] Implementar la lógica `handleMockPayment` en el paso de pago.
    - [x] Realizar la llamada a `POST /api/bookings` para crear la reserva con estado `PENDING`.
    - [x] Inmediatamente después, llamar al nuevo endpoint de confirmación simulada.
    - [x] Al tener éxito, redirigir al usuario al paso final de confirmación.

- [x] **Crear el Endpoint de Simulación de Pago (Backend):**
    - [x] **Controller:** Añadir `POST /api/bookings/{bookingId}/confirm-mock` en `BookingController.java`.
    - [x] **Service:** Crear el método `confirmBookingAfterMockPayment` en `BookingService.java`.
    - [x] **Lógica de Servicio:**
        - [x] Verificar que el usuario que confirma es el dueño de la reserva.
        - [x] Cambiar el estado de la reserva de `PENDING` a `CONFIRMED`.
        - [x] Registrar la acción en el `AuditLog`.

---

## Fase 2: Experiencia Post-Reserva y Carrito (Prioridad Alta) 🛒
**Objetivo: Ofrecer funcionalidades estándar de e-commerce como un carrito persistente y un portal de usuario.**

- [x] **Conectar el Carrito de Compras al Backend (`frontend/app/stores/cart.ts`):**
    - [x] Reescribir el store de Pinia para que utilice `$fetch` y se comunique con la API del backend (`/api/cart`).
    - [x] Dejar de usar un array local y basar el estado del carrito en la respuesta de la API.
    - [x] Asegurar que el estado persista entre recargas de página gracias a la cookie `cartId` gestionada por el backend.

- [x] **Construir el "Portal del Viajero" (Frontend):**
    - [x] Crear la página `pages/profile/bookings.vue` para listar las reservas del usuario.
    - [x] Crear la página `pages/profile/index.vue` para que los usuarios puedan ver y editar su información personal.

---

## Fase 3: Empoderamiento del Administrador (Prioridad Media) 🛠️
**Objetivo: Dar a los administradores las herramientas para gestionar sus operaciones diarias.**

- [x] **Completar la Gestión de Horarios en el Calendario (`frontend/app/pages/admin/calendar.vue`):**
    - [x] Implementar el formulario dentro del `UModal` para la creación y edición de `TourSchedules`.
    - [x] Conectar el formulario a los endpoints `POST` y `PUT` de `/api/admin/schedules`.
    - [x] Añadir validación para los campos del formulario.

- [x] **Crear la Interfaz de Alertas Climáticas (Frontend):**
    - [x] Crear la página `frontend/app/pages/admin/alerts.vue` para listar y gestionar las alertas.
    - [x] Implementar un modal para que el admin pueda "Resolver" una alerta (marcar como `KEPT`, `CANCELLED`, `RESCHEDULED`).
    - [x] Añadir un widget/badge en el `layouts/admin.vue` que muestre el conteo de alertas pendientes obtenido de `GET /api/admin/alerts/count`.

---

## Fase 4: Completitud y Pulido (Prioridad Baja) ✨
**Objetivo: Finalizar funcionalidades secundarias y refinar la experiencia general.**

- [x] **Implementar Visor de Auditoría (Frontend):**
    - [x] Crear la página `frontend/app/pages/admin/audit-logs.vue` para que el `SUPER_ADMIN` pueda consultar el historial de cambios.
    - [x] Implementar filtros por acción, tipo de entidad y email de usuario.
    - [x] Añadir paginación con navegación completa.
    - [x] Mostrar estadísticas de acciones (CREATE, UPDATE, DELETE).
    - [x] Visualización detallada de valores antiguos y nuevos.

- [x] **Crear Páginas Informativas Públicas (Frontend):**
    - [x] Construir la página del calendario lunar (`/moon-calendar`) consumiendo la API `GET /api/lunar/calendar`.
    - [x] Mostrar próximas lunas llenas.
    - [x] Navegación por meses con diseño de calendario.
    - [x] Leyenda de fases lunares con emojis.

- [x] **Refinamiento General y Corrección de Bugs:**
    - [x] Completar todas las traducciones del calendario lunar en ES/EN/PT.
    - [x] Consolidar archivos de traducción en `i18n/locales/`.
    - [x] Desactivar toggle de dark mode y fijar tema único en light mode.
    - [x] Remover UColorModeButton del layout de admin.
    - [ ] Añadir paginación en las tablas de administración que puedan tener muchos datos (Reservas, Usuarios) - OPCIONAL.

---

## Mejoras Técnicas Completadas ✅

- [x] **Manejo Robusto de Precios:**
    - [x] Migración de base de datos: Todos los campos monetarios ahora usan `NUMERIC(19,4)`.
    - [x] Backend: Entidades actualizadas con `BigDecimal` y precisión correcta.
    - [x] Backend: Configuración de Jackson para serialización de BigDecimal como plain strings.
    - [x] Frontend: Composable `useCurrency` implementado con `Intl.NumberFormat`.
    - [x] Frontend: Todos los componentes actualizados para usar el composable centralizado.

- [x] **Funcionalidad de Cancelación de Reservas:**
    - [x] Temporalmente deshabilitada hasta integración de pasarelas de pago.
