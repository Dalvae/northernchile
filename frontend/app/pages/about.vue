<script setup lang="ts">
const { t } = useI18n()

useSeoMeta({
  title: t('about.seo.title'),
  description: t('about.seo.description'),
  ogTitle: t('about.seo.og_title'),
  ogDescription: t('about.seo.og_description'),
  twitterCard: 'summary_large_image'
})

const teamMembers = [
  {
    name: 'Alex',
    role: t('about.team.alex.role'),
    bio: t('about.team.alex.bio'),
    image: 'https://ui-avatars.com/api/?name=Alex&background=10b981&color=fff',
    credentials: [
      { icon: 'i-lucide-award', text: t('about.team.alex.credentials.award') },
      {
        icon: 'i-lucide-mic',
        text: t('about.team.alex.credentials.voice_over')
      },
      {
        icon: 'i-lucide-building',
        text: t('about.team.alex.credentials.collaborator')
      }
    ]
  },
  {
    name: 'David Gallardo',
    role: t('about.team.david.role'),
    bio: t('about.team.david.bio'),
    image:
      'https://ui-avatars.com/api/?name=David+Gallardo&background=10b981&color=fff',
    credentials: [
      {
        icon: 'i-lucide-camera',
        text: t('about.team.david.credentials.photographer')
      },
      {
        icon: 'i-lucide-palette',
        text: t('about.team.david.credentials.art_teacher')
      },
      {
        icon: 'i-lucide-star',
        text: t('about.team.david.credentials.tour_guide')
      }
    ]
  }
]

const values = [
  {
    icon: 'i-lucide-sparkles',
    title: t('about.values.experience_title'),
    description: t('about.values.experience_description')
  },
  {
    icon: 'i-lucide-telescope',
    title: t('about.values.equipment_title'),
    description: t('about.values.equipment_description')
  },
  {
    icon: 'i-lucide-award',
    title: t('about.values.quality_title'),
    description: t('about.values.quality_description')
  },
  {
    icon: 'i-lucide-mountain',
    title: t('about.values.auditorium_title'),
    description: t('about.values.auditorium_description')
  }
]
</script>

