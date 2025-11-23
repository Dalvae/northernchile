<script setup lang="ts">
defineProps<{
  title: string
  price?: string
  image?: string
  category?: string
}>()
</script>

<template>
  <div class="w-full h-full flex flex-col justify-end bg-neutral-900 text-white relative">
    <!-- Imagen de Fondo (del Tour) -->
    <img
      v-if="image"
      :src="image"
      class="absolute inset-0 w-full h-full object-cover opacity-60"
    />
    <!-- Fondo de respaldo por si falla la imagen -->
    <div v-else class="absolute inset-0 bg-gradient-to-br from-neutral-900 to-slate-800" />

    <!-- Overlay oscuro para que se lea el texto -->
    <div class="absolute inset-0 bg-gradient-to-t from-black/90 via-black/40 to-transparent" />

    <!-- Contenido -->
    <div class="relative z-10 p-12 pb-16">
      <div class="flex items-center gap-4 mb-4">
        <div class="bg-orange-500 text-white px-3 py-1 rounded text-xl font-bold uppercase tracking-wider">
          {{ category || 'Northern Chile' }}
        </div>
      </div>

      <h1 class="text-7xl font-bold font-display leading-tight text-white mb-4" style="font-family: 'Playfair Display'">
        {{ title }}
      </h1>

      <div class="flex items-center justify-between border-t border-white/20 pt-6 mt-6">
        <div class="text-3xl text-gray-300 font-sans">
          San Pedro de Atacama
        </div>
        <div v-if="price" class="text-4xl font-bold text-orange-400 font-sans">
          {{ price }}
        </div>
      </div>
    </div>
  </div>
</template>