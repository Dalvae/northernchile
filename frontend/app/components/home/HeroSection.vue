<template>
  <section class="relative h-screen min-h-[600px] flex items-center justify-center overflow-hidden">
    <!-- Background Image with Overlay -->
    <div class="absolute inset-0 z-0">
      <div class="absolute inset-0 bg-neutral-950/40 z-10" />
      <div class="absolute inset-0 bg-gradient-to-b from-neutral-950/30 via-transparent to-neutral-950 z-20" />
      <!-- Placeholder for Hero Image -->
      <div class="w-full h-full bg-neutral-900" />
      <!-- Lazy load BgStars - decorative, not critical for LCP -->
      <ClientOnly>
        <LazyUiBgStars />
      </ClientOnly>
    </div>

    <!-- Content -->
    <div class="relative z-30 text-center px-4 max-w-5xl mx-auto">
      <div>
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

        <p class="text-xl md:text-2xl text-neutral-200 mb-10 max-w-2xl mx-auto font-light">
          {{ t("home.hero.subtitle") }}
        </p>

        <div class="flex flex-col sm:flex-row gap-4 justify-center items-center">
          <UButton
            :to="localePath('/tours')"
            size="xl"
            color="primary"
            variant="solid"
            class="px-8 py-3 text-lg font-bold min-w-[200px] justify-center cobre-glow hover:scale-105 transition-transform"
          >
            {{ t("home.hero.cta_primary") }}
          </UButton>
          <UButton
            :to="localePath('/contact')"
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
      <div class="mt-16 grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-8">
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
const localePath = useLocalePath()

const quickFeatures = computed(() => [
  { icon: 'i-lucide-telescope', label: t('home.hero.features.proTelescopes') },
  { icon: 'i-lucide-users', label: t('home.hero.features.expertGuides') },
  { icon: 'i-lucide-star', label: t('home.hero.features.clearSkies') },
  { icon: 'i-lucide-shield-check', label: t('home.hero.features.freeCancellation') }
])

function scrollToContent() {
  window.scrollTo({
    top: window.innerHeight,
    behavior: 'smooth'
  })
}
</script>

