<script setup lang="ts">
import type { TourRes } from 'api-client'
import type { MediaHierarchyNode } from '~/composables/useMediaHierarchy'

definePageMeta({
  layout: 'admin',
  middleware: 'auth-admin'
})

const route = useRoute()
const router = useRouter()
const slug = computed(() => route.params.slug as string)

const { fetchAdminTours } = useAdminData()
const { buildScheduleNodes } = useMediaHierarchy()

// Schedules tree for this tour
const schedules = ref<MediaHierarchyNode[]>([])
const _expandedSchedules = ref<string[]>([])

// Selected schedule (from query param)
const selectedScheduleId = computed(() => route.query.schedule as string | undefined)

// Load tour by slug with useAsyncData
const {
  data: tour,
  pending: loading,
  refresh: _refresh
} = useAsyncData(
  `media-tour-${slug.value}`,
  async () => {
    const tours = await fetchAdminTours()
    const foundTour = tours.find((t: TourRes) => t.slug === slug.value)

    if (!foundTour) {
      // Redirect to media list if tour not found
      await router.push('/admin/media')
      return null
    }

    // Load schedules for this tour (optional - don't fail if empty)
    if (foundTour.id) {
      try {
        schedules.value = await buildScheduleNodes(foundTour.id)
      } catch {
        schedules.value = [] // Empty schedules is OK
      }
    }

    return foundTour
  },
  {
    server: false,
    watch: [slug]
  }
)

// Handle schedule selection
function onScheduleSelect(schedule: MediaHierarchyNode) {
  if (schedule.scheduleId) {
    router.push({
      query: { schedule: schedule.scheduleId }
    })
  }
}

// Clear schedule selection
function clearScheduleSelection() {
  router.push({ query: {} })
}

const selectedScheduleLabel = computed(() => {
  if (!selectedScheduleId.value) return 'Fecha'
  // Cast to simpler type to avoid TS2589 (TreeItem recursive type depth issue)
  const list = schedules.value as Array<{ scheduleId?: string, label: string }>
  return list.find(s => s.scheduleId === selectedScheduleId.value)?.label || 'Fecha'
})
</script>

