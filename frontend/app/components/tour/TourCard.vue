<template>
  <NuxtLink
    :to="linkTo"
    class="block"
  >
    <UCard
      :ui="cardUi"
      class="overflow-hidden transition-all duration-300 hover:-translate-y-1 h-full"
    >
      <div
        class="relative overflow-hidden"
        :class="imageWrapperClass"
      >
        <img
          :src="imageSrc"
          :alt="title"
          class="w-full h-full object-cover transition-transform duration-300 hover:scale-110"
        >

        <div
          v-if="showCategory && categoryLabel"
          class="absolute top-3 left-3"
        >
          <UBadge
            :color="categoryColor"
            variant="solid"
            size="md"
          >
            {{ categoryLabel }}
          </UBadge>
        </div>

        <div
          v-if="showSensitivityBadges && (tour.moonSensitive || tour.windSensitive)"
          class="absolute top-3 right-3 flex gap-1"
        >
          <UTooltip
            v-if="tour.moonSensitive"
            :text="$t('tours.sensitive.moon')"
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
            :text="$t('tours.sensitive.wind')"
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

      <div class="p-5 space-y-3 flex flex-col h-full">
        <div>
          <h3
            class="font-semibold line-clamp-2"
            :class="titleClass"
          >
            {{ title }}
          </h3>
          <p
            v-if="showDescription && description"
            class="mt-1 text-sm line-clamp-2"
            :class="descriptionClass"
          >
            {{ description }}
          </p>
        </div>

        <div
          v-if="showMeta"
          class="flex items-center gap-4 text-sm"
          :class="metaClass"
        >
          <div
            v-if="tour.durationHours"
            class="flex items-center gap-1"
          >
            <UIcon name="i-lucide-clock" class="w-4 h-4" />
            <span>{{ $t('tours.duration_hours', { hours: tour.durationHours }) }}</span>
          </div>

           <div
            v-if="(tour as any).maxParticipants != null"
            class="flex items-center gap-1"
           >
            <UIcon name="i-lucide-users" class="w-4 h-4" />
            <span>{{ $t('tours.max_participants', { count: (tour as any).maxParticipants }) }}</span>
          </div>

          <div
            v-if="(tour as any).rating && showRating"
            class="flex items-center gap-1"
          >
            <UIcon name="i-lucide-star" class="w-4 h-4 text-warning" />
             <span>{{ (tour as any).rating }}</span>
          </div>
        </div>

        <div
          v-if="showPrice"
          class="pt-3 mt-auto flex flex-col"
        >
          <p class="text-xs text-neutral-400">
            {{ $t('tours.price_from') }}
          </p>
          <p class="text-xl font-semibold">
            {{ formatPrice(tour.price) }}
          </p>
        </div>
      </div>
    </UCard>
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

const cardUi = computed(() => ({
  root: 'overflow-hidden rounded-xl shadow-lg hover:shadow-xl ring-1 ring-neutral-300',
  body: 'p-0'
}))

const imageWrapperClass = computed(() =>
  props.variant === 'list'
    ? 'relative h-40'
    : 'relative h-48'
)

const titleClass = computed(() =>
  props.variant === 'list'
    ? 'text-lg text-neutral-900 dark:text-neutral-50'
    : 'text-lg text-neutral-50'
)

const descriptionClass = computed(() =>
  props.variant === 'list'
    ? 'text-neutral-600 dark:text-neutral-300'
    : 'text-neutral-300'
)

const metaClass = computed(() =>
  props.variant === 'list'
    ? 'text-neutral-600 dark:text-neutral-300'
    : 'text-neutral-300'
)
</script>
