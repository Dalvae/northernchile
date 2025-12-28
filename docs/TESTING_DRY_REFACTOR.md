# Comprehensive Testing Guide - All Refactoring Changes

This guide covers **all 12 unpushed commits** that need to be tested before pushing to remote.

## Commits Overview (oldest to newest)

| # | Commit | Description |
|---|--------|-------------|
| 1 | `f0da1ba2` | DB Optimization: reduce pool, add indexes |
| 2 | `8a60a6ef` | Backend Refactor: TourUtils, N+1 fix, Booking entity |
| 3 | `3d19fadd` | i18n Fixes: hardcoded strings replaced |
| 4 | `7ab4a160` | Type Safety: `:any` replaced with proper types |
| 5 | `4205d242` | TourCalendar memory leak fix, auth plugin simplification |
| 6 | `c4f3a6df` | Consolidate ChileDateTimeUtils into DateTimeUtils |
| 7 | `f2e5cb2d` | Extract SecurityUtils for role checking |
| 8 | `5a29ea66` | apiProxy utility + 22 API handlers refactored |
| 9 | `6a701356` | apiProxy extended + 35 more handlers (~530 lines saved) |
| 10 | `0f9875bd` | Centralize admin options (roles, status) |
| 11 | `ffda509a` | Add TypeScript types to API handlers |
| 12 | `7e98e938` | DRY Phase 1-2: TagInput, localeUtils, useFormSubmit |
| 13 | `d8797dc3` | DRY Phase 3-5: Lightbox, components, backend ownership |

---

## 1. Database Optimization (`f0da1ba2`)

### Changes
- Reduced HikariCP pool size (10→5 max, 2 min)
- Increased cleanup job intervals (1h→6h carts, 30min→2h sessions)
- Added performance indexes via `V10__add_performance_indexes.sql`

### Testing

**Verify migration ran:**
```bash
docker compose exec backend mvn flyway:info -q | grep V10
```

**Check indexes exist:**
```bash
docker compose exec db psql -U northernchile -c "\di" | grep -E "idx_booking|idx_cart|idx_payment"
```

**Monitor connection pool (optional):**
1. Start backend and frontend
2. Navigate through multiple pages
3. Check no connection pool exhaustion errors in logs

---

## 2. Backend Refactor (`8a60a6ef`)

### Changes
- Added `TourUtils.java` - centralized tour pricing/passenger calculations
- Fixed N+1 query in `BookingRepository` with `@EntityGraph`
- Simplified `Booking.java` entity (delegates to TourUtils)

### Testing

**Compile check:**
```bash
docker compose exec backend mvn compile -q
```

**Functional tests:**
1. Create a new booking (any tour)
2. Verify booking shows correct pricing in Admin > Bookings
3. Check booking details include correct participant counts
4. Verify reports show correct data (Admin > Reports)

---

## 3. i18n Fixes (`3d19fadd`)

### Changes
- `TourFilters.vue` - hardcoded "Filtros" replaced
- `ParticipantForm.vue` - hardcoded labels replaced
- `LanguageSwitcher.vue` - fixed language names
- `profile/index.vue` - hardcoded strings replaced
- Added missing keys to `en.json`, `es.json`, `pt.json`

### Testing

**Test all 3 languages:**

1. **Spanish (ES):**
   - Go to `/tours` - verify "Filtros" appears (not hardcoded)
   - Open TourFilters - verify all labels are in Spanish
   - Go to Profile - verify all text is Spanish
   - Start booking - verify ParticipantForm labels are Spanish

2. **English (EN):**
   - Switch language to English
   - Repeat above checks - all text should be English
   - Check LanguageSwitcher shows "English" not "Inglés"

3. **Portuguese (PT):**
   - Switch to Portuguese
   - Repeat checks - all text should be Portuguese
   - Check LanguageSwitcher shows "Português"

---

## 4. Type Safety (`7ab4a160`)

### Changes
- Replaced `:any` with proper types across 13 files
- Files: app.vue, UploadModal, Content, user modals, useS3Upload, admin pages, payment callback, book page

