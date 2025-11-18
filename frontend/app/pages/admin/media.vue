<script setup lang="ts">
import type { MediaRes, PageMediaRes } from 'api-client'

definePageMeta({
  layout: 'admin',
  middleware: 'auth-admin'
})

const toast = useToast()
const { fetchAdminMedia, deleteAdminMedia } = useAdminData()

// Modals
const uploadModalOpen = ref(false)
const editModalOpen = ref(false)
const selectedMedia = ref<MediaRes | null>(null)

// Bulk selection
const selectedItems = ref<string[]>([])

// Filters
const filters = ref({
  search: ''
})

const page = ref(0)
const pageSize = ref(20)

// Fetch media with useAsyncData
const {
  data: mediaResponse,
  pending: loading,
  refresh
} = useAsyncData<PageMediaRes>(
  'admin-media',
  () => fetchAdminMedia({
    page: page.value.toString(),
    size: pageSize.value.toString(),
    search: filters.value.search || undefined
  }),
  {
    server: false,
    lazy: true,
    default: () => ({
      content: [],
      totalElements: 0,
      totalPages: 0,
      pageable: { pageNumber: 0, pageSize: 20 },
      last: true,
      numberOfElements: 0,
      size: 20,
      number: 0,
      first: true,
      empty: true
    })
  }
)

const media = computed(() => mediaResponse.value?.content || [])
const totalItems = computed(() => mediaResponse.value?.totalElements || 0)

// Table columns
const columns = [
  { id: 'select', key: 'select', label: '' },
  { id: 'thumbnail', key: 'thumbnail', label: '' },
  { id: 'filename', accessorKey: 'originalFilename', header: 'Archivo' },
  { id: 'type', accessorKey: 'type', header: 'Tipo' },
  { id: 'tags', accessorKey: 'tags', header: 'Etiquetas' },
  { id: 'takenAt', accessorKey: 'takenAt', header: 'Fecha' },
  { id: 'size', accessorKey: 'sizeBytes', header: 'Tamaño' },
  { id: 'actions', key: 'actions', label: '' }
]

// Fetch media function
async function fetchMedia() {
  await refresh()
}

// Delete media
async function deleteMediaItem(id: string) {
  if (!confirm('¿Estás seguro de eliminar este medio? Esta acción no se puede deshacer.')) return

  try {
    await deleteAdminMedia(id)
    toast.add({ color: 'success', title: 'Medio eliminado' })
    await fetchMedia()
  } catch (error) {
    console.error('Error deleting media:', error)
    toast.add({ color: 'error', title: 'Error al eliminar medio' })
  }
}

// Actions per row
function getRowActions(row: MediaRes) {
  return [
    [{
      label: 'Editar',
      icon: 'i-heroicons-pencil',
      click: () => openEditModal(row)
    }],
    [{
      label: 'Descargar',
      icon: 'i-heroicons-arrow-down-tray',
      click: () => window.open(row.url, '_blank')
    }],
    [{
      label: 'Eliminar',
      icon: 'i-heroicons-trash',
      click: () => deleteMediaItem(row.id!)
    }]
  ]
}

function openEditModal(mediaItem: MediaRes) {
  selectedMedia.value = mediaItem
  editModalOpen.value = true
}

// Bulk selection handlers
function toggleSelectAll() {
  if (selectedItems.value.length === media.value.length) {
    selectedItems.value = []
  } else {
    selectedItems.value = media.value.map(m => m.id!)
  }
}

function toggleSelect(id: string) {
  const index = selectedItems.value.indexOf(id)
  if (index > -1) {
    selectedItems.value.splice(index, 1)
  } else {
    selectedItems.value.push(id)
  }
}

// Bulk delete
async function bulkDelete() {
  if (!confirm(`¿Eliminar ${selectedItems.value.length} ${selectedItems.value.length === 1 ? 'medio' : 'medios'}? Esta acción no se puede deshacer.`)) return

  let deleted = 0

  for (const id of selectedItems.value) {
    try {
      await deleteAdminMedia(id)
      deleted++
    } catch (error) {
      console.error(`Error deleting media ${id}:`, error)
    }
  }

  toast.add({
    color: 'success',
    title: `${deleted} ${deleted === 1 ? 'medio eliminado' : 'medios eliminados'}`
  })

  selectedItems.value = []
  await fetchMedia()
}

function formatFileSize(bytes?: number) {
  if (!bytes) return '-'
  const kb = bytes / 1024
  if (kb < 1024) return `${kb.toFixed(1)} KB`
  const mb = kb / 1024
  return `${mb.toFixed(1)} MB`
}

function formatDate(dateString?: string) {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('es-CL')
}

function getTypeLabel(type?: string) {
  const labels: Record<string, string> = {
    TOUR: 'Tour',
    SCHEDULE: 'Programa',
    LOOSE: 'Suelto'
  }
  return labels[type || ''] || type || '-'
}

