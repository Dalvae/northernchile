# Project Overview

This is a full-stack web application for "Northern Chile," a tour agency in San Pedro de Atacama. The application is designed to be a complete platform for managing tours, bookings, payments, and customer interactions. It combines scientific information with the cultural traditions of the Andes to offer a unique experience to its customers.Tuviste un error de formato, ahora no se si lo escribe en

The project is structured as a monorepo with a `frontend` and `backend` directory.

## Backend

The backend is a Spring Boot application written in Java 21. It uses a PostgreSQL database for data persistence and Flyway for database migrations. The backend is responsible for the business logic, API, user management, bookings, payments, and security.

**Key Technologies:**

- **Framework:** Spring Boot 3
- **Language:** Java 21
- **Database:** PostgreSQL 15+
- **ORM:** Spring Data JPA (Hibernate)
- **Security:** Spring Security 6 with JWT and OAuth2 for Google authentication
- **API Documentation:** OpenAPI 3 (Swagger) via `springdoc-openapi`
- **Build Tool:** Maven

## Key Architectural Concepts

Beyond the technologies used, the application is built on several key architectural concepts that are crucial to understand for any development.

### 1. The "Tour vs. Schedule" Model

The core of the booking logic separates the "product" from the "event".

- **`Tour` (`tours` table):** This represents the **product catalog**. It's the master template for an experience (e.g., "Astronomical Tour"), containing its description, default price, duration, etc. It defines _what_ is being sold.
- **`TourSchedule` (`tour_schedules` table):** This represents a **specific, sellable instance** of a `Tour` on a given date and time (e.g., the "Astronomical Tour on October 25, 2025, at 8:00 PM"). It has its own participant limit and status. This is _what customers actually book_.

This separation provides total flexibility to manage availability, cancel specific dates, and assign guides to individual events.

### 2. Multi-Tenant Administration (Owner-Based Logic)

The system is designed to support multiple tour operators (e.g., Alex and David) under one platform.

- **`owner_id`:** The `tours` table has an `owner_id` field that links each tour to the user who created it.
- **Role-Based Access Control (RBAC):**
  - `ROLE_SUPER_ADMIN`: Can see and manage **all** tours and bookings in the system.
  - `ROLE_PARTNER_ADMIN`: Can only create, edit, publish, and view reports for **their own** tours (where `tour.owner_id` matches their user ID).
- **Backend Logic:** API endpoints in the backend must always filter by `owner_id` when dealing with requests from a `PARTNER_ADMIN` to ensure data segregation.

### 3. Multilingual Support

All user-facing communication (emails, validation messages, etc.) from the backend is multilingual.

- **Resource Bundles:** Texts are stored in `messages_es.properties`, `messages_pt.properties`, etc., within `backend/src/main/resources/`.
- **`EmailService`:** This service is language-aware. The `language_code` is stored with each `Booking`, and the service uses this code to select the correct message bundle when sending confirmation emails.

## Frontend

The frontend is a Nuxt 3 (Vue 3) application. It's responsible for the user interface, user interaction, and consuming the backend API. The frontend is built with TypeScript and uses Tailwind CSS with Nuxt UI for styling.

**Key Technologies:**

- **Framework:** Nuxt 3 (Vue 3)
- **Language:** TypeScript
- **Styling:** Tailwind CSS 3.3 + Nuxt UI
- **State Management:** Pinia
- **Build Tool:** pnpm

# Building and Running

The entire application is containerized using Docker and can be orchestrated with `docker-compose`.

## Running the Application

To run the application, you need to have Docker and Docker Compose installed.

1.  **Create a `.env` file:**
    Create a `.env` file in the root of the project and add the following environment variables:

    ```bash
    POSTGRES_USER=user
    POSTGRES_PASSWORD=password
    SPRING_DATASOURCE_URL=jdbc:postgresql://database:5432/northernchile_db
    ```

2.  **Run the application:**
    ```bash
    docker-compose up
    ```

The frontend will be available at `http://localhost:3000` and the backend at `http://localhost:8080`.

## Backend Development

The backend is a Maven project. You can run it locally using the `docker-compose.yml` file, which also exposes a debugging port at `5005`.

To run the backend tests, you can use the following command:

```bash
cd backend
mvn test
```

## Frontend Development

The frontend is a pnpm project. To install the dependencies, run:

```bash
cd frontend
pnpm install
```

To run the frontend in development mode, run:

