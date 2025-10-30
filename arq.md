# **Documento Arquitectónico Completo: Plataforma "Northern Chile"**

## **1. Visión Estratégica y Propuesta de Valor**

### **1.1. Visión General**

**Northern Chile** no será solo una agencia de tours, se posicionará como el curador de experiencias transformadoras en San Pedro de Atacama. Nuestra plataforma digital será el corazón de esta visión, un portal que combina el rigor científico con la magia ancestral del desierto más árido del mundo, llevando al cliente desde la inspiración inicial hasta un recuerdo que perdurará toda la vida.

### **1.2. Propuesta de Valor Dual: Ciencia + Tradición Andina**

- **Excelencia Científica:** Aprovechamos la posición de Chile, que alberga el 77% de la infraestructura astronómica mundial, para ofrecer una experiencia basada en el conocimiento y la precisión.
- **Conexión Cultural:** No solo miramos las estrellas; integramos la Cosmovisión Andina, revelando cómo las culturas ancestrales interpretaban el mismo cielo que hoy exploramos con tecnología de vanguardia.
- **Garantía de Calidad:** Operamos bajo los estándares de la Norma Lumínica chilena, asegurando los cielos más prístinos del planeta para una observación incomparable.

### **1.3. Sostenibilidad y Responsabilidad Social**

- **Cuidado de los Cielos:** Operamos bajo y promovemos la **Norma Lumínica DS 43**. Educamos a nuestros clientes sobre su importancia.
- **Cero Plástico de Un Solo Uso:** En todos nuestros tours, proporcionamos agua en jarras o termos rellenables.
- **Apoyo a las Comunidades Locales:** Trabajamos con guías y proveedores atacameños. Parte de nuestras ganancias se reinvierte en un fondo para la preservación del patrimonio cultural local.
- **Sendero Sin Huella (Leave No Trace):** Nuestros guías están entrenados para inculcar este principio en todos los paseos, recogiendo toda la basura.

---

## **2. Arquitectura Técnica y Stack Tecnológico**

### **2.1. Arquitectura General**

La plataforma se compone de tres componentes principales que operan de forma independiente pero coordinada:

| Componente        | Tecnología Principal         | Responsabilidades                                                                                                  |
| :---------------- | :--------------------------- | :----------------------------------------------------------------------------------------------------------------- |
| **Frontend**      | **Nuxt 3 (Vue 3)**           | Renderizado de la interfaz, gestión del estado de la UI, interacción con el usuario, consumo de la API del backend |
| **Backend**       | **Java 17+ / Spring Boot 3** | Lógica de negocio, API RESTful, gestión de usuarios, reservas, pagos, seguridad e integración con APIs externas    |
| **Base de Datos** | **PostgreSQL 15+**           | Persistencia de todos los datos de la aplicación de forma relacional y segura                                      |

### **2.2. Stack Frontend Detallado**

- **Framework:** Nuxt 3 (Universal/SSR) con Vue 3 Composition API
- **Styling:** Tailwind CSS 3.3 + NuxtUI component library
- **Calendario:** v-calendar con integración de fases lunares personalizada
- **Iconos:** Heroicons Vue o NuxtIcon
- **Estado:** Pinia para state management
- **HTTP:** Ofetch con interceptors para autenticación
- **Lógica Lunar:** lunarphase-js para cálculos de fases lunares

### **2.3. Stack Backend Detallado**

- **Framework:** Spring Boot 3.2+ con Spring MVC, Data JPA, Security
- **Base de Datos:** PostgreSQL 15+ con connection pooling HikariCP
- **Validación:** Bean Validation 3.0 con mensajes multilingüe
- **Seguridad:** Spring Security 6 + JWT para autenticación stateless
- **Documentación:** OpenAPI 3 (Swagger) para API documentation
- **OAuth2:** spring-boot-starter-oauth2-client para autenticación con Google

### **2.4. Arquitectura Monorepo con Docker**

#### **Estructura del Monorepo:**

```
northern-chile-platform/
├── frontend/                 # Aplicación Nuxt 3
│   ├── Dockerfile
│   ├── nuxt.config.ts
│   └── package.json
├── backend/                  # API Spring Boot
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── docker-compose.yml        # Orquestación de servicios
└── README.md
```

#### **Docker Compose Configuration:**

```yaml
version: "3.8"
services:
  database:
    image: postgres:15
    environment:
      POSTGRES_USER: user
      POSTGRES_PASSWORD: password
      POSTGRES_DB: northernchile_db
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  backend:
    build: ./backend
    depends_on:
      - database
    ports:
      - "8080:8080"
    environment:
      DATABASE_URL: jdbc:postgresql://database:5432/northernchile_db
      WEATHER_API_KEY: ${WEATHER_API_KEY}
      GOOGLE_CLIENT_ID: ${GOOGLE_CLIENT_ID}
      GOOGLE_CLIENT_SECRET: ${GOOGLE_CLIENT_SECRET}

  frontend:
    build: ./frontend
    ports:
      - "3000:3000"
    environment:
      API_BASE_URL: http://localhost:8080/api

volumes:
  postgres_data:
```

### **2.5. Herramienta de Migraciones de Base de Datos: Flyway**

#### **Implementación de Flyway**

- **Dependencia:** `flyway-core` en el `pom.xml` del backend Spring Boot
- **Estructura de Directorios:**
  ```
  src/main/resources/db/migration/
  ├── V1__Create_initial_schema.sql
  ├── V2__Add_auth_provider_to_users.sql
  ├── V3__Create_participants_table.sql
  └── V4__Add_lunar_phase_restrictions.sql
  ```
