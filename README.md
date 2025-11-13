# Northern Chile - Astronomical Tours Platform

A full-stack booking and administration platform for astronomical and adventure tours in San Pedro de Atacama, Chile.

## Overview

Northern Chile is a multilingual (Spanish/English/Portuguese) tour booking platform that combines a public-facing tour catalog with a comprehensive admin dashboard for multi-tenant tour management. The platform specializes in astronomical tours with intelligent lunar phase calculations, automated weather monitoring, and smart scheduling systems.

## Architecture

### Technology Stack

**Backend:**
- Spring Boot 3.5.7 (Java 21)
- PostgreSQL 15+ with Flyway migrations
- Spring Security 6 with JWT authentication
- MapStruct for DTO mapping
- OpenAPI 3.0 (Swagger) documentation

**Frontend:**
- Nuxt 3 with Vue 3 Composition API
- Server-Side Rendering (SSR) for SEO
- Tailwind CSS 3 + Nuxt UI components
- Pinia for state management
- @nuxtjs/i18n for multilingual support

**Infrastructure:**
- Docker containerization
- GitHub Actions CI/CD
- AWS S3 for media storage
- PostgreSQL database

### Monorepo Structure

```
northernchile/
├── backend/              # Spring Boot REST API
│   └── src/main/java/com/northernchile/api/
│       ├── auth/         # User authentication & management
│       ├── tour/         # Tour catalog & scheduling
│       ├── booking/      # Reservation management
│       ├── availability/ # Schedule & availability engine
│       ├── payment/      # Payment processing
│       ├── notification/ # Email notification system
│       ├── storage/      # AWS S3 integration
│       ├── external/     # Weather API integration
│       ├── lunar/        # Lunar phase calculations
│       └── config/       # Security, CORS, i18n configuration
│
├── frontend/             # Nuxt 3 application
│   ├── pages/            # Application routes
│   ├── components/       # Reusable Vue components
│   ├── composables/      # Composition API logic
│   ├── stores/           # Pinia state stores
│   ├── layouts/          # Page layouts
│   └── assets/           # Styles and static assets
│
└── docker-compose.yml    # Development environment
```

## Core Features

### Customer-Facing Features

**Tour Catalog:**
- Multilingual tour browsing (ES/EN/PT)
- Advanced filtering by type, difficulty, duration
- Real-time availability checking
- Detailed tour descriptions with image galleries
- Lunar phase information for astronomical tours

**Booking System:**
- Multi-step booking workflow
- Shopping cart functionality
- Participant information management
- Secure payment processing
- Booking confirmation emails
- Automated tour reminders (24h before start)

**User Management:**
- Email-based registration with verification
- Secure password reset
- User profile management
- Booking history and receipts

### Admin Dashboard Features

**Multi-Tenant Tour Management:**
- Role-based access control (Super Admin, Partner Admin, Client)
- Partner Admin: Manage only their own tours
- Super Admin: Full system access
- Tour creation with rich text descriptions
- Image upload and gallery management
- Recurring schedule configuration (cron-based)

**Smart Scheduling:**
- Automatic schedule generation for next 90 days
- Individual date/time overrides
- Guide assignment per schedule
- Astronomical tour validation (no full moon nights)
- Weather-based automatic cancellations

**Booking Management:**
- Real-time booking dashboard
- Participant tracking
- Payment status monitoring
- Anti-overbooking system with database constraints
- Email notification management

**Analytics & Reports:**
- Booking statistics
- Revenue tracking
- Tour performance metrics
- Participant demographics

## Business Logic & Automation

### Astronomical Tour Intelligence
- Automatic lunar phase calculations
- Prevents tour scheduling when moon illumination >90%
- Real-time lunar data for tour detail pages
- Timezone-aware calculations (America/Santiago)

### Weather Monitoring
- OpenWeatherMap API integration
- 5-day / 3-hour forecast tracking
- Automatic tour cancellation if wind >25 knots
- Weather alerts for tour operators