<template>
  <div class="min-h-screen relative overflow-hidden">
    <!-- Background Gradient -->
    <div class="absolute inset-0 bg-gradient-to-b from-neutral-950 to-neutral-900 z-0" />
    <div class="estrellas-atacama absolute inset-0 z-0" />

    <!-- Hero Section -->
    <section class="relative pt-32 pb-20 z-10">
      <UContainer class="text-center">
        <h1 class="text-5xl md:text-7xl font-display font-bold text-neutral-50 dark:text-neutral-50 mb-8 text-glow">
          {{ t("about.title") || "Sobre Nosotros" }}
        </h1>
        <p class="text-xl md:text-2xl text-neutral-300 dark:text-neutral-300 max-w-3xl mx-auto font-light leading-relaxed">
          {{ t("about.subtitle") || "Conectando la tierra con el cielo en el corazón del desierto más árido del mundo." }}
        </p>
      </UContainer>
    </section>

    <!-- Nuestra Historia -->
    <section class="relative py-20 z-10">
      <UContainer>
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 items-start">
          <!-- Contenido de texto -->
          <div class="space-y-6">
            <h2 class="text-3xl md:text-4xl font-display font-bold text-neutral-50 dark:text-neutral-50 mb-8">
              {{ t("about.historyTitle") }}
            </h2>
            <div class="space-y-5 text-neutral-300 dark:text-neutral-300 text-base md:text-lg leading-relaxed">
              <p class="pl-4 border-l-4 border-primary-500/50 dark:border-primary-400/50">
                {{ t("about.historyP1") }}
              </p>
              <p>{{ t("about.historyP2") }}</p>
              <p>{{ t("about.historyP3") }}</p>
              <p>{{ t("about.historyP4") }}</p>
            </div>
          </div>

          <!-- Área visual - Grid de imágenes/elementos -->
          <div class="grid grid-cols-2 gap-4 h-full">
            <!-- Imagen destacada grande -->
            <div class="col-span-2 atacama-card rounded-xl overflow-hidden h-64 relative group">
              <div class="absolute inset-0 bg-gradient-to-br from-primary-900/30 via-neutral-900/50 to-tertiary-900/30 flex items-center justify-center">
                <div class="text-center space-y-3">
                  <UIcon name="i-lucide-telescope" class="w-16 h-16 text-primary-400 mx-auto cobre-glow" />
                  <p class="text-neutral-200 font-medium text-lg">Equipamiento Profesional</p>
                  <p class="text-neutral-400 text-sm px-4">4 telescopios de alta gama</p>
                </div>
              </div>
            </div>

            <!-- Tarjeta premio -->
            <div class="atacama-card rounded-xl p-6 relative group hover:bg-neutral-100/5 transition-all">
              <div class="flex flex-col items-center justify-center h-full space-y-3">
                <UIcon name="i-lucide-award" class="w-12 h-12 text-primary-500 dark:text-primary-400 cobre-glow" />
                <p class="text-neutral-200 text-center text-sm font-medium">Mejor Tour de Atacama</p>
              </div>
            </div>

            <!-- Tarjeta experiencia -->
            <div class="atacama-card rounded-xl p-6 relative group hover:bg-neutral-100/5 transition-all">
              <div class="flex flex-col items-center justify-center h-full space-y-3">
                <div class="text-4xl font-bold text-primary-500 dark:text-primary-400">15</div>
                <p class="text-neutral-200 text-center text-sm font-medium">Años de Experiencia</p>
              </div>
            </div>
          </div>
        </div>
      </UContainer>
    </section>

    <!-- Nuestros Valores -->
    <section class="relative py-20 z-10">
      <UContainer>
        <div class="text-center mb-16">
          <h2 class="text-3xl md:text-4xl font-display font-bold text-neutral-50 dark:text-neutral-50 text-glow">
            {{ t("about.valuesTitle") }}
          </h2>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <div class="atacama-card p-6 rounded-xl hover:bg-neutral-100/5 dark:hover:bg-neutral-100/5 transition-colors group">
            <div class="w-14 h-14 rounded-full bg-primary-500/10 dark:bg-primary-500/10 flex items-center justify-center mb-6 cobre-glow group-hover:scale-110 transition-transform">
              <UIcon name="i-lucide-sparkles" class="w-7 h-7 text-primary-500 dark:text-primary-400" />
            </div>
            <h3 class="text-lg font-bold text-neutral-50 dark:text-neutral-50 mb-3">
              {{ t("about.values.experience_title") }}
            </h3>
            <p class="text-neutral-400 dark:text-neutral-400 text-sm">
              {{ t("about.values.experience_description") }}
            </p>
          </div>

          <div class="atacama-card p-6 rounded-xl hover:bg-neutral-100/5 dark:hover:bg-neutral-100/5 transition-colors group">
            <div class="w-14 h-14 rounded-full bg-primary-500/10 dark:bg-primary-500/10 flex items-center justify-center mb-6 cobre-glow group-hover:scale-110 transition-transform">
              <UIcon name="i-lucide-telescope" class="w-7 h-7 text-primary-500 dark:text-primary-400" />
            </div>
            <h3 class="text-lg font-bold text-neutral-50 dark:text-neutral-50 mb-3">
              {{ t("about.values.equipment_title") }}
            </h3>
            <p class="text-neutral-400 dark:text-neutral-400 text-sm">
              {{ t("about.values.equipment_description") }}
            </p>
          </div>

          <div class="atacama-card p-6 rounded-xl hover:bg-neutral-100/5 dark:hover:bg-neutral-100/5 transition-colors group">
            <div class="w-14 h-14 rounded-full bg-primary-500/10 dark:bg-primary-500/10 flex items-center justify-center mb-6 cobre-glow group-hover:scale-110 transition-transform">
              <UIcon name="i-lucide-award" class="w-7 h-7 text-primary-500 dark:text-primary-400" />
            </div>
            <h3 class="text-lg font-bold text-neutral-50 dark:text-neutral-50 mb-3">
              {{ t("about.values.quality_title") }}
            </h3>
            <p class="text-neutral-400 dark:text-neutral-400 text-sm">
              {{ t("about.values.quality_description") }}
            </p>
          </div>

          <div class="atacama-card p-6 rounded-xl hover:bg-neutral-100/5 dark:hover:bg-neutral-100/5 transition-colors group">
            <div class="w-14 h-14 rounded-full bg-primary-500/10 dark:bg-primary-500/10 flex items-center justify-center mb-6 cobre-glow group-hover:scale-110 transition-transform">
              <UIcon name="i-lucide-mountain" class="w-7 h-7 text-primary-500 dark:text-primary-400" />
            </div>
            <h3 class="text-lg font-bold text-neutral-50 dark:text-neutral-50 mb-3">
              {{ t("about.values.auditorium_title") }}
            </h3>
            <p class="text-neutral-400 dark:text-neutral-400 text-sm">
              {{ t("about.values.auditorium_description") }}
            </p>
          </div>
        </div>
      </UContainer>
    </section>

    <!-- Nuestro Equipo -->
    <section class="relative py-20 z-10">
      <UContainer>
        <div class="text-center mb-16">
          <h2 class="text-3xl md:text-4xl font-display font-bold text-neutral-50 dark:text-neutral-50 text-glow">
            {{ t("about.teamTitle") || "Nuestro Equipo" }}
          </h2>
        </div>

        <div class="max-w-5xl mx-auto space-y-8">
          <div
            v-for="member in teamMembers"
            :key="member.name"
            class="atacama-card p-8 rounded-xl backdrop-blur-sm hover:bg-neutral-100/5 dark:hover:bg-neutral-100/5 transition-all group"
          >
            <div class="flex flex-col md:flex-row items-center gap-8">
              <img
                :src="member.image"
                :alt="member.name"
                class="w-40 h-40 rounded-full flex-shrink-0 border-4 border-primary-500/30 dark:border-primary-400/30 group-hover:border-primary-500/50 dark:group-hover:border-primary-400/50 transition-colors"
              >
              <div class="flex-1 text-center md:text-left">
                <h3 class="text-2xl md:text-3xl font-bold text-neutral-50 dark:text-neutral-50 mb-2">
                  {{ member.name }}
                </h3>
                <p class="text-primary-500 dark:text-primary-400 font-medium text-lg md:text-xl mb-4">
                  {{ member.role }}
                </p>
                <p class="text-neutral-300 dark:text-neutral-300 mb-6 text-base md:text-lg leading-relaxed">
                  {{ member.bio }}
                </p>
                <div class="space-y-3">
                  <div
                    v-for="credential in member.credentials"
                    :key="credential.text"
                    class="flex items-start gap-3 justify-center md:justify-start text-neutral-400 dark:text-neutral-400 hover:text-neutral-300 dark:hover:text-neutral-300 transition-colors"
                  >
                    <UIcon :name="credential.icon" class="w-5 h-5 text-primary-500 dark:text-primary-400 flex-shrink-0 mt-0.5" />
                    <span class="text-sm md:text-base">{{ credential.text }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </UContainer>
    </section>

    <!-- CTA Final -->
    <section class="relative py-24 z-10">
      <div class="absolute inset-0 atacama-gradient opacity-50 z-0" />
      <UContainer class="relative z-10 text-center">
        <h2 class="text-4xl md:text-5xl font-display font-bold text-neutral-50 dark:text-neutral-50 mb-4 text-glow">
          {{ t("about.cta.title") }}
        </h2>
        <p class="text-xl text-neutral-300 dark:text-neutral-300 mb-8 max-w-2xl mx-auto">
          {{ t("about.cta.subtitle") }}
        </p>
        <UButton
          to="/tours"
          size="xl"
          color="primary"
          class="px-12 py-4 text-lg font-bold cobre-glow hover:scale-105 transition-transform"
        >
          {{ t("about.cta.button") }}
        </UButton>
      </UContainer>
    </section>
  </div>
</template>