- **Configuración en `application.properties`:**
  ```properties
  spring.flyway.enabled=true
  spring.flyway.locations=classpath:db/migration
  spring.flyway.baseline-on-migrate=true
  spring.flyway.validate-on-migrate=true
  ```

### **2.6. Estructura de Paquetes del Backend Spring Boot**

#### **Organización "Package-by-Feature":**

```
src/main/java/com/northernchile/
├── NorthernChileApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── WebConfig.java
│   └── FlywayConfig.java
├── user/
│   ├── UserController.java
│   ├── UserService.java
│   ├── UserRepository.java
│   └── dto/
│       ├── UserResponse.java
│       └── UserRequest.java
├── tour/
│   ├── TourController.java
│   ├── TourService.java
│   ├── TourRepository.java
│   ├── schedule/
│   │   ├── TourScheduleService.java
│   │   └── TourScheduleRepository.java
│   └── dto/
├── booking/
│   ├── BookingController.java
│   ├── BookingService.java
│   ├── BookingRepository.java
│   └── dto/
├── participant/
│   ├── ParticipantService.java
│   └── ParticipantRepository.java
├── cart/
│   ├── CartController.java
│   ├── CartService.java
│   └── CartRepository.java
├── payment/
│   ├── PaymentController.java
│   └── PaymentService.java
├── external/
│   ├── WeatherService.java
│   └── LunarService.java
├── notification/
│   └── EmailService.java
└── common/
    ├── exceptions/
    └── utils/
```

---

## **3. Modelo de Datos y Esquema de Base de Datos**

### **3.1. Esquema Principal de PostgreSQL**

#### **Tabla: `users`** (Almacena Clientes y Administradores)

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255), -- NULLABLE para usuarios de Google
    full_name VARCHAR(255) NOT NULL,
    nationality VARCHAR(100),
    role VARCHAR(50) NOT NULL CHECK (role IN ('ROLE_CLIENT', 'ROLE_PARTNER_ADMIN', 'ROLE_SUPER_ADMIN')),
    auth_provider VARCHAR(50) DEFAULT 'LOCAL' CHECK (auth_provider IN ('LOCAL', 'GOOGLE')),
    provider_id VARCHAR(255), -- ID único del proveedor OAuth2
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

#### **Tabla: `tours`** (El catálogo padre con reglas de recurrencia)

```sql
CREATE TABLE tours (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL REFERENCES users(id), -- Creador del tour (Alex o David)
    name VARCHAR(200) NOT NULL, -- Nombre del tour (puede venir de la BD para David)
    description TEXT, -- Descripción (puede venir de la BD para David)
    content_key VARCHAR(100) UNIQUE, -- NULLABLE. Clave para contenido "codeado" (ej: 'tour.astronomical')
    category VARCHAR(50) NOT NULL CHECK (category IN ('ASTRONOMICAL', 'REGULAR', 'SPECIAL', 'PRIVATE')),
    price DECIMAL(10,2) NOT NULL,
    default_max_participants INTEGER NOT NULL,
    duration_hours INTEGER NOT NULL,
    is_wind_sensitive BOOLEAN DEFAULT FALSE,
    is_recurring BOOLEAN DEFAULT FALSE, -- <-- CAMPO CLAVE: ¿Es automático?
    recurrence_rule VARCHAR(100), -- <-- CAMPO CLAVE: Regla CRON si es recurrente (ej: '0 20 * * *')
    status VARCHAR(20) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tours_owner_id ON tours(owner_id);
CREATE INDEX idx_tours_status ON tours(status);
```

#### **Tabla: `tour_schedules`** (Instancias específicas de tours programados)

```sql
CREATE TABLE tour_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tour_id UUID NOT NULL REFERENCES tours(id),
    start_datetime TIMESTAMP WITH TIME ZONE NOT NULL,
    max_participants INTEGER NOT NULL,
    status VARCHAR(20) DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'CLOSED', 'CANCELLED')),
    assigned_guide_id UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    -- Índices para consultas de disponibilidad
    UNIQUE(tour_id, start_datetime)
);

CREATE INDEX idx_tour_schedules_datetime ON tour_schedules(start_datetime);
CREATE INDEX idx_tour_schedules_tour_status ON tour_schedules(tour_id, status);
```

#### **Tabla: `bookings`** (Reservas realizadas por usuarios)

```sql
CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    schedule_id UUID NOT NULL REFERENCES tour_schedules(id),
    tour_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED_BY_CLIENT', 'CANCELLED_BY_ADMIN', 'AWAITING_REFUND', 'REFUNDED')),
    subtotal DECIMAL(10,2) NOT NULL, -- Monto antes de impuestos
    tax_amount DECIMAL(10,2) NOT NULL, -- Monto del IVA
    total_amount DECIMAL(10,2) NOT NULL, -- Monto total pagado
    amount_paid DECIMAL(10,2) NOT NULL,
    payment_type VARCHAR(20) CHECK (payment_type IN ('PARTIAL', 'FULL')),
    payment_status VARCHAR(20) DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'PAID', 'REFUNDED', 'FAILED')),
    language_code VARCHAR(5) NOT NULL DEFAULT 'es', -- Idioma de la reserva
    special_requests TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    -- Índices para reportes y consultas
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (schedule_id) REFERENCES tour_schedules(id)
);

CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_tour_date ON bookings(tour_date);
```

#### **Tabla: `participants`** (Personas que asisten a los tours)

```sql
CREATE TABLE participants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id), -- NULLABLE: puede ser un participant sin cuenta
    full_name VARCHAR(255) NOT NULL,
    is_primary_booker BOOLEAN DEFAULT FALSE, -- Indica si este participant es el que hizo la compra
    pickup_address TEXT,
    special_requirements TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (booking_id) REFERENCES bookings(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_participants_booking_id ON participants(booking_id);
CREATE INDEX idx_participants_user_id ON participants(user_id);
```

