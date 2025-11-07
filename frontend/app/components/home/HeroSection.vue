<template>
  <section class="relative h-screen min-h-[600px] flex items-center justify-center overflow-hidden">
    <!-- Fondo animado de estrellas -->
    <UiBgStars />

    <!-- Overlay gradient -->
    <div class="absolute inset-0 bg-gradient-to-b from-transparent via-neutral-900/50 to-neutral-900" />

    <!-- Content -->
    <div class="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="text-center">
        <!-- Badge superior (opcional) -->
        <UBadge
          color="primary"
          variant="soft"
          size="lg"
          class="mb-6"
        >
          <span class="flex items-center gap-2">
            <UIcon
              name="i-lucide-sparkles"
              class="w-4 h-4"
            />
            {{ t('hero.badge') }}
          </span>
        </UBadge>

        <!-- Título principal -->
        <h1 class="text-4xl sm:text-5xl md:text-6xl lg:text-7xl font-bold text-white mb-6">
          <span class="block">{{ t('hero.title1') }}</span>
          <span class="block text-primary mt-2">{{ t('hero.title2') }}</span>
        </h1>

        <!-- Subtítulo -->
        <p class="text-lg sm:text-xl md:text-2xl text-neutral-200 mb-8 max-w-3xl mx-auto">
          {{ t('hero.subtitle') }}
        </p>

        <!-- CTAs -->
        <div class="flex flex-col sm:flex-row gap-4 justify-center items-center">
          <UButton
            :label="t('hero.buttonExplore')"
            to="/tours"
            color="primary"
            size="xl"
            icon="i-lucide-telescope"
            class="w-full sm:w-auto"
          />
          <UButton
            :label="t('hero.buttonContact')"
            to="/contact"
            color="neutral"
            variant="outline"
            size="xl"
            icon="i-lucide-mail"
            class="w-full sm:w-auto"
          />
        </div>

        <!-- Features rápidos -->
        <div class="mt-12 grid grid-cols-2 md:grid-cols-4 gap-6 max-w-4xl mx-auto">
          <div
            v-for="feature in quickFeatures"
            :key="feature.label"
            class="flex flex-col items-center gap-2 text-white"
          >
            <div class="w-12 h-12 rounded-full bg-primary/20 flex items-center justify-center">
              <UIcon
                :name="feature.icon"
                class="w-6 h-6 text-primary"
              />
            </div>
            <span class="text-sm font-medium">{{ feature.label }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Scroll indicator -->
    <div class="absolute bottom-8 left-1/2 -translate-x-1/2 z-10">
      <button
        type="button"
        class="flex flex-col items-center gap-2 text-white/80 hover:text-white transition-colors"
        @click="scrollToContent"
      >
        <span class="text-sm">{{ t('hero.discoverMore') }}</span>
        <UIcon
          name="i-lucide-chevron-down"
          class="w-6 h-6 animate-bounce"
        />
      </button>
    </div>
  </section>
</template>

<script setup lang="ts">
const { t } = useI18n()

const quickFeatures = computed(() => [
  { icon: 'i-lucide-telescope', label: t('hero.features.proTelescopes') },
  { icon: 'i-lucide-users', label: t('hero.features.expertGuides') },
  { icon: 'i-lucide-star', label: t('hero.features.clearSkies') },
  { icon: 'i-lucide-shield-check', label: t('hero.features.freeCancellation') }
])

function scrollToContent() {
  window.scrollTo({
    top: window.innerHeight,
    behavior: 'smooth'
  })
}
</script>

<style scoped>
/* Animación adicional para el título si lo deseas */
h1 {
  animation: fadeInUp 1s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
