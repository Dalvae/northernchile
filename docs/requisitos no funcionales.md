### **Documento de Definición Arquitectónica: Plataforma "Northern Chile"**

#### **1. Visión General y Principios Arquitectónicos**

Este documento define la arquitectura de software completa para la plataforma "Northern Chile". El objetivo es construir un sistema robusto, escalable y mantenible, desacoplando las responsabilidades del frontend y el backend para permitir un desarrollo paralelo y eficiente.

**Principios Clave:**

- **Separación de Responsabilidades (SoC):** El frontend (Nuxt) es el responsable exclusivo de la presentación y la experiencia de usuario. El backend (Java/Spring Boot) gestiona toda la lógica de negocio, los datos y las integraciones con servicios de terceros.
- **API-First:** La comunicación entre frontend y backend se realizará a través de una API RESTful bien definida. El contrato de la API (definido con OpenAPI/Swagger) es la única fuente de verdad.
- **Contenerización:** Toda la aplicación (frontend, backend, base de datos) será contenerizada con Docker, garantizando la consistencia entre los entornos de desarrollo, pruebas y producción.
- **Monorepo:** El código fuente del frontend y del backend residirá en un único repositorio (monorepo) para simplificar la gestión de dependencias y la configuración del CI/CD.

---

#### **2. Arquitectura General y Stack Tecnológico**

La plataforma se compondrá de tres componentes principales que operan de forma independiente pero coordinada.

| Componente        | Tecnología Principal         | Responsabilidades                                                                                                        |
| :---------------- | :--------------------------- | :----------------------------------------------------------------------------------------------------------------------- |
| **Frontend**      | **Nuxt 3 (Vue 3)**           | Renderizado de la interfaz, gestión del estado de la UI, interacción con el usuario, consumo de la API del backend.      |
| **Backend**       | **Java 17+ / Spring Boot 3** | Lógica de negocio, API RESTful, gestión de usuarios, reservas, pagos, seguridad e integración con APIs externas (Clima). |
| **Base de Datos** | **PostgreSQL 15+**           | Persistencia de todos los datos de la aplicación de forma relacional y segura.                                           |

#### **2.1. Stack Frontend Detallado**

- **Framework:** **Nuxt 3**. Elegido por su renderizado del lado del servidor (SSR) para un SEO superior, su sistema de enrutamiento basado en archivos y su ecosistema modular.
- **UI / Estilos:** **Tailwind CSS** con **Nuxt UI**. Proporciona un sistema de utilidades CSS para un desarrollo rápido y un conjunto de componentes de UI pre-construidos y personalizables.
- **Calendario:** **v-calendar**. Componente potente y personalizable para la visualización de disponibilidad, fases lunares y estados de los tours.
- **Lógica Lunar:** **lunarphase-js**. Librería ligera para los cálculos de fases lunares que se mostrarán en el calendario.

#### **2.2. Stack Backend Detallado**

- **Framework:** **Spring Boot 3**. Para la creación rápida de APIs RESTful robustas y seguras, con su ecosistema maduro (Spring Security, Spring Data JPA).
- **Persistencia:** **Spring Data JPA con Hibernate**. Para mapear los objetos Java a las tablas de la base de datos de forma eficiente.
- **Autenticación:** **Spring Security + JWT**. Para proteger los endpoints de la API y gestionar las sesiones de los usuarios.

---

#### **3. Modelo de Datos (Esquema PostgreSQL)**

Para resolver el "enredo" de la lógica de negocio, se propone el siguiente esquema de base de datos relacional.

**Tabla: `users`** (Almacena Clientes y Administradores)

- `id` (PK, UUID)
- `email` (VARCHAR, UNIQUE)
- `password_hash` (VARCHAR)
- `full_name` (VARCHAR)
- `nationality` (VARCHAR)
- `role` (VARCHAR, e.g., 'ROLE_CLIENT', 'ROLE_ADMIN')
- `created_at` (TIMESTAMP)

**Tabla: `tours`** (El catálogo padre con reglas de recurrencia)

- `id` (PK, UUID)
- `owner_id` (FK a `users.id`)
- `name` (VARCHAR)
- `description` (TEXT)
- `content_key` (VARCHAR, UNIQUE, NULLABLE)
- `category` (VARCHAR, e.g., 'ASTRONOMICAL', 'REGULAR', 'SPECIAL', 'PRIVATE')
- `price_adult` (DECIMAL)
- `price_child` (DECIMAL)
- `default_max_participants` (INTEGER)
- `duration_hours` (INTEGER)
- `is_wind_sensitive` (BOOLEAN)
- `is_recurring` (BOOLEAN)
- `recurrence_rule` (VARCHAR)
- `status` (VARCHAR, e.g., 'DRAFT', 'PUBLISHED', 'ARCHIVED')

**Tabla: `tour_schedules`** (Las instancias específicas que se venden)

- `id` (PK, UUID)
- `tour_id` (FK a `tours.id`)
- `start_datetime` (TIMESTAMP)
- `max_participants` (INTEGER)
- `status` (VARCHAR, e.g., 'OPEN', 'CLOSED', 'CANCELLED')
- `assigned_guide_id` (FK a `users.id`, NULLABLE)

**Relación:** Un `tour` (el producto) puede tener muchos `tour_schedules` (las instancias/eventos). Las reservas de los clientes se asocian a un `tour_schedule` específico.

---

#### **4. Lógica de Negocio Clave y Flujos de Datos**

**Integración de la API de Clima:**

- **Decisión Arquitectónica:** La integración con la API de clima externa se realizará **en el backend**.
- **Justificación:**
  1.  **Seguridad:** Mantiene la clave de la API (API Key) oculta en el servidor, en lugar de exponerla en el código del frontend.
  2.  **Eficiencia:** El backend puede implementar una capa de caché. Por ejemplo, puede consultar el clima para San Pedro de Atacama una vez por hora y servir la respuesta cacheada a todos los usuarios, evitando así exceder los límites de una API gratuita.
- **Flujo:**
  1.  El frontend solicita la disponibilidad de un tour para un mes específico.
  2.  El backend, además de consultar la disponibilidad en la base de datos, llama a la API de clima para obtener el pronóstico.
  3.  El backend combina la información de disponibilidad, fases lunares y clima en una única respuesta para el frontend.

---

#### **5. Despliegue y DevOps: Monorepo con Docker**

Para simplificar el entorno de desarrollo y estandarizar el despliegue, se utilizará Docker y Docker Compose.

**Estructura del Monorepo:**

```
/northern-chile-platform
├── /frontend      # Aplicación Nuxt 3
│   └── Dockerfile
├── /backend       # Aplicación Spring Boot
│   └── Dockerfile
└── docker-compose.yml
```

**`docker-compose.yml` (Ejemplo simplificado):**

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

  backend:
    build: ./backend
    depends_on:
      - database
    ports:
      - "8080:8080"
    environment:
      DATABASE_URL: jdbc:postgresql://database:5432/northernchile_db

  frontend:
    build: ./frontend
    ports:
      - "3000:3000"