#### **Tabla: `carts`** (Carritos de compra persistentes)

```sql
CREATE TABLE carts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id), -- NULLABLE para carritos de invitado
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'ABANDONED')),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cart_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL REFERENCES carts(id),
    tour_id UUID NOT NULL REFERENCES tours(id),
    tour_date DATE NOT NULL,
    num_adults INTEGER NOT NULL,
    num_children INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_carts_user_id ON carts(user_id);
CREATE INDEX idx_carts_expires_at ON carts(expires_at);
```

#### **Tabla: `private_tour_requests`** (Solicitudes de tours privados)

```sql
CREATE TABLE private_tour_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id), -- NULLABLE si no está registrado
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(50),
    requested_tour_type VARCHAR(100) NOT NULL,
    requested_datetime TIMESTAMP WITH TIME ZONE NOT NULL,
    num_adults INTEGER NOT NULL,
    num_children INTEGER DEFAULT 0,
    special_requests TEXT,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'QUOTED', 'CONFIRMED', 'REJECTED')),
    quoted_price DECIMAL(10,2),
    payment_link_id VARCHAR(255), -- ID del enlace de pago único
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

---

## **4. Lógica de Negocio y Flujos Operativos**

### **4.1. Gestión de Impuestos (IVA)**

- **Definición de Negocio:** Todos los precios mostrados en la plataforma son finales para el cliente e **incluyen el IVA** (actualmente 19% en Chile).
- **Impacto Técnico:**
  - La tabla `bookings` almacena los valores desglosados: `subtotal`, `tax_amount`, `total_amount`
  - El backend calcula automáticamente el desglose del IVA basándose en una tasa configurable
  - Comunicaciones transparentes con desglose de impuestos en emails y portal del cliente

### **4.2. Política de Cancelaciones y Reembolsos**

#### **A. Cancelación por parte del Cliente:**

- **Regla:** Reembolso completo con más de 24 horas de antelación
- **Implementación:** Endpoint `POST /api/bookings/{id}/cancel` que verifica la regla de 24 horas contra el `start_datetime` del `tour_schedule`

#### **B. Cancelación por parte del Administrador:**

- **Regla:** Reembolso completo sin importar la antelación por condiciones climáticas u operativas
- **Implementación:** Botón "Cancelar Tour" en el Admin Panel que actualiza el `status` de la instancia y todas sus reservas

#### **Gestión de Reembolsos (Fase 1):**

- Proceso manual marcando reservas como `AWAITING_REFUND`
- Notificación al administrador para procesar devoluciones manualmente
- Marcado manual como `REFUNDED` tras la devolución

### **4.3. Notificaciones para el Administrador**

- **Eventos Notificados:** Nueva reserva, nueva solicitud de tour privado, cancelación de cliente
- **Implementación:** `EmailService` expandido con métodos `sendNewBookingNotificationToAdmin`, `sendNewPrivateRequestNotificationToAdmin`
- **Configuración:** Dirección de notificación configurada en `application.properties`

### **4.4. Flujo de Entrega de Astrofotografías (Fase 1)**

- **Definición:** No incluido en el lanzamiento inicial
- **Arquitectura Preparada:** Base de datos diseñada para añadir esta funcionalidad posteriormente sin cambios estructurales

### **4.5. Sistema de Cupones y Descuentos (Fase 1)**

- **Definición:** No incluido en el lanzamiento inicial
- **Arquitectura:** Modelo de datos y flujo de checkout simplificados para transacciones directas

---

## **5. Integración de APIs Externas**

### **5.1. API de Clima: WeatherAPI.com**

#### **Justificación de la Elección:**

- Plan gratuito generoso (1M llamadas/mes)
- Pronóstico de 14 días con datos horarios
- Incluye velocidad del viento (`wind_kph`)
- API RESTful bien documentada

#### **Implementación en Spring Boot:**

**Configuración (`application.properties`):**

```properties
weather.api.baseurl=http://api.weatherapi.com/v1/forecast.json
weather.api.key=${WEATHER_API_KEY}
weather.api.location=San Pedro de Atacama
```

**Servicio de Clima (`WeatherService`):**

```java
@Service
@Cacheable(value = "weatherForecast", key = "#days")
public class WeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.baseurl}")
    private String baseUrl;

    @Value("${weather.api.location}")
    private String location;

    public WeatherForecastResponse getForecast(int days) {
        String url = String.format("%s?key=%s&q=%s&days=%d&aqi=no&alerts=no",
                                 baseUrl, apiKey, location, days);

        // Implementación con RestTemplate/WebClient
        // Cache automático por 1-3 horas mediante @Cacheable
    }

    // Lógica de validación de viento para tours sensibles
    public boolean isWindAboveThreshold(LocalDate date, double thresholdKnots) {
        WeatherForecastResponse forecast = getForecast(14);
        // Iterar sobre datos horarios y convertir wind_kph a nudos
        // Retornar true si algún valor horario supera el threshold
    }
}
```

#### **Integración con Lógica de Negocio:**

- **Validación en `BookingService`:** Para tours `is_wind_sensitive`, verificar pronóstico de viento > 25 nudos
- **Cache Estratégico:** Respuestas cacheadas por 1-3 horas para eficiencia
- **Fallback Graceful:** Manejo de errores para casos de indisponibilidad de API

### **5.2. Pasarelas de Pago**

#### **Integraciones Implementadas:**

- **Mercado Chileno:** Transbank (Webpay Plus/REST)
- **Mercado Brasileño:** Mercado Pago (API v2 con soporte PIX)
- **Mercado Internacional:** Stripe para pagos internacionales

#### **Flujo de Pagos Parciales:**

- Opción "Paga solo la reserva ahora y completa el pago en destino"
- Cálculo automático de `booking_fee` vs `total_amount`
- Gestión de estados de pago: `PENDING`, `PARTIAL`, `FULL`

### **5.3. Autenticación con Google (OAuth2)**

#### **Flujo de Implementación:**

1.  **Configuración Spring Security:** `spring-boot-starter-oauth2-client`
2.  **Servicio Personalizado:** `CustomOAuth2UserService` con lógica "Find or Create"
3.  **Generación de JWT Propio:** Tras autenticación exitosa con Google
4.  **Fusión de Carritos:** Transición seamless de carrito de invitado a usuario registrado

#### **Configuración:**

```properties
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=profile,email
```

### **5.4. Implementación del Servicio Lunar (Java/Spring Boot)**

#### **Librería para Cálculos Lunares**

- **Dependencia:** Agregar `astro-algo` o librería similar al `pom.xml`
- **Implementación del `LunarService`:**

```java
@Service
public class LunarService {

