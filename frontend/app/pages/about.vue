<script setup lang="ts">
const { t } = useI18n()

useSeoMeta({
  title: 'Sobre Nosotros - Northern Chile',
  description: 'Conoce la historia y el equipo detrás de Northern Chile, tu guía experta en tours astronómicos en San Pedro de Atacama.',
  ogTitle: 'Sobre Nosotros - Northern Chile',
  ogDescription: 'Conoce la historia y el equipo detrás de Northern Chile',
  twitterCard: 'summary_large_image'
})

const teamMembers = [
  {
    name: 'Alex',
    role: 'Fundador y Guía Astronómico',
    bio: '15 años de trayectoria en turismo astronómico en Bolivia y San Pedro de Atacama. Premio al mejor tour de Atacama.',
    image: 'https://ui-avatars.com/api/?name=Alex&background=10b981&color=fff',
    credentials: [
      { icon: 'i-lucide-award', text: 'Ganador del premio al mejor tour de Atacama' },
      { icon: 'i-lucide-mic', text: 'Voz en off del tour astronómico en feria VIVA Antofagasta' },
      { icon: 'i-lucide-building', text: 'Colaborador del Museo del Meteorito' }
    ]
  },
  {
    name: 'David Gallardo',
    role: 'Fotógrafo y Profesor de Arte',
    bio: 'Especialista en fotografía astronómica y nocturna. Captura la magia del cielo atacameño combinando técnica profesional y visión artística.',
    image: 'https://ui-avatars.com/api/?name=David+Gallardo&background=10b981&color=fff',
    credentials: [
      { icon: 'i-lucide-camera', text: 'Fotógrafo profesional especializado en astrofotografía' },
      { icon: 'i-lucide-palette', text: 'Profesor de arte con enfoque en fotografía nocturna' },
      { icon: 'i-lucide-star', text: 'Guía en tours especializados de fin de semana' }
    ]
  }
]

const values = [
  {
    icon: 'i-lucide-sparkles',
    title: 'Experiencia de Conocimiento',
    description: 'No vendemos tours, vendemos un show educativo bajo las estrellas del desierto'
  },
  {
    icon: 'i-lucide-telescope',
    title: 'Equipamiento Profesional',
    description: '4 telescopios de alta gama y cámaras profesionales para una experiencia inolvidable'
  },
  {
    icon: 'i-lucide-award',
    title: 'Calidad Reconocida',
    description: 'Premiados como el mejor tour de Atacama y seleccionados entre los 9 mejores de San Pedro'
  },
  {
    icon: 'i-lucide-mountain',
    title: 'Auditorio Natural',
    description: 'Creamos un auditorio a cielo abierto donde conectas el cielo con el suelo del desierto'
  }
]
</script>