```

Con este archivo, un desarrollador puede levantar todo el stack de la aplicación (Base de Datos, Backend, Frontend) con un solo comando: `docker-compose up`.

---

#### **6. Próximos Pasos Técnicos**

1.  **Definir el Contrato de la API:** Crear un archivo `openapi.yml` (Swagger) detallando todos los endpoints, DTOs (Data Transfer Objects) y respuestas esperadas.
2.  **Configurar el Monorepo:** Crear la estructura del repositorio en Git con las carpetas `/frontend` y `/backend`.
3.  **Desarrollar el Esquema de Base de Datos:** Crear el script de migración inicial (usando Flyway o Liquibase en el backend) para generar las tablas en PostgreSQL.
4.  **Implementar la Autenticación:** Desarrollar el flujo de registro e inicio de sesión en el backend con Spring Security y JWT.
5.  **Desarrollar el CRUD de Tours:** Crear los endpoints en el backend para que el panel de administración pueda crear, leer, actualizar y eliminar tours.
    ¡Excelente! Esta es una observación de alto nivel que refina la experiencia de usuario de manera crucial. La capacidad de manejar "carritos de invitado" persistentes, al estilo de frameworks modernos como MedusaJS, es un diferenciador clave que reduce la fricción y aumenta las conversiones.

He actualizado el documento técnico para incorporar esta funcionalidad avanzada y he especificado el uso de **Amazon S3** para el almacenamiento de imágenes, como solicitaste.

Aquí tienes las secciones actualizadas y la nueva sección añadida al documento técnico.

---

### **Secciones Actualizadas y Añadidas al Documento Arquitectónico (Versión 2.2)**

#### **4.3. Estrategia de Almacenamiento de Imágenes: Amazon S3 (Actualizado)**

Para gestionar de forma escalable y eficiente la gran cantidad de fotografías de alta calidad (tanto de los tours como las astrofotografías de los clientes), se utilizará un servicio de almacenamiento de objetos en la nube.

- **Servicio Seleccionado:** **Amazon S3 (Simple Storage Service)**. Es el estándar de la industria, ofreciendo durabilidad, disponibilidad y escalabilidad masivas a bajo costo.
- **Flujo de Carga de Imágenes (desde el Admin Panel):**
  1.  El administrador sube una imagen a través del panel de administración.
  2.  El archivo se envía al backend.
  3.  El backend, utilizando el **AWS SDK para Java**, sube el archivo de forma segura al bucket S3 designado para "Northern Chile".
  4.  S3 devuelve una URL pública y optimizada para la imagen, que puede ser servida a través de **Amazon CloudFront (CDN)** para una entrega global ultra rápida.
  5.  El backend **guarda únicamente esta URL de texto** en la base de datos (ej: en el campo `image_url` de la tabla `tours` o en una nueva tabla `tour_images`).
- **Ventajas:** Desacopla el almacenamiento de archivos de la aplicación, mejora drásticamente el rendimiento de carga, simplifica las copias de seguridad y es altamente rentable.

---

#### **4.5. Manejo de Carritos Persistentes para Invitados (Guest Carts) (Nueva Sección)**

Inspirado en sistemas de e-commerce modernos, la plataforma permitirá a los usuarios (tanto registrados como anónimos) crear un carrito de compras que persista entre sesiones. Esto elimina la necesidad de registrarse para empezar a planificar un viaje.

- **Decisión Arquitectónica:** El estado del carrito se gestionará **en el lado del servidor**, asociado a un identificador único de sesión. Esto es superior al almacenamiento local del navegador (LocalStorage) porque permite al usuario, por ejemplo, empezar en su móvil y continuar más tarde en su ordenador si inicia sesión.

- **Flujo Técnico de un Carrito de Invitado:**

  1.  **Primera Visita:** Cuando un usuario visita el sitio por primera vez, el frontend realiza una petición inicial al backend.
  2.  **Generación de ID:** El backend genera un identificador único y seguro para el carrito (`cart_id`).
  3.  **Cookie de Sesión:** El backend establece este `cart_id` en una **cookie HttpOnly y segura** en el navegador del usuario. Esta cookie se enviará automáticamente en todas las peticiones posteriores.
  4.  **Añadir al Carrito:** Cuando el invitado añade un tour, el frontend envía la petición (ej: `POST /api/cart/items`). El backend utiliza el `cart_id` de la cookie para encontrar el carrito en la base de datos (o crearlo si no existe) y añadir el tour seleccionado.
  5.  **Visitas Posteriores:** Cuando el usuario regresa al sitio días después, el navegador envía la cookie con el `cart_id`. El frontend puede solicitar el contenido del carrito (`GET /api/cart`), y el backend devolverá sus selecciones previas.

- **Flujo de Transición (Login/Registro):**

  - Cuando un usuario con un carrito de invitado decide iniciar sesión o registrarse, ocurre un proceso de "fusión": el backend asocia el `cart_id` de la sesión actual con el `user_id` del usuario recién autenticado. El carrito de invitado se convierte oficialmente en el carrito del usuario registrado.

- **Impacto en el Modelo de Datos (Esquema PostgreSQL):**
  - Se necesitan nuevas tablas para desacoplar el concepto de "carrito" (un borrador) del de "reserva" (una orden confirmada).

* Cuando un usuario con un carrito de invitado decide iniciar sesión o registrarse, ocurre un proceso de "fusión": el backend asocia el `cart_id` de la sesión actual con el `user_id` del usuario recién autenticado. El carrito de invitado se convierte oficialmente en el carrito del usuario registrado.

* **Impacto en el Modelo de Datos (Esquema PostgreSQL):**

  - Se necesitan nuevas tablas para desacoplar el concepto de "carrito" (un borrador) del de "reserva" (una orden confirmada).

  **Tabla: `carts`**

  - `id` (PK, UUID)
  - `user_id` (FK a `users.id`, **NULLABLE**) -> La clave. Si es NULL, es un carrito de invitado.
  - `status` (VARCHAR, e.g., 'ACTIVE', 'COMPLETED', 'ABANDONED')
  - `created_at` (TIMESTAMP)
  - `expires_at` (TIMESTAMP) -> Para poder limpiar carritos abandonados.

  **Tabla: `cart_items`**

  - `id` (PK, UUID)
  - `cart_id` (FK a `carts.id`)
  - `tour_id` (FK a `tours.id`)
  - `tour_date` (DATE)
  - `num_adults` (INTEGER)
  - `num_children` (INTEGER)
    ¡Absolutamente! Es un requisito fundamental y un detalle que eleva la calidad de la experiencia de usuario a un nivel superior. El sistema debe comunicarse con el cliente en el idioma en que realizó la compra.

La clave es tratar el idioma del usuario como un dato más de la transacción. No es solo una preferencia de la UI, sino un atributo de la reserva misma.

Aquí te detallo la arquitectura de la integración multilingüe para el servicio de correos.

---

### **Integración Multilingüe con Google Workspace**

El `EmailService` será modificado para ser "consciente del idioma". Para lograrlo, implementaremos los mecanismos de internacionalización (i18n) estándar de Spring Boot.

#### **Paso 1: Capturar y Almacenar el Idioma del Usuario en la Reserva**

La comunicación del idioma debe fluir desde el frontend hasta la base de datos.

1.  **Frontend (Nuxt):** Cuando el usuario inicia el proceso de checkout, el frontend ya conoce el idioma activo (ej: `'es'`, `'en'`, `'pt'`). Este código de idioma se incluirá en el payload de la API al crear la reserva.

2.  **Backend (DTO y Base de Datos):**

    - El DTO `BookingRequest` se actualiza para incluir el idioma.
    - La tabla `bookings` en PostgreSQL se modifica para almacenar esta información.

    **Tabla `bookings` (con nuevo campo):**

    - `id` (PK, UUID)
    - `user_id` (FK a `users.id`)
    - `tour_id` (FK a `tours.id`)
    - `tour_date` (DATE)
    - `status` (VARCHAR)
    - **`language_code` (VARCHAR, e.g., 'es', 'pt')** -> **¡NUEVO CAMPO!**
    - ... (resto de los campos)

    Al crear una nueva reserva, el backend guardará el `language_code` junto con el resto de los detalles.

#### **Paso 2: Centralizar Todos los Textos de los Correos (i18n Resource Bundles)**

En lugar de escribir texto directamente en el código Java, lo externalizaremos a archivos de propiedades. Esto permite que el sistema elija el texto correcto según el idioma, y facilita la traducción sin tocar el código.

En la carpeta `src/main/resources`, crearemos una familia de archivos `messages`:

**`messages_es.properties` (Español)**

```properties
email.confirmation.subject=¡Tu aventura en Atacama está confirmada! - Reserva #%s
email.confirmation.greeting=Hola %s,
email.confirmation.body=¡Gracias por elegir Northern Chile! Tu reserva para el tour '%s' está confirmada.
email.confirmation.farewell=Nos vemos pronto bajo las estrellas.
```

**`messages_pt.properties` (Portugués)**

```properties
email.confirmation.subject=Sua aventura no Atacama está confirmada! - Reserva #%s
email.confirmation.greeting=Olá %s,
email.confirmation.body=Obrigado por escolher a Northern Chile! Sua reserva para o passeio '%s' está confirmada.
email.confirmation.farewell=Nos vemos em breve sob as estrelas.
```

**`messages_en.properties` (Inglés)**

```properties
email.confirmation.subject=Your Atacama adventure is confirmed! - Booking #%s
email.confirmation.greeting=Hello %s,
email.confirmation.body=Thank you for choosing Northern Chile! Your booking for the '%s' tour is confirmed.
email.confirmation.farewell=See you soon under the stars.
```

#### **Paso 3: Actualizar el `EmailService` para Ser Multilingüe**

El servicio de correo se modificará para utilizar el `MessageSource` de Spring, que es el encargado de leer estos archivos de propiedades.

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MessageSource messageSource; // Inyectamos el gestor de i18n

    // El método ahora recibe la reserva completa (o al menos, el idioma)
    public void sendBookingConfirmationEmail(Booking booking) {
        try {
            // 1. Creamos el objeto Locale a partir del código de idioma guardado
            Locale userLocale = Locale.forLanguageTag(booking.getLanguageCode());

            String toEmail = booking.getUser().getEmail();
            String customerName = booking.getUser().getFullName();
            String tourName = booking.getTour().getName();
            String bookingId = booking.getId().toString();

            // 2. Obtenemos los textos traducidos usando las claves de los archivos .properties
            String subject = messageSource.getMessage("email.confirmation.subject", new Object[]{bookingId}, userLocale);
            String greeting = messageSource.getMessage("email.confirmation.greeting", new Object[]{customerName}, userLocale);
            String body = messageSource.getMessage("email.confirmation.body", new Object[]{tourName}, userLocale);
            String farewell = messageSource.getMessage("email.confirmation.farewell", null, userLocale);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("reservas@northernchile.cl");
            message.setTo(toEmail);
            message.setSubject(subject);

            String emailBody = String.format("%s\n\n%s\n\n%s", greeting, body, farewell);
            message.setText(emailBody);

            mailSender.send(message);

        } catch (Exception e) {
            // Manejar la excepción
            System.err.println("Error al enviar el correo de confirmación: " + e.getMessage());
        }
    }
}
```

