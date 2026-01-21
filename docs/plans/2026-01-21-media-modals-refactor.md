# Media Modals Refactor Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Standardize the 5 media modal components to use consistent patterns for state management, data fetching, and form handling.

**Architecture:**
- All form modals will use `useControlledModalForm()` composable for consistent state/submission handling
- Data for dropdowns (tours, schedules) will be passed as props from parent, not fetched inside modals
- Create a shared `useMediaModalData()` composable at the page level to provide dropdown data to all modals

**Tech Stack:** Vue 3 Composition API, Nuxt 3, TypeScript, Pinia

---

## Current State Summary

| Modal | State Pattern | Data Fetching | Issues |
|-------|---------------|---------------|--------|
| UploadModal | Manual refs | None | OK, but no `useControlledModalForm` |
| SelectorModal | Manual computed | On open/search | OK, search-driven is correct |
| MediaEditModal | `useControlledModalForm` | Lazy on open | Should receive data as props |
| BulkAssignTourModal | Manual computed + watch | Via store | Should use `useControlledModalForm` |
| BulkTagsModal | Manual computed + watch | Per-item on save | Should use `useControlledModalForm` |

---

## Task 1: Create Shared Media Modal Data Composable

**Files:**
- Create: `frontend/app/composables/useMediaModalData.ts`

**Step 1: Write the composable**

```typescript
import type { TourRes, ScheduleRes } from 'api-client'

export interface MediaModalData {
  tours: Ref<TourRes[]>
  schedules: Ref<ScheduleRes[]>
  tourOptions: ComputedRef<Array<{ label: string; value: string | undefined }>>
  scheduleOptions: ComputedRef<Array<{ label: string; value: string | undefined }>>
  loading: Ref<boolean>
  loaded: Ref<boolean>
  load: () => Promise<void>
}

export function useMediaModalData(): MediaModalData {
  const adminStore = useAdminStore()
  const { fetchAdminSchedules } = useAdminData()

  const tours = ref<TourRes[]>([])
  const schedules = ref<ScheduleRes[]>([])
  const loading = ref(false)
  const loaded = ref(false)

  const tourOptions = computed(() => [
    { label: 'Sin asignar', value: undefined },
    ...tours.value
      .filter(t => t?.id)
      .map(t => ({
        label: t.nameTranslations?.es || 'Sin nombre',
        value: t.id
      }))
  ])

  const scheduleOptions = computed(() => [
    { label: 'Sin asignar', value: undefined },
    ...schedules.value
      .filter(s => s?.id)
      .map(s => ({
        label: `${s.tourName} - ${s.startDatetime ? new Date(s.startDatetime).toLocaleDateString('es-CL') : ''}`,
        value: s.id
      }))
  ])

  async function load() {
    if (loaded.value || loading.value) return

    loading.value = true
    try {
      const [toursData, schedulesData] = await Promise.all([
        adminStore.fetchTours(),
        fetchAdminSchedules({ mode: 'past' })
      ])
      tours.value = toursData || []
      schedules.value = schedulesData || []
      loaded.value = true
    } catch (error) {
      console.error('Error loading media modal data:', error)
    } finally {
      loading.value = false
    }
  }

  return {
    tours,
    schedules,
    tourOptions,
    scheduleOptions,
    loading,
    loaded,
    load
  }
}
```

**Step 2: Verify TypeScript compiles**

Run: `cd frontend && pnpm typecheck`
Expected: No errors related to useMediaModalData

**Step 3: Commit**

```bash
git add frontend/app/composables/useMediaModalData.ts
git commit -m "feat(media): add useMediaModalData composable for shared dropdown data"
```

---

## Task 2: Refactor MediaEditModal to Receive Data as Props

**Files:**
- Modify: `frontend/app/components/admin/media/MediaEditModal.vue`

**Step 1: Update props interface**

Replace the current data fetching with props:

```typescript
// OLD - remove these lines:
const tours = ref<import('api-client').TourRes[]>([])
const schedules = ref<import('api-client').ScheduleRes[]>([])
const dataLoaded = ref(false)
async function loadDropdownData() { ... }

// NEW - add to props:
const props = defineProps<{
  modelValue: boolean
  media: MediaRes | null
  tourOptions: Array<{ label: string; value: string | undefined }>
  scheduleOptions: Array<{ label: string; value: string | undefined }>
}>()
```

