# DRY Code Audit Results

**Date:** December 28, 2025  
**Status:** Comprehensive audit completed

---

## Executive Summary

This document contains the results of a full DRY (Don't Repeat Yourself) code audit across the entire Northern Chile codebase. The audit identified duplicate patterns in both backend (Java/Spring Boot) and frontend (Nuxt 3/Vue) code.

### Already Completed Refactoring

| Refactoring | Files Changed | Lines Saved |
|-------------|---------------|-------------|
| Consolidated `ChileDateTimeUtils` into `DateTimeUtils` | 8 | ~50 |
| Created `SecurityUtils.java` for role checks | 12 | ~80 |
| Created `apiProxy.ts` utility | 70+ handlers | ~600 |
| Created `adminOptions.ts` for centralized options | 6 | ~120 |
| Added proper TypeScript types to API handlers | 21 | N/A |
| **Total** | **~115** | **~850** |

### Remaining Opportunities Summary

| Area | Patterns Found | Estimated Lines Savable | Priority |
|------|---------------|------------------------|----------|
| Backend Services | 11 | ~325 | Medium |
| Frontend Components | 10 | ~400 | Medium |
| Frontend Pages/Stores | 10 | ~350 | High |
| Frontend Composables | 9 | ~150 | Low |
| **Total** | **40** | **~1,225** |

---

## Part 1: Backend (Java/Spring Boot)

### 1.1 Ownership Validation Pattern (HIGH PRIORITY)

**Files:**
- `TourScheduleService.java` (lines 50-54, 91-94, 137-140, 182-185)
- `TourService.java` (lines 143, 203)
- `BookingService.java` (lines 276, 299, 356-358)

**Current Pattern:**
```java
if (!"ROLE_SUPER_ADMIN".equals(currentUser.getRole()) && 
    !entity.getOwner().getId().equals(currentUser.getId())) {
    throw new AccessDeniedException("No permission");
}
```

**Suggested Refactoring:**
```java
// SecurityUtils.java
public static void validateOwnership(User currentUser, OwnedEntity entity) {
    if (!hasRole(currentUser, "ROLE_SUPER_ADMIN") && 
        !entity.getOwner().getId().equals(currentUser.getId())) {
        throw new AccessDeniedException("No permission to modify this resource");
    }
}
```

**Estimated Savings:** ~30 lines

---

### 1.2 User Lookup from Authentication Pattern

**Files:**
- `BookingSecurityService.java` (lines 32-37, 56-60)
- `TourSecurityService.java` (lines 30-35)

**Current Pattern:**
```java
String userEmail = authentication.getName();
User currentUser = userRepository.findByEmail(userEmail).orElse(null);
if (currentUser == null) { return false; }
```

**Suggested Refactoring:**
```java
// SecurityUtils.java
public static Optional<User> getCurrentUser(Authentication auth, UserRepository repo) {
    if (auth == null) return Optional.empty();
    return repo.findByEmail(auth.getName());
}
```

**Estimated Savings:** ~20 lines

---

### 1.3 Entity Soft-Delete Pattern

**Files:**
- `TourService.java` (lines 205-219)
- `UserService.java` (lines 129-149)

**Current Pattern:**
```java
entity.setDeletedAt(Instant.now());
repository.save(entity);
auditLogService.logDelete(...);
```

**Suggested Refactoring:**
Create `SoftDeletable<T>` interface and generic service method.

**Estimated Savings:** ~25 lines

---

### 1.4 Update with Audit Pattern (MEDIUM PRIORITY)

**Files:**
- `TourService.java` (lines 145-200)
- `UserService.java` (lines 87-127)
- `TourScheduleService.java` (lines 84-127)
- `BookingService.java` (lines 276-296)

**Current Pattern:**
```java
Map<String, Object> oldValues = captureValues(entity);
// ... update entity fields
Map<String, Object> newValues = captureValues(entity);
auditLogService.logUpdate(entityType, id, description, oldValues, newValues, user);
```

**Suggested Refactoring:**
Use AOP aspect for automatic audit logging:
```java
@Audited(entityType = "TOUR")
public TourRes updateTour(...) { ... }
```

**Estimated Savings:** ~60 lines

---

### 1.5 ~~Missing TourScheduleMapper~~ (NOT RECOMMENDED)

**Files:**
- `TourScheduleService.java` (lines 201-227)

**Description:** Manual mapping method `toTourScheduleRes()`.

**Why NOT to refactor:**
- The mapping includes **business logic** (`availabilityValidator.getAvailabilityStatus()`) that calculates real-time availability
- MapStruct is designed for simple field-to-field mapping, not complex computed values
- The method is only used in one place (no duplication)
- Current implementation is readable and maintainable

**Estimated Savings:** 0 lines (keep as-is)

---

### 1.6 Admin Email Notification Consolidation

**Files:**
- `EmailService.java` (lines 113-149, 155-183, 189-211)

**Current Pattern:**
Three admin notification methods with similar structure.

**Suggested Refactoring:**
```java
private void sendAdminNotification(String templateName, Map<String, Object> variables) {
    if (!mailEnabled) return;
    Context context = createContext(new Locale("es", "CL"));
    variables.forEach(context::setVariable);
    sendHtmlEmail(adminEmail, getSubject(templateName), templateName, context);
}
```

**Estimated Savings:** ~40 lines

---

### 1.7 DateTimeFormatter Constants

**Files:**
- `BookingService.java` (lines 171-179)
- `EmailService.java` (lines 133-134, 142, 169, 176)

**Suggested Refactoring:**
Add constants to `DateTimeUtils.java`:
```java
public static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
public static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("HH:mm");
```

**Estimated Savings:** ~15 lines

---

### 1.8 Permission Resolver Pattern

**Files:**
- `BookingSecurityService.java` (lines 87-139)
- `TourSecurityService.java` (lines 58-116)

**Current Pattern:**
```java
if (isSuperAdmin) return true;
if (isPartnerAdmin) return isOwner();
return false;
```

**Suggested Refactoring:**
Create generic permission resolver.

**Estimated Savings:** ~30 lines

---

### 1.9 @Where Clause for Soft-Delete

**Files:**
- `TourRepository.java` (multiple queries with `AND t.deletedAt IS NULL`)

**Suggested Refactoring:**
Add to `Tour` entity:
```java
@Where(clause = "deleted_at IS NULL")
public class Tour { ... }
```

**Estimated Savings:** ~10 lines

---

### 1.10 Error Response Builder

**Files:**
- `GlobalExceptionHandler.java` (lines 69-135)

**Current Pattern:** Each handler builds ErrorResponse similarly.

**Suggested Refactoring:**
```java
private ResponseEntity<ErrorResponse> buildError(HttpStatus status, Exception ex, WebRequest req) {
    return ResponseEntity.status(status).body(ErrorResponse.of(status, ex, req));
}
```

**Estimated Savings:** ~50 lines

---

### 1.11 Audit Description Helper

**Files:**
- `BookingService.java` (lines 289-290, 312-313, 341-342, 385-386)
- `TourService.java` (lines 79, 149, 190, 212)
- `TourScheduleService.java` (lines 69-70, 117-118, 149-150, 188-189)

**Current Pattern:**
```java
String description = "Tour - " + tour.getName();
```

**Suggested Refactoring:**
```java
// AuditUtils.java
public static String buildDescription(String entityType, String identifier) {
    return entityType + " - " + identifier;
}
```

**Estimated Savings:** ~20 lines

---

### Backend Summary Table

| Pattern | Priority | Estimated Savings |
|---------|----------|-------------------|
| Ownership validation | High | ~30 lines |
| User lookup from auth | Medium | ~20 lines |
| Soft-delete pattern | Low | ~25 lines |
| Update with audit | Medium | ~60 lines |
| ~~TourScheduleMapper~~ | ~~High~~ | ~~0~~ (not recommended) |
| Admin email consolidation | Medium | ~40 lines |
| DateTimeFormatter constants | Low | ~15 lines |
| Permission resolver | Low | ~30 lines |
| @Where soft-delete | Low | ~10 lines |
| Error response builder | Low | ~50 lines |
| Audit description helper | Low | ~20 lines |
| **Total** | | **~300 lines** |

---

## Part 2: Frontend Components

### 2.1 Modal v-model Logic Pattern (11 files)

**Files:**
- `admin/media/UploadModal.vue`
- `admin/media/BulkAssignTourModal.vue`
- `admin/media/MediaEditModal.vue`
- `admin/media/BulkTagsModal.vue`
- `admin/media/SelectorModal.vue`
- `admin/users/CreateUserModal.vue`
- `admin/users/EditUserModal.vue`
- `admin/bookings/BookingDetailsModal.vue`
- `profile/EditBookingModal.vue`
- `common/ImageLightbox.vue`
- `admin/media/MediaLightbox.vue`

**Current Pattern:**
```typescript
const isOpen = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})
```

**Suggested Refactoring:**
```typescript
// composables/useModalState.ts
export function useModalState(props: { modelValue: boolean }, emit: (e: 'update:modelValue', v: boolean) => void) {
  return computed({
    get: () => props.modelValue,
    set: value => emit('update:modelValue', value)
  })
}
```

**Estimated Savings:** ~30 lines

---

### 2.2 Tag Input Management (HIGH PRIORITY)

**Files:**
- `admin/media/UploadModal.vue` (lines 43-60, 448-485)
- `admin/media/MediaEditModal.vue` (lines 58-75, 252-291)
- `admin/media/BulkTagsModal.vue` (lines 32-49, 162-201)

**Current Pattern:** 60+ lines of identical tag add/remove logic in each file.

**Suggested Refactoring:**
Create `TagInput.vue` component:
```vue
<TagInput v-model="metadata.tags" placeholder="Add tags..." />
```

**Estimated Savings:** ~120 lines

---

### 2.3 Status Badge Colors (PARTIALLY DONE)

**Files:**
- `admin/StatusBadge.vue` (exists but not used everywhere)
- `profile/BookingsList.vue`
- `admin/bookings/BookingDetailsModal.vue`
- `pages/admin/private-requests.vue`
- `pages/admin/alerts.vue`
- `pages/admin/users.vue`

**Issue:** `AdminStatusBadge` component exists but `getStatusColor` functions are still defined locally in multiple files.

**Action:** Enforce use of `AdminStatusBadge` everywhere and remove local `getStatusColor` functions.

**Estimated Savings:** ~50 lines

---

### 2.4 Lightbox Navigation Logic

**Files:**
- `common/ImageLightbox.vue` (lines 24-66)
- `admin/media/MediaLightbox.vue` (lines 24-71)

**Current Pattern:** Nearly identical navigation with keyboard support.

**Suggested Refactoring:**
```typescript
// composables/useLightboxNavigation.ts
export function useLightboxNavigation(items: Ref<any[]>, initialIndex: Ref<number>) {
  const currentIndex = ref(initialIndex.value)
  const goToPrevious = () => { ... }
  const goToNext = () => { ... }
  // keyboard handling
  return { currentIndex, goToPrevious, goToNext, currentItem }
}
```

**Estimated Savings:** ~40 lines

---

### 2.5 Loading/Empty State Components

**Files:**
- `profile/BookingsList.vue`
- `admin/media/GalleryManager.vue`
- `admin/media/SelectorModal.vue`
- `pages/admin/private-requests.vue`
- `pages/admin/alerts.vue`

**Current Pattern:**
```html
<div v-if="loading" class="flex justify-center py-12">
  <UIcon name="i-heroicons-arrow-path" class="w-8 h-8 animate-spin" />
</div>
<div v-else-if="!items.length" class="text-center py-12">
  <UIcon name="i-heroicons-photo" class="w-12 h-12 mx-auto mb-4" />
  <p>No items found</p>
</div>
```

**Suggested Refactoring:**
Create `LoadingState.vue` and `EmptyState.vue`:
```vue
<LoadingState v-if="loading" />
<EmptyState v-else-if="!items.length" icon="i-heroicons-photo" :message="t('empty.photos')" />
```

**Estimated Savings:** ~60 lines

---

### 2.6 Confirmation Dialog Pattern

**Files:**
- `pages/admin/users.vue`
- `profile/BookingsList.vue`
- `admin/users/EditUserModal.vue`
- `admin/media/GalleryManager.vue`

**Issue:** Using native `confirm()` instead of proper UI modal.

**Suggested Refactoring:**
```typescript
// composables/useConfirmDialog.ts
export function useConfirmDialog() {
  const show = ref(false)
  const resolver = ref<(v: boolean) => void>()
  
  async function confirm(options: ConfirmOptions): Promise<boolean> {
    // ... show modal and wait for user response
  }
  
  return { confirm, ConfirmModal }
}
```

**Estimated Savings:** ~30 lines + better UX

---

### 2.7 Form Error Handling (USE EXISTING)

**Files:**
- `admin/users/CreateUserModal.vue`
- `admin/users/EditUserModal.vue`
- `profile/EditBookingModal.vue`

**Issue:** Manual error extraction instead of using `useApiError.ts`.

**Action:** Replace manual error handling with:
```typescript
const { handleError } = useApiError()
// In catch block:
handleError(error, 'Error al crear usuario')
```

**Estimated Savings:** ~40 lines

---

### 2.8 Multilingual Tabs Component

**Files:**
- `admin/media/UploadModal.vue`
- `admin/media/MediaEditModal.vue`

**Current Pattern:**
```html
<UTabs :items="[{ label: 'ES', slot: 'es' }, { label: 'EN', slot: 'en' }, { label: 'PT', slot: 'pt' }]">
  <template #es><UInput v-model="state.translations.es" /></template>
  <template #en><UInput v-model="state.translations.en" /></template>
  <template #pt><UInput v-model="state.translations.pt" /></template>
</UTabs>
```

**Suggested Refactoring:**
```vue
<MultilingualInput v-model="altTranslations" label="Alt Text" />
```

**Estimated Savings:** ~50 lines

---

### 2.9 Drag & Drop Upload Logic

**Files:**
- `admin/media/UploadModal.vue`
- `common/ImageUploader.vue`

**Suggested Refactoring:**
```typescript
// composables/useDropZone.ts
export function useDropZone(onFiles: (files: File[]) => Promise<void>) {
  const isDragging = ref(false)
  // ... handlers
  return { isDragging, onDragOver, onDragLeave, onDrop }
}
```

**Estimated Savings:** ~25 lines

---

### 2.10 Admin Page Header Pattern

**Files:**
- `pages/admin/bookings.vue`
- `pages/admin/users.vue`
- `pages/admin/private-requests.vue`
- `pages/admin/alerts.vue`

**Current Pattern:** Same header structure repeated.

**Suggested Refactoring:**
```vue
<AdminPageHeader title="..." subtitle="...">
  <template #actions>...</template>
</AdminPageHeader>
```

**Estimated Savings:** ~40 lines

---

### Components Summary Table

| Pattern | Priority | Files | Estimated Savings |
|---------|----------|-------|-------------------|
| Tag input management | High | 3 | ~120 lines |
| Loading/empty states | High | 5 | ~60 lines |
| Status badge enforcement | Medium | 6 | ~50 lines |
| Multilingual tabs | Medium | 2 | ~50 lines |
| Lightbox navigation | Medium | 2 | ~40 lines |
| Admin page header | Low | 4 | ~40 lines |
| Form error handling | Medium | 3 | ~40 lines |
| Modal v-model logic | Low | 11 | ~30 lines |
| Confirmation dialog | Low | 4 | ~30 lines |
| Drag & drop | Low | 2 | ~25 lines |
| **Total** | | | **~485 lines** |

---

## Part 3: Frontend Pages & Stores

### 3.1 useFormSubmit Composable (HIGH PRIORITY)

**Files affected:** 10+ pages with forms

**Current Pattern:**
```typescript
const loading = ref(false)
async function handleSubmit() {
  loading.value = true
  try {
    await $fetch('/api/...', { method: 'POST', body: formData })
    toast.add({ title: t('success'), color: 'success' })
  } catch (e) {
    toast.add({ title: t('error'), color: 'error' })
  } finally {
    loading.value = false
  }
}
```

**Suggested Refactoring:**
```typescript
// composables/useFormSubmit.ts
export function useFormSubmit<T>() {
  const loading = ref(false)
  const error = ref<Error | null>(null)
  
  async function submit(
    fn: () => Promise<T>,
    options?: { successMessage?: string; errorMessage?: string }
  ): Promise<T | null> {
    loading.value = true
    error.value = null
    try {
      const result = await fn()
      if (options?.successMessage) showSuccessToast(options.successMessage)
      return result
    } catch (e) {
      error.value = e as Error
      showErrorToast(e, options?.errorMessage)
      return null
    } finally {
      loading.value = false
    }
  }
  
  return { loading, error, submit }
}
```

**Estimated Savings:** ~100 lines

---

### 3.2 useTableFilter Composable (HIGH PRIORITY)

**Files affected:** All admin tables (6+ pages)

**Current Pattern:**
```typescript
const filteredData = computed(() => {
  let result = data.value || []
  if (searchQuery.value) {
    result = result.filter(item => 
      item.name?.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      item.email?.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
  }
  if (statusFilter.value) {
    result = result.filter(item => item.status === statusFilter.value)
  }
  return result
})
```

**Suggested Refactoring:**
```typescript
// composables/useTableFilter.ts
export function useTableFilter<T>(
  data: Ref<T[]>,
  options: {
    searchFields: (keyof T)[]
    filterField?: keyof T
  }
) {
  const searchQuery = ref('')
  const filterValue = ref<string | null>(null)
  
  const filteredData = computed(() => {
    // ... filtering logic
  })
  
  return { searchQuery, filterValue, filteredData }
}

// Usage:
const { searchQuery, filterValue, filteredData } = useTableFilter(users, {
  searchFields: ['name', 'email'],
  filterField: 'role'
})
```

**Estimated Savings:** ~80 lines

---

### 3.3 AsyncContent Component (HIGH PRIORITY)

**Files affected:** All pages with async data

**Current Pattern:**
```vue
<div v-if="pending">Loading...</div>
<div v-else-if="error">Error</div>
<div v-else>Content</div>
```

**Suggested Refactoring:**
```vue
<AsyncContent :pending="pending" :error="error">
  <template #loading>Custom loading...</template>
  <template #error>Custom error...</template>
  <template #default>Content</template>
</AsyncContent>
```

**Estimated Savings:** ~60 lines

---

### 3.4 usePageSeo Composable

**Files affected:** All public pages (10+)

**Current Pattern:**
```typescript
useSeoMeta({
  title: t('page.title'),
  description: t('page.description'),
  ogTitle: t('page.title'),
  ogDescription: t('page.description')
})
```

**Suggested Refactoring:**
```typescript
// composables/usePageSeo.ts
export function usePageSeo(key: string) {
  const { t } = useI18n()
  useSeoMeta({
    title: t(`${key}.title`),
    description: t(`${key}.description`),
    ogTitle: t(`${key}.title`),
    ogDescription: t(`${key}.description`)
  })
}

// Usage:
usePageSeo('pages.about')
```

**Estimated Savings:** ~50 lines

---

### 3.5 useDeleteConfirmation Composable

**Files affected:** Admin CRUD pages (5+)

**Current Pattern:**
```typescript
const deleteModal = ref(false)
const itemToDelete = ref(null)
function confirmDelete(item) {
  itemToDelete.value = item
  deleteModal.value = true
}
async function deleteItem() { ... }
```

**Suggested Refactoring:**
```typescript
const { showDeleteModal, itemToDelete, confirmDelete, executeDelete } = useDeleteConfirmation({
  deleteEndpoint: (id) => `/api/admin/users/${id}`,
  onSuccess: () => refresh()
})
```

**Estimated Savings:** ~60 lines

---

### Pages/Stores Summary Table

| Pattern | Priority | Estimated Savings |
|---------|----------|-------------------|
| useFormSubmit | High | ~100 lines |
| useTableFilter | High | ~80 lines |
| AsyncContent | High | ~60 lines |
| useDeleteConfirmation | Medium | ~60 lines |
| usePageSeo | Medium | ~50 lines |
| **Total** | | **~350 lines** |

---

## Part 4: Frontend Composables & Utilities

### 4.1 Remove useDateTime Composable (QUICK WIN)

**File:** `app/composables/useDateTime.ts`

**Issue:** It's just a passthrough to `dateUtils.ts` with no added value.

**Action:** Delete and update imports to use `dateUtils.ts` directly.

---

### 4.2 Extract getLocaleCode Utility

**Files:**
- `app/composables/useCurrency.ts` (lines 71-78)
- `app/composables/useBookingPdf.ts` (lines 33-40)

**Suggested Refactoring:**
```typescript
// utils/localeUtils.ts
export const LOCALE_MAP = { es: 'es-CL', en: 'en-US', pt: 'pt-BR' } as const
export function getLocaleCode(locale: string): string {
  return LOCALE_MAP[locale as keyof typeof LOCALE_MAP] || 'es-CL'
}
```

---

### 4.3 Consolidate formatFileSize

**Files:**
- `app/composables/useImageOptimizer.ts` (lines 216-227)
- `app/utils/media.ts` (lines 5-11)

**Action:** Keep only the one in `media.ts`, update `useImageOptimizer` to import it.

---

### 4.4 Extract BadgeColor Type

**Files:**
- `app/utils/adminOptions.ts`
- `app/utils/media.ts`
- `app/composables/useCalendarData.ts`

**Suggested Refactoring:**
```typescript
// types/ui.ts
export type BadgeColor = 'error' | 'info' | 'success' | 'primary' | 'secondary' | 'tertiary' | 'warning' | 'neutral'
```

---

### 4.5 Remove Duplicate formatDate from media.ts

**Files:**
- `app/utils/media.ts` (lines 13-21)
- `app/utils/dateUtils.ts` (lines 78+)

**Action:** Remove `formatDate`/`formatDateTime` from `media.ts`, use `dateUtils.ts`.

---

### 4.6 Standardize Error Toast Usage

**Files:**
- `app/composables/useAdminTourForm.ts`
- `app/composables/useS3Upload.ts`

**Issue:** Not using `useApiError().showErrorToast()`.

**Action:** Update to use centralized error handling.

---

### 4.7 Extract baseFetchOptions

**Files:**
- `app/composables/useAdminData.ts`
- `app/composables/useS3Upload.ts`
- `app/composables/useCalendarData.ts`

**Suggested Refactoring:**
```typescript
// utils/fetchConfig.ts
export const baseFetchOptions = {
  credentials: 'include' as RequestCredentials
}
```

---

### Composables Summary Table

| Pattern | Priority | Action |
|---------|----------|--------|
| Remove useDateTime | High | Delete file |
| Extract getLocaleCode | Medium | Create localeUtils.ts |
| Consolidate formatFileSize | Medium | Keep in media.ts |
| Extract BadgeColor | Low | Create types/ui.ts |
| Remove media.ts date functions | High | Update imports |
| Standardize error toasts | Medium | Use useApiError |
| Extract baseFetchOptions | Low | Create fetchConfig.ts |

---

## Recommended Implementation Order

### Phase 1: Quick Wins (Low Effort, High Impact)

1. **Delete `useDateTime.ts`** - No added value
2. **Remove duplicate date functions from `media.ts`**
3. **Extract `BadgeColor` type**
4. **Extract `getLocaleCode` utility**
5. **Consolidate `formatFileSize`**

### Phase 2: High-Value Composables (Medium Effort)

1. **Create `useFormSubmit` composable** - Used in 10+ places
2. **Create `useTableFilter` composable** - Used in all admin tables
3. **Create `AsyncContent` component** - Universal usage
4. **Create `TagInput` component** - High duplication

### Phase 3: Component Improvements (Medium Effort)

1. **Create `LoadingState` and `EmptyState` components**
2. **Enforce `AdminStatusBadge` usage everywhere**
3. **Create `useLightboxNavigation` composable**
4. **Create `MultilingualInput` component**

### Phase 4: Backend Improvements (Higher Effort)

1. **Add ownership validation to `SecurityUtils`**
2. **Consolidate admin email notifications**
3. **Add DateTimeFormatter constants**

### Phase 5: Advanced Refactoring (High Effort)

1. **Create AOP aspect for audit logging**
2. **Create `useConfirmDialog` with proper UI**
3. **Create generic permission resolver**
4. **Add `@Where` clause for soft-delete**

---

## Metrics

| Metric | Value |
|--------|-------|
| Total patterns identified | 40 |
| Total estimated lines savable | ~1,575 |
| Already completed (this audit cycle) | ~850 lines |
| Remaining opportunity | ~725 lines |
| Files analyzed | ~200+ |
| Priority 1 items | 8 |
| Priority 2 items | 12 |
| Priority 3 items | 20 |

---

## Appendix: Files Reference

### Backend Files Analyzed
- `com.northernchile.api.service.*` (8 services)
- `com.northernchile.api.security.*` (3 security services)
- `com.northernchile.api.controller.*` (10+ controllers)
- `com.northernchile.api.repository.*` (8 repositories)
- `com.northernchile.api.util.*` (2 utilities)

### Frontend Files Analyzed
- `app/components/**/*.vue` (50+ components)
- `app/pages/**/*.vue` (25+ pages)
- `app/composables/*.ts` (15 composables)
- `app/stores/*.ts` (3 stores)
- `app/utils/*.ts` (4 utilities)
- `server/api/**/*.ts` (70+ handlers)

---

*Last updated: December 28, 2025*