### **Flujo Completo (Ejemplo Práctico):**

1.  **Un turista brasileño** navega por la web en portugués (`/pt/tours/...`).
2.  Hace una reserva. El frontend envía la petición a `POST /api/bookings` con el payload `{"tourId": "...", "date": "...", "languageCode": "pt"}`.
3.  El backend crea la reserva en la base de datos y guarda `"pt"` en la columna `language_code`.
4.  El backend llama a `emailService.sendBookingConfirmationEmail(nuevaReserva)`.
5.  El `EmailService`:
    - Crea un `Locale` para `"pt"`.
    - Usa el `MessageSource` para leer las claves del archivo `messages_pt.properties`.
    - Construye y envía un correo con el asunto y el cuerpo **completamente en portugués**.
6.  El cliente recibe una confirmación profesional y en su propio idioma. ¡Excelente punto! La autenticación es la puerta de entrada a toda la experiencia del cliente. Ofrecer "Login con Google" (Social Login) no es solo una conveniencia, es una expectativa estándar para cualquier plataforma moderna. Reduce la fricción del registro a un solo clic, lo que aumenta significativamente la tasa de conversión.

Aquí te presento la arquitectura completa para manejar la autenticación, soportando tanto el registro local (email/contraseña) como la autenticación con Google, de una manera segura y unificada.

---

### **Arquitectura de Autenticación: Local y Social Login (Google)**

#### **1. Estrategia de Autenticación Híbrida**

La plataforma soportará dos métodos de autenticación, pero los tratará como una única identidad de usuario en nuestro sistema.

1.  **Autenticación Local:** El método tradicional de registro con `email` y `contraseña`. La contraseña se almacenará de forma segura en nuestra base de datos usando un hash robusto (ej: bcrypt).
2.  **Autenticación Social (Google):** El usuario delega el proceso de autenticación a Google. Nosotros **nunca vemos ni almacenamos la contraseña de Google del usuario**. Simplemente confiamos en que Google ha verificado su identidad.

El sistema se diseñará para que, si un usuario se registra con `usuario@gmail.com` a través de Google y más tarde intenta registrarse con el mismo email de forma local, el sistema lo reconozca.

#### **2. El Flujo de Autenticación con Google (OAuth 2.0)**

Este es el "baile" técnico que ocurre entre el usuario, nuestro frontend, nuestro backend y Google.

1.  **Inicio (Frontend):**

    - El usuario hace clic en el botón "Iniciar Sesión con Google" en la página de Nuxt.

2.  **Redirección a Google (Gestionado por el Backend):**

    - El botón no llama directamente a Google. En su lugar, es un enlace a un endpoint de nuestro propio backend (ej: `GET /api/oauth2/authorization/google`).
    - Spring Security intercepta esta petición y redirige automáticamente al usuario a la página de consentimiento de Google, incluyendo nuestro `client_id` para que Google sepa quiénes somos.

3.  **Consentimiento del Usuario (Google):**

    - El usuario ve la pantalla de Google pidiendo permiso para que "Northern Chile" acceda a su información básica (email, nombre, foto de perfil).
    - El usuario acepta.

4.  **Callback al Backend (Google -> Backend):**

    - Google redirige al usuario de vuelta a nuestro backend, a una URL de "callback" preconfigurada (ej: `/api/login/oauth2/code/google`). Esta redirección incluye un **código de autorización** temporal.

5.  **Intercambio de Tokens (Backend <-> Google):**

    - Nuestro backend recibe el código de autorización. De forma segura (sin que el usuario lo vea), realiza una llamada directa a los servidores de Google, intercambiando este código (junto con nuestro `client_secret`) por un **token de acceso**.
    - Con este token de acceso, el backend solicita a Google la información del perfil del usuario.

6.  **Procesamiento del Usuario (Lógica de Negocio en Backend):**

    - El backend recibe la información del usuario de Google (ej: `email`, `name`).
    - **Lógica "Find or Create":**
      - **¿Existe un usuario en nuestra tabla `users` con este email?**
        - **Sí:** Lo encontramos y procedemos a autenticarlo.
        - **No:** Creamos un nuevo usuario en nuestra tabla `users` con la información de Google. El campo `password_hash` se deja nulo y se marca el `auth_provider` como 'GOOGLE'.

7.  **Generación de Nuestro Propio Token (JWT):**

    - Una vez que el usuario ha sido encontrado o creado, nuestro backend genera su propio token de autenticación (un **JWT**), que es el que nuestra aplicación utilizará internamente. Este JWT contiene el `id` de nuestro usuario y su rol (`ROLE_CLIENT`).

8.  **Redirección Final al Frontend:**
    - El backend redirige al usuario de vuelta a una página específica del frontend (ej: `/login/callback`), adjuntando el JWT en la URL o en una cookie.
    - El frontend de Nuxt captura este JWT, lo guarda de forma segura (en LocalStorage o una cookie) y redirige al usuario a su panel de control (`/mi-cuenta`), ahora completamente autenticado en nuestra plataforma.

#### **3. Implementación en el Backend (Spring Boot)**

Spring Security hace que este flujo sea sorprendentemente sencillo de implementar.

1.  **Dependencia:** Se añade `spring-boot-starter-oauth2-client` al `pom.xml`.

2.  **Configuración (`application.properties`):**
    Se configuran las credenciales obtenidas desde la Google Cloud Console.

    ```properties
    # application.properties
    spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
    spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
    spring.security.oauth2.client.registration.google.scope=profile,email
    ```

3.  **Lógica "Find or Create" (`CustomOAuth2UserService`):**
    Se crea un servicio personalizado que Spring Security ejecutará después de obtener la información del usuario de Google, donde implementaremos la lógica para buscar o crear el usuario en nuestra base de datos.

#### **4. Impacto en el Modelo de Datos (Esquema PostgreSQL)**

Para soportar la autenticación híbrida, la tabla `users` necesita una ligera modificación.

**Tabla `users` (Actualizada):**

- `id` (PK, UUID)
- `email` (VARCHAR, UNIQUE)
- `password_hash` (VARCHAR, **NULLABLE**) -> Será nulo para usuarios de Google.
- `full_name` (VARCHAR)
- `nationality` (VARCHAR)
- `role` (VARCHAR)
- **`auth_provider` (VARCHAR, e.g., 'LOCAL', 'GOOGLE')** -> **¡NUEVO CAMPO!** Para saber cómo se registró el usuario.
- **`provider_id` (VARCHAR, NULLABLE)** -> **¡NUEVO CAMPO!** Para almacenar el ID único que Google asigna al usuario.
- `created_at` (TIMESTAMP)

Esta estructura nos da la flexibilidad de añadir más proveedores de autenticación social en el futuro (Facebook, Apple, etc.) simplemente añadiendo nuevos valores al campo `auth_provider`.
¡Excelente pregunta! Es fundamental definir no solo la regla de negocio, sino también la herramienta específica que la hará posible y el plan de implementación técnica.

