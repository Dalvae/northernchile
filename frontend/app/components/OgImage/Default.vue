<script setup lang="ts">
defineProps<{
  title?: string
  description?: string
  siteName?: string
}>()
</script>

<template>
  <div class="h-full w-full flex flex-col bg-[#020420] text-white p-12 justify-between">
    <!-- Fondo decorativo sutil (estrellas) -->
    <div class="absolute inset-0 bg-[#020420]" style="z-index: -1;">
      <svg width="100%" height="100%" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <pattern id="stars" x="0" y="0" width="100" height="100" patternUnits="userSpaceOnUse">
            <circle cx="2" cy="2" r="1" fill="#FFF" fill-opacity="0.2"/>
            <circle cx="50" cy="50" r="1.5" fill="#FFF" fill-opacity="0.3"/>
            <circle cx="80" cy="20" r="1" fill="#FFF" fill-opacity="0.2"/>
          </pattern>
        </defs>
        <rect width="100%" height="100%" fill="url(#stars)"/>
      </svg>
    </div>

    <!-- Header -->
    <div class="flex items-center gap-4">
      <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#F97316" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z" />
        <path d="M19 3v4" />
        <path d="M21 5h-4" />
      </svg>
      <h1 class="text-2xl font-bold font-serif tracking-wide text-white/90 uppercase">
        {{ siteName || 'Northern Chile Tours' }}
      </h1>
    </div>

    <!-- Main Content -->
    <div class="flex flex-col gap-4 max-w-4xl mt-auto mb-auto">
      <h2 class="text-7xl font-serif font-bold text-[#FF9E4A] leading-tight">
        {{ title || 'San Pedro de Atacama' }}
      </h2>
      <p class="text-3xl text-gray-300 font-sans leading-relaxed mt-4">
        {{ description || 'Live a unique experience observing the universe in one of the clearest skies on the planet.' }}
      </p>
    </div>

    <!-- Footer -->
    <div class="flex items-center justify-between border-t border-white/10 pt-8 mt-12">
      <div class="flex items-center gap-2">
        <div class="w-2 h-2 rounded-full bg-green-500"></div>
        <span class="text-xl text-gray-400">Tours Disponibles / Available Tours</span>
      </div>
      <div class="text-xl text-[#F97316] font-medium">
        www.northernchile.com
      </div>
    </div>
  </div>
</template>
