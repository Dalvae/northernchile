<script setup lang="ts">
import { h, resolveComponent } from 'vue'
import type { TableColumn } from '@nuxt/ui'
import type { MediaRes, PageMediaRes } from 'api-client'
import { formatFileSize, formatDate, getMediaTypeLabel, getMediaTypeBadgeColor } from '~/utils/media'

definePageMeta({
  layout: 'admin',
  middleware: 'auth-admin'
})

const toast = useToast()
const { fetchAdminMedia, deleteAdminMedia } = useAdminData()

// Resolve components for use in h()
const UCheckbox = resolveComponent('UCheckbox')
const UBadge = resolveComponent('UBadge')
const UButton = resolveComponent('UButton')
const NuxtImg = resolveComponent('NuxtImg')

// Modals
const uploadModalOpen = ref(false)
const editModalOpen = ref(false)
const selectedMedia = ref<MediaRes | null>(null)
const lightboxOpen = ref(false)
const lightboxIndex = ref(0)

// Row selection state (TanStack Table format: { rowIndex: true })
const rowSelection = ref<Record<string, boolean>>({})

// Computed: get selected media IDs from rowSelection
const selectedItems = computed(() => {
  return Object.entries(rowSelection.value)
    .filter(([_, selected]) => selected)
    .map(([index]) => media.value[parseInt(index)]?.id)
    .filter((id): id is string => !!id)
})

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

// Watch page changes to trigger refresh and clear selection
watch([page, pageSize], () => {
  rowSelection.value = {} // Clear selection when page changes
  refresh()
})

const media = computed(() => mediaResponse.value?.content || [])
const totalItems = computed(() => mediaResponse.value?.totalElements || 0)
const hasMoreItems = computed(() => allLoadedMedia.value.length < totalItems.value)

// Update allLoadedMedia when media changes
watch(media, (newMedia) => {
  if (newMedia.length > 0) {
    // Calculate the start index for the current page
    const startIndex = (page.value - 1) * pageSize.value

    // Create a new array to ensure reactivity
    const updated = [...allLoadedMedia.value]
    
    // Ensure we have space for all items up to this page
    while (updated.length < startIndex) {
      updated.push(null as unknown as MediaRes)
    }

    // Add/replace items for current page
    newMedia.forEach((item, idx) => {
      updated[startIndex + idx] = item
    })
    
    allLoadedMedia.value = updated
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
      // Append new items to the array
      allLoadedMedia.value = [...allLoadedMedia.value, ...response.content]
    }
  } catch (error) {
    console.error('Error loading more media:', error)
  } finally {
    lightboxLoadingMore.value = false
  }
}

