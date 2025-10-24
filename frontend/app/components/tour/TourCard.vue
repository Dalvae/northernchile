<script setup lang="ts">
import type { TourRes } from '~/lib/api-client'

const props = defineProps<{ tour: TourRes }>()
const localePath = useLocalePath()
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
          src="https://source.unsplash.com/random/800x600?desert,stars"
          alt="Tour Image"
        />
      </div>
    </template>

    <div class="p-4">
      <h3 class="text-xl font-bold mb-2 h-14 text-ellipsis overflow-hidden font-display text-gray-100">{{ props.tour.name }}</h3>
      <p class="text-gray-300 text-sm mb-4 h-20 overflow-hidden text-ellipsis">
        {{ props.tour.description }}
      </p>
    </div>

    <template #footer>
      <div class="flex justify-between items-center p-4 bg-gray-900/50">
        <span class="text-2xl font-bold text-primary-400">${{ props.tour.priceAdult }}</span>
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