Aquí te presento una propuesta completa y concreta: la API recomendada, por qué la elegimos y el plan de integración detallado para el backend en Spring Boot.

---

### **Integración de la API de Clima: Herramienta y Plan de Implementación**

#### **1. API Seleccionada y Justificación**

Para obtener los datos de pronóstico del tiempo, se recomienda utilizar **WeatherAPI.com**.

**Justificación de la Elección:**

- **Generoso Nivel Gratuito:** Su plan gratuito es robusto, ofreciendo hasta 1 millón de llamadas al mes, lo cual es más que suficiente para el lanzamiento y las primeras etapas de crecimiento de la plataforma.
- **Datos de Pronóstico Detallados:** Ofrece un pronóstico de hasta 14 días, lo que se alinea perfectamente con la necesidad de un calendario de reservas. Lo más importante es que proporciona datos **horarios**, incluyendo la velocidad del viento.
- **Facilidad de Uso:** La API es RESTful, bien documentada y devuelve respuestas JSON limpias y fáciles de procesar.
- **Datos de Viento Precisos:** Devuelve la velocidad del viento en múltiples unidades, incluyendo `wind_kph`. El backend podrá hacer la conversión a nudos (knots) fácilmente (1 nudo ≈ 1.852 kph).

**Alternativa:** OpenWeatherMap es otra opción sólida, pero WeatherAPI.com suele ser preferida por la simplicidad y generosidad de su plan gratuito para este tipo de casos de uso.

#### **2. Arquitectura de la Integración (Backend - Spring Boot)**

La integración se realizará en el backend para proteger la clave de la API (API Key) y para implementar una estrategia de caché eficiente.

##### **Paso 1: Configuración Segura**

La clave de la API y la URL base se gestionarán como propiedades en el `application.properties`, inyectadas a través de variables de entorno para no exponerlas en el código fuente.

```properties
# application.properties
weather.api.baseurl=http://api.weatherapi.com/v1/forecast.json
weather.api.key=${WEATHER_API_KEY}
weather.api.location=San Pedro de Atacama
```

##### **Paso 2: Creación de un Servicio Dedicado (`WeatherService`)**

Se creará un servicio específico para encapsular toda la lógica de interacción con la API de clima. Esto sigue el principio de responsabilidad única.

- **Mapeo de la Respuesta (DTOs):** Crearemos clases Java (POJOs/DTOs) que representen la estructura de la respuesta JSON de la API. Esto nos permite trabajar con objetos tipados y no con JSON crudo.

  ```java
  // DTOs simplificados para ilustrar
  public class WeatherForecastResponse {
      private Forecast forecast;
  }

  public class Forecast {
      private List<ForecastDay> forecastday;
  }

  public class ForecastDay {
      private String date;
      private Day day;
      private List<Hour> hour;
  }

  public class Hour {
      private String time;
      private double temp_c;
      private double wind_kph; // Velocidad del viento en km/h
  }
  ```

- **Implementación del Servicio:** El servicio utilizará `RestTemplate` o `WebClient` de Spring para realizar la llamada HTTP a la API.

  ```java
  import org.springframework.beans.factory.annotation.Value;
  import org.springframework.stereotype.Service;
  import org.springframework.web.client.RestTemplate;

  @Service
  public class WeatherService {

      private final RestTemplate restTemplate = new RestTemplate();

      @Value("${weather.api.key}")
      private String apiKey;

      @Value("${weather.api.baseurl}")
      private String baseUrl;

      @Value("${weather.api.location}")
      private String location;

      // Este método será la puerta de entrada para obtener el pronóstico
      public WeatherForecastResponse getForecast(int days) {
          String url = String.format("%s?key=%s&q=%s&days=%d&aqi=no&alerts=no",
                                   baseUrl, apiKey, location, days);

          try {
              return restTemplate.getForObject(url, WeatherForecastResponse.class);
          } catch (Exception e) {
              // Loggear el error y manejarlo apropiadamente
              System.err.println("Error al obtener el pronóstico del tiempo: " + e.getMessage());
              return null; // O lanzar una excepción personalizada
          }
      }
  }
  ```

##### **Paso 3: Implementación de una Estrategia de Caché**

Realizar una llamada a la API de clima por cada solicitud de disponibilidad de un usuario es ineficiente y agotaría rápidamente la cuota del plan gratuito. Implementaremos una caché en el backend.

- **Mecanismo:** Se utilizará el sistema de caché de Spring Boot (`@Cacheable`).
- **Lógica:** El resultado de la llamada a la API (`getForecast`) se almacenará en la memoria del servidor durante un período de tiempo configurable (ej: **1 a 3 horas**). Si otra solicitud llega dentro de ese período, el backend devolverá la respuesta cacheada sin volver a llamar a la API externa.

  ```java
  // Habilitar caché en la clase principal de Spring Boot
  @SpringBootApplication
  @EnableCaching // <-- Habilitar caché
  public class NorthernChileApplication { ... }

  // Añadir la anotación @Cacheable al método del servicio
  @Service
  public class WeatherService {
      // ...

      @Cacheable(value = "weatherForecast", key = "#days") // El caché se llamará "weatherForecast"
      public WeatherForecastResponse getForecast(int days) {
          // ... la misma lógica de llamada a la API
          // Este código solo se ejecutará si no hay un resultado válido en el caché
      }
  }
  ```

##### **Paso 4: Integración con la Lógica de Negocio (`BookingService`)**

Finalmente, el `BookingService` (o el servicio que gestiona la disponibilidad) utilizará el `WeatherService` para aplicar la regla de los +25 nudos.

- **Flujo de Validación:**
  1.  El `BookingService` recibe una solicitud para una fecha y un tour `is_wind_sensitive`.
  2.  Llama al `weatherService.getForecast(14)`. Este devolverá el pronóstico (probablemente desde el caché).
  3.  El servicio busca el día correspondiente en la respuesta del pronóstico.
  4.  Itera sobre los datos **horarios** (`hour`) para ese día.
  5.  En cada hora, convierte `wind_kph` a nudos.
  6.  Si **algún valor horario** supera los 25 nudos, la validación falla y la fecha se marca como no disponible por "Pronóstico de Viento Fuerte".

Esta arquitectura garantiza una integración segura, eficiente y robusta, proporcionando a la plataforma los datos necesarios para aplicar reglas de negocio críticas y ofrecer una experiencia de usuario transparente.
¡Excelente pregunta! Es fundamental definir no solo la regla de negocio, sino también la herramienta específica que la hará posible y el plan de implementación técnica.

Aquí te presento una propuesta completa y concreta: la API recomendada, por qué la elegimos y el plan de integración detallado para el backend en Spring Boot.

---

### **Integración de la API de Clima: Herramienta y Plan de Implementación**

#### **1. API Seleccionada y Justificación**

Para obtener los datos de pronóstico del tiempo, se recomienda utilizar **WeatherAPI.com**.

**Justificación de la Elección:**

- **Generoso Nivel Gratuito:** Su plan gratuito es robusto, ofreciendo hasta 1 millón de llamadas al mes, lo cual es más que suficiente para el lanzamiento y las primeras etapas de crecimiento de la plataforma.
- **Datos de Pronóstico Detallados:** Ofrece un pronóstico de hasta 14 días, lo que se alinea perfectamente con la necesidad de un calendario de reservas. Lo más importante es que proporciona datos **horarios**, incluyendo la velocidad del viento.
- **Facilidad de Uso:** La API es RESTful, bien documentada y devuelve respuestas JSON limpias y fáciles de procesar.
- **Datos de Viento Precisos:** Devuelve la velocidad del viento en múltiples unidades, incluyendo `wind_kph`. El backend podrá hacer la conversión a nudos (knots) fácilmente (1 nudo ≈ 1.852 kph).

**Alternativa:** OpenWeatherMap es otra opción sólida, pero WeatherAPI.com suele ser preferida por la simplicidad y generosidad de su plan gratuito para este tipo de casos de uso.

#### **2. Arquitectura de la Integración (Backend - Spring Boot)**

La integración se realizará en el backend para proteger la clave de la API (API Key) y para implementar una estrategia de caché eficiente.

##### **Paso 1: Configuración Segura**

La clave de la API y la URL base se gestionarán como propiedades en el `application.properties`, inyectadas a través de variables de entorno para no exponerlas en el código fuente.