// Table columns with TanStack Table row selection
const columns: TableColumn<MediaRes>[] = [
  {
    id: 'select',
    header: ({ table }) => h(UCheckbox, {
      'modelValue': table.getIsSomePageRowsSelected() ? 'indeterminate' : table.getIsAllPageRowsSelected(),
      'onUpdate:modelValue': (value: boolean | 'indeterminate') => table.toggleAllPageRowsSelected(!!value),
      'ariaLabel': 'Seleccionar todos'
    }),
    cell: ({ row }) => h(UCheckbox, {
      'modelValue': row.getIsSelected(),
      'onUpdate:modelValue': (value: boolean | 'indeterminate') => row.toggleSelected(!!value),
      'ariaLabel': 'Seleccionar fila'
    }),
    enableSorting: false,
    enableHiding: false
  },
  {
    id: 'thumbnail',
    header: '',
    cell: ({ row }) => {
      const mediaItem = row.original
      return h(NuxtImg, {
        src: mediaItem.variants?.thumbnail || mediaItem.url,
        alt: mediaItem.altTranslations?.es || mediaItem.originalFilename,
        class: 'w-16 h-16 object-cover rounded-lg shadow-sm cursor-pointer hover:opacity-80 transition-opacity',
        onClick: () => openLightbox(mediaItem),
        format: 'webp',
        loading: 'lazy',
        placeholder: true
      })
    }
  },
  {
    id: 'description',
    header: 'Descripción',
    cell: ({ row }) => {
      const mediaItem = row.original
      const altText = mediaItem.altTranslations?.es || mediaItem.originalFilename
      const caption = mediaItem.captionTranslations?.es
      return h('div', [
        h('p', { class: 'font-medium text-highlighted truncate max-w-xs' }, altText),
        caption ? h('p', { class: 'text-sm text-muted truncate max-w-xs' }, caption) : null
      ].filter(Boolean))
    }
  },
  {
    id: 'type',
    accessorKey: 'type',
    header: 'Tipo',
    cell: ({ row }) => {
      const type = row.original.type
      return h(UBadge, {
        color: getMediaTypeBadgeColor(type),
        variant: 'soft',
        size: 'xs'
      }, () => getMediaTypeLabel(type))
    }
  },
  {
    id: 'tags',
    accessorKey: 'tags',
    header: 'Etiquetas',
    cell: ({ row }) => {
      const tags = row.original.tags || []
      if (tags.length === 0) return h('span', { class: 'text-muted' }, '-')
      return h('div', { class: 'flex flex-wrap gap-1 max-w-xs' }, [
        ...tags.slice(0, 3).map(tag =>
          h(UBadge, { size: 'xs', color: 'neutral', variant: 'soft', key: tag }, () => tag)
        ),
        tags.length > 3
          ? h(UBadge, { size: 'xs', color: 'neutral', variant: 'soft' }, () => `+${tags.length - 3}`)
          : null
      ].filter(Boolean))
    }
  },
  {
    id: 'takenAt',
    accessorKey: 'takenAt',
    header: 'Fecha',
    cell: ({ row }) => {
      const mediaItem = row.original
      const date = mediaItem.takenAt || mediaItem.uploadedAt
      return h('div', { class: 'text-sm text-muted' }, formatDate(date))
    }
  },
  {
    id: 'size',
    accessorKey: 'sizeBytes',
    header: 'Tamaño',
    cell: ({ row }) => {
      return h('div', { class: 'text-sm text-muted' }, formatFileSize(row.original.sizeBytes))
    }
  },
  {
    id: 'actions',
    header: '',
    cell: ({ row }) => {
      return h('div', { class: 'flex justify-end' }, [
        h(UButton, {
          icon: 'i-heroicons-pencil',
          size: 'sm',
          color: 'primary',
          variant: 'ghost',
          onClick: () => openEditModal(row.original)
        }),
        h(UButton, {
          icon: 'i-heroicons-arrow-down-tray',
          size: 'sm',
          color: 'neutral',
          variant: 'ghost',
          onClick: () => window.open(row.original.url, '_blank')
        }),
        h(UButton, {
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

// Bulk delete
async function bulkDelete() {
  const idsToDelete = selectedItems.value
  if (!confirm(`¿Eliminar ${idsToDelete.length} ${idsToDelete.length === 1 ? 'medio' : 'medios'}? Esta acción no se puede deshacer.`)) return

  let deleted = 0

  for (const id of idsToDelete) {
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

  rowSelection.value = {}
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
      <span class="text-sm font-medium text-highlighted">
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
          @click="rowSelection = {}"
        >
          Cancelar
        </UButton>
      </div>
    </div>

    <!-- Table -->
    <UCard>
      <UTable
        v-model:row-selection="rowSelection"
        :data="media"
        :columns="columns"
        :loading="loading"
      />

      <!-- Pagination -->
      <template #footer>
        <div class="flex items-center justify-between">
          <div class="text-sm text-muted">
            Mostrando {{ (page - 1) * pageSize + 1 }} - {{ Math.min(page * pageSize, totalItems) }} de {{ totalItems }}
          </div>

          <UPagination
            v-model:page="page"
            :items-per-page="pageSize"
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
