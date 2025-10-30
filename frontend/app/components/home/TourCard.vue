<template>
  <UCard
    :ui="{
      body: { padding: 'p-0' },
      rounded: 'rounded-xl',
      shadow: 'shadow-lg hover:shadow-xl',
      ring: 'ring-1 ring-neutral-200 dark:ring-neutral-800'
    }"
    class="overflow-hidden transition-all duration-300 hover:-translate-y-1"
  >
    <!-- Imagen -->
    <div class="relative h-48 overflow-hidden">
      <img
        :src="tour.imageUrl || '/images/tour-placeholder.jpg'"
        :alt="tour.name"
        class="w-full h-full object-cover transition-transform duration-300 hover:scale-110"
      />

      <!-- Badge de categoría -->
      <div class="absolute top-3 left-3">
        <UBadge
          :color="getCategoryColor(tour.category)"
          variant="solid"
          size="md"
        >
          {{ getCategoryLabel(tour.category) }}
        </UBadge>
      </div>

      <!-- Badges de sensibilidad -->
      <div class="absolute top-3 right-3 flex gap-1">
        <UTooltip v-if="tour.moonSensitive" :text="t('tours.sensitive.moon')">
          <UBadge color="tertiary" variant="solid" size="sm">
            <UIcon name="i-lucide-moon" class="w-3 h-3" />
          </UBadge>
        </UTooltip>
        <UTooltip v-if="tour.windSensitive" :text="t('tours.sensitive.wind')">
          <UBadge color="info" variant="solid" size="sm">
            <UIcon name="i-lucide-wind" class="w-3 h-3" />
          </UBadge>
        </UTooltip>
      </div>
    </div>

    <!-- Contenido -->
    <div class="p-5 space-y-4">
      <!-- Título -->
      <div>
        <h3 class="text-lg font-semibold text-neutral-900 dark:text-white line-clamp-2">
          {{ tour.name }}
        </h3>
        <p class="mt-1 text-sm text-neutral-600 dark:text-neutral-400 line-clamp-2">
          {{ tour.description }}
        </p>
      </div>

      <!-- Info rápida -->
      <div class="flex items-center gap-4 text-sm text-neutral-600 dark:text-neutral-400">
        <div class="flex items-center gap-1">
          <UIcon name="i-lucide-clock" class="w-4 h-4" />
          <span>{{ t('tours.duration_hours', { hours: tour.durationHours }) }}</span>
        </div>
        <div class="flex items-center gap-1">
          <UIcon name="i-lucide-users" class="w-4 h-4" />
          <span>{{ t('tours.max_participants', { count: tour.maxParticipants }) }}</span>
        </div>
        <div v-if="tour.rating" class="flex items-center gap-1">
          <UIcon name="i-lucide-star" class="w-4 h-4 text-warning" />
          <span>{{ tour.rating }}</span>
        </div>
      </div>

      <!-- Precio y CTA -->
      <div class="flex items-center justify-between pt-4 border-t border-neutral-200 dark:border-neutral-800">
        <div>
          <p class="text-xs text-neutral-500">{{ t('tours.price_from') }}</p>
          <p class="text-2xl font-bold text-neutral-900 dark:text-white">
            {{ formatPrice(tour.price) }}
          </p>
        </div>
        <UButton
          :to="`/tours/${tour.id}`"
          color="primary"
          icon="i-lucide-arrow-right"
          trailing
        >
          {{ t('tours.view_tour') }}
        </UButton>
      </div>
    </div>
  </UCard>
</template>

<script setup lang="ts">
import type { TourRes } from '~/lib/api-client'

const props = defineProps<{
  tour: TourRes
}>()

const { t } = useI18n()

function formatPrice(price: number) {
  return new Intl.NumberFormat('es-CL', {
    style: 'currency',
    currency: 'CLP',
    minimumFractionDigits: 0
  }).format(price)
}

function getCategoryColor(category: string): string {
  const colors: Record<string, string> = {
    'ASTRONOMICAL': 'tertiary',
    'REGULAR': 'primary',
    'SPECIAL': 'warning',
    'PRIVATE': 'secondary'
  }
  return colors[category] || 'neutral'
}

function getCategoryLabel(category: string): string {
  return t(`tours.category.${category}`, category)
}
</script>
