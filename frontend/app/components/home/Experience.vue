<script setup lang="ts">
// 1. Imports y Composables SIEMPRE al principio
const { t } = useI18n()
const localePath = useLocalePath()

// 2. Datos Computados (Reactivos)
// Usamos computed para que si cambia el idioma, el texto cambie
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
  <section class="relative py-24 overflow-hidden">
    <!-- Background Elements -->
    <div class="absolute inset-0 bg-neutral-950 z-0" />
    <div class="absolute top-0 left-0 w-full h-px bg-gradient-to-r from-transparent via-primary/50 to-transparent" />

    <UContainer class="relative z-10">
      <div class="text-center mb-20">
        <span class="inline-block py-1 px-3 rounded-full bg-primary/10 text-primary text-sm font-medium mb-4 tracking-wider uppercase">
          {{ t('home.experience.badge') }}
        </span>
        <h2 class="text-4xl md:text-5xl font-display font-bold text-white mb-6 text-glow">
          {{ t('home.experience.title') }}
        </h2>
        <p class="text-xl text-neutral-200 max-w-3xl mx-auto leading-relaxed">
          {{ t('home.experience.subtitle') }}
        </p>
      </div>

      <div class="space-y-24">
        <div
          v-for="(item, index) in items"
          :key="index"
          class="flex flex-col lg:flex-row gap-12 lg:gap-20 items-center"
          :class="{ 'lg:flex-row-reverse': index % 2 !== 0 }"
        >
          <!-- Content -->
          <div class="flex-1 space-y-6 text-center lg:text-left">
            <div
              class="inline-flex p-4 rounded-2xl mb-2 transition-transform hover:scale-110 duration-300"
              :class="item.bg"
            >
              <UIcon
                :name="item.icon"
                class="w-10 h-10"
                :class="item.color"
              />
            </div>

            <h3 class="text-3xl md:text-4xl font-display font-bold text-white">
              {{ item.title }}
            </h3>

            <p class="text-lg text-neutral-300 leading-relaxed">
              {{ item.description }}
            </p>

            <div class="pt-4">
              <UButton
                variant="link"
                color="neutral"
                class="p-0 text-lg text-white hover:text-primary transition-colors group"
                :to="localePath('/about')"
              >
                {{ t('home.experience.learn_more') }}
                <UIcon
                  name="i-lucide-arrow-right"
                  class="ml-2 w-5 h-5 group-hover:translate-x-1 transition-transform"
                />
              </UButton>
            </div>
          </div>

          <!-- Visual/Image -->
          <div class="flex-1 w-full">
            <div class="relative aspect-[4/3] rounded-2xl overflow-hidden group border border-white/10 bg-neutral-900">
              
              <NuxtImg
                :src="item.image"
                :alt="item.title"
                class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
                format="webp"
                loading="lazy"
                placeholder
                sizes="100vw sm:50vw md:600px"
              />

              <div class="absolute inset-0 bg-gradient-to-tr from-neutral-900/80 via-transparent to-transparent z-10 pointer-events-none" />

              <div class="absolute bottom-0 left-0 p-8 z-20 w-full pointer-events-none">
                <div
                  class="h-1 w-20 rounded-full mb-4"
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