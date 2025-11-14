# Internationalization (i18n) Audit Report
## Northern Chile Tours Application

**Audit Date:** 2025-11-14  
**Scope:** Frontend (Nuxt 3/Vue 3) & Backend (Spring Boot)  
**Locales:** Spanish (es-CL), English (en-US), Portuguese (pt-BR)

---

## Executive Summary

The application has good i18n infrastructure but **critical hardcoded locale issues** in multiple frontend components prevent proper locale switching. **40+ instances of hardcoded Spanish text** in admin pages are not internationalized. Backend i18n is limited to email templates only.

**Severity:** HIGH - Admin pages will not display properly when users switch languages

---

## 1. FRONTEND i18n ANALYSIS

### 1.1 Translation File Status

**Files:** 3 JSON files (es.json, en.json, pt.json)  
**Lines per file:** 539 lines  
**Completeness:** ✅ All three locales present and balanced

#### Key Sections Covered:
- Navigation & common UI (nav, common)
- Tours catalog (tours)
- Booking flow (booking)
- Shopping cart (cart)
- User profile (user, profile)
- Authentication (auth)
- Moon calendar (moon)
- Hero/About pages (hero, about)
- Private tours (privateTours)
- Payment system (payment with Transbank & PIX)
- Tour scheduling (schedule)

### 1.2 Critical Issues: Hardcoded Text in Admin Pages

#### Pages with Hardcoded Spanish Text:

1. **`/app/pages/admin/audit-logs.vue`** (7+ strings)
   - Line 7: `"Registro de Auditoría"`
   - Line 10: `"Historial completo de acciones administrativas"`
   - Line 26: `"Total"`
   - Line 45: `"Creados"`
   - Line 64: `"Actualizados"`
   - Line 83: `"Eliminados"`
   - Line 100: `"Acción"`
   - **Impact:** Admin dashboard unreadable in non-Spanish languages

2. **`/app/pages/admin/index.vue`** (6+ strings)
   - Line 72: `label: 'Total Reservas'`
   - Line 80: `label: 'Ingresos del Mes'`
   - Line 88: `label: 'Tours Activos'`
   - Line 96: `label: 'Alertas Pendientes'`
   - Line 107: `header: 'Tour'`
   - Line 110: `header: 'Fecha/Hora'`
   - Line 124: `header: 'Cliente'`
   - Line 125: `header: 'Estado'`
   - Line 126: `header: 'Monto'`
   - Line 144: `label: 'Ver Detalles'`

3. **`/app/pages/cart.vue`** (17+ strings)
   - Line 56: `"Carrito de Compras"`
   - Line 61: `"Tu carrito está vacío"`
   - Line 81: `"Tu carrito está vacío"`
   - Line 84: `"No has agregado ningún tour a tu carrito aún"`
   - Line 92: `"Explorar Tours"`
   - Line 136: `"horas"`
   - Line 143: `"por persona"`
   - Line 151: `"persona" / "personas"`
   - Line 168: `"Eliminar"`
   - Line 181: `"Resumen del Pedido"`
   - Line 188: `"Subtotal"`
   - Line 196: `"IVA (19%)"`
   - Line 203: `"Total"`
   - Line 216: `"Proceder al Pago"`
   - Line 227: `"Seguir Comprando"`
   - Line 241: `"Pago seguro y encriptado"`
   - Line 250: `"Cancelación gratis hasta 24h antes"`

4. **Other Admin Pages with Hardcoded Text:**
   - `admin/users.vue`: Likely has `label:` with Spanish text
   - `admin/reports.vue`: `label="Total Reservas"`, `label="Ingresos Totales"`, `label="Total Participantes"`
   - `admin/private-requests.vue`: `label="Total"`
   - `admin/schedules/[id]/participants.vue`: `"Total Reservas"`, `"Total Participantes"`
   - `admin/alerts.vue`: Multiple hardcoded labels

#### Missing Translation Keys in JSON Files:

