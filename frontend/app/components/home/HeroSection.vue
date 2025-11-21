<template>
  <section
    class="relative h-screen min-h-[750px] flex items-center justify-center overflow-hidden"
  >
    <!-- 1. BACKGROUND LAYERS -->
    <div class="absolute inset-0 z-0">
      <NuxtImg
        src="/images/hero-bg.jpg"
        alt="Cielo de Atacama"
        class="w-full h-full object-cover opacity-50 scale-105 animate-slow-zoom"
        placeholder
      />
      <!-- Gradiente de fondo -->
      <div
        class="absolute inset-0 bg-gradient-to-b from-neutral-950/60 via-neutral-950/20 to-neutral-950 z-10"
      />

      <ClientOnly>
        <LazyUiBgStars class="z-0 opacity-60" />
      </ClientOnly>
    </div>

    <!-- 2. MAIN CONTENT -->
    <div
      class="relative z-30 w-full max-w-6xl mx-auto px-4 flex flex-col items-center justify-center h-full"
    >
      <!-- Hero Text Group -->
      <div class="text-center space-y-6 animate-fade-in-up w-full">
        <!-- Badge (Rounded-lg en vez de full) -->
        <div class="flex justify-center mb-4">
          <div
            class="inline-flex items-center gap-2 px-3 py-1 rounded-lg border border-primary/30 bg-primary/5 backdrop-blur-sm"
          >
            <UIcon name="i-lucide-sparkles" class="w-3.5 h-3.5 text-primary" />
            <span
              class="text-xs font-semibold text-primary tracking-widest uppercase"
            >
              {{ t("home.hero.badge") }}
            </span>
          </div>
        </div>

        <!-- Título -->
        <h1
          class="text-5xl md:text-7xl lg:text-8xl font-display font-bold text-white leading-[0.9] drop-shadow-2xl tracking-tight"
        >
          <span class="block text-neutral-100">{{
            t("home.hero.title_line1")
          }}</span>
          <span
            class="block mt-2 text-transparent bg-clip-text bg-gradient-to-r from-primary-400 via-primary-300 to-primary-500 pb-2"
          >
            {{ t("home.hero.title_line2") }}
          </span>
        </h1>

        <p
          class="text-lg md:text-xl text-neutral-300 max-w-2xl mx-auto font-light leading-relaxed text-balance mt-4"
        >
          {{ t("home.hero.subtitle") }}
        </p>

        <!-- Botones (Rounded-xl) -->
        <div
          class="flex flex-col sm:flex-row gap-4 justify-center items-center pt-8 pb-12"
        >
          <UButton
            :to="localePath('/tours')"
            size="xl"
            color="primary"
            variant="solid"
            class="px-8 py-3.5 text-base font-bold rounded-xl shadow-[0_0_25px_-5px_var(--ui-primary)] hover:shadow-[0_0_35px_-5px_var(--ui-primary)] hover:scale-105 transition-all duration-300"
            trailing-icon="i-lucide-arrow-right"
          >
            {{ t("home.hero.cta_primary") }}
          </UButton>

          <UButton
            :to="localePath('/contact')"
            size="xl"
            variant="ghost"
            color="white"
            class="px-8 py-3.5 text-base font-medium rounded-xl hover:bg-white/5 text-neutral-200 hover:text-white transition-all border border-white/10 hover:border-white/30"
          >
            {{ t("home.hero.cta_secondary") }}
          </UButton>
        </div>

        <!-- 3. FEATURES (Brillantes y definidos) -->
        <div
          class="w-full max-w-4xl mx-auto pt-8 border-t border-white/10 animate-fade-in-delayed"
        >
          <div class="grid grid-cols-2 md:grid-cols-4 gap-6 md:gap-4">
            <div
              v-for="(feature, index) in quickFeatures"
              :key="feature.label"
              class="flex flex-col items-center justify-center gap-3 group cursor-default"
            >
              <!-- Ícono: Contenedor rounded-xl + Color Primario Brillante -->
              <div
                class="p-3 rounded-xl bg-white/5 border border-white/10 group-hover:border-primary/50 group-hover:bg-primary/10 transition-all duration-300"
              >
                <UIcon
                  :name="feature.icon"
                  class="w-6 h-6 text-primary group-hover:scale-110 transition-transform duration-300"
                />
              </div>

              <!-- Texto más claro (neutral-300 -> 200) -->
              <span
                class="text-xs font-medium text-neutral-300 uppercase tracking-wider group-hover:text-white transition-colors"
              >
                {{ feature.label }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Scroll Indicator -->
    <div
      class="absolute bottom-6 left-1/2 -translate-x-1/2 z-30 opacity-50 animate-bounce pointer-events-none"
    >
      <UIcon name="i-lucide-chevron-down" class="w-6 h-6 text-primary" />
    </div>
  </section>
</template>

<script setup lang="ts">
const { t } = useI18n();
const localePath = useLocalePath();

const quickFeatures = computed(() => [
  { icon: "i-lucide-telescope", label: t("home.hero.features.proTelescopes") },
  { icon: "i-lucide-users", label: t("home.hero.features.expertGuides") },
  { icon: "i-lucide-star", label: t("home.hero.features.clearSkies") },
  {
    icon: "i-lucide-shield-check",
    label: t("home.hero.features.freeCancellation"),
  },
]);
</script>

<style scoped>
@keyframes slow-zoom {
  0% {
    transform: scale(1);
  }
  100% {
    transform: scale(1.1);
  }
}
.animate-slow-zoom {
  animation: slow-zoom 25s linear infinite alternate;
}

@keyframes fade-in-up {
  0% {
    opacity: 0;
    transform: translateY(20px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}
.animate-fade-in-up {
  animation: fade-in-up 1s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
}

.animate-fade-in-delayed {
  opacity: 0;
  animation: fade-in-up 1s cubic-bezier(0.2, 0.8, 0.2, 1) 0.4s forwards;
}
</style>