<template>
  <div class="space-y-6">
    <!-- Debug Panel (temporary) -->
    <UCard v-if="!loading">
      <div class="text-xs font-mono space-y-1">
        <p><strong>Slug from URL:</strong> {{ slug }}</p>
        <p><strong>Tour found:</strong> {{ tour ? 'YES' : 'NO' }}</p>
        <p v-if="tour">
          <strong>Tour name:</strong> {{ tour.nameTranslations?.es }}
        </p>
        <p v-if="tour">
          <strong>Tour ID:</strong> {{ tour.id }}
        </p>
        <p v-if="tour">
          <strong>Images count:</strong> {{ tour.images?.length || 0 }}
        </p>
        <p><strong>Schedules loaded:</strong> {{ schedules.length }}</p>
      </div>
    </UCard>

    <!-- Loading State -->
    <div
      v-if="loading"
      class="flex items-center justify-center py-12"
    >
      <UIcon
        name="i-lucide-loader-2"
        class="w-8 h-8 animate-spin text-primary-500"
      />
    </div>

    <!-- Tour Media View -->
    <div
      v-else-if="tour"
      class="flex gap-6"
    >
      <!-- Left Sidebar - Schedules Tree -->
      <div class="w-80 shrink-0">
        <UCard class="sticky top-4">
          <template #header>
            <div class="flex items-center justify-between">
              <div>
                <NuxtLink
                  to="/admin/media"
                  class="text-sm text-neutral-600 dark:text-neutral-300 hover:text-primary-600 dark:hover:text-primary-400"
                >
                  ← Volver a Media
                </NuxtLink>
                <h2 class="text-lg font-semibold text-neutral-900 dark:text-neutral-100 mt-2">
                  {{ tour.nameTranslations?.es || 'Tour' }}
                </h2>
                <p class="text-sm text-neutral-600 dark:text-neutral-300">
                  Galería principal y fechas
                </p>
              </div>
            </div>
          </template>

          <!-- Tour Gallery (Always visible) -->
          <div class="mb-6">
            <button
              class="w-full flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-neutral-100 dark:hover:bg-neutral-800 transition-colors"
              :class="!selectedScheduleId ? 'bg-primary-100 dark:bg-primary-900/30 text-primary-700 dark:text-primary-400' : 'text-neutral-700 dark:text-neutral-200'"
              @click="clearScheduleSelection"
            >
              <UIcon
                name="i-lucide-image"
                class="w-4 h-4"
              />
              <span class="font-medium">Galería Principal</span>
              <UBadge
                v-if="tour.images?.length"
                size="xs"
                color="neutral"
                variant="soft"
                class="ml-auto"
              >
                {{ tour.images.length }}
              </UBadge>
            </button>
          </div>

          <!-- Schedules List -->
          <div v-if="schedules.length > 0">
            <div class="flex items-center justify-between mb-3">
              <h3 class="text-sm font-medium text-neutral-900 dark:text-neutral-100">
                Fechas Realizadas
              </h3>
              <UBadge
                size="xs"
                color="neutral"
                variant="soft"
              >
                {{ schedules.length }}
              </UBadge>
            </div>

            <div class="space-y-1 max-h-[calc(100vh-500px)] overflow-y-auto">
              <button
                v-for="schedule in schedules"
                :key="schedule.id"
                class="w-full flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-neutral-100 dark:hover:bg-neutral-800 transition-colors text-left"
                :class="selectedScheduleId === schedule.scheduleId ? 'bg-primary-100 dark:bg-primary-900/30 text-primary-700 dark:text-primary-400' : 'text-neutral-700 dark:text-neutral-200'"
                @click="onScheduleSelect(schedule as any)"
              >
                <UIcon
                  name="i-lucide-calendar"
                  class="w-4 h-4 shrink-0"
                />
                <span class="text-sm truncate">{{ schedule.label }}</span>
              </button>
            </div>
          </div>

          <!-- No Schedules -->
          <div
            v-else
            class="text-center py-8"
          >
            <UIcon
              name="i-lucide-calendar-x"
              class="w-12 h-12 mx-auto mb-2 text-neutral-200 dark:text-neutral-700"
            />
            <p class="text-sm text-neutral-600 dark:text-neutral-300">
              No hay fechas realizadas
            </p>
          </div>
        </UCard>
      </div>

      <!-- Right Content - Gallery Grid -->
      <div class="flex-1">
        <UCard>
          <template #header>
            <div class="flex items-center justify-between">
              <div>
                <h2 class="text-lg font-semibold text-neutral-900 dark:text-neutral-100">
                  <template v-if="selectedScheduleId">
                    {{ selectedScheduleLabel }}
                  </template>
                  <template v-else>
                    Galería Principal
                  </template>
                </h2>
                <p class="text-sm text-neutral-600 dark:text-neutral-300">
                  <template v-if="selectedScheduleId">
                    Fotos específicas de esta fecha
                  </template>
                  <template v-else>
                    Fotos generales del tour
                  </template>
                </p>
              </div>
            </div>
          </template>

          <!-- Gallery Manager -->
          <div class="min-h-[400px]">
            <ClientOnly>
              <!-- Tour Gallery -->
              <AdminMediaGalleryManager
                v-if="!selectedScheduleId && tour.id"
                :key="`tour-${tour.id}`"
                :tour-id="tour.id"
              />

              <!-- Schedule Gallery -->
              <AdminMediaGalleryManager
                v-else-if="selectedScheduleId"
                :key="`schedule-${selectedScheduleId}`"
                :schedule-id="selectedScheduleId"
              />
            </ClientOnly>
          </div>
        </UCard>
      </div>
    </div>
  </div>
</template>