<template>
  <div>
    <!-- Hero Section -->
    <section class="relative py-20 bg-gradient-to-b from-neutral-900 to-neutral-800 text-white">
      <UContainer>
        <div class="max-w-3xl mx-auto text-center">
          <h1 class="text-4xl md:text-5xl font-bold mb-6">
            Sobre Northern Chile
          </h1>
          <p class="text-xl text-neutral-300">
            Conectando personas con el universo desde el desierto más claro del mundo
          </p>
        </div>
      </UContainer>
    </section>

    <!-- Nuestra Historia -->
    <section class="py-16 bg-white dark:bg-neutral-900">
      <UContainer>
        <div class="max-w-4xl mx-auto">
          <h2 class="text-3xl font-bold text-neutral-900 dark:text-white mb-8 text-center">
            Nuestra Historia
          </h2>
          <div class="prose prose-lg dark:prose-invert max-w-none">
            <p class="text-neutral-600 dark:text-neutral-400 leading-relaxed mb-6">
              Northern Chile nace de 15 años de pasión por el turismo astronómico en el desierto de Atacama y Bolivia.
              No vendemos simplemente un tour, vendemos una experiencia de conocimiento, un verdadero show bajo las estrellas.
            </p>
            <p class="text-neutral-600 dark:text-neutral-400 leading-relaxed mb-6">
              Creamos un <strong>"Auditorio a Cielo Abierto"</strong> donde nuestros visitantes se recuestan a los pies del desierto
              para conectar el cielo con el suelo. Es una experiencia natural que implica caminar por las dunas,
              equipados con telescopios profesionales (Celestron 8se, Skywatcher ecuatorial, Celestron 114 LSM, Seestar S50zvw)
              y cámaras para capturar la magia del momento.
            </p>
            <p class="text-neutral-600 dark:text-neutral-400 leading-relaxed mb-6">
              Fuimos galardonados con el <strong>premio al mejor tour de Atacama</strong> el año pasado, y nuestra agencia fue
              seleccionada entre las 9 mejores de San Pedro por emisarios turísticos. Nuestro fundador participó como voz en off
              del tour astronómico en la feria VIVA Antofagasta y colaboró con el Museo del Meteorito.
            </p>
            <p class="text-neutral-600 dark:text-neutral-400 leading-relaxed">
              Los cielos de Atacama son totalmente diferentes cada 6 meses, ofreciendo experiencias únicas según la época del año.
              Te invitamos a descubrir por qué el desierto de Atacama es considerado uno de los mejores lugares del mundo
              para la observación astronómica.
            </p>
          </div>
        </div>
      </UContainer>
    </section>

    <!-- Nuestros Valores -->
    <section class="py-16 bg-neutral-50 dark:bg-neutral-800/50">
      <UContainer>
        <h2 class="text-3xl font-bold text-neutral-900 dark:text-white mb-12 text-center">
          Nuestros Valores
        </h2>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          <div v-for="value in values" :key="value.title" class="text-center">
            <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-primary-100 dark:bg-primary-900/30 mb-4">
              <UIcon :name="value.icon" class="w-8 h-8 text-primary-600 dark:text-primary-400" />
            </div>
            <h3 class="text-xl font-semibold text-neutral-900 dark:text-white mb-2">
              {{ value.title }}
            </h3>
            <p class="text-neutral-600 dark:text-neutral-400">
              {{ value.description }}
            </p>
          </div>
        </div>
      </UContainer>
    </section>

    <!-- Nuestro Equipo -->
    <section class="py-16 bg-white dark:bg-neutral-900">
      <UContainer>
        <h2 class="text-3xl font-bold text-neutral-900 dark:text-white mb-12 text-center">
          Nuestro Equipo
        </h2>
        <div class="max-w-5xl mx-auto space-y-12">
          <div v-for="member in teamMembers" :key="member.name" class="flex flex-col md:flex-row items-center gap-8">
            <img
              :src="member.image"
              :alt="member.name"
              class="w-40 h-40 rounded-full flex-shrink-0"
            >
            <div class="flex-1 text-center md:text-left">
              <h3 class="text-2xl font-semibold text-neutral-900 dark:text-white mb-2">
                {{ member.name }}
              </h3>
              <p class="text-primary-600 dark:text-primary-400 font-medium text-lg mb-4">
                {{ member.role }}
              </p>
              <p class="text-neutral-600 dark:text-neutral-400 mb-4">
                {{ member.bio }}
              </p>
              <div class="space-y-2 text-sm text-neutral-600 dark:text-neutral-400">
                <p v-for="credential in member.credentials" :key="credential.text" class="flex items-center gap-2 justify-center md:justify-start">
                  <UIcon :name="credential.icon" class="w-4 h-4 text-primary-600" />
                  {{ credential.text }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </UContainer>
    </section>

    <!-- CTA Final -->
    <section class="py-16 bg-gradient-to-r from-primary-600 to-primary-700 text-white">
      <UContainer>
        <div class="max-w-3xl mx-auto text-center">
          <h2 class="text-3xl font-bold mb-4">
            ¿Listo para explorar el universo?
          </h2>
          <p class="text-xl mb-8 text-primary-100">
            Únete a nosotros en una aventura astronómica inolvidable
          </p>
          <UButton
            to="/tours"
            size="xl"
            color="white"
            icon="i-lucide-telescope"
          >
            Ver Nuestros Tours
          </UButton>
        </div>
      </UContainer>
    </section>
  </div>
</template>
