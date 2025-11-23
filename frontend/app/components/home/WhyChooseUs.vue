<script setup lang="ts">
const { t } = useI18n()

const features = [
  {
    icon: 'i-lucide-telescope',
    title: 'Equipamiento Profesional',
    description: 'Telescopios de alta gama y equipos de observación certificados'
  },
  {
    icon: 'i-lucide-users',
    title: 'Guías Expertos',
    description: 'Astrónomos y guías certificados con años de experiencia'
  },
  {
    icon: 'i-lucide-star',
    title: 'Cielos Únicos',
    description: 'El mejor lugar del mundo para observación astronómica'
  },
  {
    icon: 'i-lucide-shield-check',
    title: 'Cancelación Gratis',
    description: 'Cancela sin cargo hasta 24 horas antes'
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
          v-for="(feature, index) in features"
          :key="feature.title"
          class="relative overflow-hidden atacama-card p-8 rounded-2xl text-center group hover:bg-white/5 transition-colors duration-500 border border-white/10"
        >
          <!-- EFECTO: Visible siempre (borré opacity-0 y hover) -->
          <ClientOnly>
            <UiMeteors
              :count="10"
              class="opacity-50"
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
              {{ feature.title }}
            </h3>
            <p class="text-neutral-300 leading-relaxed">
              {{ feature.description }}
            </p>
          </div>
        </div>
      </div>
    </UContainer>
  </section>
</template>
