<script setup lang="ts">
import type { MediaRes } from 'api-client'
import { formatFileSize } from '~/utils/media'

const props = defineProps<{
  modelValue: boolean
  excludeMediaIds?: string[]
}>()

const emit = defineEmits(['update:modelValue', 'selected'])

const { showErrorToast } = useApiError()
const { fetchAdminMedia } = useAdminData()

const isOpen = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

// State
const media = ref<MediaRes[]>([])
const loading = ref(false)
const selectedItems = ref<string[]>([])
const search = ref('')
const page = ref(1)
const pageSize = ref(12)
const totalItems = ref(0)

// Computed: Available media (filtered)
const availableMedia = computed(() => {
  if (!props.excludeMediaIds || props.excludeMediaIds.length === 0) {
    return media.value
  }
  return media.value.filter(m => !props.excludeMediaIds!.includes(m.id!))
})

// Fetch media
async function fetchMedia() {
  loading.value = true
  try {
    const response = await fetchAdminMedia({
      page: page.value - 1,
      size: pageSize.value,
      search: search.value || undefined
    })

    media.value = response.content || []
    totalItems.value = response.totalElements || 0
  } catch (error) {
    console.error('Error fetching media:', error)
    showErrorToast(error, 'Error al cargar medios')
  } finally {
    loading.value = false
  }
}

// Toggle selection
function toggleSelect(id: string) {
  const index = selectedItems.value.indexOf(id)
  if (index > -1) {
    selectedItems.value.splice(index, 1)
  } else {
    selectedItems.value.push(id)
  }
}

// Handle add selected
function addSelected() {
  if (selectedItems.value.length === 0) {
    showErrorToast({ message: 'Selecciona al menos una foto' })
    return
  }

  emit('selected', selectedItems.value)
  selectedItems.value = []
  isOpen.value = false
}

// Reset on open
watch(isOpen, (open) => {
  if (open) {
    selectedItems.value = []
    fetchMedia()
  }
})
</script>

<template>
  <AdminBaseAdminModal
    v-model:open="isOpen"
    title="Seleccionar Fotos"
    subtitle="Elige las fotos que quieres añadir a la galería"
    size="2xl"
  >
    <div class="space-y-4">
      <!-- Search -->
      <UInput
        v-model="search"
        icon="i-heroicons-magnifying-glass"
        placeholder="Buscar fotos..."
        size="lg"
        class="w-full"
        @update:model-value="fetchMedia"
      />

      <!-- Selected count -->
      <div
        v-if="selectedItems.length > 0"
        class="text-sm text-muted"
      >
        {{ selectedItems.length }} {{ selectedItems.length === 1 ? 'foto seleccionada' : 'fotos seleccionadas' }}
      </div>

      <!-- Loading -->
      <CommonLoadingState
        v-if="loading"
        size="lg"
        text="Cargando fotos..."
      />

      <!-- Empty -->
      <CommonEmptyState
        v-else-if="availableMedia.length === 0"
        icon="i-heroicons-photo"
        title="No hay fotos disponibles"
        description="No hay fotos disponibles en la biblioteca"
      />

      <!-- Grid -->
      <div
        v-else
        class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 max-h-96 overflow-y-auto"
      >
        <div
          v-for="item in availableMedia"
          :key="item.id"
          class="relative cursor-pointer group"
          @click="item.id && toggleSelect(item.id)"
        >
          <!-- Image -->
          <NuxtImg
            :src="item.url"
            :alt="item.altTranslations?.es || item.originalFilename"
            class="w-full h-32 object-cover rounded-lg shadow-sm transition-all"
            :class="item.id && selectedItems.includes(item.id) ? 'ring-4 ring-primary-500' : 'group-hover:opacity-80'"
            format="webp"
            loading="lazy"
            placeholder
          />

          <!-- Selection indicator -->
          <div
            v-if="item.id && selectedItems.includes(item.id)"
            class="absolute top-2 right-2 bg-primary-500 text-white rounded-full p-1"
          >
            <UIcon
              name="i-heroicons-check"
              class="w-4 h-4"
            />
          </div>

          <!-- Info overlay -->
          <div class="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/70 to-transparent p-2 rounded-b-lg">
            <p class="text-white text-xs truncate">
              {{ item.originalFilename }}
            </p>
            <p class="text-white/70 text-xs">
              {{ formatFileSize(item.sizeBytes) }}
            </p>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div
        v-if="totalItems > pageSize"
        class="flex justify-center"
      >
        <UPagination
          v-model="page"
          :page-count="pageSize"
          :total="totalItems"
          @update:model-value="fetchMedia"
        />
      </div>
    </div>

    <template #footer>
      <UButton
        variant="outline"
        color="neutral"
        @click="isOpen = false"
      >
        Cancelar
      </UButton>

      <UButton
        color="primary"
        :disabled="selectedItems.length === 0"
        @click="addSelected"
      >
        Añadir {{ selectedItems.length > 0 ? `(${selectedItems.length})` : '' }}
      </UButton>
    </template>
  </AdminBaseAdminModal>
</template>
