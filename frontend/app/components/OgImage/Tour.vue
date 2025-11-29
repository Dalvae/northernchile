<script setup lang="ts">
defineProps<{
  title?: string
  price?: string
  image?: string
  category?: string
}>()
</script>

<template>
  <div class="w-full h-full flex flex-col justify-between bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 text-white relative overflow-hidden">
    <!-- Imagen de Fondo con overlay -->
    <img
      v-if="image"
      :src="image"
      class="absolute inset-0 w-full h-full object-cover opacity-40"
    >

    <!-- Pattern overlay para textura -->
    <div
      class="absolute inset-0 opacity-5"
      style="background-image: radial-gradient(circle, white 1px, transparent 1px); background-size: 24px 24px;"
    />

    <!-- Degradado superior -->
    <div class="absolute inset-0 bg-gradient-to-b from-black/60 via-transparent to-black/90" />

    <!-- Estrellas decorativas -->
    <div class="absolute top-12 right-12 w-2 h-2 bg-white rounded-full opacity-60" />
    <div class="absolute top-24 right-32 w-1 h-1 bg-white rounded-full opacity-40" />
    <div class="absolute top-32 right-20 w-1.5 h-1.5 bg-orange-400 rounded-full opacity-50" />

    <!-- Header -->
    <div class="relative z-10 pt-12 px-16">
      <div class="flex items-center gap-4">
        <div class="text-orange-400 text-3xl font-bold tracking-tight">
          NORTHERN CHILE
        </div>
        <div class="h-px flex-1 bg-gradient-to-r from-orange-400/50 to-transparent" />
      </div>
    </div>

    <!-- Contenido principal -->
    <div class="relative z-10 px-16 pb-12 flex-1 flex flex-col justify-end">
      <!-- Categoría -->
      <div
        v-if="category"
        class="inline-flex items-center gap-2 mb-6 w-fit"
      >
        <div class="w-3 h-3 bg-orange-400 rounded-full" />
        <span class="text-orange-400 text-xl font-semibold uppercase tracking-wider">
          {{ category }}
        </span>
      </div>

      <!-- Título -->
      <h1
        class="text-7xl font-bold leading-tight text-white mb-12 drop-shadow-2xl"
        style="font-family: 'Playfair Display', serif; text-shadow: 0 4px 20px rgba(0,0,0,0.8);"
      >
        {{ title || 'Northern Chile Tours' }}
      </h1>

      <!-- Footer -->
      <div class="flex items-end justify-between pt-8 border-t border-white/10">
        <div class="flex items-center gap-3">
          <svg
            class="w-6 h-6 text-orange-400"
            fill="currentColor"
            viewBox="0 0 20 20"
          >
            <path
              fill-rule="evenodd"
              d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z"
              clip-rule="evenodd"
            />
          </svg>
          <span class="text-2xl text-gray-300 font-light">San Pedro de Atacama</span>
        </div>
        <div
          v-if="price"
          class="flex flex-col items-end"
        >
          <span class="text-lg text-gray-400 font-light mb-1">Desde</span>
          <span
            class="text-5xl font-bold text-orange-400"
            style="text-shadow: 0 2px 10px rgba(251,146,60,0.3);"
          >{{ price }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
