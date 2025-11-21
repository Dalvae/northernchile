# TODO - Northern Chile Platform

## Media Management - Future Optimizations

### Gallery Pagination (Low Priority)
**Issue**: Gallery endpoints (`/api/admin/media/tour/{tourId}/gallery` and `/api/admin/media/schedule/{scheduleId}/gallery`) return all photos without pagination.

**Current State**:
- Returns `List<MediaRes>` (all photos)
- Works fine for typical galleries (5-50 photos)
- Needed for drag-and-drop reordering UX

**Potential Issues**:
- If tours have 100+ photos, could be slow
- Currently not a problem with typical usage

**Solution Options** (if becomes issue):
1. Add pagination to gallery endpoints (but loses full-view reordering)
2. Implement lazy loading with infinite scroll
3. Add "Load More" button with chunked loading
4. Virtualization with `@vueuse/virtual-list`

**Decision**: Monitor in production. Only implement if users report slowness.

---

## TourModal Rendering Issue (FIXED - Workaround Applied)

### Bug Description
When opening the `TourModal` component, the "General" tab content didn't render on first load. Content only appeared after switching to "Media" tab and back to "General".

### Root Cause
- **Primary Issue**: UTabs slots (`#general`, `#gallery`) don't render correctly on initial modal mount
- **Contributing Factors**:
  - Combination of `UTabs` + `UModal` + `UForm` + nested reactive state
  - Possible timing issue between modal opening and slot content initialization
  - State from `useAdminTourForm` composable may not sync correctly on first render

### Files Affected
```
frontend/app/components/admin/tours/TourModal.vue
frontend/app/composables/useAdminTourForm.ts
```

### Solution Applied (Workaround)
**Changed from**: Using UTabs with named slots (`<template #general>`, `<template #gallery>`)

**Changed to**:
- UTabs only handles navigation (tab buttons)
- Content rendered separately with `v-show` based on `mainTab` value
- Added `nextTick()` in composable watcher for better DOM sync

**Key Changes**:
```vue
<!-- Before: Content inside slots -->
<UTabs v-model="mainTab" :items="items">
  <template #general><!-- content --></template>
</UTabs>

<!-- After: Content separated with v-show -->
<UTabs v-model="mainTab" :items="items" />
<div v-show="mainTab === 0"><!-- content --></div>
```

### Notes
- Not a documented "bug" in Nuxt UI - may be project-specific
- Related issues found: nuxt/ui#2356 (tabs + URL), nuxt/ui#645 (SFC in slots)
- Workaround is a common pattern when tabs have slot rendering issues
- Consider revisiting if Nuxt UI updates fix underlying issue

### Priority
- ✅ **FIXED** - Working with current workaround
- 🔍 **MONITOR** - Watch for Nuxt UI updates that might resolve properly

---
