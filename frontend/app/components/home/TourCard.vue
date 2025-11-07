<template>
  <NuxtLink
    :to="`/tours/${tour.slug || tour.id}`"
    class="block"
  >
    <UCard
      :ui="{
        body: { padding: 'p-0' },
        rounded: 'rounded-xl',
        shadow: 'shadow-lg hover:shadow-xl',
        ring: 'ring-1 ring-neutral-300'
      }"
      class="overflow-hidden transition-all duration-300 hover:-translate-y-1"
    >
      <!-- Imagen -->
      <div class="relative h-48 overflow-hidden">
        <img
          :src="tour.images?.[0] || '/images/tour-placeholder.svg'"
          :alt="getTourName()"
          class="w-full h-full object-cover transition-transform duration-300 hover:scale-110"
        >

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
          <UTooltip
            v-if="tour.moonSensitive"
            :text="t('tours.sensitive.moon')"
          >
            <UBadge
              color="tertiary"
              variant="solid"
              size="sm"
            >
              <UIcon
                name="i-lucide-moon"
                class="w-3 h-3"
              />
            </UBadge>
          </UTooltip>
          <UTooltip
            v-if="tour.windSensitive"
            :text="t('tours.sensitive.wind')"
          >
            <UBadge
              color="info"
              variant="solid"
              size="sm"
            >
              <UIcon
                name="i-lucide-wind"
                class="w-3 h-3"
              />
            </UBadge>
          </UTooltip>
        </div>
      </div>

      <!-- Contenido -->
      <div class="p-5 space-y-4">
        <!-- Título -->
        <div>
          <h3 class="text-lg font-semibold text-neutral-50 line-clamp-2">
            {{ getTourName() }}
          </h3>
          <p class="mt-1 text-sm text-neutral-300 line-clamp-2">
            {{ getTourDescription() }}
          </p>
        </div>

        <!-- Info rápida -->
        <div class="flex items-center gap-4 text-sm text-neutral-300">
          <div class="flex items-center gap-1">
            <UIcon
              name="i-lucide-clock"
              class="w-4 h-4"
            />
            <span>{{
              t("tours.duration_hours", { hours: tour.durationHours })
            }}</span>
          </div>
          <div class="flex items-center gap-1">
            <UIcon
              name="i-lucide-users"
              class="w-4 h-4"
            />
            <span>{{
              t("tours.max_participants", { count: tour.maxParticipants })
            }}</span>
          </div>
          <div
            v-if="tour.rating"
            class="flex items-center gap-1"
          >
            <UIcon
              name="i-lucide-star"
              class="w-4 h-4 text-warning"
            />
            <span>{{ tour.rating }}</span>
          </div>
        </div>

        <!-- Precio -->
        <div class="pt-4 border-t border-neutral-700">
          <p class="text-xs text-neutral-400">{{ t("tours.price_from") }}</p>
          <p class="text-2xl font-bold text-neutral-50">
            {{ formatPrice(tour.price) }}
          </p>
        </div>
      </div>
    </UCard>
  </NuxtLink>
</template>

<script setup lang="ts">
import type { TourRes } from '~/lib/api-client'

const props = defineProps<{
  tour: TourRes
}>()

const { t, locale } = useI18n()
const { formatPrice } = useCurrency()

function getTourName(): string {
  return (
    props.tour.nameTranslations?.[locale.value]
    || props.tour.nameTranslations?.['es']
    || 'Tour'
  )
}

function getTourDescription(): string {
  return (
    props.tour.descriptionTranslations?.[locale.value]
    || props.tour.descriptionTranslations?.['es']
    || ''
  )
}

function getCategoryColor(category: string): string {
  const colors: Record<string, string> = {
    ASTRONOMICAL: 'tertiary',
    REGULAR: 'primary',
    SPECIAL: 'warning',
    PRIVATE: 'secondary'
  }
  return colors[category] || 'neutral'
}

function getCategoryLabel(category: string): string {
  return t(`tours.category.${category}`, category)
}
</script>