### Automated Scheduling
- Cron-based recurrence rules
- Scheduled service runs hourly
- Generates tour instances 90 days in advance
- Respects astronomical constraints
- Individual schedule overrides supported

### Anti-Overbooking Protection
- Database-level unique constraints
- Service-layer validation
- Transaction isolation for concurrent bookings
- Real-time capacity tracking

## Data Model

### Core Entities

**Users:**
- Authentication (local + OAuth providers)
- Role-based permissions
- Email verification
- Profile management

**Tours:**
- Product catalog template
- Base pricing and descriptions
- Recurrence rules
- Owner (for multi-tenancy)
- Image galleries

**Tour Schedules:**
- Specific date/time instances
- Individual pricing overrides
- Guide assignments
- Availability tracking
- Weather status

**Bookings:**
- Customer reservations
- Payment tracking (subtotal, tax, total)
- Status management (pending, confirmed, cancelled)
- Multi-participant support

**Participants:**
- Individual attendee information
- May or may not have user accounts
- Linked to bookings

## Security & Authentication

**Authentication:**
- JWT-based token system
- Secure password hashing (BCrypt)
- Email verification tokens (24h expiry)
- Password reset tokens (2h expiry)
- Google OAuth 2.0 integration

**Authorization:**
- Role-based access control (RBAC)
- Multi-tenant data isolation
- API endpoint protection
- CORS configuration for frontend

**Data Protection:**
- Input validation on all endpoints
- SQL injection prevention (JPA/Hibernate)
- XSS protection
- CSRF protection

## Internationalization (i18n)

**Supported Languages:**
- Spanish (es-CL) - Default
- English (en-US)
- Portuguese (pt-BR)

**URL Strategy:**
- Spanish: `/tours` (default, no prefix)
- English: `/en/tours`
- Portuguese: `/pt/tours`

**Features:**
- SEO-optimized with hreflang tags
- Browser language detection
- Cookie-based language persistence
- Multilingual email templates
- Admin interface in all languages

## Rendering Strategy

**Server-Side Rendered (SSR):**
- Homepage and landing pages
- Tour catalog and detail pages
- Static pages (about, contact)
- Optimized for SEO and social sharing

**Client-Side Rendered (CSR):**
- Admin dashboard (`/admin/**`)
- User profile (`/profile/**`)
- Booking management (`/bookings/**`)
- Shopping cart (`/cart`)
- Authentication pages

## Email System

**Automated Emails:**
- Email verification (on registration)
- Password reset
- Booking confirmation
- Tour reminders (24h before start)
- Cancellation notifications

**Features:**
- Multilingual templates (Thymeleaf)
- Language based on user preference
- SMTP configuration (Gmail/Google Workspace)
- Graceful degradation for development

## API Documentation

RESTful API with OpenAPI 3.0 specification:
- Interactive Swagger UI at `/swagger-ui.html`
- JSON specification at `/api-docs`
- Comprehensive endpoint documentation
- Request/response examples
- Authentication requirements

## Deployment

**Backend:**
- Dockerized Spring Boot application
- GitHub Actions CI/CD pipeline
- Automated deployment to VPS
- Health checks via Spring Boot Actuator
- Production: https://api.northernchile.com

**Frontend:**
- Universal SSR with Nuxt 3
- Static asset optimization
- Production build optimization
- SEO meta tags and structured data

## Environment Configuration

The application requires environment variables for:
- Database connection (PostgreSQL)
- JWT secret (256-bit minimum)
- Admin user initialization
- AWS S3 credentials
- Email/SMTP configuration
- Weather API key
- Google OAuth credentials

See `.env.example` for complete configuration template.

## License

Copyright (c) 2025 Diego Alvarez Espinoza. All rights reserved.

This project is source-available for educational and reference purposes only. Commercial use, redistribution, or derivative works are not permitted without explicit permission. See [LICENSE](LICENSE) for details.

## Contact

For licensing inquiries: diego.alvarez.e@ug.uchile.cl

---

**Note:** This is a production application currently in active development. The codebase is public for portfolio and educational purposes.
