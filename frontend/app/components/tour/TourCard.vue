<template>
  <NuxtLink
    :to="linkTo"
    class="block h-full group"
    :aria-label="`Ver detalles del tour ${title}`"
  >
    <div
      class="relative h-[500px] rounded-xl overflow-hidden atacama-card transition-all duration-500 hover:shadow-2xl hover:shadow-primary-500/20 hover:border-primary/50"
    >
      <!-- Image Background (80% height initially, or full height with overlay) -->
      <div class="absolute inset-0 h-full w-full">
        <NuxtImg
          :src="imageSrc"
          :alt="title"
          class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
          :loading="isLCP ? 'eager' : 'lazy'" 
          :fetchpriority="isLCP ? 'high' : 'auto'"
          format="webp"
          sizes="100vw sm:50vw md:400px"
          placeholder
        />
        <!-- Gradient Overlay -->
        <div class="absolute inset-0 bg-gradient-to-t from-neutral-950 via-neutral-950/60 to-transparent opacity-90 group-hover:opacity-80 transition-opacity duration-500" />
      </div>

      <!-- Content Container -->
      <div class="absolute inset-0 flex flex-col justify-end p-6 z-10">
        <!-- Badges (Top) -->
        <div class="absolute top-4 left-4 right-4 flex justify-between items-start">
          <div v-if="showCategory && categoryLabel">
             <UBadge
              :color="categoryColor"
              variant="solid"
              size="md"
              class="backdrop-blur-md shadow-lg"
            >
              {{ categoryLabel }}
            </UBadge>
          </div>
          
          <div
            v-if="showSensitivityBadges && (tour.moonSensitive || tour.windSensitive)"
            class="flex gap-1"
          >
            <UTooltip
              v-if="tour.moonSensitive"
              :text="$t('tours.sensitive.moon')"
            >
              <UBadge
                color="tertiary"
                variant="solid"
                size="sm"
                class="backdrop-blur-md"
              >
                <UIcon
                  name="i-lucide-moon"
                  class="w-3 h-3"
                />
              </UBadge>
            </UTooltip>
            <UTooltip
              v-if="tour.windSensitive"
              :text="$t('tours.sensitive.wind')"
            >
              <UBadge
                color="info"
                variant="solid"
                size="sm"
                class="backdrop-blur-md"
              >
                <UIcon
                  name="i-lucide-wind"
                  class="w-3 h-3"
                />
              </UBadge>
            </UTooltip>
          </div>
        </div>

        <!-- Main Content -->
        <div class="transform transition-transform duration-500 translate-y-4 group-hover:translate-y-0">
          <h3
            class="text-2xl font-display font-bold text-white mb-2 text-glow leading-tight"
          >
            {{ title }}
          </h3>
          
          <div class="h-0 overflow-hidden group-hover:h-auto transition-all duration-500 opacity-0 group-hover:opacity-100">
             <p
              v-if="showDescription && description"
              class="text-sm text-neutral-200 line-clamp-3 mb-4"
            >
              {{ description }}
            </p>
          </div>

          <!-- Meta Info -->
          <div
            v-if="showMeta"
            class="flex items-center gap-4 text-sm text-neutral-200 mt-2"
          >
            <div
              v-if="tour.durationHours"
              class="flex items-center gap-1"
            >
              <UIcon name="i-lucide-clock" class="w-4 h-4 text-primary" />
              <span>{{ $t('tours.duration_hours', { hours: tour.durationHours }) }}</span>
            </div>

             <div
              v-if="(tour as any).maxParticipants != null"
              class="flex items-center gap-1"
             >
              <UIcon name="i-lucide-users" class="w-4 h-4 text-primary" />
              <span>{{ $t('tours.max_participants', { count: (tour as any).maxParticipants }) }}</span>
            </div>
          </div>
          
          <!-- Price and Action -->
          <div class="mt-4 pt-4 border-t border-white/10 flex items-center justify-between">
             <div v-if="showPrice">
              <p class="text-xs text-neutral-300 uppercase tracking-wider">
                {{ $t('tours.price_from') }}
              </p>
              <p class="text-xl font-bold text-white">
                {{ formatPrice(tour.price) }}
              </p>
            </div>
            
            <UButton
              icon="i-lucide-arrow-right"
              variant="ghost"
              color="primary"
              :aria-label="`Ver tour ${title}`"
              class="group-hover:translate-x-1 transition-transform"
            />
          </div>
        </div>
      </div>
    </div>
  </NuxtLink>
</template>

<script setup lang="ts">
import type { TourRes } from 'api-client'

const props = withDefaults(defineProps<{
  tour: TourRes
  variant?: 'home' | 'list'
  showCategory?: boolean
  showSensitivityBadges?: boolean
  showDescription?: boolean
  showMeta?: boolean
  showRating?: boolean
  showPrice?: boolean
  index?: number // <-- Agregamos index para saber si es la primera tarjeta
}>(), {
  variant: 'home',
  showCategory: true,
  showSensitivityBadges: true,
  showDescription: true,
  showMeta: true,
  showRating: true,
  showPrice: true
})

const localePath = useLocalePath()
const { locale } = useI18n()
const { formatPrice } = useCurrency()

const linkTo = computed(() => localePath(`/tours/${props.tour.slug || props.tour.id}`))

const title = computed(() =>
  props.tour.nameTranslations?.[locale.value]
  || props.tour.nameTranslations?.es
  || 'Tour'
)

const description = computed(() => {
  const blocks = (props.tour as any).descriptionBlocksTranslations?.[locale.value]
    || (props.tour as any).descriptionBlocksTranslations?.es
  if (Array.isArray(blocks) && blocks.length) {
    const firstText = blocks.find((b: any) => b?.content)?.content
    if (firstText) return firstText
  }
  return (props.tour as any).descriptionTranslations?.[locale.value]
    || (props.tour as any).descriptionTranslations?.es
    || ''
})

const imageSrc = computed(() => {
  const hero = props.tour.images?.find(img => (img as any).isHeroImage)?.imageUrl
  const first = props.tour.images?.[0]?.imageUrl || (props.tour as any).images?.[0]

  return hero || first || '/images/tour-placeholder.svg'
})

type BadgeColor = 'error' | 'info' | 'success' | 'primary' | 'secondary' | 'tertiary' | 'warning' | 'neutral'

const categoryColor = computed((): BadgeColor => {
  const map: Record<string, BadgeColor> = {
    ASTRONOMICAL: 'tertiary',
    REGULAR: 'primary',
    SPECIAL: 'warning',
    PRIVATE: 'secondary'
  }
  return map[props.tour.category as string] || 'neutral'
})

const categoryLabel = computed(() =>
  (useI18n().t(`tours.category.${props.tour.category}`, props.tour.category || 'REGULAR') as string)
)

const isLCP = computed(() => (props.index ?? 10) < 2)
</script>
