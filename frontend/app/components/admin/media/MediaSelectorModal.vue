<script setup lang="ts">
import { formatFileSize } from '~/utils/media'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits(['update:modelValue', 'selected'])

const toast = useToast()
const { fetchAdminMedia } = useAdminData()

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// State
const media = ref([])
const loading = ref(false)
const selectedItems = ref<string[]>([])
const search = ref('')
const page = ref(1)
const pageSize = ref(12)
const totalItems = ref(0)

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
    toast.add({ color: 'error', title: 'Error al cargar medios' })
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
    toast.add({ color: 'warning', title: 'Selecciona al menos una foto' })
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
  <UModal v-model:open="isOpen" :ui="{ width: 'sm:max-w-5xl' }">
    <template #content>
      <!-- Header -->
      <div class="flex items-center justify-between pb-4 border-b border-neutral-200 dark:border-neutral-800">
        <div>
          <h3 class="text-lg font-semibold text-neutral-900 dark:text-neutral-100">
            Seleccionar Fotos
          </h3>
          <p class="text-sm text-neutral-600 dark:text-neutral-400">
            Elige las fotos que quieres añadir a la galería
          </p>
        </div>
        <UButton
          icon="i-heroicons-x-mark"
          variant="ghost"
          color="neutral"
          @click="isOpen = false"
        />
      </div>

      <!-- Content -->
      <div class="py-6 space-y-4">
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
        <div v-if="selectedItems.length > 0" class="text-sm text-neutral-600 dark:text-neutral-400">
          {{ selectedItems.length }} {{ selectedItems.length === 1 ? 'foto seleccionada' : 'fotos seleccionadas' }}
        </div>

        <!-- Loading -->
        <div v-if="loading" class="flex justify-center py-12">
          <UIcon name="i-heroicons-arrow-path" class="w-8 h-8 animate-spin text-neutral-400" />
        </div>

        <!-- Empty -->
        <div
          v-else-if="media.length === 0"
          class="text-center py-12"
        >
          <UIcon name="i-heroicons-photo" class="w-12 h-12 mx-auto mb-4 text-neutral-400" />
          <p class="text-neutral-600 dark:text-neutral-400">
            No hay fotos en la biblioteca
          </p>
        </div>

        <!-- Grid -->
        <div v-else class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 max-h-96 overflow-y-auto">
          <div
            v-for="item in media"
            :key="item.id"
            class="relative cursor-pointer group"
            @click="toggleSelect(item.id)"
          >
            <!-- Image -->
            <img
              :src="item.url"
              :alt="item.altTranslations?.es || item.originalFilename"
              class="w-full h-32 object-cover rounded-lg shadow-sm transition-all"
              :class="selectedItems.includes(item.id) ? 'ring-4 ring-primary-500' : 'group-hover:opacity-80'"
            />

            <!-- Selection indicator -->
            <div
              v-if="selectedItems.includes(item.id)"
              class="absolute top-2 right-2 bg-primary-500 text-white rounded-full p-1"
            >
              <UIcon name="i-heroicons-check" class="w-4 h-4" />
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
        <div v-if="totalItems > pageSize" class="flex justify-center">
          <UPagination
            v-model="page"
            :page-count="pageSize"
            :total="totalItems"
            @update:model-value="fetchMedia"
          />
        </div>
      </div>

      <!-- Footer -->
      <div class="flex justify-end gap-3 pt-4 border-t border-neutral-200 dark:border-neutral-800">
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
      </div>
    </template>
  </UModal>
</template>
