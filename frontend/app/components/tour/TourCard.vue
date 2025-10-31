<script setup lang="ts">
import type { TourRes } from '~/lib/api-client'
import { useI18n } from 'vue-i18n'

const props = defineProps<{ tour: TourRes }>()
const localePath = useLocalePath()
const { locale } = useI18n()
const { formatPrice } = useCurrency()

const translatedName = computed(() =>
  props.tour.nameTranslations?.[locale.value] || props.tour.nameTranslations?.['es']
)

const heroImage = computed(() =>
  props.tour.images?.find(img => img.isHeroImage)?.imageUrl || props.tour.images?.[0]?.imageUrl || 'https://source.unsplash.com/random/800x600?desert,stars'
)
</script>

<template>
  <UCard
    :ui="{
      body: { padding: 'p-0 sm:p-0' },
      header: { padding: 'p-0 sm:p-0' },
      footer: { padding: 'p-0 sm:p-0' },
      base: 'overflow-hidden transform hover:scale-105 transition-transform duration-300'
    }"
  >
    <template #header>
      <div class="aspect-w-16 aspect-h-9">
        <img
          class="w-full h-48 object-cover"
          :src="heroImage"
          :alt="translatedName"
        />
      </div>
    </template>

    <div class="p-4">
      <h3 class="text-xl font-bold mb-2 h-14 text-ellipsis overflow-hidden font-display text-gray-900 dark:text-white">{{ translatedName }}</h3>
    </div>

    <template #footer>
      <div class="flex justify-between items-center p-4 bg-gray-100 dark:bg-gray-900/50">
        <span class="text-2xl font-bold text-primary-400">{{ formatPrice(props.tour.price) }}</span>
        <UButton
          :to="localePath(`/tours/${props.tour.id}`)"
          icon="i-lucide-arrow-right"
          size="md"
          trailing
          color="accent"
          variant="solid"
        >
          {{ $t('tours.view_details') }}
        </UButton>
      </div>
    </template>
  </UCard>
</template>
