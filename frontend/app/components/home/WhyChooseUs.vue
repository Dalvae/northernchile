<script setup lang="ts">
const { t } = useI18n()

const features = [
  {
    icon: 'i-lucide-telescope',
    titleKey: 'home.why_us.features.0.title',
    descriptionKey: 'home.why_us.features.0.description'
  },
  {
    icon: 'i-lucide-users',
    titleKey: 'home.why_us.features.1.title',
    descriptionKey: 'home.why_us.features.1.description'
  },
  {
    icon: 'i-lucide-star',
    titleKey: 'home.why_us.features.2.title',
    descriptionKey: 'home.why_us.features.2.description'
  },
  {
    icon: 'i-lucide-shield-check',
    titleKey: 'home.why_us.features.3.title',
    descriptionKey: 'home.why_us.features.3.description'
  }
]
</script>

<template>
  <section class="relative py-24 z-10 overflow-hidden">
    <div class="absolute inset-0 bg-neutral-900/50 backdrop-blur-sm z-0" />
    <UContainer class="relative z-10">
      <div class="text-center mb-16">
        <h2 class="text-4xl md:text-5xl font-display font-bold text-white mb-4 text-glow">
          {{ t("home.why_us_title") }}
        </h2>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
        <div
          v-for="feature in features"
          :key="feature.titleKey"
          class="relative overflow-hidden atacama-card p-8 rounded-2xl text-center group hover:bg-white/5 transition-colors duration-500 border border-white/10"
        >
          <!-- EFECTO: Meteoros animados de fondo -->
          <ClientOnly>
            <UiMeteors
              :count="12"
              class="opacity-60"
            />
          </ClientOnly>

          <!-- Contenido (z-10 para que esté encima de los meteoros) -->
          <div class="relative z-10">
            <div class="mb-6 inline-flex p-4 rounded-full bg-primary/10 group-hover:bg-primary/20 transition-colors cobre-glow">
              <UIcon
                :name="feature.icon"
                class="w-10 h-10 text-primary"
              />
            </div>
            <h3 class="text-2xl font-display font-bold text-white mb-4">
              {{ t(feature.titleKey) }}
            </h3>
            <p class="text-neutral-300 leading-relaxed">
              {{ t(feature.descriptionKey) }}
            </p>
          </div>
        </div>
      </div>
    </UContainer>
  </section>
</template>
