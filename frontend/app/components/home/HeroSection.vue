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
        <!-- Badge -->
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

        <!-- Botones -->
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

        <!-- 3. FEATURE DOCK (Magnético) -->
        <div
          class="w-full flex justify-center mt-12 animate-fade-in-delayed relative z-40"
        >
          <UiDock :magnification="30" :distance="120">
            <!-- Telescopios -->
            <UiDockIcon :tooltip="t('home.hero.tooltips.proTelescopes')">
              <template #default="{ size }">
                <UIcon
                  name="i-heroicons-sparkles"
                  class="text-primary"
                  :style="{ width: size * 0.55 + 'px', height: size * 0.55 + 'px' }"
                />
              </template>
            </UiDockIcon>

            <!-- Guías -->
            <UiDockIcon :tooltip="t('home.hero.tooltips.expertGuides')">
              <template #default="{ size }">
                <UIcon
                  name="i-heroicons-user-group"
                  class="text-primary"
                  :style="{ width: size * 0.55 + 'px', height: size * 0.55 + 'px' }"
                />
              </template>
            </UiDockIcon>

            <!-- Cielos -->
            <UiDockIcon :tooltip="t('home.hero.tooltips.clearSkies')">
              <template #default="{ size }">
                <UIcon
                  name="i-heroicons-moon"
                  class="text-primary"
                  :style="{ width: size * 0.55 + 'px', height: size * 0.55 + 'px' }"
                />
              </template>
            </UiDockIcon>

            <!-- Cancelación -->
            <UiDockIcon :tooltip="t('home.hero.tooltips.freeCancellation')">
              <template #default="{ size }">
                <UIcon
                  name="i-heroicons-shield-check"
                  class="text-primary"
                  :style="{ width: size * 0.55 + 'px', height: size * 0.55 + 'px' }"
                />
              </template>
            </UiDockIcon>
          </UiDock>
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

@keyframes ping-slow {
  0% {
    transform: scale(0.8);
    opacity: 0.8;
  }
  100% {
    transform: scale(2);
    opacity: 0;
  }
}
.animate-ping-slow {
  animation: ping-slow 3s cubic-bezier(0, 0, 0.2, 1) infinite;
}
</style>
