<script setup lang="ts">
import type { TourRes } from '~/lib/api-client'

const props = defineProps<{ tour: TourRes }>()
</script>

<template>
  <UCard class="overflow-hidden transform hover:scale-105 transition-transform duration-300">
    <template #header>
      <div class="aspect-w-16 aspect-h-9">
        <img
          class="w-full h-full object-cover rounded-t-lg"
          src="https://source.unsplash.com/random/800x600?desert,stars"
          alt="Tour Image"
        />
      </div>
    </template>

    <div class="p-4">
      <h3 class="text-xl font-bold mb-2">{{ props.tour.name }}</h3>
      <p class="text-gray-600 dark:text-gray-300 text-sm mb-4 h-20 overflow-hidden text-ellipsis">
        {{ props.tour.description }}
      </p>
    </div>

    <template #footer>
      <div class="flex justify-between items-center p-4 bg-gray-50 dark:bg-gray-800">
        <span class="text-2xl font-bold text-primary">${{ props.tour.priceAdult }}</span>
        <UButton
          :to="`/tours/${props.tour.id}`"
          icon="i-heroicons-arrow-right-20-solid"
          size="md"
          class="rounded-full"
        >
          {{ $t('tours.view_details') }}
        </UButton>
      </div>
    </template>
  </UCard>
</template>