    private static final double FULL_MOON_THRESHOLD = 0.9; // 90% de iluminación

    public boolean isFullMoon(LocalDate date) {
        // Usar librería astronómica para calcular iluminación lunar
        double illumination = MoonPhaseCalculator.getIllumination(date);
        return illumination >= FULL_MOON_THRESHOLD;
    }

    public double getMoonIllumination(LocalDate date) {
        return MoonPhaseCalculator.getIllumination(date);
    }
}
```

#### **Integración en `TourService` o `BookingService`:**

```java
@Service
public class BookingService {

    @Autowired
    private LunarService lunarService;

    public boolean isDateAvailableForAstronomicalTour(LocalDate date) {
        // Validar regla de luna llena para tours astronómicos
        if (lunarService.isFullMoon(date)) {
            return false; // No disponible por luna llena
        }
        return true;
    }
}
```

---

## **6. Gestión de Contenido Multilingüe**

### **6.1. Estrategia de Internacionalización (i18n)**

#### **Frontend (Nuxt 3):**

- Routing basado en idioma: `/es/tours`, `/en/tours`, `/pt/tours`
- Cambio dinámico de idioma manteniendo el estado de la aplicación
- Meta tags SEO optimizados por idioma

#### **Backend (Spring Boot):**

- **Resource Bundles:** `messages_es.properties`, `messages_en.properties`, `messages_pt.properties`
- **Locale Resolution:** Basado en `Accept-Language` header y preferencia de usuario
- **Email Service Multilingüe:** Selección dinámica de plantillas basada en `booking.language_code`

### **6.2. Implementación de Emails Multilingües**

#### **Archivos de Recursos:**

**`messages_es.properties`**

```properties
email.confirmation.subject=¡Tu aventura en Atacama está confirmada! - Reserva #{0}
email.confirmation.greeting=Hola {0},
email.confirmation.body=¡Gracias por elegir Northern Chile! Tu reserva para el tour '{0}' está confirmada.
```

**`messages_pt.properties`**

```properties
email.confirmation.subject=Sua aventura no Atacama está confirmada! - Reserva #{0}
email.confirmation.greeting=Olá {0},
email.confirmation.body=Obrigado por escolher a Northern Chile! Sua reserva para o passeio '{0}' está confirmada.
```

#### **Servicio de Email Actualizado:**

```java
@Service
public class EmailService {

    @Autowired
    private MessageSource messageSource;

    public void sendBookingConfirmationEmail(Booking booking) {
        Locale userLocale = Locale.forLanguageTag(booking.getLanguageCode());

        String subject = messageSource.getMessage(
            "email.confirmation.subject",
            new Object[]{booking.getId()},
            userLocale
        );

        // Construcción del email multilingüe...
        // Envío a través de Google Workspace
    }
}
```

### **6.3. Admin Panel en Español**

- Interfaz completa del panel de administración en español
- Campos de contenido con soporte multilingüe (pestañas para español, inglés, portugués)
- Gestión centralizada de todas las traducciones

---

## **7. Sistema de Roles y Permisos**

### **7.1. Modelo RBAC (Role-Based Access Control)**

#### **Roles Definidos:**

- **`ROLE_CLIENT`:** Usuario final que realiza reservas
- **`ROLE_PARTNER_ADMIN`:** Colaborador con autonomía sobre sus tours
- **`ROLE_SUPER_ADMIN`:** Administrador con control total del sistema

### **7.2. Permisos Detallados por Rol**

| Acción                                  | SUPER_ADMIN | PARTNER_ADMIN  |
| :-------------------------------------- | :---------- | :------------- |
| **Crear Tour**                          | Sí          | Sí             |
| **Editar CUALQUIER Tour**               | Sí          | No             |
| **Editar SUS PROPIOS Tours**            | N/A         | Sí             |
| **Publicar/Archivar CUALQUIER Tour**    | Sí          | No             |
| **Publicar/Archivar SUS PROPIOS Tours** | N/A         | Sí             |
| **Programar Fechas**                    | Sí (todos)  | Sí (sus tours) |
| **Ver Reportes de Ventas**              | Sí (todos)  | Sí (sus tours) |
| **Gestionar Usuarios**                  | Sí          | No             |

### **7.3. Implementación de Seguridad**

#### **Configuración Spring Security:**

````java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/admin/tours/**").hasAnyRole("SUPER_ADMIN", "PARTNER_ADMIN")
                .requestMatchers("/api/admin/users/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/bookings/**").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(oauth2AuthenticationSuccessHandler)
            );
        return http.build();
    }
}```