```properties
# application.properties
weather.api.baseurl=http://api.weatherapi.com/v1/forecast.json
weather.api.key=${WEATHER_API_KEY}
weather.api.location=San Pedro de Atacama
```

##### **Paso 2: Creación de un Servicio Dedicado (`WeatherService`)**

Se creará un servicio específico para encapsular toda la lógica de interacción con la API de clima. Esto sigue el principio de responsabilidad única.

- **Mapeo de la Respuesta (DTOs):** Crearemos clases Java (POJOs/DTOs) que representen la estructura de la respuesta JSON de la API. Esto nos permite trabajar con objetos tipados y no con JSON crudo.

  ```java
  // DTOs simplificados para ilustrar
  public class WeatherForecastResponse {
      private Forecast forecast;
  }

  public class Forecast {
      private List<ForecastDay> forecastday;
  }

  public class ForecastDay {
      private String date;
      private Day day;
      private List<Hour> hour;
  }

  public class Hour {
      private String time;
      private double temp_c;
      private double wind_kph; // Velocidad del viento en km/h
  }
  ```

- **Implementación del Servicio:** El servicio utilizará `RestTemplate` o `WebClient` de Spring para realizar la llamada HTTP a la API.

  ```java
  import org.springframework.beans.factory.annotation.Value;
  import org.springframework.stereotype.Service;
  import org.springframework.web.client.RestTemplate;

  @Service
  public class WeatherService {

      private final RestTemplate restTemplate = new RestTemplate();

      @Value("${weather.api.key}")
      private String apiKey;

      @Value("${weather.api.baseurl}")
      private String baseUrl;

      @Value("${weather.api.location}")
      private String location;

      // Este método será la puerta de entrada para obtener el pronóstico
      public WeatherForecastResponse getForecast(int days) {
          String url = String.format("%s?key=%s&q=%s&days=%d&aqi=no&alerts=no",
                                   baseUrl, apiKey, location, days);

          try {
              return restTemplate.getForObject(url, WeatherForecastResponse.class);
          } catch (Exception e) {
              // Loggear el error y manejarlo apropiadamente
              System.err.println("Error al obtener el pronóstico del tiempo: " + e.getMessage());
              return null; // O lanzar una excepción personalizada
          }
      }
  }
  ```

##### **Paso 3: Implementación de una Estrategia de Caché**

Realizar una llamada a la API de clima por cada solicitud de disponibilidad de un usuario es ineficiente y agotaría rápidamente la cuota del plan gratuito. Implementaremos una caché en el backend.

- **Mecanismo:** Se utilizará el sistema de caché de Spring Boot (`@Cacheable`).
- **Lógica:** El resultado de la llamada a la API (`getForecast`) se almacenará en la memoria del servidor durante un período de tiempo configurable (ej: **1 a 3 horas**). Si otra solicitud llega dentro de ese período, el backend devolverá la respuesta cacheada sin volver a llamar a la API externa.

  ```java
  // Habilitar caché en la clase principal de Spring Boot
  @SpringBootApplication
  @EnableCaching // <-- Habilitar caché
  public class NorthernChileApplication { ... }

  // Añadir la anotación @Cacheable al método del servicio
  @Service
  public class WeatherService {
      // ...

      @Cacheable(value = "weatherForecast", key = "#days") // El caché se llamará "weatherForecast"
      public WeatherForecastResponse getForecast(int days) {
          // ... la misma lógica de llamada a la API
          // Este código solo se ejecutará si no hay un resultado válido en el caché
      }
  }
  ```

##### **Paso 4: Integración con la Lógica de Negocio (`BookingService`)**

Finalmente, el `BookingService` (o el servicio que gestiona la disponibilidad) utilizará el `WeatherService` para aplicar la regla de los +25 nudos.

- **Flujo de Validación:**
  1.  El `BookingService` recibe una solicitud para una fecha y un tour `is_wind_sensitive`.
  2.  Llama al `weatherService.getForecast(14)`. Este devolverá el pronóstico (probablemente desde el caché).
  3.  El servicio busca el día correspondiente en la respuesta del pronóstico.
  4.  Itera sobre los datos **horarios** (`hour`) para ese día.
  5.  En cada hora, convierte `wind_kph` a nudos.
  6.  Si **algún valor horario** supera los 25 nudos, la validación falla y la fecha se marca como no disponible por "Pronóstico de Viento Fuerte".

Esta arquitectura garantiza una integración segura, eficiente y robusta, proporcionando a la plataforma los datos necesarios para aplicar reglas de negocio críticas y ofrecer una experiencia de usuario transparente.
¡Perfecto! Has llegado al núcleo de la operación. La forma en que se definen y gestionan los tours y su disponibilidad es la base de todo el sistema de reservas. Tu descripción es exactamente la lógica correcta, y ahora la traduciremos a una arquitectura técnica clara.

Olvida la idea de que los tours son "productos estáticos". En la práctica, son **eventos programados**. Un "Tour Geiser del Tatio" no es algo que se vende en abstracto; lo que se vende es el **"Tour Geiser del Tatio del 18 de Octubre de 2025 a las 04:30 AM"**.

Esta distinción es fundamental y resolverá toda la confusión.

---

### **Arquitectura de Disponibilidad de Tours: Un Modelo Basado en "Instancias de Tour"**

En lugar de tener solo una tabla `tours`, vamos a introducir un concepto nuevo: **`tour_schedules`** (o "Horarios de Tour"). Esta tabla representará cada instancia específica y programada de un tour que se ofrece a la venta.

#### **1. El Modelo de Datos Revisado**

**Tabla `tours` (El Catálogo)**
Esta tabla sigue siendo el **catálogo maestro** de los tipos de tour que ofreces. Contiene la información que **no cambia** de un día para otro:

- `id` (PK, UUID)
- `name` (ej: "Tour Astronómico Premium")
- `description` (TEXT)
- `category` (ej: 'ASTRONOMICAL')
- `price_adult` (DECIMAL)
- `price_child` (DECIMAL)
- `duration_hours` (INTEGER)
- `default_max_participants` (INTEGER, ej: 20)
- `is_wind_sensitive` (BOOLEAN)
- ... (otros datos generales del tour)

**Tabla `tour_schedules` (La Agenda de Eventos)**
Esta es la **tabla clave** que define la disponibilidad real. Cada fila es una **instancia única y reservable** de un tour.

- `id` (PK, UUID)
- `tour_id` (FK a `tours.id`)
- `start_datetime` (TIMESTAMP) -> **Fecha y hora exactas de inicio** (ej: '2025-10-18 04:30:00')
- `max_participants` (INTEGER) -> **Cupo para esta instancia específica**. Puede ser diferente del valor por defecto del tour.
- `status` (VARCHAR, e.g., 'OPEN', 'CLOSED', 'CANCELLED')
- `assigned_guide_id` (FK a `users.id`, NULLABLE) -> Para saber quién dirige este evento.

**Tabla `bookings` (Las Reservas)**
Ahora, las reservas no se asocian directamente al tour genérico, sino a la **instancia específica** de ese tour.

- `id` (PK, UUID)
- `user_id` (FK a `users.id`)
- **`schedule_id` (FK a `tour_schedules.id`)** -> **¡CAMBIO CLAVE!**
- ... (resto de los campos de la reserva)

#### **2. Cómo Funciona en la Práctica (El Flujo)**

##### **A. El Admin Panel - La Herramienta de Programación**

El Panel de Administración tendrá una nueva sección llamada "Agenda" o "Calendario de Tours". Aquí, Alex (el admin) podrá:

1.  **Programar Tours Recurrentes:**

    - Podrá seleccionar el "Tour Astronómico Premium" y decir: "Generar este tour **todos los días** del próximo mes, a las 20:00, con un cupo de 20 personas, excepto los días de luna llena".
    - El backend recibirá esta orden y creará automáticamente una fila en `tour_schedules` para cada día válido.

2.  **Programar un Tour Único (Tu ejemplo):**

    - Alex quiere añadir un tour a los Géisers que no estaba planeado.
    - Va al día "X" en el calendario del admin.
    - Hace clic en "Añadir Tour".
    - Se abre un formulario donde elige "Tour Géisers del Tatio" de una lista (desde la tabla `tours`), establece la **fecha y hora de inicio**, y define los **cupos** para ese día específico.
    - Al guardar, el sistema crea **una única fila** nueva en `tour_schedules`.

