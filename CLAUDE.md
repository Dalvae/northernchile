# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Northern Chile is a full-stack tour booking platform for astronomical and regular tours in San Pedro de Atacama, Chile. The platform serves a multilingual audience (Spanish, English, Portuguese) and supports multi-tenant tour management.

## Architecture

### Monorepo Structure
```
northernchile/
├── backend/          # Spring Boot 3 + Java 21 API
├── frontend/         # Nuxt 3 (Vue 3) SSR application
└── docker-compose.yml
```

### Backend (Spring Boot)
- **Language**: Java 21
- **Framework**: Spring Boot 3.5.7
- **Database**: PostgreSQL 15+ with Flyway migrations
- **Security**: Spring Security 6 with JWT authentication
- **API Docs**: OpenAPI 3 (Swagger) at `/api-docs`
- **Package Structure**: Feature-based packages under `com.northernchile.api/`

Key backend packages:
- `auth/` - Authentication and user management
- `tour/` - Tour catalog and scheduling
- `booking/` - Booking management and participant tracking
- `cart/` - Shopping cart functionality
- `payment/` - Payment processing
- `availability/` - Tour availability calculation
- `external/` - Weather and lunar phase APIs
- `config/` - Security, CORS, i18n configuration
- `model/` - JPA entities

### Frontend (Nuxt 3)
- **Framework**: Nuxt 3 with Vue 3 Composition API
- **Rendering**: Universal SSR (except `/admin/**`, `/profile/**`, `/bookings/**` which are CSR)
- **Styling**: Tailwind CSS 3 + Nuxt UI component library
- **State**: Pinia stores
- **i18n**: `@nuxtjs/i18n` with prefix_except_default strategy
- **Routes**: ES default, /en/*, /pt/* for translations

## Development Commands

### Full Stack Development
```bash
# Start all services (database + backend)
docker-compose up

# Frontend development (separate terminal)
cd frontend
pnpm install
pnpm dev
```

The frontend runs at `http://localhost:3000` and backend at `http://localhost:8080`.

### Backend Only
```bash
cd backend

# Run tests
mvn test

# Build
mvn clean package

# Run with debugging (port 5005)
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
```

### Frontend Only
```bash
cd frontend

# Development
pnpm dev

# Type checking
pnpm typecheck

# Linting
pnpm lint

# Build for production
pnpm build

# Preview production build
pnpm preview
```

### API Client Generation
When the backend OpenAPI spec changes, regenerate the frontend API client:
```bash
cd frontend
pnpm generate-api-client
```
This fetches the spec from `http://localhost:8080/api-docs` and generates a TypeScript Axios client in `lib/api-client/`.

## Core Domain Concepts

### Tour vs. Schedule Model
The booking system separates "product" from "sellable instance":

- **Tour** (`tours` table): Product catalog template with base price, description, and recurrence rules
- **TourSchedule** (`tour_schedules` table): Specific date/time instance that customers book

This enables flexible availability management, individual date cancellations, and guide assignments.

### Multi-Tenant Administration
- Each tour has an `owner_id` linking to the user who created it
- **Roles**:
  - `ROLE_SUPER_ADMIN`: Full system access
  - `ROLE_PARTNER_ADMIN`: Can only manage their own tours
  - `ROLE_CLIENT`: Standard booking user
- Backend services must filter by `owner_id` for `PARTNER_ADMIN` requests

### Recurrence and Automation
Tours can be recurring (`is_recurring = true`) with `recurrence_rule` (cron expression). A scheduled service (`TourScheduleGeneratorService`) automatically creates `TourSchedule` instances for the next 90 days based on recurrence rules.

### Business Rules
- **Astronomical tours**: Cannot be scheduled on full moon nights (>90% illumination)
- **Wind-sensitive tours**: Cancelled if wind exceeds 25 knots
- **Tax handling**: All prices include IVA (19% in Chile). `bookings` table stores `subtotal`, `tax_amount`, and `total_amount`
- **Cancellation policy**: Full refund if cancelled >24 hours before tour start

## Database Management

### Migrations
Flyway migrations are in `backend/src/main/resources/db/migration/`:
```
V1__Create_initial_schema.sql
V2__Add_auth_provider_to_users.sql
...
```

Migrations run automatically on backend startup. Version migration files following the `V{number}__{description}.sql` pattern.

### Main Tables
- `users` - All users (clients and admins) with role and auth provider
- `tours` - Tour catalog with recurrence rules
- `tour_schedules` - Specific tour instances
- `bookings` - Customer reservations
- `participants` - People attending tours (may not have user accounts)
- `carts` / `cart_items` - Persistent shopping carts
- `private_tour_requests` - Custom tour inquiries

## Frontend Conventions

### Theming and Semantic Colors

**CRITICAL**: Always use semantic colors, never hardcode color values.

The application uses Nuxt UI's semantic color system configured in `app/app.config.ts`:
- `primary`: Main CTAs, active navigation, brand elements
- `secondary`: Secondary buttons, alternative actions
- `tertiary`: Astronomical/special accents (custom `deep-space` color)
- `success`: Success messages, confirmations
- `info`: Info alerts, tooltips, neutral notifications
- `warning`: Warnings, pending states
- `error`: Error messages, validation errors, destructive actions
- `neutral`: Text, borders, backgrounds, disabled states

**Correct usage:**
```vue
<!-- Components -->
<UButton color="primary" />
<UButton color="error" />
<UBadge color="success">Published</UBadge>

<!-- Toasts -->
toast.add({ color: "success" })
toast.add({ color: "error" })

<!-- Tailwind classes -->
<p class="text-neutral-900 dark:text-white">
<div class="bg-neutral-800 border-neutral-700">
```

**NEVER use:**
```vue
<!-- ❌ WRONG -->
<UButton color="red" />
<UBadge color="green">
<p class="text-gray-900">
<div class="bg-blue-800">
```

See `frontend/THEMING.md` for complete guidelines.

### Nuxt UI Modals
All modal content must be inside the `#content` slot:
```vue
<UModal>
  <UButton label="Open" />

  <template #content>
    <!-- Header -->
    <div class="flex justify-between items-center pb-4 border-b">
      <h3>Title</h3>
      <UButton icon="i-heroicons-x-mark" @click="$emit('close')" />
    </div>

    <!-- Scrollable content -->
    <div class="max-h-[60vh] overflow-y-auto py-4">
      <!-- Form content -->
    </div>

    <!-- Footer -->
    <div class="flex justify-end gap-3 pt-4 border-t">
      <UButton label="Cancel" />
      <UButton label="Save" color="primary" />
    </div>
  </template>
</UModal>
```

Do NOT manually control `isOpen` state - the modal manages this internally.

### USelect Configuration
When using objects in select options:
```vue
<USelect
  v-model="state.status"
  :items="statusOptions"
  option-attribute="label"
  value-attribute="value"
  placeholder="Select status"
  size="lg"
  class="w-full"
/>
```

Where `statusOptions` has structure:
```typescript
const statusOptions = [
  { label: "Draft", value: "DRAFT" },
  { label: "Published", value: "PUBLISHED" }
];
```

### i18n and SEO
The app uses `@nuxtjs/i18n` with SEO-optimized configuration:
- `useLocaleHead` is implemented in `layouts/default.vue` for automatic `lang`, `hreflang`, and canonical tags
- Locales use ISO codes: `es-CL`, `en-US`, `pt-BR`
- Base URL configured for production: `https://www.northernchile.cl`

## Environment Variables

### Required for Backend
```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://database:5432/northernchile_db
POSTGRES_USER=user
POSTGRES_PASSWORD=password

# Admin initialization
ADMIN_EMAIL=admin@northernchile.cl
ADMIN_FULL_NAME=Administrador Principal
ADMIN_PASSWORD=Admin123!secure

# External APIs (optional for dev)
WEATHER_API_KEY=your_weatherapi_key
GOOGLE_CLIENT_ID=your_google_oauth_id
GOOGLE_CLIENT_SECRET=your_google_oauth_secret
```

### Required for Frontend
```bash
NUXT_PUBLIC_API_BASE_URL=http://localhost:8080/api
NUXT_PUBLIC_BASE_URL=http://localhost:3000
```

Copy `.env.example` to `.env` and fill in values.

## Testing Strategy

### Backend
Tests use Spring Boot Test framework with JUnit 5. Run with `mvn test`.

### Frontend
Type checking with `pnpm typecheck`. ESLint configured with stylistic rules (1tbs brace style, no trailing commas).

## Integration Points

### External APIs
- **WeatherAPI.com**: 14-day forecast for wind and cloud conditions
- **commons-suncalc**: Lunar phase calculations
- **Payment gateways**: Transbank (Chile), Mercado Pago (Brazil/PIX), Stripe (international)

### Email Service
Multilingual email templates using Spring's `MessageSource` with `messages_{locale}.properties` files. Language determined by `booking.language_code`.

## Known Issues and TODO
See `TODO.md` for current pending tasks:
- Missing translations
- Theme configuration (light/dark mode)
- Adult/child price differentiation in some flows
