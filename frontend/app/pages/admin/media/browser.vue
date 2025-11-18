<script setup lang="ts">
import type { TreeItem } from '@nuxt/ui'
import type { MediaHierarchyNode } from '~/composables/useMediaHierarchy'

definePageMeta({
  layout: 'admin',
  middleware: 'auth-admin'
})

const { loading, tours, loadTours, buildTree, buildScheduleNodes } = useMediaHierarchy()

// Tree state
const treeItems = ref<TreeItem[]>([])
const expandedItems = ref<string[]>(['media-root'])

// Selected item state
const selectedNode = ref<MediaHierarchyNode | null>(null)
const selectedTourId = ref<string | null>(null)
const selectedScheduleId = ref<string | null>(null)

// Gallery modal state
const galleryModalOpen = ref(false)

// Load tours on mount
onMounted(async () => {
  await loadTours()
  treeItems.value = buildTree() as TreeItem[]
})

// Handle tree item toggle (expand/collapse)
async function onToggle(e: any, item: MediaHierarchyNode) {
  if (item.type === 'tour' && item.tourId) {
    // Load schedules for this tour when expanded
    const parentItem = treeItems.value[0]
    const tourItem = parentItem?.children?.find((t: any) => t.id === item.tourId)

    if (tourItem && (!tourItem.children || tourItem.children.length === 0)) {
      // Load schedules
      const scheduleNodes = await buildScheduleNodes(item.tourId)
      tourItem.children = scheduleNodes as TreeItem[]
    }
  }
}

// Handle tree item selection
function onSelect(e: any, item: MediaHierarchyNode) {
  selectedNode.value = item

  if (item.type === 'tour') {
    selectedTourId.value = item.tourId || null
    selectedScheduleId.value = null
  } else if (item.type === 'schedule') {
    selectedTourId.value = null
    selectedScheduleId.value = item.scheduleId || null
  } else if (item.type === 'root') {
    selectedTourId.value = null
    selectedScheduleId.value = null
  }
}

// Open gallery manager modal
function openGalleryManager() {
  galleryModalOpen.value = true
}
</script>

<template>
  <div class="flex h-[calc(100vh-200px)] gap-6">
    <!-- Left Sidebar - Tree Navigation -->
    <div class="w-80 shrink-0">
      <UCard class="h-full">
        <template #header>
          <div class="flex items-center justify-between">
            <div>
              <h2 class="text-lg font-semibold text-neutral-900 dark:text-neutral-100">
                Explorador
              </h2>
              <p class="text-sm text-neutral-600 dark:text-neutral-400">
                Navega por tours y fechas
              </p>
            </div>

            <NuxtLink to="/admin/media">
              <UButton
                icon="i-lucide-grid-3x3"
                variant="ghost"
                color="neutral"
                size="sm"
                title="Vista de tabla"
              />
            </NuxtLink>
          </div>
        </template>

        <!-- Loading State -->
        <div v-if="loading && tours.length === 0" class="flex justify-center py-12">
          <UIcon name="i-lucide-loader-2" class="w-6 h-6 animate-spin text-neutral-400" />
        </div>

        <!-- Tree Navigation -->
        <div v-else class="overflow-y-auto max-h-[calc(100vh-320px)]">
          <UTree
            v-model:expanded="expandedItems"
            :items="treeItems"
            :get-key="(item: MediaHierarchyNode) => item.id"
            color="primary"
            size="md"
            @toggle="onToggle"
            @select="onSelect"
          >
            <!-- Custom label with photo count -->
            <template #item-label="{ item }">
              <div class="flex items-center justify-between w-full">
                <span class="truncate">{{ item.label }}</span>
                <UBadge
                  v-if="item.type === 'tour' && item.photoCount !== undefined"
                  size="xs"
                  color="neutral"
                  variant="soft"
                  class="ml-2"
                >
                  {{ item.photoCount }} fotos
                </UBadge>
              </div>
            </template>
          </UTree>
        </div>

        <!-- Empty State -->
        <div
          v-if="!loading && tours.length === 0"
          class="text-center py-12"
        >
          <UIcon name="i-lucide-folder-x" class="w-12 h-12 mx-auto mb-4 text-neutral-400" />
          <p class="text-neutral-600 dark:text-neutral-400 mb-4">
            No hay tours aún
          </p>
          <NuxtLink to="/admin/tours">
            <UButton color="primary" variant="soft">
              Crear Primer Tour
            </UButton>
          </NuxtLink>
        </div>
      </UCard>
    </div>

    <!-- Right Content Area - Photo Grid -->
    <div class="flex-1 overflow-hidden">
      <UCard class="h-full">
        <template #header>
          <div class="flex items-center justify-between">
            <div>
              <h2 class="text-lg font-semibold text-neutral-900 dark:text-neutral-100">
                {{ selectedNode?.label || 'Selecciona un tour o fecha' }}
              </h2>
              <p class="text-sm text-neutral-600 dark:text-neutral-400">
                <template v-if="selectedNode?.type === 'tour'">
                  Galería principal del tour
                </template>
                <template v-else-if="selectedNode?.type === 'schedule'">
                  Fotos específicas de esta fecha
                </template>
                <template v-else>
                  Selecciona un item en el árbol para ver sus fotos
                </template>
              </p>
            </div>

            <UButton
              v-if="selectedTourId || selectedScheduleId"
              icon="i-lucide-pencil"
              color="primary"
              @click="openGalleryManager"
            >
              Gestionar Galería
            </UButton>
          </div>
        </template>

        <!-- Gallery Preview Grid -->
        <div class="overflow-y-auto max-h-[calc(100vh-350px)]">
          <!-- Show gallery for selected tour -->
          <AdminMediaGalleryManager
            v-if="selectedTourId"
            :tour-id="selectedTourId"
            :key="`tour-${selectedTourId}`"
          />

          <!-- Show gallery for selected schedule -->
          <AdminMediaGalleryManager
            v-else-if="selectedScheduleId"
            :schedule-id="selectedScheduleId"
            :key="`schedule-${selectedScheduleId}`"
          />

          <!-- Empty state when nothing selected -->
          <div
            v-else
            class="flex flex-col items-center justify-center py-24 text-center"
          >
            <UIcon name="i-lucide-image" class="w-16 h-16 mb-4 text-neutral-300 dark:text-neutral-700" />
            <p class="text-neutral-600 dark:text-neutral-400 mb-2">
              Navega por el árbol de la izquierda
            </p>
            <p class="text-sm text-neutral-500 dark:text-neutral-500">
              Selecciona un tour o una fecha para ver y gestionar sus fotos
            </p>
          </div>
        </div>
      </UCard>
    </div>
  </div>
</template>
