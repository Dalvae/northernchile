<template>
  <section class="relative h-screen min-h-[600px] flex items-center justify-center overflow-hidden">
    <!-- Background Image with Overlay -->
    <div class="absolute inset-0 z-0">
      <div class="absolute inset-0 bg-neutral-950/40 z-10" />
      <div class="absolute inset-0 bg-gradient-to-b from-neutral-950/30 via-transparent to-neutral-950 z-20" />
      <!-- Placeholder for Hero Image -->
      <div class="w-full h-full bg-neutral-900" />
      <UiBgStars />
    </div>

    <!-- Content -->
    <div class="relative z-30 text-center px-4 max-w-5xl mx-auto">
      <div :class="{ 'animate-fade-in-up': animationsEnabled }">
        <UBadge
          variant="outline"
          color="primary"
          class="mb-6 px-4 py-1 text-sm tracking-widest uppercase backdrop-blur-md border-primary/30"
        >
          {{ t("home.hero.badge") }}
        </UBadge>

        <h1 class="text-5xl md:text-7xl lg:text-8xl font-display font-bold text-white mb-6 leading-tight text-glow">
          <span class="block">{{ t("home.hero.title_line1") }}</span>
          <span class="block texto-cobre">{{ t("home.hero.title_line2") }}</span>
        </h1>

        <p
          class="text-xl md:text-2xl text-neutral-200 mb-10 max-w-2xl mx-auto font-light"
          :class="{ 'animate-fade-in-up delay-300': animationsEnabled }"
        >
          {{ t("home.hero.subtitle") }}
        </p>

        <div
          class="flex flex-col sm:flex-row gap-4 justify-center items-center"
          :class="{ 'animate-fade-in-up delay-500': animationsEnabled }"
        >
          <UButton
            to="/tours"
            size="xl"
            color="primary"
            variant="solid"
            class="px-8 py-3 text-lg font-bold min-w-[200px] justify-center cobre-glow hover:scale-105 transition-transform"
          >
            {{ t("home.hero.cta_primary") }}
          </UButton>
          <UButton
            to="/contact"
            size="xl"
            variant="ghost"
            color="white"
            class="px-8 py-3 text-lg min-w-[200px] justify-center backdrop-blur-sm hover:bg-white/10 border border-white/20"
          >
            {{ t("home.hero.cta_secondary") }}
          </UButton>
        </div>
      </div>

      <!-- Quick Features -->
      <div
        class="mt-16 grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-8"
        :class="{ 'animate-fade-in-up delay-700': animationsEnabled }"
      >
        <div v-for="feature in quickFeatures" :key="feature.label" class="flex flex-col items-center">
          <div class="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center mb-2 border border-primary/20">
            <UIcon :name="feature.icon" class="w-6 h-6 text-primary" />
          </div>
          <span class="text-sm text-neutral-200 uppercase tracking-wider">{{ feature.label }}</span>
        </div>
      </div>
    </div>

    <!-- Scroll Indicator -->
    <div class="absolute bottom-8 left-1/2 -translate-x-1/2 z-30 animate-bounce">
      <UButton
        variant="ghost"
        color="white"
        icon="i-lucide-chevron-down"
        class="rounded-full p-2 hover:bg-white/10"
        aria-label="Scroll down"
        @click="scrollToContent"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
const { t } = useI18n()

const quickFeatures = computed(() => [
  { icon: 'i-lucide-telescope', label: t('home.hero.features.proTelescopes') },
  { icon: 'i-lucide-users', label: t('home.hero.features.expertGuides') },
  { icon: 'i-lucide-star', label: t('home.hero.features.clearSkies') },
  { icon: 'i-lucide-shield-check', label: t('home.hero.features.freeCancellation') }
])

// Defer animations until first user interaction for better LCP
const animationsEnabled = ref(false)

onMounted(() => {
  // Enable animations on first user interaction
  const enableAnimations = () => {
    animationsEnabled.value = true
    // Clean up listeners after first interaction
    window.removeEventListener('scroll', enableAnimations, { passive: true } as any)
    window.removeEventListener('mousemove', enableAnimations, { passive: true } as any)
    window.removeEventListener('touchstart', enableAnimations, { passive: true } as any)
    window.removeEventListener('click', enableAnimations, { passive: true } as any)
  }

  // Listen for ANY user interaction
  window.addEventListener('scroll', enableAnimations, { passive: true })
  window.addEventListener('mousemove', enableAnimations, { passive: true })
  window.addEventListener('touchstart', enableAnimations, { passive: true })
  window.addEventListener('click', enableAnimations, { passive: true })

  // Fallback: Enable after 2s if no interaction (for users reading content)
  const fallbackTimer = setTimeout(() => {
    if (!animationsEnabled.value) {
      animationsEnabled.value = true
    }
  }, 2000)

  // Cleanup on unmount
  onUnmounted(() => {
    clearTimeout(fallbackTimer)
    window.removeEventListener('scroll', enableAnimations)
    window.removeEventListener('mousemove', enableAnimations)
    window.removeEventListener('touchstart', enableAnimations)
    window.removeEventListener('click', enableAnimations)
  })
})

function scrollToContent() {
  window.scrollTo({
    top: window.innerHeight,
    behavior: 'smooth'
  })
}
</script>

<style scoped>
/* Deferred animations for LCP optimization */
/* Animations only trigger on first user interaction, not on load */

.animate-fade-in-up {
  animation: fadeInUp 0.8s ease-out forwards;
  will-change: transform, opacity;
}

.delay-300 {
  animation-delay: 200ms;
}

.delay-500 {
  animation-delay: 400ms;
}

.delay-700 {
  animation-delay: 600ms;
}

h1 {
  animation: fadeInUp 0.8s ease-out;
  will-change: transform, opacity;
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

@media (prefers-reduced-motion: reduce) {
  .animate-fade-in-up,
  h1 {
    animation: none;
    opacity: 1;
    transform: none;
  }
}
</style>