#### **Lógica de Propiedad en Servicios:**

```java
@Service
public class TourService {

    public Tour updateTour(UUID tourId, TourUpdateRequest request, User currentUser) {
        Tour tour = tourRepository.findById(tourId)
            .orElseThrow(() -> new TourNotFoundException(tourId));

        // Verificar permisos: SUPER_ADMIN o propietario del tour
        if (!currentUser.getRole().equals("ROLE_SUPER_ADMIN")
            && !tour.getOwnerId().equals(currentUser.getId())) {
            throw new AccessDeniedException("No tiene permisos para editar este tour");
        }

        // Actualizar tour...
        return tourRepository.save(tour);
    }
}
````

---

## **8. Flujos de Usuario Específicos**

### **8.1. Flujo de Reserva Estándar**

#### **Paso 1: Descubrimiento y Disponibilidad**

1.  Cliente navega tours en su idioma preferido
2.  Frontend consulta disponibilidad: `GET /api/availability?tourId=X&month=10&year=2025`
3.  Backend responde con días disponibles y slots restantes

#### **Paso 2: Gestión de Carrito Persistente**

1.  **Usuario Invitado:** Se crea `cart_id` y se almacena en cookie HttpOnly
2.  **Añadir al Carrito:** Los ítems persisten entre sesiones mediante `cart_id`
3.  **Transición a Usuario Registrado:** Fusión automática del carrito al login/registro

#### **Paso 3: Checkout y Pago**

1.  Selección de método de pago (completo o parcial)
2.  Integración con pasarela según mercado (Transbank, Mercado Pago, Stripe)
3.  Cálculo automático de impuestos (IVA 19%)
4.  Confirmación inmediata vía email multilingüe

#### **Paso 4: Comunicaciones Automatizadas**

- **Email de Confirmación:** Inmediato tras la reserva
- **Email Recordatorio:** 48 horas antes del tour
- **Email de Agradecimiento:** 24 horas después, con enlace a reseñas

### **8.2. Flujo de Tours Privados**

#### **Paso 1: Solicitud de Cotización**

1.  Formulario especializado sin calendario de reservas
2.  Captura de requerimientos específicos y personalizaciones
3.  Envío como `private_tour_requests` con status `PENDING`

#### **Paso 2: Procesamiento por Administrador**

1.  Notificación inmediata a admin por email
2.  Verificación de disponibilidad y cálculo de precio
3.  Generación de cotización personalizada

#### **Paso 3: Confirmación y Pago**

1.  Envío de email con cotización y enlace de pago único
2.  Procesamiento de pago a través de pasarelas estándar
3.  Confirmación automática al completar el pago

### **8.3. Flujo de Administración de Tours (Modelo Simplificado)**

#### **A. El Servicio Automático de Generación (`TourScheduleGeneratorService`)**

Este es el corazón de la automatización. Es un servicio en el backend que se ejecuta automáticamente (por ejemplo, cada noche a medianoche).

1.  **Visión a Futuro:** El servicio se asegura de que el calendario de tours recurrentes esté siempre lleno para los próximos **90 días**.
2.  **Busca las Reglas:** Identifica todos los tours en la base de datos que están `PUBLISHED` y tienen `is_recurring = true`.
3.  **Genera Instancias Faltantes:** Para cada día dentro de los próximos 90 días, el servicio comprueba:
    - a) Si la `recurrence_rule` del tour (la regla CRON) coincide con ese día.
    - b) Si **NO existe ya una instancia** para ese tour en esa fecha.
4.  **Creación Inteligente:** Si ambas condiciones se cumplen, crea una nueva fila en `tour_schedules` para esa fecha, "heredando" los valores por defecto (cupos, precio) del tour padre. También aplica reglas de negocio (ej: no crear tours astronómicos en luna llena).

**Crucial:** El servicio **solo crea instancias si no existen**. Nunca modifica ni borra nada. Esto le da a los administradores control total para hacer cambios manuales.

#### **B. Flujo de Trabajo para los Administradores**

##### **Para Alex (Tours Recurrentes):**

1.  **Configurar y Olvidar:** Alex crea el "Tour Astronómico", le pone su precio, activa la casilla `is_recurring`, y define la regla: `0 20 * * *` (todos los días a las 20:00).
2.  El servicio automático se encarga de llenar el calendario por él.
3.  **Gestión por Excepción:** Su única tarea diaria es ir al calendario y, si es necesario, **cancelar una instancia específica** (cambiar su `status` a `CANCELLED`). El servicio no la volverá a crear porque ya "existe" (aunque esté cancelada).

##### **Para David (Tours Manuales y Basados en Plantillas):**

1.  **Creación Manual:** David crea su "Tour de Astrofotografía". Deja la casilla `is_recurring` desactivada. En `name` y `description` pone su propio contenido personalizado.
2.  **Programación Manual:** Va al calendario y añade manualmente las instancias en las fechas exactas que él quiere operar.
3.  **Creación Basada en un Recurrente (Herencia):** David quiere ofrecer su propia versión del Tour Astronómico.
    - Alex (o un `SUPER_ADMIN`) puede darle a David un botón de "Clonar Tour".
    - Esto crea una copia del "Tour Astronómico" de Alex, asignando a David como `owner_id`.
    - David puede entonces ajustar el precio, los cupos, el nombre y la descripción a su gusto, y empezar a programar sus propias fechas manualmente.

---

## **9. Estrategia de Almacenamiento y Assets**

### **9.1. Almacenamiento de Imágenes: Amazon S3**

#### **Arquitectura:**

- **Servicio:** Amazon S3 con CloudFront CDN
- **Flujo de Carga:**
  1.  Admin sube imagen a través del panel
  2.  Backend sube a S3 usando AWS SDK para Java
  3.  Se almacena únicamente la URL en la base de datos
  4.  Entrega optimizada a través de CDN global

#### **Estructura de Buckets:**

```
s3://northern-chile-assets/
├── tours/
│   ├── {tour-id}/
│   │   ├── hero.jpg
│   │   ├── gallery-1.jpg
│   │   └── gallery-2.jpg
├── astrophotography/
│   ├── {booking-id}/
│   │   ├── client-photo-1.jpg
│   │   └── client-photo-2.jpg
```

### **9.2. Optimización de Imágenes**

- Compresión automática para web
- Múltiples resoluciones (thumbnails, web, original)
- Lazy loading en frontend para performance
- CDN global para entrega rápida

---

## **10. Requisitos No Funcionales**

### **10.1. Performance**

- **Tiempo de Carga:** < 2 segundos en conexión 4G
- **Lighthouse Score:** > 90 en performance, accessibility, SEO
- **Lazy Loading:** Imágenes y componentes no críticos

### **10.2. Seguridad**

- **SSL/TLS:** Certificado wildcard para todos los subdominios
- **SQL Injection:** Prepared statements y JPA typed queries
- **XSS Protection:** Vue auto-escaping + CSP headers
- **Data Protection:** Encriptación de datos sensibles (PCI DSS compliance)

### **10.3. Escalabilidad**

- **Horizontal Scaling:** Load balancer + múltiples instancias de app
- **Database:** Connection pooling + read replicas para reportes
- **Storage:** S3-compatible para uploads de imágenes y documentos

### **10.4. Monitorización y Logs**

- **Health Checks:** Endpoints para verificación de estado
- **Log Aggregation:** Centralización de logs de aplicación
- **Error Tracking:** Seguimiento de errores frontend y backend
- **Performance Monitoring:** Métricas de respuesta y uso de recursos

---

## **11. Plan de Implementación en Fases**

### **Fase 1: Núcleo y Infraestructura (Semanas 1-4)**

- [x] Setup monorepo con Docker
- [x] Configuración PostgreSQL +
- [x] Entidades JPA y repositorios base
- [ ] Autenticación JWT y seguridad
- [ ] Layout base Nuxt 3 + Tailwind

### **Fase 2: Módulos de Negocio (Semanas 5-8)**

- \[ ] CRUD Tours con gestión de roles
- \[ ] Sistema de reservas y calendario
- \[ ] Integración APIs clima y fases lunares
- \[ ] Panel de administración básico

### **Fase 3: Pagos y Comunicación (Semanas 9-12)**

- \[ ] Integración Transbank + Mercado Pago
- \[ ] Sistema de mailing automatizado
- \[ ] Portal del viajero
- \[ ] Reportes y analytics

### **Fase 4: Optimización y Lanzamiento (Semanas 13-16)**

- \[ ] Testing end-to-end
- \[ ] Optimización SEO multilingüe
- \[ ] Load testing y ajustes performance
- \[ ] Deployment producción y monitorización

---

## **12. Costos Operativos Estimados**

### **12.1. Infraestructura Mensual**

- **VPS (4GB RAM, 2vCPU):** $20-30 USD (DigitalOcean/AWS)
- **Domain (.cl):** \~$2 USD/mes (prorrateado anual)
- **Email Professional (Microsoft 365):** $6 USD/usuario/mes
- **CDN (Cloudflare):** $0-20 USD (dependiendo tráfico)
- **Backup Storage (S3):** $5-10 USD
- **Monitoring (Sentry/LogRocket):** $0-29 USD (plan free/startup)

**Total Estimado Infraestructura:** $40-100 USD mensuales

### **12.2. Servicios Externos**

- **WeatherAPI.com:** Plan Free (1M llamadas/mes)
- **Transbank:** Comisión por transacción (\~2.9%)
- **Mercado Pago:** Comisión por transacción (\~3.5% + PIX)
- **Stripe:** Comisión por transacción internacional (\~3.4%)

---

## **13. Visión Evolutiva (Post-Lanzamiento)**

### **13.1. Comunicación Avanzada**

- Integración de recordatorios automatizados por **WhatsApp Business**
- Notificaciones push para móviles
- Sistema de alertas meteorológicas proactivas

### **13.2. Inteligencia Artificial Experiencial**

- Implementación de **asistente virtual** para consultas frecuentes 24/7
- Recomendaciones personalizadas basadas en historial
- Chatbots para soporte pre y post venta

### **13.3. Contenido de Valor Continuo**

- Creación de **blog** sobre astronomía, cultura atacameña y tutoriales de astrofotografía
- Guías de viaje descargables
- Galería comunitaria de astrofotografías

### **13.4. Funcionalidades Futuras**

- Sistema automatizado de entrega de fotografías
- Programa de lealtad y sistema de cupones
- Integración con más proveedores de tours
- App móvil nativa para clientes y guías
- Sistema de reembolsos automatizado a través de APIs de pago

---

## **14. Variables de Entorno y Configuración**

### **14.1. Backend Environment Variables**

```env
# Database
DATABASE_URL=postgresql://user:pass@postgres:5432/northern_chile