**Step 2: Remove the watch for loading data**

Remove this watch:
```typescript
// DELETE this watch
watch(() => props.modelValue, (isOpen) => {
  if (isOpen) {
    loadDropdownData()
  }
}, { immediate: true })
```

**Step 3: Update computed options to use props directly**

```typescript
// OLD - remove these computeds:
const tourOptions = computed(() => [...])
const scheduleOptions = computed(() => [...])

// NEW - use props directly in template:
// :items="tourOptions" becomes :items="props.tourOptions"
```

**Step 4: Update template to use props**

In template, change:
```vue
<!-- OLD -->
<USelect
  v-model="state.tourId"
  :items="tourOptions"
  ...
/>

<!-- NEW -->
<USelect
  v-model="state.tourId"
  :items="props.tourOptions"
  ...
/>
```

Same for scheduleOptions.

**Step 5: Remove unused imports and refs**

Remove:
- `const adminStore = useAdminStore()`
- Any unused imports related to fetching

**Step 6: Verify TypeScript compiles**

Run: `cd frontend && pnpm typecheck`
Expected: Errors in parent components (we'll fix next)

**Step 7: Commit**

```bash
git add frontend/app/components/admin/media/MediaEditModal.vue
git commit -m "refactor(media): MediaEditModal receives dropdown data as props"
```

---

## Task 3: Update GalleryManager to Provide Data to MediaEditModal

**Files:**
- Modify: `frontend/app/components/admin/media/GalleryManager.vue`

**Step 1: Import and use the composable**

At the top of script setup:
```typescript
const {
  tourOptions,
  scheduleOptions,
  load: loadModalData
} = useMediaModalData()
```

**Step 2: Load data when edit modal opens**

Update openEditModal function:
```typescript
async function openEditModal(media: MediaRes) {
  selectedMediaForEdit.value = media
  await loadModalData() // Load dropdown data before opening
  editModalOpen.value = true
}
```

**Step 3: Pass props to MediaEditModal**

```vue
<AdminMediaEditModal
  v-model="editModalOpen"
  :media="selectedMediaForEdit"
  :tour-options="tourOptions"
  :schedule-options="scheduleOptions"
  @success="onEditSuccess"
/>
```

**Step 4: Verify TypeScript compiles**

Run: `cd frontend && pnpm typecheck`
Expected: PASS

**Step 5: Manual test**

1. Go to `/admin/media/tour-lagunas-escondidas-de-baltinache-y-vallecito`
2. Click edit (pencil) on any image
3. Modal should open with tour/schedule dropdowns populated
4. No infinite loop or 429 errors

**Step 6: Commit**

```bash
git add frontend/app/components/admin/media/GalleryManager.vue
git commit -m "refactor(media): GalleryManager provides dropdown data to MediaEditModal"
```

---

## Task 4: Update Media Index Page to Provide Data to MediaEditModal

**Files:**
- Modify: `frontend/app/pages/admin/media/index.vue`

**Step 1: Check current usage of MediaEditModal**

Find where `LazyAdminMediaEditModal` is used and update similarly to Task 3.

**Step 2: Import composable**

```typescript
const {
  tourOptions,
  scheduleOptions,
  load: loadModalData
} = useMediaModalData()
```

**Step 3: Update edit modal opening logic**

Find the function that opens the edit modal and add `await loadModalData()` before setting open to true.

**Step 4: Pass props**

```vue
<LazyAdminMediaEditModal
  v-model="editModalOpen"
  :media="selectedMedia"
  :tour-options="tourOptions"
  :schedule-options="scheduleOptions"
  @success="fetchMedia"
/>
```

**Step 5: Verify and test**

Run: `cd frontend && pnpm typecheck`
Test: Go to `/admin/media`, click edit on any image, verify modal works.

**Step 6: Commit**

```bash
git add frontend/app/pages/admin/media/index.vue
git commit -m "refactor(media): media index page provides dropdown data to MediaEditModal"
```

---

## Task 5: Refactor BulkAssignTourModal to Use useControlledModalForm

**Files:**
- Modify: `frontend/app/components/admin/media/BulkAssignTourModal.vue`

**Step 1: Read current implementation**

Review the current manual computed + watch pattern.

**Step 2: Replace with useControlledModalForm**

```typescript
// OLD - remove:
const isOpen = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})
const saving = ref(false)
watch(isOpen, (open) => {
  if (open) selectedTourId.value = undefined
})

// NEW - add:
const selectedTourId = ref<string | undefined>()

const { isOpen, isSubmitting, handleSubmit } = useControlledModalForm({
  modelValue: toRef(props, 'modelValue'),
  onUpdateModelValue: v => emit('update:modelValue', v),
  onSubmit: async () => {
    if (!selectedTourId.value) {
      throw new Error('Selecciona un tour')
    }
    await assignMediaToTour(selectedTourId.value, props.mediaIds)
  },
  onSuccess: () => {
    selectedTourId.value = undefined
    emit('success')
  },
  successMessage: `${props.mediaIds.length} fotos asignadas al tour`,
  errorMessage: 'Error al asignar fotos'
})

// Reset on open
watch(() => props.modelValue, (open) => {
  if (open) selectedTourId.value = undefined
})
```

**Step 3: Update template**

```vue
<AdminBaseAdminModal
  v-model:open="isOpen"
  title="Asignar a Tour"
  :submit-loading="isSubmitting"
  :submit-disabled="!selectedTourId"
  @submit="handleSubmit"
>
  <!-- content -->
</AdminBaseAdminModal>
```

**Step 4: Also update to receive tourOptions as prop**

```typescript
const props = defineProps<{
  modelValue: boolean
  mediaIds: string[]
  tourOptions: Array<{ label: string; value: string | undefined }>
}>()
```

**Step 5: Verify and test**

Run: `cd frontend && pnpm typecheck`
Test: Go to `/admin/media`, select images, click "Asignar a Tour", verify modal works.

**Step 6: Commit**

```bash
git add frontend/app/components/admin/media/BulkAssignTourModal.vue
git commit -m "refactor(media): BulkAssignTourModal uses useControlledModalForm"
```

---

## Task 6: Update Media Index Page to Pass tourOptions to BulkAssignTourModal

**Files:**
- Modify: `frontend/app/pages/admin/media/index.vue`

**Step 1: Pass tourOptions prop**

```vue
<LazyAdminMediaBulkAssignTourModal
  v-model="bulkAssignTourModalOpen"
  :media-ids="selectedItems"
  :tour-options="tourOptions"
  @success="handleBulkSuccess"
/>
```

**Step 2: Verify and test**

Run: `cd frontend && pnpm typecheck`
Test: Select multiple images, bulk assign to tour.

**Step 3: Commit**

```bash
git add frontend/app/pages/admin/media/index.vue
git commit -m "refactor(media): pass tourOptions to BulkAssignTourModal"
```

---

## Task 7: Refactor BulkTagsModal to Use useControlledModalForm

**Files:**
- Modify: `frontend/app/components/admin/media/BulkTagsModal.vue`

**Step 1: Replace manual state with useControlledModalForm**

Similar pattern to Task 5:

```typescript
const tags = ref<string[]>([])
const mode = ref<'add' | 'replace'>('add')

const { isOpen, isSubmitting, handleSubmit } = useControlledModalForm({
  modelValue: toRef(props, 'modelValue'),
  onUpdateModelValue: v => emit('update:modelValue', v),
  onSubmit: async () => {
    if (tags.value.length === 0) {
      throw new Error('Añade al menos una etiqueta')
    }

    let successCount = 0
    let failCount = 0

    for (const mediaId of props.mediaIds) {
      try {
        let finalTags = tags.value

        if (mode.value === 'add') {
          const existingMedia = await fetchAdminMediaById(mediaId)
          const existingTags = existingMedia?.tags || []
          finalTags = [...new Set([...existingTags, ...tags.value])]
        }

        await updateAdminMedia(mediaId, { tags: finalTags })
        successCount++
      } catch {
        failCount++
      }
    }

    if (failCount > 0) {
      throw new Error(`${failCount} de ${props.mediaIds.length} fallaron`)
    }

    return { successCount }
  },
  onSuccess: () => {
    tags.value = []
    mode.value = 'add'
    emit('success')
  },
  successMessage: (result) => `Etiquetas actualizadas en ${result?.successCount} fotos`,
  errorMessage: 'Error al actualizar etiquetas'
})

// Reset on open
watch(() => props.modelValue, (open) => {
  if (open) {
    tags.value = []
    mode.value = 'add'
  }
})
```

**Step 2: Update template**

```vue
<AdminBaseAdminModal
  v-model:open="isOpen"
  title="Gestionar Etiquetas"
  :submit-loading="isSubmitting"
  :submit-disabled="tags.length === 0"
  @submit="handleSubmit"
>
  <!-- content -->
</AdminBaseAdminModal>
```

**Step 3: Verify and test**

Run: `cd frontend && pnpm typecheck`
Test: Select multiple images, add/replace tags.

**Step 4: Commit**

```bash
git add frontend/app/components/admin/media/BulkTagsModal.vue
git commit -m "refactor(media): BulkTagsModal uses useControlledModalForm"
```

---

## Task 8: Refactor UploadModal to Use useControlledModalForm

**Files:**
- Modify: `frontend/app/components/admin/media/UploadModal.vue`

**Step 1: Analyze current state**

UploadModal has complex state for file tracking. We'll keep the file state but use `useControlledModalForm` for the modal open/close and submission.

**Step 2: Replace manual isOpen computed**

```typescript
// Keep file-related state
const uploadingFiles = ref<UploadingFile[]>([])
const metadata = ref({ ... })
const isDragging = ref(false)
const isOptimizing = ref(false)

// Replace manual isOpen with composable
const { isOpen, isSubmitting, handleSubmit } = useControlledModalForm({
  modelValue: toRef(props, 'modelValue'),
  onUpdateModelValue: v => emit('update:modelValue', v),
  onSubmit: async () => {
    // Move existing upload logic here
    await uploadAllFiles()
  },
  onSuccess: () => {
    // Reset state
    uploadingFiles.value = []
    metadata.value = { ... }
    emit('success')
  },
  successMessage: 'Fotos subidas correctamente',
  errorMessage: 'Error al subir fotos'
})
```

**Step 3: Verify and test**

Run: `cd frontend && pnpm typecheck`
Test: Upload images via drag-drop and file picker.

**Step 4: Commit**

```bash
git add frontend/app/components/admin/media/UploadModal.vue
git commit -m "refactor(media): UploadModal uses useControlledModalForm"
```

---

## Task 9: Final Cleanup and Verification

**Files:**
- Review all modified files

**Step 1: Run full typecheck**

Run: `cd frontend && pnpm typecheck`
Expected: No errors

**Step 2: Run linter**

Run: `cd frontend && pnpm lint`
Expected: No errors (or fix any that appear)

**Step 3: Manual integration test**

Test each modal:
1. `/admin/media` - Upload modal
2. `/admin/media` - Edit modal (single image)
3. `/admin/media` - Bulk assign tour
4. `/admin/media` - Bulk tags
5. `/admin/media/[slug]` - Gallery manager edit modal
6. `/admin/media/[slug]` - Gallery manager selector modal

**Step 4: Verify no console errors or infinite loops**

Open browser DevTools, check for:
- No 429 errors
- No "Incompatible options" warnings
- No "onMounted called without active instance" warnings

**Step 5: Final commit**

```bash
git add -A
git commit -m "refactor(media): complete modal standardization - all use useControlledModalForm"
```

---

## Summary of Changes

| Modal | Before | After |
|-------|--------|-------|
| MediaEditModal | Fetched tours/schedules internally | Receives as props |
| BulkAssignTourModal | Manual computed + watch | `useControlledModalForm` + props |
| BulkTagsModal | Manual computed + watch | `useControlledModalForm` |
| UploadModal | Manual isOpen | `useControlledModalForm` |
| SelectorModal | No changes | (search-driven pattern is correct) |

**New Composable:**
- `useMediaModalData()` - Centralized dropdown data loading

**Benefits:**
1. Consistent state management across all modals
2. No more `useAsyncData` key conflicts
3. Data fetching is explicit and controlled by parent
4. Reduced boilerplate code
5. Better separation of concerns
