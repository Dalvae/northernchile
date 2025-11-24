<script setup lang="ts">
defineProps<{
  title?: string
  price?: string
  image?: string
  category?: string
}>()
</script>

<template>
  <div class="w-full h-full flex flex-col justify-end bg-[#0f172a] text-white relative overflow-hidden">
    <!-- Imagen de Fondo -->
    <img
      v-if="image"
      :src="image"
      class="absolute inset-0 w-full h-full object-cover opacity-60"
    >
    <!-- Fondo de respaldo -->
    <div
      v-else
      class="absolute inset-0 bg-gradient-to-br from-[#0f172a] to-[#1e293b]"
    />

    <!-- Degradado -->
    <div class="absolute inset-0 bg-gradient-to-t from-black/90 via-black/50 to-transparent" />

    <!-- Contenido -->
    <div class="relative z-10 p-16 w-full">
      <div class="flex items-center gap-4 mb-6">
        <div
          v-if="category"
          class="bg-orange-500 text-white px-4 py-2 rounded-lg text-2xl font-bold uppercase tracking-widest shadow-lg"
        >
          {{ category }}
        </div>
        <div class="text-gray-300 text-xl uppercase tracking-widest font-semibold">
          Northern Chile
        </div>
      </div>

      <h1 class="text-7xl font-bold leading-tight text-white mb-8 drop-shadow-xl font-serif">
        {{ title || 'Northern Chile Tours' }}
      </h1>

      <div class="flex items-center justify-between border-t border-white/20 pt-8 mt-8">
        <div class="text-3xl text-gray-200">
          San Pedro de Atacama
        </div>
        <div
          v-if="price"
          class="flex flex-col items-end"
        >
          <span class="text-xl text-gray-400 uppercase text-right">Desde</span>
          <span class="text-5xl font-bold text-orange-400">{{ price }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
