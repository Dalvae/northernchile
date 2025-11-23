<script setup lang="ts">
const { t } = useI18n()
const localePath = useLocalePath()

const items = computed(() => [
  {
    title: t('home.experience.astro_title'),
    description: t('home.experience.astro_desc'),
    icon: 'i-lucide-stars',
    image: '/images/astro-experience.jpg',
    color: 'text-primary-400',
    bg: 'bg-primary-500/10'
  },
  {
    title: t('home.experience.geo_title'),
    description: t('home.experience.geo_desc'),
    icon: 'i-lucide-mountain',
    image: '/images/geo-experience.jpg',
    color: 'text-tertiary-400',
    bg: 'bg-tertiary-500/10'
  }
])
</script>

<template>
  <section class="relative py-20 sm:py-32 overflow-hidden">
    <!-- Background Elements -->
    <div class="absolute inset-0 bg-neutral-950 z-0" />
    <div
      class="absolute top-0 left-0 w-full h-px bg-gradient-to-r from-transparent via-primary/50 to-transparent"
    />

    <UContainer class="relative z-10">
      <!-- HEADER SECTION -->
      <div class="text-center mb-16 sm:mb-24 px-4">
        <span
          class="inline-block py-1.5 px-4 rounded-full bg-primary/10 text-primary text-xs sm:text-sm font-bold mb-6 tracking-widest uppercase border border-primary/20"
        >
          {{ t("home.experience.badge") }}
        </span>

        <h2
          class="text-4xl md:text-6xl font-display font-bold text-white mb-6 text-glow leading-tight"
        >
          {{ t("home.experience.title") }}
        </h2>

        <!-- text-balance: Hace que el texto centrado se vea simétrico y profesional -->
        <p
          class="text-lg sm:text-xl text-neutral-300 max-w-3xl mx-auto leading-relaxed text-balance"
        >
          {{ t("home.experience.subtitle") }}
        </p>
      </div>

      <!-- CONTENT LOOPS -->
      <div class="space-y-20 sm:space-y-32">
        <div
          v-for="(item, index) in items"
          :key="index"
          class="flex flex-col lg:flex-row gap-10 lg:gap-24 items-center"
          :class="{ 'lg:flex-row-reverse': index % 2 !== 0 }"
        >
          <!-- TEXT CONTENT -->
          <!-- px-6 en móvil le da el aire lateral que pedías -->
          <div class="flex-1 space-y-6 text-center lg:text-left px-6 lg:px-0">
            <div
              class="inline-flex p-4 rounded-2xl mb-2 transition-transform hover:scale-110 duration-300 shadow-lg shadow-white/5"
              :class="item.bg"
            >
              <UIcon
                :name="item.icon"
                class="w-10 h-10"
                :class="item.color"
              />
            </div>

            <h3
              class="text-3xl md:text-5xl font-display font-bold text-white leading-tight"
            >
              {{ item.title }}
            </h3>

            <!-- leading-loose da más espacio entre líneas para que respire -->
            <p
              class="text-base sm:text-lg text-neutral-300 leading-loose text-balance lg:text-pretty"
            >
              {{ item.description }}
            </p>

            <div class="pt-4 pb-4 lg:pb-0">
              <UButton
                variant="link"
                color="neutral"
                class="p-0 text-base sm:text-lg text-primary-400 hover:text-primary transition-colors group"
                :to="localePath('/about')"
              >
                {{ t("home.experience.learn_more") }}
                <UIcon
                  name="i-lucide-arrow-right"
                  class="ml-2 w-5 h-5 group-hover:translate-x-2 transition-transform"
                />
              </UButton>
            </div>
          </div>

          <!-- IMAGE -->
          <div class="flex-1 w-full px-4 lg:px-0">
            <div
              class="relative aspect-[4/3] rounded-3xl overflow-hidden group border border-white/10 bg-neutral-900 shadow-2xl shadow-black/50"
            >
              <NuxtImg
                :src="item.image"
                :alt="item.title"
                class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105 opacity-90 group-hover:opacity-100"
                format="webp"
                loading="lazy"
                placeholder
              />

              <!-- Gradiente sutil sobre la imagen -->
              <div
                class="absolute inset-0 bg-gradient-to-t from-neutral-950/60 via-transparent to-transparent z-10"
              />

              <!-- Linea decorativa -->
              <div class="absolute bottom-0 left-0 p-6 sm:p-8 z-20 w-full">
                <div
                  class="h-1 w-16 sm:w-24 rounded-full"
                  :class="item.color.replace('text-', 'bg-')"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </UContainer>
  </section>
</template>