3.  **Gestionar Instancias Individuales:**
    - ¿El guía se enfermó? Alex puede entrar a la instancia del "18 de Octubre" y cambiar el `status` a `'CANCELLED'`.
    - ¿Consiguió una van más grande? Puede editar la instancia y aumentar el `max_participants` de 20 a 25.

##### **B. La Web Pública - El Calendario de Reservas del Cliente**

1.  **Petición de Disponibilidad:**

    - Cuando un cliente entra a la página del "Tour Astronómico Premium" y mira el calendario, el frontend pregunta al backend: `GET /api/availability?tourId={id_tour_astronomico}&month=10&year=2025`.

2.  **Respuesta Inteligente del Backend:**

    - El backend consulta la tabla `tour_schedules` y busca todas las instancias (`schedule_id`) para ese `tour_id` en el mes solicitado.
    - Para cada instancia encontrada, calcula los **cupos disponibles**: `schedule.max_participants - (suma de participantes en bookings asociados a ese schedule_id)`.
    - Luego, enriquece esta información con las reglas de negocio (fase lunar, clima).
    - La respuesta al frontend será una lista de días con su estado:

    ```json
    [
      { "date": "2025-10-17", "status": "AVAILABLE", "available_slots": 15 },
      {
        "date": "2025-10-18",
        "status": "FEW_SLOTS_LEFT",
        "available_slots": 3
      },
      { "date": "2025-10-19", "status": "SOLD_OUT", "available_slots": 0 },
      { "date": "2025-10-20", "status": "UNAVAILABLE_MOON" }
    ]
    ```

3.  **El Proceso de Reserva:**
    - Cuando el cliente elige una fecha disponible (ej: 17 de Octubre), el frontend ya sabe internamente a qué `schedule_id` corresponde.
    - Al crear la reserva, le dirá al backend: "Quiero 2 cupos para el **schedule_id** `abc-123-def-456`". El backend solo tiene que validar si quedan cupos para esa instancia específica.

#### **Ventajas de esta Arquitectura:**

- **Flexibilidad Total:** Permite tener tours fijos y recurrentes, pero también añadir tours únicos y espontáneos.
- **Claridad:** Separa el "qué se ofrece" (`tours`) del "cuándo y cómo se ofrece" (`tour_schedules`).
- **Gestión de Recursos:** Permite asignar guías y controlar cupos por evento, no por producto.
- **Escalabilidad:** Es el modelo que usan todas las grandes plataformas de eventos y reservas (desde aerolíneas hasta venta de entradas a conciertos).
  ¡Excelente pregunta! Los tours privados son un producto de alto valor y requieren una lógica de negocio y un flujo de usuario completamente diferentes a los tours regulares. No se "reservan", se "solicitan" o "cotizan".

Tratar de forzar los tours privados en el mismo sistema de calendario que los tours compartidos sería un error. La arquitectura debe reflejar esta diferencia fundamental. Aquí te presento cómo manejaríamos los tours privados de manera elegante y eficiente.

---

### **Arquitectura para la Gestión de Tours Privados**

#### **1. Separación de Conceptos: No es una Reserva, es una "Solicitud"**

Un tour privado no tiene un horario ni un precio fijo. El precio depende del número de personas, las personalizaciones y la fecha. Por lo tanto, el flujo del usuario no termina en un pago, sino en el envío de una solicitud que el administrador debe aprobar y cotizar.

#### **2. El Flujo de Usuario para un Tour Privado**

1.  **Página de "Tours Especiales / Privados":**

    - Esta página no tendrá un calendario de reservas. En su lugar, tendrá un **formulario de solicitud de tour privado**.
    - Mostrará los beneficios de un tour privado: exclusividad, personalización, ritmo propio, ideal para fotógrafos o familias.

2.  **El Formulario de Solicitud:**

    - **Fecha y Hora Deseadas:** El usuario selecciona una fecha y una hora preferida (ej: "15 de Noviembre, 20:00").
    - **Tipo de Experiencia:** Un selector con los tours base que se pueden hacer de forma privada (ej: "Tour Astronómico Privado", "Valle de la Luna Privado").
    - **Número de Participantes:** Adultos y niños.
    - **Datos de Contacto:** Nombre, email, teléfono (WhatsApp).
    - **Necesidades Especiales:** Un campo de texto abierto para que el cliente pueda escribir sus deseos (ej: "Nos enfocaremos en astrofotografía", "Vamos con una persona mayor, necesitamos un ritmo lento", "Queremos celebrar un cumpleaños").

3.  **Envío y Confirmación:**
    - Al enviar el formulario, **no se realiza ningún pago**.
    - El usuario ve una página de agradecimiento que dice: _"Hemos recibido tu solicitud de tour privado. Nuestro equipo revisará la disponibilidad y te contactará en las próximas 24 horas con una cotización personalizada y los próximos pasos."_
    - Se envía un email automático al usuario confirmando la recepción de su solicitud.

#### **3. El Flujo del Administrador (Admin Panel)**

1.  **Notificación y Nueva Sección:**

    - El Panel de Administración tendrá una nueva sección llamada **"Solicitudes de Tours Privados"**.
    - Cuando llega una nueva solicitud, Alex recibe una notificación por correo electrónico.

2.  **Gestión de la Solicitud:**

    - En el panel, Alex verá la solicitud con todos los detalles.
    - Tendrá que realizar dos acciones clave:
      - **Verificar Disponibilidad:** Consultará su calendario interno de guías y recursos para ver si puede cubrir la solicitud en la fecha y hora pedidas.
      - **Calcular el Precio:** Basado en el número de personas y las personalizaciones, calculará el costo total del tour privado.

3.  **Respuesta al Cliente (Generación de la Oferta):**

    - Desde el mismo panel, Alex podrá cambiar el estado de la solicitud a **"Cotizado"** y escribir la respuesta.
    - El panel tendrá un botón: **"Enviar Cotización y Enlace de Pago"**.
    - Al hacer clic, el sistema:
      - Envía un email al cliente con la propuesta: _"¡Buenas noticias! Tenemos disponibilidad para tu Tour Astronómico Privado. El costo total es de X. Para confirmar tu reserva, por favor realiza el pago a través del siguiente enlace seguro."_
      - **Genera dinámicamente un enlace de pago único** a través de la pasarela (Transbank/Mercado Pago) por el monto exacto cotizado.

4.  **Confirmación Final:**
    - El cliente recibe el correo, hace clic en el enlace y paga.
    - Una vez que el pago se confirma (vía webhook), el estado de la solicitud en el panel cambia automáticamente a **"Confirmado"**, y se crea una entrada en el calendario de operaciones para el tour privado.

#### **4. Impacto en el Modelo de Datos (Esquema PostgreSQL)**

Necesitamos una nueva tabla para gestionar este flujo de solicitudes.

**Tabla: `private_tour_requests`**

- `id` (PK, UUID)
- `user_id` (FK a `users.id`, NULLABLE si el solicitante no es un usuario registrado)
- `customer_name` (VARCHAR)
- `customer_email` (VARCHAR)
- `customer_phone` (VARCHAR)
- `requested_tour_type` (VARCHAR, ej: "Astronómico")
- `requested_datetime` (TIMESTAMP)
- `num_adults` (INTEGER)
- `num_children` (INTEGER)
- `special_requests` (TEXT)
- `status` (VARCHAR, e.g., 'PENDING', 'QUOTED', 'CONFIRMED', 'REJECTED')
- `quoted_price` (DECIMAL, NULLABLE)
- `payment_link_id` (VARCHAR, NULLABLE) -> Para el enlace de pago único.
- `created_at` (TIMESTAMP)

#### **Ventajas de esta Arquitectura:**

- **Flujo Correcto:** Trata a los tours privados como lo que son: un producto de alta gama que requiere una interacción humana y una cotización, no una simple transacción.
- **No Contamina el Sistema Principal:** Mantiene la lógica de los tours compartidos (con cupos y horarios fijos) completamente separada de la lógica de solicitudes privadas.
- **Profesionalismo:** El proceso de "solicitud -> cotización -> enlace de pago" es el estándar de la industria para servicios personalizados y de alto valor.
- **Eficiencia para el Admin:** Alex puede gestionar todo el ciclo de vida de una venta privada desde un único lugar, sin necesidad de emails o planillas externas.
  ¡Perfecto! Has tomado las decisiones clave. Ahora voy a traducir esas decisiones en definiciones técnicas claras y concisas, listas para que las integres en el documento final.

