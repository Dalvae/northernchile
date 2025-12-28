# DRY Refactoring - Testing Guide

This guide covers all the changes made during the DRY (Don't Repeat Yourself) refactoring sessions that need to be tested before pushing to remote.

## Summary of Changes

### Commit 1: `7e98e938` - Phase 1 & 2 Refactoring
- Created shared `BadgeColor` type
- Created `localeUtils.ts` with `getLocaleCode()`
- Created `useFormSubmit` composable
- Created `useTableFilter` composable
- Created `TagInput.vue` component
- Updated 3 media modals to use TagInput

### Current Uncommitted Changes - Phase 3, 4 & 5
- `LoadingState.vue` component
- `EmptyState.vue` component
- `ConfirmDialog.vue` component
- `useLightboxNavigation.ts` composable
- `useConfirmDialog.ts` composable
- Updated `ImageLightbox.vue` and `MediaLightbox.vue`
- Backend: `OwnedEntity.java` interface
- Backend: `SecurityUtils.java` ownership validation
- Backend: `DateTimeUtils.java` formatter constants
- Backend: `TourScheduleService.java` using new validation
- Backend: `Tour.java` implements OwnedEntity

---

## Frontend Testing

### 1. TagInput Component
**Location:** Used in media upload/edit modals

**Test Steps:**
1. Go to Admin > Media
2. Click "Upload" to open UploadModal
3. In the "Etiquetas" field:
   - Type a tag and press Enter - should add tag
   - Click on a tag - should remove it
   - Try adding duplicate tag - should be prevented
4. Open MediaEditModal (click edit on any image)
   - Verify tags load correctly
   - Test add/remove tags
5. Select multiple images and use "Editar Etiquetas" bulk action
   - Verify BulkTagsModal works with add/replace modes

### 2. Lightbox Navigation
**Location:** Tour detail page gallery, Admin media gallery

**Test Steps:**
1. Go to any tour detail page with multiple images
2. Click on an image to open lightbox
3. Test navigation:
   - Click left/right arrows
   - Press Arrow Left/Right keys
   - Press Escape to close
   - Verify looping (end wraps to start)
4. Go to Admin > Media
5. Click on any image to open MediaLightbox
6. Test same navigation (without looping)
7. Test "load more" at end if hasMore is true

### 3. LoadingState & EmptyState Components
**Note:** These are new components, not yet integrated. Manual verification:
```bash
# The components exist and lint passes
ls frontend/app/components/common/LoadingState.vue
ls frontend/app/components/common/EmptyState.vue
```

### 4. ConfirmDialog Component
**Note:** New component, not yet integrated. Manual verification:
```bash
# The component and composable exist and lint passes
ls frontend/app/components/common/ConfirmDialog.vue
ls frontend/app/composables/useConfirmDialog.ts
```

### 5. Currency Formatting
**Location:** Cart, Checkout, Booking pages

**Test Steps:**
1. Change language to English (EN)
2. Add item to cart
3. Verify prices show with correct format (USD, etc.)
4. Change to Portuguese (PT)
5. Verify format changes appropriately
6. Change back to Spanish (ES)
7. Verify CLP format

### 6. Booking PDF Download
**Location:** Profile > My Bookings

**Test Steps:**
1. Log in as a user with bookings
2. Go to Profile > My Bookings
3. Click "Download" on a booking
4. Verify PDF generates with correct locale formatting

---

## Backend Testing

### 1. Ownership Validation (SecurityUtils)
**Location:** Tour schedule CRUD operations

**Test Steps:**
1. Log in as PARTNER_ADMIN user
2. Go to Admin > Calendar
3. Create a new schedule for a tour YOU OWN - should succeed
4. Try to edit/delete that schedule - should succeed
5. (If possible) Try to access a schedule for a tour you DON'T own - should fail with 403

6. Log in as SUPER_ADMIN user
7. Verify you can create/edit/delete schedules for ANY tour

### 2. DateTimeUtils Formatters
**Note:** These are new constants, integration happens organically when code is updated to use them.

**Quick Backend Compile Test:**
```bash
docker compose exec backend mvn compile -q
# Should complete without errors
```

---

## Automated Tests

### Frontend Lint
```bash
cd frontend && pnpm lint .
```
Expected: No NEW errors from our changes (pre-existing errors may exist)

### Backend Compile
```bash
docker compose exec backend mvn compile -q
```
Expected: Successful compilation

### E2E Tests (if time permits)
```bash
cd frontend && pnpm test:e2e
```

---

## Files Changed Summary

### New Files (Frontend)
- `app/types/ui.ts` - BadgeColor type
- `app/utils/localeUtils.ts` - Locale utilities
- `app/composables/useFormSubmit.ts` - Form submission helper
- `app/composables/useTableFilter.ts` - Table filtering helper
- `app/composables/useLightboxNavigation.ts` - Lightbox navigation
- `app/composables/useConfirmDialog.ts` - Confirmation dialog
- `app/components/ui/TagInput.vue` - Tag input component
- `app/components/common/LoadingState.vue` - Loading spinner
- `app/components/common/EmptyState.vue` - Empty state display
- `app/components/common/ConfirmDialog.vue` - Confirmation modal

### Modified Files (Frontend)
- `app/utils/adminOptions.ts` - Uses shared BadgeColor
- `app/utils/media.ts` - Uses shared BadgeColor, delegates date formatting
- `app/composables/useCurrency.ts` - Uses localeUtils
- `app/composables/useBookingPdf.ts` - Uses localeUtils
- `app/composables/useImageOptimizer.ts` - Uses shared formatFileSize
- `app/components/admin/media/UploadModal.vue` - Uses TagInput
- `app/components/admin/media/MediaEditModal.vue` - Uses TagInput
- `app/components/admin/media/BulkTagsModal.vue` - Uses TagInput
- `app/components/common/ImageLightbox.vue` - Uses useLightboxNavigation
- `app/components/admin/media/MediaLightbox.vue` - Uses useLightboxNavigation

### New Files (Backend)
- `model/OwnedEntity.java` - Interface for owned entities

### Modified Files (Backend)
- `util/SecurityUtils.java` - Added ownership validation helpers
- `util/DateTimeUtils.java` - Added formatter constants
- `model/Tour.java` - Implements OwnedEntity
- `tour/TourScheduleService.java` - Uses SecurityUtils.validateOwnership()

---

## Rollback Plan

If issues are found, individual changes can be reverted:

```bash
# Revert last commit (Phase 1 & 2)
git revert 7e98e938

# Discard uncommitted changes
git checkout -- .
```

---

## Sign-off Checklist

- [ ] TagInput works in all 3 media modals
- [ ] Lightbox navigation works (keyboard + buttons)
- [ ] Currency formatting correct in all locales
- [ ] PDF download works with correct formatting
- [ ] Backend compiles successfully
- [ ] Schedule CRUD respects ownership
- [ ] Frontend lint passes (no new errors)
- [ ] E2E tests pass (optional)
