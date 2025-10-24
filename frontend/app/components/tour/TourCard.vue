<script setup lang="ts">
import type { TourRes } from '~/lib/api-client'

const props = defineProps<{ tour: TourRes }>()
</script>

<template>
  <UCard>
    <template #header>
      <h3 class="text-lg font-semibold">{{ props.tour.name }}</h3>
    </template>

    <p class="text-gray-500 dark:text-gray-400">{{ props.tour.description }}</p>

    <template #footer>
      <div class="flex justify-between items-center">
        <span class="text-xl font-bold">${{ props.tour.priceAdult }}</span>
        <UButton :to="`/tours/${props.tour.id}`" icon="i-heroicons-arrow-right-20-solid">View Details</UButton>
      </div>
    </template>
  </UCard>
</template>