Aquí tienes los puntos definidos para cada una de las áreas que mencionaste.

---

### **Definiciones de Lógica de Negocio y Flujos Operativos**

#### **1. Gestión de Impuestos (IVA)**

- **Definición de Negocio:** Todos los precios mostrados en la plataforma son finales para el cliente e **incluyen el IVA** (actualmente 19% en Chile).
- **Impacto Técnico:**
  - **Base de Datos:** La tabla `bookings` almacenará los valores desglosados para una contabilidad clara. Se añadirán los campos: `subtotal` (monto antes de impuestos), `tax_amount` (monto del impuesto) y `total_amount` (monto pagado por el cliente).
  - **Backend:** Al momento de crear una reserva, el backend será el responsable de calcular el `subtotal` y el `tax_amount` a partir del `total_amount` pagado, basándose en una tasa de impuesto configurable en el archivo `application.properties`.
  - **Comunicaciones:** El email de confirmación y el detalle de la reserva en el portal del cliente mostrarán el desglose del IVA para total transparencia.

#### **2. Política de Cancelaciones y Reembolsos**

Se definen dos escenarios de cancelación:

- **A. Cancelación por parte del Cliente:**

  - **Regla de Negocio:** El cliente puede cancelar su reserva y recibir un reembolso completo siempre que lo haga con **más de 24 horas de antelación** al inicio del tour. Las cancelaciones dentro de las 24 horas no son reembolsables.
  - **Impacto Técnico:**
    - **Portal del Viajero:** El cliente tendrá un botón para "Cancelar Reserva" en su panel.
    - **Backend:** Se creará un endpoint (`POST /api/bookings/{id}/cancel`) que, al ser llamado, verificará la regla de las 24 horas contra el `start_datetime` del `tour_schedule`. Si la regla se cumple, el `status` del `booking` cambiará a `CANCELLED_BY_CLIENT` y se iniciará el proceso de reembolso.

- **B. Cancelación por parte del Administrador:**

  - **Regla de Negocio:** El administrador puede cancelar un tour programado en cualquier momento (ej: por mal tiempo, problemas operativos). En este caso, todos los clientes afectados tienen derecho a un reembolso completo, sin importar la antelación.
  - **Impacto Técnico:**
    - **Admin Panel:** Alex tendrá un botón para "Cancelar Tour" en una instancia específica del calendario (`tour_schedule`).
    - **Backend:** Al activarse, el backend cambiará el `status` de la instancia a `CANCELLED` y, en cascada, actualizará todos los `bookings` asociados a `CANCELLED_BY_ADMIN`, iniciando el proceso de reembolso para cada uno y enviando un email de notificación a los clientes afectados.

- **Gestión de Reembolsos (La Plata):**
  - **Definición del Proceso (Fase 1):** Para el lanzamiento inicial, el proceso de devolución del dinero será **manual**.
  - **Impacto Técnico:**
    - Cuando una cancelación válida ocurre, el sistema marcará la reserva con un estado `AWAITING_REFUND`.
    - Alex recibirá una notificación y deberá procesar la devolución manualmente desde el portal de la pasarela de pago (Transbank o Mercado Pago).
    - Una vez realizada la devolución, deberá marcar la reserva como `REFUNDED` en el Admin Panel.
    - **Nota:** Se diseñará la arquitectura para que, en una fase futura, este proceso pueda ser automatizado a través de la API de la pasarela de pago.

#### **3. Notificaciones para el Administrador**

- **Definición de Negocio:** Se requiere una **notificación por email inmediata** al administrador cada vez que ocurra un evento de negocio clave.
- **Impacto Técnico:**
  - El `EmailService` se expandirá para incluir métodos como `sendNewBookingNotificationToAdmin` y `sendNewPrivateRequestNotificationToAdmin`.
  - Estos métodos se activarán automáticamente después de que un cliente complete una reserva o envíe una solicitud de tour privado.
  - La dirección de correo del administrador será un parámetro configurable en `application.properties` (ej: `admin.notification.email=alex@northernchile.cl`).

#### **4. Flujo de Entrega de Astrofotografías**

- **Definición de Negocio (Fase 1):** El lanzamiento inicial de la plataforma **no incluirá un sistema automatizado de entrega de fotografías** a través del portal del cliente. La entrega se seguirá realizando por los medios actuales (email, WhatsApp, etc.).
- **Impacto Técnico:**
  - No se requiere desarrollo para esta funcionalidad en la Fase 1, lo que permite enfocar los recursos en las funcionalidades de reserva y pago.
  - La arquitectura de la base de datos (que ya asocia a un `user` con un `tour_schedule`) está diseñada para que esta funcionalidad pueda ser añadida fácilmente en una fase futura sin necesidad de grandes cambios estructurales.

#### **5. Sistema de Cupones y Descuentos**

- **Definición de Negocio:** El lanzamiento inicial de la plataforma **no incluirá un sistema de cupones o descuentos**. Todos los precios son finales.
- **Impacto Técnico:** No se requiere desarrollo para esta funcionalidad en la Fase 1. El modelo de datos y el flujo de checkout se mantienen simples, enfocados en la transacción directa. ¡Entendido! Esta precisión es clave y hace el modelo aún más flexible y realista. David no es un simple operador, es un socio creativo que puede proponer y crear sus propias experiencias.

He ajustado la arquitectura para reflejar este modelo de negocio: **David tiene autonomía para crear sus tours**, pero el sistema garantiza que toda la gestión financiera y de publicación pase por el control de Alex.

Aquí tienes la versión revisada de la arquitectura de administración y roles, lista para integrar.

---

### **Definiciones Actualizadas: Arquitectura de Administración y Roles (Modelo Revisado)**

#### **1. Modelo de Roles y Permisos (RBAC)**

El modelo de `SUPER_ADMIN` y `PARTNER_ADMIN` se mantiene, pero las responsabilidades del `PARTNER_ADMIN` se expanden para permitirle mayor autonomía creativa, manteniendo el control financiero centralizado.

- **`ROLE_SUPER_ADMIN` (Alex - El Propietario):**

  - **Control Total:** Acceso sin restricciones a todo el Panel de Administración.
  - **Gestión Financiera Central:** Visualiza todos los reportes de ventas, gestiona las pasarelas de pago y es el único que puede ver la información consolidada del negocio.
  - **Curador de Contenido:** Tiene el poder de **publicar o despublicar** cualquier tour en el sitio web público.
  - **Gestión de Usuarios:** Puede crear, modificar y eliminar cualquier cuenta de usuario, incluyendo la creación de nuevas cuentas de `PARTNER_ADMIN`.

- **`ROLE_PARTNER_ADMIN` (David - El Colaborador):**
  - **Autonomía Creativa:** Puede **crear nuevos tours desde cero** dentro del panel. Estos tours, por defecto, se crean en un estado de "borrador" y no son visibles públicamente.
  - **Gestión de Sus Propios Tours:** Puede editar, programar y gestionar las reservas y manifiestos **únicamente de los tours que él ha creado**.
  - **Visibilidad Financiera Restringida:** Puede generar reportes de ventas detallados, pero **solo de sus propios tours**. No tiene acceso a los datos de los tours de Alex ni a los totales de la empresa.

#### **2. El Flujo de Trabajo para un Nuevo Tour de David**

Este es el nuevo ciclo de vida de un tour creado por un `PARTNER_ADMIN`:

1.  **Creación (Tarea de David):**

    - David inicia sesión y, en su panel restringido, tiene un botón para "Crear Nuevo Tour".
    - Completa el formulario multilingüe con toda la información de su nueva experiencia (descripción, precios sugeridos, duración, fotos, etc.).
    - Al guardar, el tour se crea en la base de datos con un nuevo `status`: **`DRAFT` (Borrador)**, y se le asigna a él como propietario. Este tour **no es visible** para los clientes en el sitio web público.

2.  **Revisión y Publicación (Tarea de Alex):**

    - Alex, en su panel de `SUPER_ADMIN`, ve una notificación o una lista de "Tours Pendientes de Revisión".
    - Revisa el tour creado por David. Puede hacer ajustes si es necesario (ej: alinear el precio con la estrategia comercial de la empresa).
    - Cuando está conforme, Alex tiene el botón "Publicar". Al hacer clic, cambia el `status` del tour a **`PUBLISHED` (Publicado)**. Solo en este momento el tour aparece en la web pública y David puede empezar a programar fechas para él.

