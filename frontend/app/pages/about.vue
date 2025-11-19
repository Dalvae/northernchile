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
        <h1 class="text-5xl md:text-7xl font-display font-bold text-white mb-8 text-glow">
          {{ t("about.title") || "Sobre Nosotros" }}
        </h1>
        <p class="text-xl md:text-2xl text-neutral-300 max-w-3xl mx-auto font-light leading-relaxed">
          {{ t("about.subtitle") || "Conectando la tierra con el cielo en el corazón del desierto más árido del mundo." }}
        </p>
      </UContainer>
    </section>

    <!-- Nuestra Historia -->
    <section class="relative py-20 z-10">
      <UContainer>
        <div class="atacama-card p-8 md:p-12 rounded-xl backdrop-blur-sm">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-12 items-center">
            <div class="space-y-6">
              <h2 class="text-3xl md:text-4xl font-display font-bold text-white">
                {{ t("about.history_title") || "Nuestra Historia" }}
              </h2>
              <div class="space-y-4 text-neutral-300 text-lg leading-relaxed">
                <p>
                  Nacimos bajo los cielos de San Pedro de Atacama con una misión clara: compartir la magia del universo combinando la ciencia astronómica con la cosmovisión ancestral de los pueblos andinos.
                </p>
                <p>
                  Northern Chile no es solo una agencia de tours; es un puente entre el pasado y el futuro, entre la tierra (Pachamama) y el cosmos.
                </p>
              </div>
            </div>
            <div class="relative h-80 rounded-xl overflow-hidden border border-white/10 shadow-2xl">
              <!-- Placeholder image -->
              <div class="absolute inset-0 bg-neutral-800 flex items-center justify-center">
                <UIcon name="i-lucide-mountain" class="w-20 h-20 text-neutral-700" />
              </div>
              <div class="absolute inset-0 bg-gradient-to-tr from-primary/20 to-transparent" />
            </div>
          </div>
        </div>
      </UContainer>
    </section>

    <!-- Nuestros Valores -->
    <section class="relative py-20 z-10">
      <UContainer>
        <div class="text-center mb-16">
          <h2 class="text-3xl md:text-4xl font-display font-bold text-white text-glow">
            {{ t("about.values_title") || "Nuestros Valores" }}
          </h2>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div class="atacama-card p-8 rounded-xl hover:bg-white/5 transition-colors group">
            <div class="w-14 h-14 rounded-full bg-primary/10 flex items-center justify-center mb-6 cobre-glow group-hover:scale-110 transition-transform">
              <UIcon name="i-lucide-heart-handshake" class="w-7 h-7 text-primary" />
            </div>
            <h3 class="text-xl font-bold text-white mb-3">Respeto Ancestral</h3>
            <p class="text-neutral-400">
              Honramos las tradiciones y la tierra que nos acoge, trabajando en armonía con las comunidades locales.
            </p>
          </div>

          <div class="atacama-card p-8 rounded-xl hover:bg-white/5 transition-colors group">
            <div class="w-14 h-14 rounded-full bg-primary/10 flex items-center justify-center mb-6 cobre-glow group-hover:scale-110 transition-transform">
              <UIcon name="i-lucide-graduation-cap" class="w-7 h-7 text-primary" />
            </div>
            <h3 class="text-xl font-bold text-white mb-3">Excelencia Científica</h3>
            <p class="text-neutral-400">
              Nuestros guías son expertos en astronomía, garantizando una experiencia educativa de primer nivel.
            </p>
          </div>

          <div class="atacama-card p-8 rounded-xl hover:bg-white/5 transition-colors group">
            <div class="w-14 h-14 rounded-full bg-primary/10 flex items-center justify-center mb-6 cobre-glow group-hover:scale-110 transition-transform">
              <UIcon name="i-lucide-sparkles" class="w-7 h-7 text-primary" />
            </div>
            <h3 class="text-xl font-bold text-white mb-3">Magia y Conexión</h3>
            <p class="text-neutral-400">
              Creamos momentos inolvidables que conectan a nuestros viajeros con la inmensidad del universo.
            </p>
          </div>
        </div>
      </UContainer>
    </section>

    <!-- Nuestro Equipo (Placeholder) -->
    <section class="relative py-20 z-10">
      <UContainer>
        <div class="text-center mb-16">
          <h2 class="text-3xl md:text-4xl font-display font-bold text-white text-glow">
            {{ t("about.team_title") || "Nuestro Equipo" }}
          </h2>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <div v-for="i in 4" :key="i" class="atacama-card p-6 rounded-xl text-center group">
            <div class="w-32 h-32 mx-auto rounded-full bg-neutral-800 mb-4 overflow-hidden border-2 border-primary/30 group-hover:border-primary transition-colors">
              <UIcon name="i-lucide-user" class="w-full h-full p-6 text-neutral-600" />
            </div>
            <h3 class="text-lg font-bold text-white">Guía Experto {{ i }}</h3>
            <p class="text-primary text-sm mb-2">Astrónomo</p>
            <p class="text-neutral-400 text-sm">
              Apasionado por las estrellas y la historia del desierto.
            </p>
          </div>
        </div>
      </UContainer>
    </section>

    <!-- CTA Final -->
    <section class="relative py-24 z-10">
      <div class="absolute inset-0 atacama-gradient opacity-50 z-0" />
      <UContainer class="relative z-10 text-center">
        <h2 class="text-4xl font-display font-bold text-white mb-8 text-glow">
          Únete a la Aventura
        </h2>
        <UButton
          to="/tours"
          size="xl"
          color="primary"
          class="px-12 py-4 text-lg font-bold cobre-glow hover:scale-105 transition-transform"
        >
          Ver Tours Disponibles
        </UButton>
      </UContainer>
    </section>
  </div>
</template>