### Testing

**TypeScript check:**
```bash
cd frontend && pnpm typecheck
```
Expected: No NEW type errors from our changes

**Lint check:**
```bash
cd frontend && pnpm lint .
```

---

## 5. TourCalendar Fix (`4205d242`)

### Changes
- Fixed memory leak in `TourCalendar.vue` (proper cleanup of watchers/refs)
- Simplified `auth.ts` plugin
- Simplified 4 API handlers

### Testing

**Memory leak test:**
1. Go to any tour detail page with calendar
2. Open browser DevTools > Performance > Memory
3. Take heap snapshot
4. Navigate away and back to tour page 5+ times
5. Take another heap snapshot
6. Compare - memory should not continuously grow

**Calendar functionality:**
1. Navigate to a tour with schedules
2. Verify calendar renders correctly
3. Click on different months
4. Click on available dates - verify schedule info appears

---

## 6. DateTimeUtils Consolidation (`c4f3a6df`)

### Changes
- Merged `ChileDateTimeUtils.java` into `DateTimeUtils.java`
- Added null checks to prevent NPEs

### Testing

**Backend compile:**
```bash
docker compose exec backend mvn compile -q
```

**Verify date formatting:**
1. Create a booking
2. Check confirmation email shows correct date format
3. Check Admin > Bookings shows correct dates

---

## 7. SecurityUtils Extraction (`f2e5cb2d`)

### Changes
- Created `SecurityUtils.java` with `hasRole()`, `hasAnyRole()`, `getCurrentUserId()`
- Removed duplicate role-checking code from services

### Testing

**Role-based access:**

1. **As SUPER_ADMIN:**
   - Login as super admin
   - Verify access to all admin sections
   - Verify can edit any tour/user

2. **As PARTNER_ADMIN:**
   - Login as partner admin
   - Verify access to own tours only
   - Verify cannot see other partners' data

3. **As regular USER:**
   - Login as normal user
   - Verify admin routes return 403

---

## 8. apiProxy Utility (`5a29ea66`, `6a701356`)

### Changes
- Created `frontend/server/utils/apiProxy.ts`
- Refactored 70+ API handlers to use centralized proxy functions
- Reduced ~530 lines of duplicate error handling

### Testing

**Test key API endpoints:**

1. **Admin Tours API:**
   - Go to Admin > Tours
   - Verify list loads
   - Create a tour - verify success
   - Edit a tour - verify saves
   - Delete a tour - verify removes

2. **Admin Users API:**
   - Go to Admin > Users
   - Verify list loads
   - Create user - verify works
   - Edit user - verify saves

3. **Admin Bookings API:**
   - Go to Admin > Bookings
   - Verify list loads with filters
   - View booking details

4. **Admin Media API:**
   - Go to Admin > Media
   - Verify gallery loads
   - Upload image - verify success
   - Edit tags - verify saves
   - Delete image - verify removes

5. **Public APIs:**
   - `/api/tours` - verify tours load
   - `/api/calendar/data` - verify calendar data
   - `/api/lunar/calendar` - verify lunar data
   - `/api/weather/forecast` - verify weather data

6. **Auth APIs:**
   - Register new user - verify works
   - Login - verify works
   - Password change - verify works

---

## 9. Admin Options Centralization (`0f9875bd`)

### Changes
- Created centralized role/status options in `adminOptions.ts`
- Removed duplicate definitions from user modals

### Testing

**User management:**
1. Go to Admin > Users
2. Create new user - verify role dropdown has all options
3. Edit user - verify status dropdown has all options
4. Verify badge colors match role/status correctly

---

## 10. API Handler Types (`ffda509a`)

### Changes
- Added proper TypeScript types to API handlers using api-client types

### Testing

```bash
cd frontend && pnpm typecheck
```

---

## 11. DRY Phase 1-2 (`7e98e938`)

### Changes
- `BadgeColor` type in `app/types/ui.ts`
- `localeUtils.ts` with `getLocaleCode()`
- `useFormSubmit` composable
- `useTableFilter` composable
- `TagInput.vue` component
- Updated 3 media modals