```bash
cd frontend
pnpm dev
```

# Development Conventions

## API Client Generation

The frontend has a script to generate an API client from the backend's OpenAPI specification. To generate the client, run the following command from the `frontend` directory:

```bash
pnpm generate-api-client
```

This will fetch the OpenAPI specification from the backend and generate a TypeScript Axios client in the `lib/api-client` directory.

## Database Migrations

The backend uses Flyway for database migrations. The migration scripts are located in the `backend/src/main/resources/db/migration` directory. Flyway will automatically apply the migrations when the backend starts.

## Frontend - Nuxt UI

### Uso Práctico del Modal

#### Estructura Correcta de Slots

El componente `UModal` requiere que TODO el contenido esté dentro del slot `#content`:

```vue
<UModal>
  <!-- Trigger -->
  <UButton label="Abrir Modal" />
  
  <template #content>
    <!-- HEADER -->
    <div class="header-custom">
      Título y botón cerrar
    </div>
    
    <!-- CONTENIDO SCROLLEABLE -->
    <div class="max-h-[60vh] overflow-y-auto">
      Formularios y contenido largo
    </div>
    
    <!-- FOOTER -->
    <div class="footer-custom">
      Botones de acción
    </div>
  </template>
</UModal>
```

### Manejo de Estado

❌ **NO es necesario** controlar manualmente `isOpen`:

```vue
<!-- INCORRECTO -->
<UModal v-model:open="isOpen"></UModal>
```

✅ **CORRECTO** - El modal maneja su estado internamente:

```vue
<!-- CORRECTO -->
<UModal></UModal>
```

### Scroll en Contenido Largo

Para formularios largos, aplica estas clases al contenedor del contenido:

```vue
<div class="max-h-[60vh] overflow-y-auto px-4 py-2">
  <!-- Contenido del formulario -->
</div>
```

### Selects en Modals

Usa `USelect` en lugar de `USelectMenu` para mejor compatibilidad:

```vue
<USelect
  v-model="state.categoria"
  :options="opciones"
  placeholder="Selecciona..."
  size="lg"
  class="w-full"
/>
```

### Estructura Recomendada

```vue
<template #content>
  <!-- 1. Header personalizado -->
  <div class="flex justify-between items-center pb-4 border-b">
    <h3>Título</h3>
    <UButton icon="i-heroicons-x-mark" @click="$emit('close')" />
  </div>

  <!-- 2. Contenido scrolleable -->
  <div class="max-h-[60vh] overflow-y-auto py-4">
    <!-- Tu formulario aquí -->
  </div>

  <!-- 3. Footer con acciones -->
  <div class="flex justify-end gap-3 pt-4 border-t">
    <UButton label="Cancelar" />
    <UButton label="Guardar" color="primary" />
  </div>
</template>
```

### Selects en Formularios

#### Configuración Correcta de USelect

Cuando se usan objetos en las opciones del `USelect`, es crucial definir los atributos:

```vue
<USelect
  v-model="state.status"
  :items="statusOptions"
  option-attribute="label"
  value-attribute="value"
  placeholder="Selecciona un estado"
  size="lg"
  class="w-full"
/>
```

**Donde:**

- `option-attribute="label"` - Define qué propiedad mostrar en la interfaz
- `value-attribute="value"` - Define qué propiedad usar para el valor del modelo
- Las opciones deben ser objetos con estructura: `{ label: "Texto visible", value: "valor" }`

#### Opciones de Ejemplo Correctas:

```typescript
const statusOptions = [
  { label: "Borrador", value: "DRAFT" },
  { label: "Publicado", value: "PUBLISHED" },
  { label: "Archivado", value: "ARCHIVED" },
];
```

### Estructura Recomendada

```vue
<template #content>
  <!-- 1. Header personalizado -->
  <div class="flex justify-between items-center pb-4 border-b">
    <h3>Título</h3>
    <UButton icon="i-heroicons-x-mark" @click="$emit('close')" />
  </div>

  <!-- 2. Contenido scrolleable -->
  <div class="max-h-[60vh] overflow-y-auto py-4">
    <!-- Tu formulario aquí -->
  </div>

  <!-- 3. Footer con acciones -->
  <div class="flex justify-end gap-3 pt-4 border-t">
    <UButton label="Cancelar" />
    <UButton label="Guardar" color="primary" />
  </div>
</template>
```

Esto asegura que el modal funcione correctamente con contenido largo y formularios complejos.