# External APIs
WEATHER_API_KEY=tu_api_key_weather

# Payment Gateways
TRANSBANK_API_KEY=tu_api_key_transbank
MERCADO_PAGO_ACCESS_TOKEN=tu_token_mp
STRIPE_SECRET_KEY=tu_key_stripe

# Email Service
SMTP_HOST=smtp.office365.com
SMTP_PORT=587
SMTP_USER=reservas@northernchile.cl
SMTP_PASSWORD=tu_password_email

# OAuth2 Google
GOOGLE_CLIENT_ID=tu_google_client_id
GOOGLE_CLIENT_SECRET=tu_google_client_secret

# Application
ADMIN_NOTIFICATION_EMAIL=alex@northernchile.cl
TAX_RATE=0.19
```

### **14.2. Frontend Environment Variables**

```env
# API Configuration
API_BASE_URL=http://localhost:8080/api
API_TIMEOUT=30000

# Feature Flags
ENABLE_GOOGLE_LOGIN=true
ENABLE_PARTIAL_PAYMENTS=true
ENABLE_GUEST_CART=true

# External Services
GOOGLE_ANALYTICS_ID=UA-XXXXX-Y
TRIPADVISOR_INTEGRATION=true
```

## **15. Estrategia de Renderizado y SEO Técnico**

### **15.1. Modo de Renderizado: Universal (Server-Side Rendering)**

La plataforma se ejecutará en modo **Universal (SSR)**, la estrategia de renderizado principal de Nuxt 3. Esto significa que cada solicitud de página será procesada en el servidor por el motor **Nitro**. El servidor generará el documento HTML completo con todo el contenido dinámico (detalles del tour, fechas, precios) antes de enviarlo al cliente.

Esta es la elección óptima para lograr los objetivos de SEO y rendimiento por las siguientes razones:

- **Indexación Perfecta:** Los motores de búsqueda reciben contenido HTML completamente renderizado, asegurando una indexación rápida y precisa.
- **Rendimiento Percibido Superior:** El usuario ve el contenido de inmediato, mejorando métricas clave como el First Contentful Paint (FCP) y el Time to First Byte (TTFB).
- **Meta-Tags Dinámicos:** Permite generar etiquetas `<title>`, `<meta description>` y tags de Open Graph (para redes sociales) únicos para cada página de tour, lo cual es crítico para el SEO.

### **15.2. El Ecosistema Vite + Nitro**

La estrategia de la plataforma no se basa en una elección entre Vite y Nitro, sino en su **integración simbiótica** dentro de Nuxt 3:

- **Vite (Bundler):** Actúa como el sistema de construcción. Es responsable de optimizar el código del lado del cliente (JavaScript/CSS), aplicando técnicas como el tree-shaking y la división de código para generar los archivos más pequeños y eficientes posibles.
- **Nitro (Server Engine):** Es el motor que se ejecuta en el servidor. Utiliza los assets generados por Vite para realizar el Server-Side Rendering (SSR), gestionar rutas de API, aplicar estrategias de caché a nivel de servidor y desplegar la aplicación en cualquier entorno de JavaScript.

### **15.3. Implementaciones SEO Clave**

Gracias a esta arquitectura, se implementarán las siguientes funcionalidades SEO avanzadas:

- **Gestión de Metadatos:** Se utilizará el composable `useHead` de Nuxt 3 para gestionar los metadatos de cada página de forma dinámica y reactiva.
- **Datos Estructurados (JSON-LD):** Cada página de tour incluirá datos estructurados del tipo `Event` y `Product` de `Schema.org`. Esto permitirá que Google muestre resultados enriquecidos (rich snippets) con precios, fechas y valoraciones directamente en la página de búsqueda.
- **Sitemap Dinámico:** Se creará un endpoint de servidor (`/sitemap.xml`) usando las capacidades de Nitro para generar automáticamente un mapa del sitio actualizado que incluya todos los tours publicados.
- **`robots.txt` y Redirecciones:** Se gestionarán desde el servidor para un control total sobre el rastreo y la indexación.<!-- layouts/default.vue -->
<script setup>
const i18nHead = useLocaleHead({
  addDirAttribute: true,
  identifierAttribute: 'id',
  addSeoAttributes: true
})

// Se utiliza useHead para fusionar los metadatos de i18n con otros metadatos
useHead(() => ({
htmlAttrs: {
lang: i18nHead.value.htmlAttrs.lang,
dir: i18nHead.value.htmlAttrs.dir
},
link: [...(i18nHead.value.link || [])],
meta: [...(i18nHead.value.meta || [])]
}))
</script>

<template>
  <div>
    <!-- Contenido del Layout -->
    <slot />
  </div>
</template>
  ¡Absolutamente! Has identificado el punto exacto que necesita una actualización. La sección de SEO que teníamos era una buena declaración de intenciones, pero la documentación que has traído del módulo `@nuxtjs/i18n` nos permite definir la **estrategia técnica exacta y accionable**.

Basándome en la información que proporcionaste, voy a reescribir y expandir la sección `15.3` para que refleje con precisión cómo vamos a aprovechar `useLocaleHead` para un SEO multilingüe perfecto. Esto hace el documento mucho más robusto y útil para el equipo de desarrollo.

Aquí está el documento actualizado. He dejado intactas las secciones `15.1` y `15.2` y he reemplazado completamente la `15.3` con la nueva, detallada y mejorada versión.

---

## **15. Estrategia de Renderizado y SEO Técnico**

### **15.1. Modo de Renderizado: Universal (Server-Side Rendering)**

La plataforma se ejecutará en modo **Universal (SSR)**, la estrategia de renderizado principal de Nuxt 3. Esto significa que cada solicitud de página será procesada en el servidor por el motor **Nitro**. El servidor generará el documento HTML completo con todo el contenido dinámico (detalles del tour, fechas, precios) antes de enviarlo al cliente.

Esta es la elección óptima para lograr los objetivos de SEO y rendimiento por las siguientes razones:

- **Indexación Perfecta:** Los motores de búsqueda reciben contenido HTML completamente renderizado, asegurando una indexación rápida y precisa.
- **Rendimiento Percibido Superior:** El usuario ve el contenido de inmediato, mejorando métricas clave como el First Contentful Paint (FCP) y el Time to First Byte (TTFB).
- **Meta-Tags Dinámicos:** Permite generar etiquetas `<title>`, `<meta description>` y tags de Open Graph (para redes sociales) únicos para cada página de tour, lo cual es crítico para el SEO.

### **15.2. El Ecosistema Vite + Nitro**

La estrategia de la plataforma no se basa en una elección entre Vite y Nitro, sino en su **integración simbiótica** dentro de Nuxt 3:

- **Vite (Bundler):** Actúa como el sistema de construcción. Es responsable de optimizar el código del lado del cliente (JavaScript/CSS), aplicando técnicas como el tree-shaking y la división de código para generar los archivos más pequeños y eficientes posibles.
- **Nitro (Server Engine):** Es el motor que se ejecuta en el servidor. Utiliza los assets generados por Vite para realizar el Server-Side Rendering (SSR), gestionar rutas de API, aplicar estrategias de caché a nivel de servidor y desplegar la aplicación en cualquier entorno de JavaScript.

### **15.3. Implementación SEO Específica con Nuxt i18n**

Para lograr la excelencia en SEO multilingüe, se aprovechará intensivamente el composable `useLocaleHead` que provee el módulo `@nuxtjs/i18n`. Esta herramienta automatiza la generación de metadatos críticos para que los motores de búsqueda entiendan la estructura de idiomas del sitio.

#### **Configuración Requerida en `nuxt.config.ts`**

Para que `useLocaleHead` funcione correctamente, se configurará el módulo `i18n` con dos requisitos clave:

1.  **Definición de Idiomas:** Los `locales` se definirán como un array de objetos, especificando el `code` (usado en la URL) y el `language` (usado en los tags `hreflang` y `lang`).
2.  **URL Base:** Se establecerá la `baseUrl` al dominio de producción para generar URLs absolutas y canónicas correctas.

```typescript
// nuxt.config.ts
export default defineNuxtConfig({
  i18n: {
    baseUrl: "https://www.northernchile.cl", // URL final de producción
    locales: [
      {
        code: "es",
        language: "es-CL", // Español de Chile
        isCatchallLocale: true, // Se usará para hreflang="x-default"
      },
      {
        code: "en",
        language: "en-US", // Inglés Americano
      },
      {
        code: "pt",
        language: "pt-BR", // Portugués de Brasil
      },
    ],
    // ... otras configuraciones de i18n
  },
});
```

#### **Estrategia de Implementación Global**

Para evitar la duplicación de código y asegurar que todas las páginas tengan los metadatos SEO base, `useLocaleHead` se implementará de forma global en el layout principal de la aplicación (ej. `layouts/default.vue`).

```vue
<!-- layouts/default.vue -->
<script setup>
const i18nHead = useLocaleHead({
  addDirAttribute: true,
  identifierAttribute: "id",
  addSeoAttributes: true,
});

