<script setup lang="ts">
const { fetchAll } = useTours()
const { data: tours, pending, error } = await fetchAll()
</script>

<template>
  <div>
    <h1 class="text-3xl font-bold mb-8">{{ $t('tours.all') }}</h1>

    <div v-if="pending">
      <p>Cargando tours...</p>
    </div>
    <div v-else-if="error">
      <p>Ocurrió un error al cargar los tours: {{ error.message }}</p>
    </div>
    <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-8">
      <TourCard v-for="tour in tours" :key="tour.id" :tour="tour" />
    </div>
  </div>
</template>