3.  **Programación y Operación (Tarea de David):**

    - Una vez que su tour está publicado, David puede acceder a él desde su calendario de programación y empezar a crear las instancias (`tour_schedules`) en los días y horas que desee, como se definió anteriormente.
    - A partir de aquí, gestiona sus reservas y manifiestos de forma autónoma.

4.  **Conciliación (Tarea de Alex):**
    - El flujo de cobro y liquidación no cambia. Todas las ventas pasan por las cuentas de Alex, y él puede generar el reporte de ventas de David para la conciliación.

#### **3. Admin Page en Español y la Gestión de Contenido Multilingüe**

Esta definición se mantiene exactamente igual. Es la solución perfecta para el problema:

- **Interfaz del Admin en Español:** Toda la navegación, botones y etiquetas del Panel de Administración estarán en español para ambos roles.
- **Campos de Contenido Multilingües:** Al crear o editar un tour, el formulario presentará pestañas (`Español`, `English`, `Português`) para que el administrador (sea Alex o David) pueda introducir el título y la descripción en cada idioma.

#### **4. Resumen de Impactos Técnicos de esta Definición (Revisado)**

- **Base de Datos:**

  - La tabla `users` se divide en `users`, `roles`, y `user_roles` para un control de acceso granular.
  - La tabla `tours` necesita un campo `owner_id` (FK a `users.id`) para saber quién creó el tour.
  - La tabla `tours` también necesita un campo `status` (VARCHAR, e.g., `'DRAFT'`, `'PUBLISHED'`, `'ARCHIVED'`) para gestionar el ciclo de vida de la publicación.

- **Backend (Spring Security):**

  - La lógica de RBAC se vuelve más detallada. Por ejemplo, el endpoint `PUT /api/admin/tours/{id}` debe verificar no solo que el usuario es un `PARTNER_ADMIN`, sino que el `owner_id` del tour que intenta modificar coincide con su propio `id`.
  - Se necesitará un endpoint específico para el `SUPER_ADMIN` para cambiar el estado de un tour (ej: `PUT /api/admin/tours/{id}/status`).

- **Backend (API):**

  - El endpoint `GET /api/tours` (para la web pública) debe filtrar y devolver **solo los tours con `status = 'PUBLISHED'`**.
  - El endpoint `GET /api/admin/tours` debe tener una lógica diferente según el rol: el `SUPER_ADMIN` ve todos los tours, mientras que el `PARTNER_ADMIN` solo ve los tours donde él es el `owner_id`.

- **Frontend (Admin Panel):**
  - La lógica de renderizado condicional se mantiene: la interfaz cambia según el rol del usuario que inicia sesión.
  - El `SUPER_ADMIN` verá botones de "Publicar" y "Revisar", mientras que el `PARTNER_ADMIN` no. <BS>¡Entendido! Esto simplifica significativamente el flujo de trabajo y le da a David (y a futuros colaboradores) una autonomía casi total sobre sus productos. Alex sigue manteniendo el control financiero y la visión global, pero delega por completo la gestión del ciclo de vida de los tours a sus creadores.

He ajustado la arquitectura para reflejar este modelo de **confianza y autonomía**. El flujo de "revisión y aprobación" se elimina.

---

### **Definición del Flujo de Publicación (Modelo de Autonomía Total)**

Este modelo se basa en la confianza y en empoderar a los colaboradores (`PARTNER_ADMIN`) para que gestionen su portafolio de productos de principio a fin. El `SUPER_ADMIN` actúa como un supervisor con acceso global, pero no como un cuello de botella para la publicación.

#### **1. El Concepto: Autonomía sobre la Propiedad**

- **Propiedad (`owner_id`):** Este campo sigue siendo la piedra angular. Cada tour pertenece a quien lo crea.
- **Gestión del Ciclo de Vida por el Propietario:** El creador de un tour (`owner_id`) tiene control total sobre el ciclo de vida de **sus propios tours**, incluyendo la publicación, despublicación y edición de todos sus campos.

#### **2. El Flujo Paso a Paso (Revisado)**

##### **Paso 1: David Crea y Publica su Tour (`PARTNER_ADMIN`)**

1.  David inicia sesión y va a su sección "Mis Tours".
2.  Hace clic en "Crear Nuevo Tour".
3.  Rellena toda la información, incluyendo los campos críticos como el precio, la duración y los cupos por defecto.
4.  Junto al formulario, ve un interruptor o checkbox que dice **"Publicado"**, que por defecto está desactivado (el tour se crea como `DRAFT`).
5.  Cuando está listo para que su tour sea visible en la web, simplemente activa el interruptor "Publicado" y guarda. No necesita ninguna aprobación.
6.  Al guardar, el `status` del tour cambia a `PUBLISHED`. El tour aparece **inmediatamente** en el sitio web público.

##### **Paso 2: David Edita y Archiva su Tour (`PARTNER_ADMIN`)**

1.  En cualquier momento, David puede volver a la lista de "Mis Tours".
2.  Puede abrir uno de sus tours y **editar cualquier campo**, incluyendo el precio. Los cambios se reflejarán en la web pública al guardar (aunque no afectarán a las reservas ya hechas).
3.  Si decide dejar de ofrecer un tour temporalmente o de forma permanente, puede desactivar el interruptor "Publicado". Esto cambiará el `status` a `ARCHIVED` (o `DRAFT`). El tour desaparecerá de la lista pública, pero todas sus reservas existentes y datos históricos se mantendrán en el sistema.

##### **Paso 3: Alex Supervisa y Gestiona (`SUPER_ADMIN`)**

1.  Alex, como `SUPER_ADMIN`, tiene una vista de **todos** los tours en el sistema, independientemente de quién sea el propietario.
2.  Tiene **permisos de anulación (override)**. Si necesita ajustar el precio de un tour de David por una razón estratégica, puede hacerlo.
3.  También puede archivar o publicar cualquier tour si es necesario (ej: por un problema de calidad o una decisión de negocio).
4.  Su rol principal se centra en la supervisión global, la gestión financiera y la operación de sus propios tours, confiando en que sus colaboradores gestionan los suyos.

#### **Resumen de Permisos por Rol (Modelo de Autonomía):**

| Acción                                  | `SUPER_ADMIN` (Alex)                     | `PARTNER_ADMIN` (David)             |
| :-------------------------------------- | :--------------------------------------- | :---------------------------------- |
| **Crear Tour**                          | Sí                                       | Sí                                  |
| **Editar CUALQUIER Tour**               | Sí (todos los campos de todos los tours) | No                                  |
| **Editar SUS PROPIOS Tours**            | N/A                                      | Sí (todos los campos)               |
| **Publicar/Archivar CUALQUIER Tour**    | Sí                                       | No                                  |
| **Publicar/Archivar SUS PROPIOS Tours** | N/A                                      | Sí                                  |
| **Programar Fechas**                    | Sí (para cualquier tour publicado)       | Sí (solo para sus tours publicados) |
| **Ver Reportes de Ventas**              | Sí (todos)                               | Sí (solo los suyos)                 |

#### **Impactos Técnicos de esta Definición (Ajustados):**

- **Base de Datos:** El `status` del tour (`DRAFT`, `PUBLISHED`, `ARCHIVED`) y el `owner_id` siguen siendo cruciales.
- **Backend (Spring Security):** La lógica de seguridad es ahora más directa. Para las operaciones de edición/publicación de tours (`PUT /api/admin/tours/{id}`), la regla es:
  - Permitir el acceso si el rol del usuario es `SUPER_ADMIN`.
  - **O**
  - Permitir el acceso si el rol del usuario es `PARTNER_ADMIN` **Y** el `owner_id` del tour que se está modificando coincide con el `id` del usuario que realiza la petición.
- **Frontend (Admin Panel):**
  - La interfaz para el `PARTNER_ADMIN` ya no tendrá un botón de "Enviar a Revisión". Tendrá un interruptor de "Publicar" que controla directamente el estado del tour.
  - La lista de tours para el `PARTNER_ADMIN` solo mostrará los tours donde él es el propietario. La lista para el `SUPER_ADMIN` mostrará todos los tours, quizás con una columna que indique el propietario.

Este modelo es más ágil y escalable si se planea tener múltiples colaboradores en el futuro, ya que reduce la carga administrativa de Alex y empodera a sus socios.
