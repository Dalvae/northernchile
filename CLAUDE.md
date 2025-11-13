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
- `auth/` - Authentication and user management with email verification and password reset
- `tour/` - Tour catalog and scheduling
- `booking/` - Booking management and participant tracking with anti-overbooking system
- `cart/` - Shopping cart functionality
- `payment/` - Payment processing
- `availability/` - Tour availability calculation with timezone handling
- `external/` - Weather API integration
- `lunar/` - Lunar phase calculations for astronomical tours
- `notification/` - Email notification system with multilingual templates
- `alert/` - System alerts and notifications
- `audit/` - Activity tracking and audit logs
- `reports/` - Analytics and reporting functionality
- `privatetour/` - Custom private tour request management
- `storage/` - AWS S3 file storage integration
- `diagnostic/` - System health checks and diagnostics
- `config/` - Security, CORS, i18n, timezone, and Jackson configuration
- `model/` - JPA entities with MapStruct DTO mappings

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

### Schema Management
**IMPORTANT**: The project is currently in development mode with the following configuration:
- **Flyway**: Disabled (`spring.flyway.enabled=false`)
- **Hibernate DDL**: Set to `create` mode (`spring.jpa.hibernate.ddl-auto=create`)
- **Development Note**: Database schema is recreated on each backend startup
- **Migration Strategy**: Once moving to production, migrations will be managed via Flyway with versioned SQL files in `backend/src/main/resources/db/migration/` following the `V{number}__{description}.sql` pattern

### Data Seeding
The backend supports optional data seeding for development and demos:
- Set `SEED_DATA=true` in `.env` to populate database with synthetic tour data
- Seeding only runs if the database is empty (no tours exist)
- Useful for MVP presentations and development testing

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

**CRITICAL**: ALWAYS use Nuxt UI semantic colors. NEVER use hardcoded color names or direct Tailwind color classes.

The application uses Nuxt UI's semantic color system. All themes are defined in `frontend/app/assets/css/themes/*.css` and switch dynamically using the `useTheme()` composable.

#### Available Semantic Colors (registered in `nuxt.config.ts`)

Use ONLY these semantic color names in your components and styles:

- **`primary`**: Main CTAs, active navigation, brand elements, important actions
- **`secondary`**: Secondary buttons, alternative actions, complementary UI elements
- **`tertiary`**: Special accents, astronomical elements, unique features
- **`success`**: Success messages, completed states, positive confirmations
- **`info`**: Info alerts, tooltips, help text, neutral notifications
- **`warning`**: Warning messages, pending states, attention-needed items
- **`error`**: Error messages, validation errors, destructive actions
- **`neutral`**: Text, borders, backgrounds, disabled states, structural elements

#### Correct Usage Examples

```vue
<!-- ✅ Nuxt UI Components - ALWAYS use semantic colors -->
<UButton color="primary">Save</UButton>
<UButton color="secondary">Cancel</UButton>
<UButton color="error">Delete</UButton>
<UBadge color="success">Published</UBadge>
<UBadge color="warning">Pending</UBadge>
<UAlert color="info" title="Information" />

<!-- ✅ Toasts -->
const toast = useToast()
toast.add({ color: "success", title: "Saved!" })
toast.add({ color: "error", title: "Failed!" })

<!-- ✅ Tailwind classes with semantic colors -->
<p class="text-primary-600 dark:text-primary-400">
<div class="bg-neutral-100 dark:bg-neutral-900">
<div class="border-neutral-200 dark:border-neutral-800">
```

#### WRONG - Never Do This

```vue
<!-- ❌ NEVER use hardcoded color names -->
<UButton color="red" />
<UButton color="blue" />
<UBadge color="green">

<!-- ❌ NEVER use direct Tailwind color classes -->
<p class="text-gray-900">
<div class="bg-blue-500">
<div class="border-red-600">

<!-- ❌ NEVER use hex/rgb values -->
<div style="color: #ff0000">
<div style="background: rgb(0, 123, 255)">
```

#### Theme System Architecture

Themes are defined in CSS files using Tailwind CSS v4 `@theme` directive:
- `themes/atacama-nocturna.css` (default theme)
- `themes/atacama-cobre-lunar.css`
- `themes/atacama-cosmic-desert.css`
- `themes/aurora.css`
- `themes/atacama-classic.css`
- `themes/cosmic.css`

Each theme maps semantic colors to specific palettes:
```css
/* Example: atacama-nocturna.css */
@theme {
  --color-atacama-copper-500: oklch(55% 0.13 35);
  /* ... other color definitions */
}

.atacama-nocturna {
  --ui-color-primary-500: var(--color-atacama-copper-500);
  --ui-color-secondary-500: var(--color-atacama-dorado-500);
  /* ... semantic color mappings */
}
```