### Testing

**TagInput Component:**
1. Go to Admin > Media
2. Click "Upload" to open UploadModal
3. In "Etiquetas" field:
   - Type tag + Enter - should add
   - Click tag - should remove
   - Try duplicate - should be prevented
4. Open MediaEditModal (edit any image)
   - Verify tags load correctly
   - Test add/remove
5. Select multiple images > "Editar Etiquetas"
   - Test add/replace modes

**Currency formatting:**
1. Change language to EN, add item to cart
2. Verify price format (USD style)
3. Change to PT - verify format changes
4. Change to ES - verify CLP format

---

## 12. DRY Phase 3-5 (`d8797dc3`)

### Changes
- `LoadingState.vue`, `EmptyState.vue`, `ConfirmDialog.vue` components
- `useLightboxNavigation.ts` composable
- `useConfirmDialog.ts` composable
- Backend: `OwnedEntity.java` interface
- Backend: `SecurityUtils.validateOwnership()`
- Backend: `DateTimeUtils` formatter constants

### Testing

**Lightbox Navigation:**
1. Go to any tour detail with multiple images
2. Click image to open lightbox
3. Test:
   - Left/right arrows
   - Arrow Left/Right keys
   - Escape to close
   - Looping (end wraps to start)
4. Go to Admin > Media
5. Click image to open MediaLightbox
6. Test same navigation (no looping)

**Ownership Validation:**
1. Login as PARTNER_ADMIN
2. Go to Admin > Calendar
3. Create schedule for tour you OWN - should succeed
4. Edit/delete that schedule - should succeed
5. Login as SUPER_ADMIN
6. Verify can manage ANY tour's schedules

---

## Automated Test Commands

```bash
# Frontend lint
cd frontend && pnpm lint .

# Frontend typecheck
cd frontend && pnpm typecheck

# Backend compile
docker compose exec backend mvn compile -q

# Backend tests
docker compose exec backend mvn test

# E2E tests
cd frontend && pnpm test:e2e
```

---

## Quick Smoke Test Checklist

Run these in order to quickly verify nothing is broken:

- [ ] `docker compose up -d` - services start without errors
- [ ] `docker compose exec backend mvn compile -q` - backend compiles
- [ ] `cd frontend && pnpm lint .` - no new lint errors
- [ ] Homepage loads at http://localhost:3000
- [ ] Tour listing page loads
- [ ] Tour detail page with calendar works
- [ ] Language switcher works (ES/EN/PT)
- [ ] Login works
- [ ] Admin dashboard loads (with admin user)
- [ ] Admin > Tours loads and CRUD works
- [ ] Admin > Media gallery loads
- [ ] Cart/checkout flow works

---

## Rollback Plan

If issues are found after pushing, revert specific commits:

```bash
# Revert a single commit
git revert <commit-hash>

# Revert multiple commits (interactive)
git revert --no-commit <oldest>^..<newest>
git commit -m "Revert: description"
```

Commits are independent, so most can be reverted individually without breaking others.

---

## Sign-off Checklist

### Backend
- [ ] Backend compiles successfully
- [ ] Flyway migration V10 ran (indexes exist)
- [ ] DateTimeUtils works (dates display correctly)
- [ ] SecurityUtils role checks work
- [ ] TourUtils pricing calculations correct
- [ ] Ownership validation works for PARTNER_ADMIN

### Frontend
- [ ] Frontend lint passes
- [ ] TypeScript check passes (no new errors)
- [ ] i18n works in all 3 languages (ES/EN/PT)
- [ ] TourCalendar no memory leak
- [ ] TagInput works in all 3 modals
- [ ] Lightbox navigation works (keyboard + arrows)
- [ ] Currency formatting correct per locale
- [ ] All admin CRUD operations work
- [ ] Public APIs return data correctly

### E2E (optional)
- [ ] E2E test suite passes

---

## Notes

- All changes are backward compatible
- No database schema changes (only indexes added)
- No API contract changes
- No breaking changes to frontend components
