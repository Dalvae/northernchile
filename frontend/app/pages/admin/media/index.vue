<script setup lang="ts">
import { h, resolveComponent } from 'vue'
import type { MediaRes, PageMediaRes } from 'api-client'
import { formatFileSize, formatDate, getMediaTypeLabel, getMediaTypeBadgeColor } from '~/utils/media'

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
const lightboxOpen = ref(false)
const lightboxIndex = ref(0)

// Bulk selection
const selectedItems = ref<string[]>([])

// Filters
const filters = ref({
  search: ''
})

const page = ref(1)
const pageSize = ref(20)

// Lightbox state - accumulates all loaded media for seamless navigation
const allLoadedMedia = ref<MediaRes[]>([])
const lightboxLoadingMore = ref(false)

// Fetch media with useAsyncData
const {
  data: mediaResponse,
  pending: loading,
  refresh
} = useAsyncData<PageMediaRes>(
  'admin-media',
  () => fetchAdminMedia({
    page: (page.value - 1).toString(),
    size: pageSize.value.toString(),
    search: filters.value.search || undefined
  }),
  {
    server: false,
    lazy: true,
    watch: [page, pageSize], // Re-fetch when page or pageSize changes
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
const hasMoreItems = computed(() => allLoadedMedia.value.length < totalItems.value)

// Update allLoadedMedia when media changes
watch(media, (newMedia) => {
  if (newMedia.length > 0) {
    // Calculate the start index for the current page
    const startIndex = (page.value - 1) * pageSize.value

    // Ensure we have space for all items up to this page
    if (allLoadedMedia.value.length < startIndex) {
      // Fill gaps with placeholders if needed (shouldn't happen with normal navigation)
      allLoadedMedia.value.length = startIndex
    }

    // Add/replace items for current page
    newMedia.forEach((item, idx) => {
      allLoadedMedia.value[startIndex + idx] = item
    })
  }
}, { immediate: true })

// Reset allLoadedMedia when filters change
watch(() => filters.value.search, () => {
  allLoadedMedia.value = []
  page.value = 1
})

// Load more media for lightbox
async function loadMoreForLightbox() {
  if (lightboxLoadingMore.value || !hasMoreItems.value) return

  const nextPage = Math.floor(allLoadedMedia.value.length / pageSize.value)

  lightboxLoadingMore.value = true
  try {
    const response = await fetchAdminMedia({
      page: nextPage.toString(),
      size: pageSize.value.toString(),
      search: filters.value.search || undefined
    })

    if (response?.content) {
      const startIndex = nextPage * pageSize.value
      response.content.forEach((item, idx) => {
        allLoadedMedia.value[startIndex + idx] = item
      })
    }
  } catch (error) {
    console.error('Error loading more media:', error)
  } finally {
    lightboxLoadingMore.value = false
  }
}

// Table columns
const columns = [
  { id: 'select', header: '' },
  {
    id: 'thumbnail',
    header: '',
    cell: ({ row }: { row: { original: MediaRes } }) => {
      const media = row.original
      return h(resolveComponent('NuxtImg'), {
        src: media.variants?.thumbnail || media.url,
        alt: media.altTranslations?.es || media.originalFilename,
        class: 'w-16 h-16 object-cover rounded-lg shadow-sm cursor-pointer hover:opacity-80 transition-opacity',
        onClick: () => openLightbox(media),
        format: 'webp',
        loading: 'lazy',
        placeholder: true
      })
    }
  },
  {
    id: 'description',
    header: 'Descripción',
    cell: ({ row }: { row: { original: MediaRes } }) => {
      const media = row.original
      const altText = media.altTranslations?.es || media.originalFilename
      const caption = media.captionTranslations?.es
      return h('div', [
        h('p', { class: 'font-medium text-neutral-900 dark:text-neutral-100 truncate max-w-xs' }, altText),
        caption ? h('p', { class: 'text-sm text-neutral-600 dark:text-neutral-300 truncate max-w-xs' }, caption) : null
      ].filter(Boolean))
    }
  },
  {
    id: 'type',
    accessorKey: 'type' as const,
    header: 'Tipo',
    cell: ({ row }: { row: { original: MediaRes } }) => {
      const type = row.original.type
      return h(resolveComponent('UBadge'), {
        color: getMediaTypeBadgeColor(type),
        variant: 'soft',
        size: 'xs'
      }, () => getMediaTypeLabel(type))
    }
  },
  { id: 'tags', accessorKey: 'tags' as const, header: 'Etiquetas' },
  {
    id: 'takenAt',
    accessorKey: 'takenAt' as const,
    header: 'Fecha',
    cell: ({ row }: { row: { original: MediaRes } }) => {
      const media = row.original
      const date = media.takenAt || media.uploadedAt
      return h('div', { class: 'text-sm text-neutral-600 dark:text-neutral-300' }, formatDate(date))
    }
  },
  {
    id: 'size',
    accessorKey: 'sizeBytes' as const,
    header: 'Tamaño',
    cell: ({ row }: { row: { original: MediaRes } }) => {
      return h('div', { class: 'text-sm text-neutral-600 dark:text-neutral-300' }, formatFileSize(row.original.sizeBytes))
    }
  },
  {
    id: 'actions',
    header: '',
    cell: ({ row }: { row: { original: MediaRes } }) => {
      return h('div', { class: 'flex justify-end' }, [
        h(resolveComponent('UButton'), {
          icon: 'i-heroicons-pencil',
          size: 'sm',
          color: 'primary',
          variant: 'ghost',
          onClick: () => openEditModal(row.original)
        }),
        h(resolveComponent('UButton'), {
          icon: 'i-heroicons-arrow-down-tray',
          size: 'sm',
          color: 'neutral',
          variant: 'ghost',
          onClick: () => window.open(row.original.url, '_blank')
        }),
        h(resolveComponent('UButton'), {
          icon: 'i-heroicons-trash',
          size: 'sm',
          color: 'error',
          variant: 'ghost',
          onClick: () => deleteMediaItem(row.original.id!)
        })
      ])
    }
  }
]

// Fetch media function - also resets accumulated media
async function fetchMedia() {
  allLoadedMedia.value = []
  await refresh()
}

// Delete media
async function deleteMediaItem(id: string) {
  if (!confirm('¿Estás seguro de eliminar este medio? Esta acción no se puede deshacer.')) return

  try {
    await deleteAdminMedia(id)
    toast.add({ color: 'success', title: 'Medio eliminado' })
    allLoadedMedia.value = [] // Reset accumulated media
    await refresh()
  } catch (error) {
    console.error('Error deleting media:', error)
    toast.add({ color: 'error', title: 'Error al eliminar medio' })
  }
}

// Actions per row (available for future context menu usage)
function _getRowActions(row: MediaRes) {
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

function openLightbox(mediaItem: MediaRes) {
  // Find index in current page
  const pageIndex = media.value.findIndex(m => m.id === mediaItem.id)
  if (pageIndex !== -1) {
    // Calculate global index: (page - 1) * pageSize + pageIndex
    const globalIndex = (page.value - 1) * pageSize.value + pageIndex
    lightboxIndex.value = globalIndex
    lightboxOpen.value = true
  }
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
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-3xl font-bold text-neutral-900 dark:text-neutral-100">
          Biblioteca de Medios
        </h1>
        <p class="text-neutral-600 dark:text-neutral-300 mt-1">
          Gestiona las fotos de tus tours y programas
        </p>
      </div>

      <div class="flex gap-2">
        <NuxtLink to="/admin/media/browser">
          <UButton
            icon="i-lucide-folder-tree"
            size="lg"
            color="neutral"
            variant="outline"
            title="Vista de explorador"
          >
            Explorador
          </UButton>
        </NuxtLink>

        <UButton
          icon="i-heroicons-cloud-arrow-up"
          size="lg"
          color="primary"
          @click="uploadModalOpen = true"
        >
          Subir Fotos
        </UButton>
      </div>
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
        :data="media"
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
            :model-value="row.original.id ? selectedItems.includes(row.original.id) : false"
            @update:model-value="row.original.id && toggleSelect(row.original.id)"
          />
        </template>

        <!-- Tags -->
        <template #tags-data="{ row }">
          <div class="flex flex-wrap gap-1 max-w-xs">
            <UBadge
              v-for="tag in row.original.tags?.slice(0, 3)"
              :key="tag"
              size="xs"
              color="neutral"
              variant="soft"
            >
              {{ tag }}
            </UBadge>
            <UBadge
              v-if="row.original.tags && row.original.tags.length > 3"
              size="xs"
              color="neutral"
              variant="soft"
            >
              +{{ row.original.tags.length - 3 }}
            </UBadge>
          </div>
        </template>
      </UTable>

      <!-- Pagination -->
      <template #footer>
        <div class="flex items-center justify-between">
          <div class="text-sm text-neutral-600 dark:text-neutral-300">
            Mostrando {{ (page - 1) * pageSize + 1 }} - {{ Math.min(page * pageSize, totalItems) }} de {{ totalItems }}
          </div>

          <UPagination
            v-model="page"
            :page-count="pageSize"
            :total="totalItems"
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

    <AdminMediaLightbox
      v-model="lightboxOpen"
      :media="allLoadedMedia"
      :initial-index="lightboxIndex"
      :total-items="totalItems"
      :has-more="hasMoreItems"
      :loading-more="lightboxLoadingMore"
      @load-more="loadMoreForLightbox"
    />
  </div>
</template>