// Se utiliza useHead para fusionar los metadatos de i18n con otros metadatos
useHead(() => ({
  htmlAttrs: {
    lang: i18nHead.value.htmlAttrs.lang,
    dir: i18nHead.value.htmlAttrs.dir,
  },
  link: [...(i18nHead.value.link || [])],
  meta: [...(i18nHead.value.meta || [])],
}));
</script>

<template>
  <div>
    <!-- Contenido del Layout -->
    <slot />
  </div>
</template>
```

Las páginas individuales podrán añadir o sobreescribir metadatos específicos (como el `og:title`) usando su propio `useHead`, que Nuxt fusionará inteligentemente con la configuración global.

#### **Funcionalidades SEO Automatizadas por `useLocaleHead`**

Esta implementación activará las siguientes optimizaciones de forma automática en todo el sitio:

1.  **Atributo `lang` en `<html>`:** Se asignará dinámicamente el `language` del locale actual (ej. `lang="es-CL"`), señalando el idioma de la página al navegador y a los motores de búsqueda.

2.  **Etiquetas `<link rel="alternate" hreflang="...">`:** Para cada página, se generará un conjunto de etiquetas `alternate` que indicarán a Google las URLs de esa misma página en todos los demás idiomas disponibles. Esto es fundamental para la correcta indexación de sitios multilingües y para dirigir a los usuarios a la versión correcta.

3.  **Etiquetas Open Graph (`og:locale`):** Se generarán las meta etiquetas `og:locale` y `og:locale:alternate`, mejorando la forma en que las plataformas sociales (Facebook, LinkedIn, etc.) entienden y previsualizan el contenido compartido en diferentes idiomas.

4.  **Etiqueta `<link rel="canonical">`:** Se generará una URL canónica para cada página. Esto es crucial para evitar problemas de contenido duplicado, especialmente al usar la estrategia de rutas `prefix_and_default`, asegurando que Google indexe la versión sin prefijo como la principal para el idioma por defecto.

#### **SEO Adicional**

Además de lo automatizado, se mantendrán las otras implementaciones clave:

- **Datos Estructurados (JSON-LD):** Cada página de tour incluirá datos estructurados del tipo `Event` y `Product` de `Schema.org`.
- **Sitemap Dinámico:** Se creará un endpoint de servidor (`/sitemap.xml`) que generará un mapa del sitio con todas las URLs públicas en todos los idiomas.
- **`robots.txt`:** Se gestionará desde el servidor para un control total sobre el rastreo.
