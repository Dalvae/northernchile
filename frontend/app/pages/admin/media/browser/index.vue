<script setup lang="ts">
definePageMeta({
  layout: 'admin',
  middleware: 'auth-admin'
})

const { fetchAdminTours } = useAdminData()

// Fetch all tours
const {
  data: tours,
  pending: loading
} = useAsyncData(
  'admin-media-tours',
  () => fetchAdminTours(),
  {
    server: false,
    lazy: true,
    default: () => []
  }
)

// Computed: Published tours count
const publishedCount = computed(() => tours.value.filter(t => t.status === 'PUBLISHED').length)
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-3xl font-bold text-neutral-900 dark:text-neutral-100">
          Explorador de Medios
        </h1>
        <p class="text-neutral-600 dark:text-neutral-300 mt-1">
          Navega por tus tours para gestionar sus fotos
        </p>
      </div>

      <div class="flex gap-2">
        <NuxtLink to="/admin/media">
          <UButton
            icon="i-lucide-table"
            size="lg"
            color="neutral"
            variant="outline"
            title="Vista de tabla"
          >
            Vista Tabla
          </UButton>
        </NuxtLink>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <UCard>
        <div class="flex items-center gap-4">
          <div class="p-3 bg-primary-100 dark:bg-primary-900/30 rounded-lg">
            <UIcon
              name="i-lucide-folder-open"
              class="w-6 h-6 text-primary-600 dark:text-primary-400"
            />
          </div>
          <div>
            <p class="text-2xl font-bold text-neutral-900 dark:text-neutral-100">
              {{ tours.length }}
            </p>
            <p class="text-sm text-neutral-600 dark:text-neutral-300">
              Tours Totales
            </p>
          </div>
        </div>
      </UCard>

      <UCard>
        <div class="flex items-center gap-4">
          <div class="p-3 bg-success-100 dark:bg-success-900/30 rounded-lg">
            <UIcon
              name="i-lucide-check-circle"
              class="w-6 h-6 text-success-600 dark:text-success-400"
            />
          </div>
          <div>
            <p class="text-2xl font-bold text-neutral-900 dark:text-neutral-100">
              {{ publishedCount }}
            </p>
            <p class="text-sm text-neutral-600 dark:text-neutral-300">
              Tours Publicados
            </p>
          </div>
        </div>
      </UCard>

      <UCard>
        <div class="flex items-center gap-4">
          <div class="p-3 bg-info-100 dark:bg-info-900/30 rounded-lg">
            <UIcon
              name="i-lucide-image"
              class="w-6 h-6 text-info-600 dark:text-info-400"
            />
          </div>
          <div>
            <p class="text-2xl font-bold text-neutral-900 dark:text-neutral-100">
              {{ tours.reduce((sum, t) => sum + (t.images?.length || 0), 0) }}
            </p>
            <p class="text-sm text-neutral-600 dark:text-neutral-300">
              Fotos Totales
            </p>
          </div>
        </div>
      </UCard>
    </div>

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

    <!-- Tours Grid -->
    <div
      v-else-if="tours.length > 0"
      class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
    >
      <NuxtLink
        v-for="tour in tours"
        :key="tour.id"
        :to="`/admin/media/${tour.slug}`"
        class="group"
      >
        <UCard
          class="h-full hover:shadow-lg transition-shadow cursor-pointer"
        >
          <!-- Tour Cover Image -->
          <div class="aspect-video bg-neutral-100 dark:bg-neutral-800 rounded-lg mb-4 overflow-hidden relative">
            <NuxtImg
              v-if="tour.images && tour.images.length > 0 && tour.images[0]"
              :src="tour.images[0].variants?.medium || tour.images[0].imageUrl"
              :alt="tour.nameTranslations?.es || 'Tour'"
              class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
              format="webp"
              loading="lazy"
              placeholder
            />
            <div
              v-else
              class="flex items-center justify-center h-full"
            >
              <UIcon
                name="i-lucide-image"
                class="w-12 h-12 text-neutral-200 dark:text-neutral-700"
              />
            </div>

            <!-- Image Count Badge -->
            <div
              v-if="tour.images && tour.images.length > 0"
              class="absolute top-3 right-3"
            >
              <UBadge
                color="neutral"
                variant="solid"
                size="md"
              >
                <UIcon
                  name="i-lucide-images"
                  class="w-3 h-3 mr-1"
                />
                {{ tour.images.length }}
              </UBadge>
            </div>
          </div>

          <!-- Tour Info -->
          <div class="space-y-3">
            <div>
              <h3 class="text-lg font-semibold text-neutral-900 dark:text-neutral-100 line-clamp-2 group-hover:text-primary-600 dark:group-hover:text-primary-400 transition-colors">
                {{ tour.nameTranslations?.es || 'Sin nombre' }}
              </h3>
              <p class="text-sm text-neutral-600 dark:text-neutral-300 mt-1">
                {{ tour.category }}
              </p>
            </div>

            <!-- Status & Stats -->
            <div class="flex items-center justify-between">
              <UBadge
                :color="tour.status === 'PUBLISHED' ? 'success' : tour.status === 'DRAFT' ? 'warning' : 'neutral'"
                variant="soft"
                size="xs"
              >
                {{ tour.status === 'PUBLISHED' ? 'Publicado' : tour.status === 'DRAFT' ? 'Borrador' : tour.status }}
              </UBadge>

              <div class="flex items-center gap-2 text-sm text-neutral-600 dark:text-neutral-300">
                <UIcon
                  name="i-lucide-arrow-right"
                  class="w-4 h-4 group-hover:translate-x-1 transition-transform"
                />
              </div>
            </div>
          </div>
        </UCard>
      </NuxtLink>
    </div>

    <!-- Empty State -->
    <UCard v-else>
      <div class="text-center py-12">
        <UIcon
          name="i-lucide-folder-x"
          class="w-16 h-16 mx-auto mb-4 text-neutral-200 dark:text-neutral-700"
        />
        <h3 class="text-lg font-semibold text-neutral-900 dark:text-neutral-100 mb-2">
          No hay tours aún
        </h3>
        <p class="text-neutral-600 dark:text-neutral-300 mb-6">
          Crea tu primer tour para comenzar a gestionar sus fotos
        </p>
        <NuxtLink to="/admin/tours">
          <UButton
            color="primary"
            size="lg"
          >
            <UIcon
              name="i-lucide-plus"
              class="w-4 h-4 mr-2"
            />
            Crear Primer Tour
          </UButton>
        </NuxtLink>
      </div>
    </UCard>
  </div>
</template>