The `useTheme()` composable switches themes by changing the class on `<html>`:
```typescript
const { setTheme, currentTheme } = useTheme()
setTheme('atacama-nocturna') // Changes <html class="atacama-nocturna">
```

See `frontend/THEMING.md` for complete theming guidelines.

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

### Frontend Routing and SSR Strategy

The application uses a hybrid SSR approach for optimal performance and SEO:

**Server-Side Rendered (SSR) Pages**:
- `/` - Homepage (prerendered)
- `/tours/**` - Tour catalog and detail pages
- `/about`, `/contact` - Static pages
- SEO-optimized with meta tags, structured data, and multilingual support

**Client-Side Rendered (CSR) Pages**:
- `/admin/**` - Admin dashboard and management
- `/profile/**` - User profile and settings
- `/bookings/**` - Booking history and details
- `/cart` - Shopping cart
- `/auth` - Login and registration

**Route Rules**: Configured in `nuxt.config.ts` using `routeRules`

### i18n and SEO
The app uses `@nuxtjs/i18n` with SEO-optimized configuration:
- `useLocaleHead` is implemented in `layouts/default.vue` for automatic `lang`, `hreflang`, and canonical tags
- Locales use ISO codes: `es-CL`, `en-US`, `pt-BR`
- Base URL configured for production: `https://www.northernchile.cl`
- URL Strategy: `prefix_except_default` (ES default, /en/*, /pt/* for translations)
- Browser language detection with cookie persistence

## Environment Variables

### Required for Backend
```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://database:5432/northernchile_db
SPRING_DATASOURCE_USERNAME=user
SPRING_DATASOURCE_PASSWORD=password

# Security
JWT_SECRET=your-256-bit-secret-key
SPRING_REMOTE_SECRET=change-me-in-dev

# Admin initialization (Legacy - single admin)
ADMIN_EMAIL=admin@northernchile.cl
ADMIN_FULL_NAME=Administrador Principal
ADMIN_PASSWORD=Admin123!secure

# Multi-Admin Configuration (Recommended)
# Format: email:password:role;email2:password2:role2
# Roles: ROLE_SUPER_ADMIN, ROLE_PARTNER_ADMIN, ROLE_CLIENT
ADMIN_USERS_CONFIG=admin@example.com:pass123:ROLE_SUPER_ADMIN;partner@example.com:pass456:ROLE_PARTNER_ADMIN

# Data Seeding (for development/demos)
SEED_DATA=false

# Email Configuration (Google Workspace / Gmail SMTP)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=noreply@northernchile.cl
MAIL_PASSWORD=your_app_specific_password_here
MAIL_FROM_EMAIL=noreply@northernchile.cl
MAIL_FROM_NAME=Northern Chile Tours
MAIL_ENABLED=true

# AWS S3 Storage (for tour images and assets)
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
AWS_REGION=sa-east-1
AWS_S3_BUCKET_NAME=northern-chile-assets

# External APIs
WEATHER_API_KEY=your_openweathermap_key
GOOGLE_CLIENT_ID=your_google_oauth_id
GOOGLE_CLIENT_SECRET=your_google_oauth_secret
```

### Required for Frontend
```bash
NUXT_PUBLIC_API_BASE_URL=http://localhost:8080/api
NUXT_PUBLIC_BASE_URL=http://localhost:3000
```

### Environment Setup
1. Copy `.env.example` to `.env`
2. Fill in required values
3. For email setup, see `backend/docs/EMAIL_SETUP.md`
4. For AWS S3 setup, create a bucket in sa-east-1 region and configure IAM user with S3 permissions

## Testing Strategy

### Backend
Tests use Spring Boot Test framework with JUnit 5. Run with `mvn test`.

### Frontend
Type checking with `pnpm typecheck`. ESLint configured with stylistic rules (1tbs brace style, no trailing commas).

## Integration Points

### External APIs
- **OpenWeatherMap**: 5-day / 3-hour forecast for wind and cloud conditions (Free tier: 60 calls/min)
- **Lunar phase calculations**: Built-in astronomical calculations for moon illumination
- **Payment gateways**: Transbank (Chile), Mercado Pago (Brazil/PIX), Stripe (international)
- **Google OAuth 2.0**: Social login integration

### Email Service
Comprehensive email system with Google Workspace/Gmail SMTP integration:

**Email Types**:
1. **Email Verification** - Sent on registration (24-hour token validity)
2. **Password Reset** - Sent on password reset request (2-hour token validity)
3. **Booking Confirmation** - Sent immediately after successful booking
4. **Tour Reminders** - Automated emails sent 24 hours before tour start

**Key Features**:
- Multilingual templates using Thymeleaf
- i18n support via `messages_{locale}.properties` (es, en, pt)
- Language determined by user's `languageCode` or `Accept-Language` header
- Graceful degradation: Set `MAIL_ENABLED=false` to log emails instead of sending
- Scheduled task runs hourly to send tour reminders
- Templates located in `backend/src/main/resources/templates/email/`

**Configuration**: See `backend/docs/EMAIL_SETUP.md` for complete setup guide with Google Workspace app passwords, SPF/DKIM configuration, and troubleshooting.

### File Storage
AWS S3 integration for storing:
- Tour images and gallery photos
- User profile pictures
- Booking attachments and documents
- Configured via AWS SDK with region `sa-east-1`
- Max file upload size: 10MB

## Timezone and Date/Time Handling

**CRITICAL**: The application uses a standardized approach for date/time serialization to prevent timezone bugs.

### Application Timezone
- **Primary Timezone**: `America/Santiago` (Chile)
- **DST Handling**: Automatic (UTC-04:00 in winter, UTC-03:00 in summer)
- **Database Storage**: All timestamps stored as UTC
- **Serialization**: ISO-8601 format

### Type Contracts (Backend ↔ Frontend)

1. **Instant** (Timezone-aware timestamps)
   - **Backend Type**: `java.time.Instant`
   - **JSON Format**: `"2025-01-15T14:30:00Z"` (ISO-8601 with UTC)
   - **Use Cases**: `TourSchedule.startDatetime`, `Booking.createdAt`, `Booking.updatedAt`

2. **LocalDate** (Date without time)
   - **Backend Type**: `java.time.LocalDate`
   - **JSON Format**: `"2025-01-15"` (YYYY-MM-DD)
   - **Use Cases**: `User.dateOfBirth`, `Participant.dateOfBirth`
   - **IMPORTANT**: Always send as string, NEVER convert to JavaScript Date object

3. **LocalTime** (Time without date)
   - **Backend Type**: `java.time.LocalTime`
   - **JSON Format**: `"14:30:00"` (HH:mm:ss)
   - **Use Cases**: `Tour.defaultStartTime`

### Frontend Best Practices
```typescript
// ✅ DO: Parse Instant for display
const date = new Date(booking.createdAt);
const formatter = new Intl.DateTimeFormat('es-CL', {
  timeZone: 'America/Santiago'
});

// ✅ DO: Send LocalDate as string
const participant = { dateOfBirth: "1990-05-15" };

// ❌ DON'T: Convert LocalDate to Date object
const participant = { dateOfBirth: new Date("1990-05-15") }; // ❌ Timezone issues!
```

**Complete Guide**: See `docs/DATE_TIME_HANDLING.md` for detailed patterns, testing strategies, and troubleshooting.

## Backend Architecture Patterns

### DTO Mapping with MapStruct
The backend uses MapStruct 1.5.5 for type-safe, compile-time DTO mappings:
- Mappers located in each feature package (e.g., `auth/mapper/`, `tour/mapper/`)
- Automatic mapping generation at compile time
- Custom mappings for complex transformations
- No runtime reflection overhead

### Anti-Overbooking System
Centralized booking validation prevents double-booking of tour schedules:
- Database-level constraint checks
- Service-layer validation before booking creation
- Transaction isolation to prevent race conditions

### Service Layer Architecture
- Feature-based package structure
- Clear separation of concerns
- Dependency injection via Spring constructor injection
- Service extraction for reusable business logic

### API Conventions

**REST Endpoint Structure**:
```
/api/{resource}           - List/Create
/api/{resource}/{id}      - Get/Update/Delete
/api/{resource}/{id}/sub  - Nested resources
```

**Request/Response Patterns**:
- **Request DTOs**: Suffixed with `Req` (e.g., `BookingReq`, `TourCreateReq`)
- **Response DTOs**: Suffixed with `Res` (e.g., `BookingRes`, `TourRes`)
- **Validation**: JSR-303 annotations on request DTOs (`@NotNull`, `@Valid`, `@Size`, etc.)
- **Error Responses**: Standardized error format with HTTP status codes
- **Pagination**: Use Spring Data `Pageable` for list endpoints
- **Filtering**: Query parameters for filtering (e.g., `?status=CONFIRMED&startDate=2025-01-01`)

**Authentication & Authorization**:
- JWT tokens in `Authorization: Bearer {token}` header
- Role-based access control with `@PreAuthorize` annotations
- Multi-tenant filtering by `owner_id` for `PARTNER_ADMIN` role
- Public endpoints: Auth registration, login, tour catalog
- Protected endpoints: Bookings, admin operations, profile management

**OpenAPI Documentation**:
- All endpoints documented with `@Operation`, `@ApiResponse` annotations
- Request/response examples in Swagger UI
- Available at `/swagger-ui.html` and `/api-docs` (JSON)

## Best Practices for AI Assistants

### When Working on Backend Features
1. **Always use feature-based packages**: Place new features in their own package with `controller/`, `service/`, `repository/`, `model/`, `dto/`, `mapper/` structure
2. **Use MapStruct for DTOs**: Create mappers for entity ↔ DTO conversions
3. **Add validation**: Use JSR-303 annotations on request DTOs
4. **Consider multi-tenancy**: Check if `owner_id` filtering is needed for `PARTNER_ADMIN` users
5. **Test timezone handling**: Ensure `Instant` types for timestamps, `LocalDate` for dates
6. **Add OpenAPI docs**: Document all new endpoints with Swagger annotations
7. **Handle email notifications**: Check if the feature requires email notifications

### When Working on Frontend Features
1. **Use semantic colors only**: Never use hardcoded color names (see Theming section)
2. **Use API client types**: Import types from `api-client` generated by OpenAPI
3. **Handle i18n**: Add translations to all three locale files (`es.json`, `en.json`, `pt.json`)
4. **Respect SSR/CSR boundaries**: Check `routeRules` before adding new pages
5. **Use composables**: Extract reusable logic to `composables/` directory
6. **Follow USelect patterns**: Use `option-attribute` and `value-attribute` for object arrays
7. **Test date handling**: Keep `LocalDate` as strings, don't convert to Date objects
8. **Use Pinia stores**: For shared state management across components

### Code Quality Standards
- **Backend**: Follow Spring Boot best practices, use constructor injection, avoid `@Autowired`
- **Frontend**: Use TypeScript strict mode, avoid `any` types, use Composition API
- **Testing**: Write unit tests for services, integration tests for controllers
- **Logging**: Use SLF4J for backend, avoid excessive console.log in frontend production
- **Error Handling**: Use try-catch blocks, return appropriate HTTP status codes
- **Security**: Sanitize user input, validate all request data, use parameterized queries

## Key Dependencies and Tools

### Backend
- **MapStruct**: DTO mapping (annotation processor)
- **JJWT**: JWT token generation and validation
- **Caffeine**: In-memory caching
- **Flyway**: Database migrations (currently disabled, will be enabled for production)
- **SpringDoc OpenAPI**: API documentation and Swagger UI
- **Thymeleaf**: Email template engine
- **AWS SDK**: S3 file storage
- **Cron-utils**: Recurrence rule parsing
- **Hibernate Types**: JSON column type support

### Frontend
- **@nuxt/ui**: Component library with semantic theming
- **@nuxtjs/i18n**: Internationalization
- **@pinia/nuxt**: State management
- **@vueuse/nuxt**: Vue composition utilities
- **@fullcalendar/vue3**: Calendar component for tour scheduling
- **date-fns**: Date manipulation utilities
- **@openapitools/openapi-generator-cli**: API client generation

## Monitoring and Health Checks

### Spring Boot Actuator Endpoints
Available at `http://localhost:8080/actuator`:
- `/actuator/health` - Application health status
- `/actuator/health/liveness` - Liveness probe (Kubernetes)
- `/actuator/health/readiness` - Readiness probe (Kubernetes)
- `/actuator/info` - Application information

Health checks include:
- Database connectivity
- Disk space
- Application status

## Documentation References

- **Email Setup**: `backend/docs/EMAIL_SETUP.md`
- **Date/Time Handling**: `docs/DATE_TIME_HANDLING.md`
- **Frontend Theming**: `frontend/THEMING.md`
- **Nuxt UI Updates**: `frontend/NUXT_UI_UPDATES.md`
- **API Documentation**: `http://localhost:8080/swagger-ui.html`

## Known Issues and Development Notes

**Current Development Status**:
- Database schema management using Hibernate `create` mode (recreates on each startup)
- Flyway migrations disabled during active development
- Production deployment will use Flyway with versioned migrations
- `TODO.md` is currently empty - tasks are tracked in pull requests and issues

**Important Notes**:
- Always test timezone-sensitive features with `America/Santiago` timezone
- Email sending disabled by default (`MAIL_ENABLED=false`) - check logs for email content
- Data seeding available for quick development setup (`SEED_DATA=true`)
- Multi-admin configuration allows multiple administrators with different roles