Several admin pages reference keys that don't exist in translation files:
- `schedule.error_description`
- `schedule.back_to_tours`
- Numerous admin-specific keys (audit logs, reports, user management, etc.)

**Missing i18n structure for admin panel entirely!**

---

### 1.3 Critical Issue: Hardcoded Locale in Date/Time Formatting

#### The `useDateTime` Composable (CRITICAL BUG)

**File:** `/app/composables/useDateTime.ts`

```typescript
// Lines 7, 19, 33 ALL use hardcoded 'es-CL'
const formatDate = (value: string) => {
  return date.toLocaleDateString('es-CL', { ... })  // ❌ HARDCODED
}

const formatDateTime = (value: string) => {
  return date.toLocaleString('es-CL', { ... })      // ❌ HARDCODED
}

const formatTime = (value: string) => {
  return date.toLocaleTimeString('es-CL', { ... })  // ❌ HARDCODED
}
```

**Used in 40+ locations** across the app, preventing locale switching for all date/time displays.

#### Other Hardcoded Locale Issues:

1. **`/app/pages/admin/audit-logs.vue`**
   - Line 561: `toLocaleDateString('es-CL', ...)`
   - Line 569: `toLocaleTimeString('es-CL', ...)`

2. **`/app/pages/admin/users.vue`**
   - Line 124: `toLocaleDateString('es-CL', ...)`

3. **`/app/pages/admin/private-requests.vue`**
   - Line 195: `toLocaleString('es-CL', ...)`

4. **`/app/pages/admin/index.vue`**
   - Line 115: `toLocaleDateString('es-CL', ...)`

5. **`/app/pages/admin/alerts.vue`**
   - Line 647: `toLocaleDateString('es-CL', ...)`

6. **`/app/pages/admin/reports.vue`**
   - Line 125: `toLocaleDateString('es-CL', ...)`

7. **`/app/pages/payment/callback.vue`**
   - Line 194: `toLocaleDateString('es-CL', ...)`

8. **`/app/pages/cart.vue`**
   - Line 32: Currency formatter hardcoded to `'es-CL'` in `formatCurrency()` function

9. **`/app/pages/tours/[slug]/schedule.vue`**
   - Lines with `.toLocaleString()` without proper locale parameter for currency display

**Solution:** Update `useDateTime.ts` to accept and use current locale from `useI18n()`.

---

### 1.4 Currency Formatting Issues

#### Issue 1: `useCurrency.ts` Mapping
The composable correctly maps locales:
```typescript
es: 'es-CL'
en: 'en-US'
pt: 'pt-BR'
```
✅ Good implementation, but not used everywhere.

#### Issue 2: Improper Currency Display in Components

1. **`/app/pages/tours/[slug]/schedule.vue`**
   ```vue
   ${{ (tour.price || 0)?.toLocaleString() }}  <!-- ❌ No locale/currency -->
   ${{ ((tour.price || 0) * participantCount).toLocaleString() }}  <!-- ❌ -->
   ```
   Should use: `{{ formatPrice(tour.price) }}`

2. **`/app/pages/cart.vue`**
   ```typescript
   function formatCurrency(amount: number) {
     return new Intl.NumberFormat('es-CL', {  // ❌ HARDCODED locale
       style: 'currency',
       currency: 'CLP'
     }).format(amount)
   }
   ```
   Should use: `useCurrency()` composable

---

### 1.5 Missing Translation Keys

The following keys are referenced in code but don't exist in translation files:

