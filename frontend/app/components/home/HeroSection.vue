<template>
  <section
    class="relative w-full h-[calc(100dvh-5rem)] overflow-hidden bg-neutral-950"
  >
    <!-- 1. BACKGROUND -->
    <div class="absolute inset-0 z-0 pointer-events-none">
      <NuxtImg
        src="https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/fac678c9-7461-4559-bc59-daf110182709.webp"
        alt="Cielo de Atacama"
        class="w-full h-full object-cover object-bottom scale-105 animate-slow-zoom"
        placeholder
      />
      <div
        class="absolute inset-0 bg-gradient-to-b from-neutral-950/70 via-transparent to-neutral-950/30 z-10"
      />
      <ClientOnly>
        <LazyUiBgStars class="z-0 opacity-60" />
      </ClientOnly>
    </div>

    <!-- 2. MAIN LAYOUT -->
    <div
      class="relative z-30 w-full h-full max-w-6xl mx-auto px-4 flex flex-col items-center"
    >
      <!-- A. BADGE (Posición Absoluta Superior) -->
      <div
        class="absolute top-6 sm:top-12 left-0 right-0 flex justify-center animate-fade-in-up z-50 mx-2"
      >
        <div
          class="inline-flex items-center gap-2 px-3 py-1.5 rounded-full border border-primary/20 bg-neutral-950/50 backdrop-blur-md shadow-[0_0_15px_-3px_rgba(var(--ui-primary),0.2)]"
        >
          <UIcon
            name="i-lucide-sparkles"
            class="w-3 h-3 text-primary animate-pulse"
          />
          <span
            class="text-[10px] sm:text-xs font-bold text-primary tracking-widest uppercase"
          >
            {{ t("home.hero.badge") }}
          </span>
        </div>
      </div>

      <div
        class="flex-1 w-full flex flex-col justify-center items-center pb-10 z-40"
      >
        <div class="text-center w-full animate-fade-in-up">
          <!-- TITULO -->
          <h1
            class="font-display font-bold text-white drop-shadow-2xl tracking-tighter leading-none"
          >
            <!-- Texto responsivo pero controlado en desktop -->
            <span
              class="block text-[10vw] sm:text-7xl lg:text-8xl text-neutral-100 whitespace-nowrap"
            >
              {{ t("home.hero.title_line1") }}
            </span>

            <span
              class="block mt-1 sm:mt-3 text-[8.5vw] sm:text-7xl lg:text-8xl text-transparent bg-clip-text bg-gradient-to-r from-primary-400 via-primary-300 to-primary-500 whitespace-nowrap pb-2"
            >
              {{ t("home.hero.title_line2") }}
            </span>
          </h1>

          <!-- SUBTITULO -->
          <p
            class="mt-5 sm:mt-8 mb-10 sm:mb-10 text-sm sm:text-xl text-neutral-300 max-w-xs sm:max-w-2xl mx-auto font-light leading-relaxed text-balance px-2"
          >
            {{ t("home.hero.subtitle") }}
          </p>
        </div>

        <!-- BOTONES -->
        <div
          class="flex flex-row gap-3 sm:gap-4 w-auto justify-center animate-fade-in-delayed"
        >
          <UButton
            :to="localePath('/tours')"
            size="md"
            color="primary"
            variant="solid"
            class="w-auto justify-center px-6 py-2.5 sm:px-8 sm:py-3.5 text-xs sm:text-base font-bold rounded-full shadow-[0_0_20px_-5px_var(--ui-primary)] min-w-[130px] sm:min-w-[160px] transition-transform hover:scale-105"
            trailing-icon="i-lucide-arrow-right"
          >
            {{ t("home.hero.cta_primary") }}
          </UButton>

          <UButton
            :to="localePath('/contact')"
            size="md"
            variant="ghost"
            color="neutral"
            class="w-auto justify-center px-6 py-2.5 sm:px-8 sm:py-3.5 text-xs sm:text-base font-medium rounded-full text-neutral-200 border border-white/10 bg-white/5 min-w-[100px] sm:min-w-[140px] hover:bg-white/10"
          >
            {{ t("home.hero.cta_secondary") }}
          </UButton>
        </div>
      </div>

      <!-- C. DOCKER (Siempre abajo) -->
      <div
        class="absolute bottom-4 sm:bottom-8 left-0 right-0 flex justify-center animate-fade-in-delayed z-50 px-4"
      >
        <div class="max-w-md sm:max-w-lg">
          <UiDock
            :magnification="30"
            :distance="120"
            class="w-full justify-between"
          >
            <UiDockIcon :tooltip="t('home.hero.tooltips.proTelescopes')">
              <template #default="{ size }">
                <UIcon
                  name="i-heroicons-sparkles"
                  class="text-primary"
                  :style="{
                    width: size * 0.55 + 'px',
                    height: size * 0.55 + 'px'
                  }"
                />
              </template>
            </UiDockIcon>
            <UiDockIcon :tooltip="t('home.hero.tooltips.expertGuides')">
              <template #default="{ size }">
                <UIcon
                  name="i-heroicons-user-group"
                  class="text-primary"
                  :style="{
                    width: size * 0.55 + 'px',
                    height: size * 0.55 + 'px'
                  }"
                />
              </template>
            </UiDockIcon>
            <UiDockIcon :tooltip="t('home.hero.tooltips.clearSkies')">
              <template #default="{ size }">
                <UIcon
                  name="i-heroicons-moon"
                  class="text-primary"
                  :style="{
                    width: size * 0.55 + 'px',
                    height: size * 0.55 + 'px'
                  }"
                />
              </template>
            </UiDockIcon>
            <UiDockIcon :tooltip="t('home.hero.tooltips.freeCancellation')">
              <template #default="{ size }">
                <UIcon
                  name="i-heroicons-shield-check"
                  class="text-primary"
                  :style="{
                    width: size * 0.55 + 'px',
                    height: size * 0.55 + 'px'
                  }"
                />
              </template>
            </UiDockIcon>
          </UiDock>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
const { t } = useI18n()
const localePath = useLocalePath()
</script>
