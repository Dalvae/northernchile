¡Excelente iniciativa! Hacer un "barrido" completo manual antes de cualquier lanzamiento es crucial. He estructurado esta guía para que sea lógica y eficiente, agrupando pruebas por **Flujos de Usuario (User Journeys)**. Así no tienes que saltar de un lado a otro sin sentido.

Copia este contenido en un archivo `MANUAL_TESTING_CHECKLIST.md` y ve marcando las casillas.

---

# 🧪 Guía Maestra de Pruebas Manuales - Northern Chile

**Entorno:** Staging / Local (Docker)
**Configuración Requerida:** `PAYMENT_TEST_MODE=true`, `MAIL_ENABLED=false` (o true si revisas bandeja de entrada real).

---

## 1. 🛒 Flujo Principal: De Invitado a Viajero Confirmado (Happy Path)

_Objetivo: Verificar que un usuario nuevo puede encontrar un tour, reservar, registrarse durante el checkout y pagar exitosamente._

### A. Selección y Carrito

- [ ] **Navegación Home:** Entrar a la home, verificar que carguen las imágenes "Hero" y los tours destacados.
- [ ] **Filtros de Tours:** Ir a `/tours`. Filtrar por categoría "Astronómico". Verificar que la lista cambie.
- [ ] **Detalle de Tour:** Entrar al "Tour Astronómico".
  - [ ] Verificar que carga la info de fase lunar y clima (simulada o real).
  - [ ] Revisar galería de fotos.
- [ ] **Calendario:** Bajar al calendario de disponibilidad.
  - [ ] Verificar que los días pasados están deshabilitados.
  - [ ] Seleccionar una fecha futura disponible.
- [ ] **Agregar al Carrito:** Seleccionar 2 participantes y dar click en "Reservar".
  - [ ] Verificar redirección al Carrito o mensaje "Agregado".
  - [ ] Verificar que el ícono del carrito en el header muestra "1".

### B. Checkout y Registro

- [ ] **Ir a Checkout:** Desde el carrito, dar click en "Proceder al Pago".
- [ ] **Paso 1 (Contacto):** Llenar formulario con un **email nuevo** (no registrado).
  - [ ] Llenar contraseña.
  - [ ] Verificar que al avanzar, el sistema crea la cuenta automáticamente (revisar logs backend o network tab: `POST /auth/register`).
- [ ] **Paso 2 (Participantes):** Llenar datos.
  - [ ] Probar botón "Copiar mis datos" (debe llenar al participante 1).
  - [ ] Dejar un campo obligatorio vacío e intentar avanzar (debe mostrar error).
- [ ] **Paso 3 (Pago - Transbank):**
  - [ ] Seleccionar "Webpay Plus".
  - [ ] Confirmar monto total (debe incluir IVA).
  - [ ] Click en Pagar -> Redirección a Transbank (pantalla de prueba).

### C. Pago Exitoso (Transbank)

- [ ] **Pasarela:** Usar tarjeta de prueba ÉXITO:
  - **Tarjeta:** `4051 8856 0044 6623` | **Fecha:** Futura | **CVV:** 123
  - **RUT:** `11.111.111-1` | **Pass:** `123`
- [ ] **Retorno:** Verificar redirección automática a `/payment/callback`.
- [ ] **Confirmación:**
  - [ ] Ver mensaje de éxito "¡Pago Exitoso!".
  - [ ] Ver ID de reserva.
  - [ ] **Backend:** Verificar en BD que `bookings.status = 'CONFIRMED'`.
  - [ ] **Backend:** Verificar en BD que `payments.status = 'COMPLETED'`.
  - [ ] **Email:** Verificar envío de correo de confirmación (en logs o inbox).

---

## 2. 💳 Flujos de Pago Alternativos y Errores

_Objetivo: Asegurar que el sistema maneja fallos de pago y métodos alternativos sin romper la experiencia._

### A. Transbank Rechazado/Cancelado

- [ ] **Iniciar nueva reserva.**
- [ ] En pasarela Transbank, usar botón **"Anular compra"** o tarjeta de FALLO:
  - **Tarjeta:** `5186 0595 5959 0568`
- [ ] **Resultado:**
  - [ ] Redirección a `/payment/callback` con error.
  - [ ] Mensaje claro: "Pago cancelado" o "Rechazado".
  - [ ] Botón "Intentar nuevamente" visible.
  - [ ] **BD:** La reserva debe seguir en `PENDING` (no confirmada).

### B. Mercado Pago - PIX (Brasil)

- [ ] **Cambiar idioma** a Portugués (PT).
- [ ] Iniciar reserva.
- [ ] En Checkout, seleccionar **PIX**.
- [ ] **Resultado:**
  - [ ] Modal o pantalla con Código QR y código "Copia e Cola".
  - [ ] Verificar temporizador de expiración (30 min).
- [ ] **Simulación (Backend):**
  - Como no puedes escanear el QR de prueba real, simula el Webhook.
  - Usar Postman/Curl a `/api/webhooks/mercadopago` con el ID del pago generado.
  - **Resultado:** El frontend debería actualizarse solo (polling) o al recargar, mostrando "Confirmado".

---

## 3. 👮 Panel de Administración (Backoffice)

_Objetivo: Verificar que el staff puede gestionar el negocio._