**In `/app/pages/tours/[slug]/schedule.vue`:**
- `schedule.title` (referenced line 98, doesn't exist)
- `schedule.error_description` (referenced line 138, doesn't exist)
- `schedule.back_to_tours` (referenced line 144, doesn't exist)

**Admin Panel (ENTIRE SECTION MISSING):**
- No `admin.*` namespace in any translation file
- Need to add keys for:
  - Audit logs labels
  - User management labels
  - Reports labels
  - Alert management labels
  - Dashboard labels
  - Table headers

---

### 1.6 SEO & Localization

#### ✅ Implemented Correctly:
- `useLocaleHead()` in `/layouts/default.vue` adds:
  - `hreflang` tags for alternate language versions
  - `lang` attribute on HTML
  - Canonical URLs
- `useSeoMeta()` on key pages (tours, cart, contact, about, private-tours)

#### ⚠️ Issues:
- SEO meta titles hardcoded in some places (e.g., `/pages/cart.vue` line 8)
  ```typescript
  title: 'Carrito - Northern Chile',  // ❌ Hardcoded Spanish
  ```
  Should be internationalized

- Page titles in `nuxt.config.ts` titleTemplate (line 16) uses Spanish as default

---

### 1.7 Language Detection & Cookie Persistence

#### ✅ Implemented:
- Browser language detection: `detectBrowserLanguage: true`
- Cookie persistence: `cookieKey: "i18n_redirected"`
- Redirect on root: `redirectOn: "root"`

#### ✅ URL Strategy:
- `strategy: "prefix_except_default"`
- Default: Spanish (es)
- English: `/en/*`
- Portuguese: `/pt/*`

---

## 2. BACKEND i18n ANALYSIS

### 2.1 Message Properties Files Status

**Files:** 3 properties files (messages_es.properties, messages_en.properties, messages_pt.properties)  
**Lines per file:** 46 lines  
**Current Coverage:** Email templates ONLY

#### Messages Implemented:
- Email verification (6 keys)
- Password reset (6 keys)
- Booking confirmation (9 keys)
- Tour reminder (7 keys)

#### Thymeleaf Template Integration: ✅ Correct
Email templates properly use `th:text="#{email.*.*}"`

Example from `/resources/templates/email/verification.html`:
```html
<h2 style="color: #1a1a2e; margin-top: 0;" th:text="#{email.verification.title}">
```

---

### 2.2 CRITICAL GAPS: Missing Backend i18n

The backend has **NO i18n for:**

1. **Validation Error Messages**
   - JSR-303 validation annotations present (@NotNull, @Valid, @Size, etc.)
   - No custom messages in messages*.properties
   - Users see English Spring defaults regardless of locale

2. **API Error Responses**
   - No business logic error messages
   - No error messages for:
     - Invalid bookings
     - Overbooking prevention
     - Tour cancellation reasons
     - Payment failures
     - Authentication failures
   - Frontend must handle hardcoded error messages

3. **Global Exception Handler**
   - `/exception/GlobalExceptionHandler.java` likely returns hardcoded messages
   - No locale detection for error responses

4. **No Accept-Language Header Processing**
   - Backend doesn't read `Accept-Language` header
   - No LocaleResolver configured
   - Emails always use Spring default locale (usually system default)

---

### 2.3 Email Template Completeness

#### ✅ Translations Complete for:
- Email verification (es, en, pt)
- Password reset (es, en, pt)
- Booking confirmation (es, en, pt)
- Tour reminder (es, en, pt)

#### ⚠️ Hardcoded in Templates:
- "Northern Chile Tours" (brand name - acceptable)
- URLs and contact info (acceptable)
- Email footer text not translated

#### Issue: Language Selection
No clear mechanism for choosing email language:
- Uses `User.languageCode`? 
- Reads `Accept-Language` header?
- Uses thread-local locale?

**Needs documentation/code review**

---

## 3. DATE/TIME HANDLING ISSUES

### 3.1 Frontend Date Format Inconsistencies

#### Problem: No Unified Date Format per Locale

Different components use different date formats:
1. Some use: `DD/MM/YYYY` (Spanish/Brazilian preference)
2. Some use: Full locale date formatting with weekday names
3. Some use: ISO-8601 format in API responses

#### Locale-Specific Best Practices:
- **Spanish (es-CL):** `DD/MM/YYYY` format, weekday names in Spanish
- **English (en-US):** `MM/DD/YYYY` or `YYYY-MM-DD` format
- **Portuguese (pt-BR):** `DD/MM/YYYY` format, weekday names in Portuguese

Current implementation partially handles this, but hardcoded locales prevent it from working.

### 3.2 Timezone Issues

**Application Timezone:** `America/Santiago` (Chile, UTC-3/-4)

#### ✅ Good Practices Mentioned in CLAUDE.md:
- All timestamps stored as UTC in database
- `java.time.Instant` for timestamps
- ISO-8601 serialization

#### ⚠️ Potential Issues:
- Frontend may not account for timezone display differences
- Calendar displays may show wrong dates for Australian/Asian users
- No explicit timezone conversion in frontend components

---

## 4. RECOMMENDATIONS

### Priority 1: CRITICAL (Fix immediately)

#### 4.1 Fix `useDateTime.ts` Composable
```typescript
export function useDateTime() {
  const { locale } = useI18n()
  
  const getLocaleCode = (currentLocale: string): string => {
    const localeMap: Record<string, string> = {
      es: 'es-CL',
      en: 'en-US',
      pt: 'pt-BR'
    }
    return localeMap[currentLocale] || 'es-CL'
  }

  const formatDate = (value: string | number | Date): string => {
    if (!value) return ''
    const date = value instanceof Date ? value : new Date(value)
    if (Number.isNaN(date.getTime())) return ''

    return date.toLocaleDateString(getLocaleCode(locale.value), {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    })
  }
  
  // ... fix formatDateTime and formatTime similarly
}
```

#### 4.2 Remove Hardcoded Locale from All Admin Pages
Replace all instances of:
```typescript
toLocaleDateString('es-CL', ...)  // ❌
```

With:
```typescript
const { locale } = useI18n()
// ... then use
toLocaleDateString(locale.value === 'es' ? 'es-CL' : locale.value === 'pt' ? 'pt-BR' : 'en-US', ...)
```

Or better: Use the fixed `useDateTime()` composable

#### 4.3 Add Admin Panel i18n
Create new admin section in translation files:

**In es.json, en.json, pt.json:**
```json
{
  "admin": {
    "audit_logs": {
      "title": "Registro de Auditoría",
      "description": "Historial completo de acciones administrativas",
      "total": "Total",
      "created": "Creados",
      "updated": "Actualizados",
      "deleted": "Eliminados",
      "action": "Acción"
    },
    "dashboard": {
      "total_bookings": "Total Reservas",
      "monthly_revenue": "Ingresos del Mes",
      "active_tours": "Tours Activos",
      "pending_alerts": "Alertas Pendientes"
    },
    "bookings_table": {
      "tour": "Tour",
      "date_time": "Fecha/Hora",
      "client": "Cliente",
      "status": "Estado",
      "amount": "Monto",
      "view_details": "Ver Detalles"
    },
    // ... etc
  }
}
```

#### 4.4 Fix Cart Page i18n
Replace hardcoded strings with translation keys (already in es.json):
```typescript
// Instead of:
"Carrito de Compras"  // ❌

// Use:
t('cart.title')  // ✅
```

### Priority 2: HIGH (Fix within 1-2 weeks)

#### 4.5 Backend Locale-Aware Error Messages

Add to `messages_*.properties`:
```properties
# Validation Errors
error.booking.overbooking=Tour completamente reservado
error.booking.invalid_participant_count=Número de participantes inválido
error.tour.not_found=Tour no encontrado
error.schedule.not_found=Horario de tour no encontrado

# Business Logic
error.cancellation.too_late=La cancelación no está permitida dentro de 24 horas de inicio
error.payment.failed=Error al procesar el pago
error.authentication.invalid_credentials=Credenciales inválidas
```

#### 4.6 Implement Locale-Aware Error Handling in Spring

Create a global message provider:
```java
@Component
public class LocalizedMessageProvider {
  private final MessageSource messageSource;
  
  public String getMessage(String key, Object[] args, Locale locale) {
    return messageSource.getMessage(key, args, locale);
  }
}
```

#### 4.7 Add Accept-Language Header Processing

Configure Spring to read and use Accept-Language:
```java
@Configuration
public class LocaleConfiguration {
  @Bean
  public LocaleResolver localeResolver() {
    AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
    resolver.setDefaultLocale(new Locale("es", "CL"));
    return resolver;
  }
}
```

#### 4.8 Fix Currency Formatting in Schedule Page
Use `useCurrency()` composable instead of `.toLocaleString()`:
```vue
<!-- Instead of: -->
${{ (tour.price || 0)?.toLocaleString() }}

<!-- Use: -->
{{ formatPrice(tour.price) }}

<script setup>
const { formatPrice } = useCurrency()
</script>
```

### Priority 3: MEDIUM (Fix within 1 month)

#### 4.9 Add Missing Translation Keys
- `schedule.error_description`
- `schedule.back_to_tours`
- `schedule.title`

#### 4.10 Internationalize Page Meta Tags
```typescript
// Instead of hardcoding in cart.vue:
useSeoMeta({
  title: () => `${t('cart.title')} - Northern Chile`,
  description: () => t('cart.meta_description'),
})
```

#### 4.11 Create Comprehensive Admin i18n Keys
Document and add all admin page labels, headers, and messages to translation files.

#### 4.12 Add Phone Number & Address Formatting by Locale
Different locales have different formatting conventions:
- Chile: `+56 9 1234 5678`
- Brazil: `+55 11 91234 5678`
- USA: `+1 (555) 123-4567`

Create a `useFormatting()` composable for locale-specific formatting.

---

## 5. SUMMARY TABLE

| Category | Status | Severity | Count | Action |
|----------|--------|----------|-------|--------|
| Frontend translation files | ✅ Complete | - | 539 lines × 3 | - |
| Backend email messages | ✅ Complete | - | 46 lines × 3 | - |
| Hardcoded admin text | ❌ Missing | HIGH | 40+ strings | Add to i18n |
| Hardcoded locale in formatters | ❌ Critical | CRITICAL | 7 files, 15+ instances | Fix useDateTime |
| Missing schedule translation keys | ❌ Missing | MEDIUM | 3 keys | Add to JSON |
| Backend error messages | ❌ Missing | HIGH | All errors | Add messages*.properties |
| Accept-Language support | ❌ Not implemented | MEDIUM | - | Configure Spring |
| Admin panel i18n | ❌ Missing | HIGH | ~50+ keys | Create namespace |

---

## 6. FILES TO MODIFY

### Frontend Changes Required:
1. `/app/composables/useDateTime.ts` - Fix hardcoded locale
2. `/app/composables/useCurrency.ts` - Ensure all components use it
3. `/app/pages/cart.vue` - Add i18n for all hardcoded text
4. `/app/pages/admin/*.vue` - Add i18n for all labels and headers
5. `/app/pages/payment/callback.vue` - Use useDateTime() composable
6. `/app/pages/tours/[slug]/schedule.vue` - Fix currency display
7. `/i18n/locales/es.json, en.json, pt.json` - Add admin namespace and missing keys

### Backend Changes Required:
1. `/src/main/resources/messages_es.properties` - Add error messages
2. `/src/main/resources/messages_en.properties` - Add error messages
3. `/src/main/resources/messages_pt.properties` - Add error messages
4. Create Spring configuration for `LocaleResolver`
5. Update `GlobalExceptionHandler.java` to use localized messages
6. Review/update email sending to use user's language preference

---

## 7. TESTING CHECKLIST

- [ ] Admin pages display correctly in English and Portuguese
- [ ] Date/time displays in correct format for each locale
- [ ] Currency displays in correct locale format
- [ ] Email templates send in correct language based on user preference
- [ ] API error messages appear in user's language
- [ ] Validation errors display in user's language
- [ ] Language switching doesn't break UI
- [ ] SEO hreflang tags point to correct alternate language URLs
- [ ] Accept-Language header is properly processed
- [ ] Timezone conversion displays correctly for international users