function getTypeBadgeColor(type?: string) {
  const colors: Record<string, string> = {
    TOUR: 'primary',
    SCHEDULE: 'secondary',
    LOOSE: 'neutral'
  }
  return colors[type || ''] || 'neutral'
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-3xl font-bold text-neutral-900 dark:text-neutral-100">
          Biblioteca de Medios
        </h1>
        <p class="text-neutral-600 dark:text-neutral-400 mt-1">
          Gestiona las fotos de tus tours y programas
        </p>
      </div>

      <UButton
        icon="i-heroicons-cloud-arrow-up"
        size="lg"
        color="primary"
        @click="uploadModalOpen = true"
      >
        Subir Fotos
      </UButton>
    </div>

    <!-- Filters -->
    <UCard>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- Search -->
        <UInput
          v-model="filters.search"
          icon="i-heroicons-magnifying-glass"
          placeholder="Buscar por nombre de archivo o etiquetas..."
          size="lg"
          @update:model-value="fetchMedia"
        />
      </div>
    </UCard>

    <!-- Bulk actions bar -->
    <div
      v-if="selectedItems.length > 0"
      class="flex items-center justify-between p-4 bg-primary-50 dark:bg-primary-900/20 border border-primary-200 dark:border-primary-800 rounded-lg"
    >
      <span class="text-sm font-medium text-neutral-900 dark:text-neutral-100">
        {{ selectedItems.length }} {{ selectedItems.length === 1 ? 'medio seleccionado' : 'medios seleccionados' }}
      </span>

      <div class="flex gap-2">
        <UButton
          icon="i-heroicons-trash"
          color="error"
          variant="soft"
          @click="bulkDelete"
        >
          Eliminar
        </UButton>
        <UButton
          variant="outline"
          color="neutral"
          @click="selectedItems = []"
        >
          Cancelar
        </UButton>
      </div>
    </div>

    <!-- Table -->
    <UCard>
      <UTable
        :rows="media"
        :columns="columns"
        :loading="loading"
        :empty-state="{
          icon: 'i-heroicons-photo',
          label: 'No hay medios aún. ¡Sube tu primera foto!'
        }"
      >
        <!-- Select checkbox header -->
        <template #select-header>
          <UCheckbox
            :model-value="selectedItems.length === media.length && media.length > 0"
            :indeterminate="selectedItems.length > 0 && selectedItems.length < media.length"
            @update:model-value="toggleSelectAll"
          />
        </template>

        <!-- Select checkbox data -->
        <template #select-data="{ row }">
          <UCheckbox
            :model-value="selectedItems.includes(row.id)"
            @update:model-value="toggleSelect(row.id)"
          />
        </template>

        <!-- Thumbnail -->
        <template #thumbnail-data="{ row }">
          <img
            :src="row.variants?.thumbnail || row.url"
            :alt="row.altTranslations?.es || row.originalFilename"
            class="w-16 h-16 object-cover rounded-lg shadow-sm cursor-pointer hover:opacity-80 transition-opacity"
            @click="selectedMedia = row"
          />
        </template>

        <!-- Filename -->
        <template #filename-data="{ row }">
          <div>
            <p class="font-medium text-neutral-900 dark:text-neutral-100 truncate max-w-xs">
              {{ row.originalFilename }}
            </p>
            <p v-if="row.captionTranslations?.es" class="text-sm text-neutral-600 dark:text-neutral-400 truncate max-w-xs">
              {{ row.captionTranslations.es }}
            </p>
          </div>
        </template>

        <!-- Type badge -->
        <template #type-data="{ row }">
          <UBadge
            :color="getTypeBadgeColor(row.type)"
            variant="soft"
          >
            {{ getTypeLabel(row.type) }}
          </UBadge>
        </template>

        <!-- Tags -->
        <template #tags-data="{ row }">
          <div class="flex flex-wrap gap-1 max-w-xs">
            <UBadge
              v-for="tag in row.tags?.slice(0, 3)"
              :key="tag"
              size="xs"
              color="neutral"
              variant="soft"
            >
              {{ tag }}
            </UBadge>
            <UBadge
              v-if="row.tags && row.tags.length > 3"
              size="xs"
              color="neutral"
              variant="soft"
            >
              +{{ row.tags.length - 3 }}
            </UBadge>
          </div>
        </template>

        <!-- Date -->
        <template #takenAt-data="{ row }">
          <div class="text-sm text-neutral-600 dark:text-neutral-400">
            {{ row.takenAt ? formatDate(row.takenAt) : formatDate(row.uploadedAt) }}
          </div>
        </template>

        <!-- Size -->
        <template #size-data="{ row }">
          <div class="text-sm text-neutral-600 dark:text-neutral-400">
            {{ formatFileSize(row.sizeBytes) }}
          </div>
        </template>

        <!-- Actions -->
        <template #actions-data="{ row }">
          <UDropdownMenu :items="getRowActions(row)">
            <UButton
              icon="i-heroicons-ellipsis-vertical"
              variant="ghost"
              color="neutral"
            />
          </UDropdownMenu>
        </template>
      </UTable>

      <!-- Pagination -->
      <template #footer>
        <div class="flex items-center justify-between">
          <div class="text-sm text-neutral-600 dark:text-neutral-400">
            Mostrando {{ (page - 1) * pageSize + 1 }} - {{ Math.min(page * pageSize, totalItems) }} de {{ totalItems }}
          </div>

          <UPagination
            v-model="page"
            :page-count="pageSize"
            :total="totalItems"
            @update:model-value="fetchMedia"
          />
        </div>
      </template>
    </UCard>

    <!-- Modals -->
    <AdminMediaUploadModal
      v-model="uploadModalOpen"
      @success="fetchMedia"
    />

    <AdminMediaEditModal
      v-model="editModalOpen"
      :media="selectedMedia"
      @success="fetchMedia"
    />
  </div>
</template>