### A. Dashboard y Métricas

- [ ] Login como `admin@northernchile.com`.
- [ ] **Dashboard:** Verificar que el contador de "Reservas Totales" subió tras las pruebas anteriores.
- [ ] Verificar gráfico de ingresos.

### B. Gestión de Tours (CRUD)

- [ ] **Crear Tour:**
  - [ ] Llenar datos básicos (Nombre, Precio, Cupos).
  - [ ] Subir una imagen (probar drag & drop).
  - [ ] Marcar "Sensible a la Luna".
  - [ ] Guardar como "Borrador".
- [ ] **Verificar Público:** Ir a la web pública (incógnito) -> El tour **NO** debe aparecer.
- [ ] **Publicar:** Editar tour -> Cambiar a "Publicado" -> Guardar.
- [ ] **Verificar Público:** El tour **SÍ** debe aparecer.

### C. Gestión de Schedules (Calendario)

- [ ] Ir a "Calendario".
- [ ] **Generación:** Click en "Generar Schedules" (si está disponible manual) o crear uno manual en una fecha vacía.
- [ ] **Bloqueo:** Seleccionar un schedule con reservas.
  - [ ] Cambiar estado a "CANCELLED".
  - [ ] Verificar alerta de "Hay X pasajeros afectados".

### D. Gestión de Reservas y Reembolsos

- [ ] Ir a "Reservas".
- [ ] Buscar la reserva creada en el Punto 1.
- [ ] Ver detalles (modal).
- [ ] **Reembolso Parcial:** (Si está implementado el botón) o cambio de estado manual.
  - [ ] Cambiar estado a "CANCELLED".
  - [ ] Verificar que se dispare el email de cancelación (logs).

---

## 4. 👤 Perfil de Usuario (Mi Cuenta)

_Objetivo: Que el cliente pueda autogestionarse._

- [ ] Login con el usuario creado en el Punto 1.
- [ ] Ir a "Mis Reservas".
  - [ ] Verificar que aparece la reserva confirmada.
  - [ ] Verificar botón "Descargar PDF/Voucher".
- [ ] Ir a "Perfil".
  - [ ] Cambiar teléfono o nacionalidad.
  - [ ] Guardar y recargar página para verificar persistencia.

---

## 5. 🌟 Tours Privados (Cotizaciones)

_Objetivo: Probar el flujo asíncrono de solicitud -> cotización._

- [ ] **Cliente:** Ir a `/private-tours`.
  - [ ] Llenar formulario de solicitud ("Quiero celebrar un cumpleaños").
  - [ ] Enviar.
- [ ] **Admin:**
  - [ ] Ir a Panel Admin -> "Tours Privados".
  - [ ] Verificar que aparece la nueva solicitud "Pendiente".
  - [ ] Click en "Cotizar" -> Ingresar monto y mensaje.
  - [ ] Guardar (Cambia estado a "Cotizado").
- [ ] **Cliente (Simulado):**
  - [ ] Verificar recepción de email con cotización (logs).
  - [ ] (Opcional si está implementado) Click en enlace de pago del correo.

---

## 6. 🌍 Internacionalización y UX

_Objetivo: Verificar que no se rompa el diseño o las traducciones._

- [ ] **Cambio de Idioma:**
  - [ ] Cambiar a Inglés (EN).
  - [ ] Verificar textos de la Home y del Checkout.
  - [ ] Verificar formato de moneda (debería seguir siendo CLP o cambiar a USD según tu lógica, revisar `useCurrency`).
- [ ] **Responsive (Móvil):**
  - [ ] Abrir herramientas de desarrollo (F12) -> Vista móvil (iPhone 12/14).
  - [ ] Abrir menú hamburguesa.
  - [ ] Verificar que el calendario no se desborda horizontalmente.
  - [ ] Verificar que el botón de "Pagar" en checkout es accesible.
- [ ] **Tema Oscuro/Claro:**
  - [ ] Cambiar tema. Verificar que los textos sean legibles (contraste).

---

## 7. 🛡️ Pruebas de Seguridad Básicas

_Objetivo: Sanidad básica._

- [ ] **Rutas Protegidas:**
  - [ ] Cerrar sesión.
  - [ ] Intentar entrar directo a `/admin/dashboard`.
  - [ ] Debe redirigir a `/auth/login`.
- [ ] **Acceso a Datos Ajenos:**
  - [ ] Loguearse como "Usuario A".
  - [ ] Intentar ver detalle de reserva de "Usuario B" (si tienes IDs a mano en la URL). Debe dar 403/404.

---

## ✅ Checklist Final de Validación

Antes de dar por terminada la sesión de pruebas:

- [ ] **Logs de Backend:** ¿Hubo algún "Exception" o "Error" grave en la consola de Java mientras hacías las pruebas?
- [ ] **Base de Datos:** ¿Los registros en `audit_logs` se están creando correctamente?
- [ ] **Limpieza:** Si usaste datos "basura", recuerda borrarlos o resetear la BD antes de la siguiente fase si es necesario.

---

### 💡 Tip Pro para Pruebas Manuales

Abre la consola del navegador (F12 -> Console) y la pestaña Network mientras pruebas. Si algo falla visualmente, el error rojo ahí te dirá exactamente qué pasó (ej: CORS, 500 Internal Server Error, etc.).
