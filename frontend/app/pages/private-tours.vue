<script setup lang="ts">
const { t } = useI18n()
const toast = useToast()
const config = useRuntimeConfig()

useSeoMeta({
  title: 'Tours Privados - Northern Chile',
  description: 'Diseña tu experiencia astronómica personalizada. Tours privados a medida con guías expertos en San Pedro de Atacama.',
  ogTitle: 'Tours Privados - Northern Chile',
  ogDescription: 'Experiencias astronómicas personalizadas a medida',
  twitterCard: 'summary_large_image'
})

const state = reactive({
  fullName: '',
  email: '',
  phone: '',
  numberOfPeople: 2,
  preferredDate: '',
  preferredTime: 'evening',
  tourType: 'ASTRONOMICAL',
  specialRequests: '',
  budget: '',
  loading: false
})

const tourTypeOptions = [
  { label: 'Tour Astronómico', value: 'ASTRONOMICAL' },
  { label: 'Tour Cultural', value: 'CULTURAL' },
  { label: 'Tour de Aventura', value: 'ADVENTURE' },
  { label: 'Tour Fotográfico', value: 'PHOTOGRAPHY' },
  { label: 'Combinado', value: 'COMBINED' }
]

const timeOptions = [
  { label: 'Mañana (8:00 - 12:00)', value: 'morning' },
  { label: 'Tarde (14:00 - 18:00)', value: 'afternoon' },
  { label: 'Noche (20:00 - 00:00)', value: 'evening' },
  { label: 'Flexible', value: 'flexible' }
]

const benefits = [
  {
    icon: 'i-lucide-users',
    title: 'Experiencia Exclusiva',
    description: 'Tour diseñado específicamente para ti y tu grupo'
  },
  {
    icon: 'i-lucide-clock',
    title: 'Horarios Flexibles',
    description: 'Elige el horario que mejor se adapte a tu itinerario'
  },
  {
    icon: 'i-lucide-map',
    title: 'Itinerario Personalizado',
    description: 'Visita los lugares que más te interesan'
  },
  {
    icon: 'i-lucide-graduation-cap',
    title: 'Guía Exclusivo',
    description: 'Atención personalizada de nuestros expertos'
  },
  {
    icon: 'i-lucide-camera',
    title: 'Fotografía Profesional',
    description: 'Sesión fotográfica incluida en tours nocturnos'
  },
  {
    icon: 'i-lucide-sparkles',
    title: 'Experiencias Únicas',
    description: 'Acceso a lugares y momentos especiales'
  }
]

async function submitRequest() {
  state.loading = true
  try {
    const response = await $fetch(`${config.public.apiBase}/private-tour-requests`, {
      method: 'POST',
      credentials: 'include',
      body: {
        fullName: state.fullName,
        email: state.email,
        phone: state.phone,
        numberOfPeople: state.numberOfPeople,
        preferredDate: state.preferredDate,
        tourType: state.tourType,
        specialRequests: state.specialRequests,
        status: 'PENDING'
      }
    })

    toast.add({
      title: 'Solicitud enviada',
      description: 'Te contactaremos pronto para diseñar tu experiencia personalizada',
      color: 'success'
    })

    // Reset form
    state.fullName = ''
    state.email = ''
    state.phone = ''
    state.numberOfPeople = 2
    state.preferredDate = ''
    state.preferredTime = 'evening'
    state.tourType = 'ASTRONOMICAL'
    state.specialRequests = ''
    state.budget = ''
  } catch (error) {
    toast.add({
      title: 'Error',
      description: 'No se pudo enviar la solicitud. Intenta nuevamente.',
      color: 'error'
    })
  } finally {
    state.loading = false
  }
}
</script>

<template>
  <div>
    <!-- Hero Section -->
    <section class="relative py-20 bg-gradient-to-b from-neutral-900 to-neutral-800 text-white">
      <UContainer>
        <div class="max-w-3xl mx-auto text-center">
          <h1 class="text-4xl md:text-5xl font-bold mb-6">
            Tours Privados Personalizados
          </h1>
          <p class="text-xl text-neutral-300">
            Diseña tu experiencia astronómica ideal. Tours exclusivos adaptados a tus intereses y necesidades.
          </p>
        </div>
      </UContainer>
    </section>

    <!-- Benefits -->
    <section class="py-16 bg-white dark:bg-neutral-900">
      <UContainer>
        <h2 class="text-3xl font-bold text-neutral-900 dark:text-white mb-12 text-center">
          ¿Por qué elegir un tour privado?
        </h2>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          <div v-for="benefit in benefits" :key="benefit.title" class="flex gap-4">
            <div class="flex-shrink-0">
              <div class="w-12 h-12 rounded-lg bg-primary-100 dark:bg-primary-900/30 flex items-center justify-center">
                <UIcon :name="benefit.icon" class="w-6 h-6 text-primary-600 dark:text-primary-400" />
              </div>
            </div>
            <div>
              <h3 class="text-lg font-semibold text-neutral-900 dark:text-white mb-2">
                {{ benefit.title }}
              </h3>
              <p class="text-neutral-600 dark:text-neutral-400 text-sm">
                {{ benefit.description }}
              </p>
            </div>
          </div>
        </div>
      </UContainer>
    </section>

    <!-- Request Form -->
    <section class="py-16 bg-neutral-50 dark:bg-neutral-800/50">
      <UContainer>
        <div class="max-w-3xl mx-auto">
          <div class="text-center mb-12">
            <h2 class="text-3xl font-bold text-neutral-900 dark:text-white mb-4">
              Solicita tu Tour Privado
            </h2>
            <p class="text-neutral-600 dark:text-neutral-400">
              Completa el formulario y nos pondremos en contacto para diseñar tu experiencia perfecta
            </p>
          </div>

          <UCard>
            <form @submit.prevent="submitRequest" class="space-y-6">
              <!-- Personal Info -->
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2">
                    Nombre completo *
                  </label>
                  <UInput
                    v-model="state.fullName"
                    size="lg"
                    placeholder="Juan Pérez"
                    class="w-full"
                    required
                  />
                </div>

                <div>
                  <label class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2">
                    Email *
                  </label>
                  <UInput
                    v-model="state.email"
                    type="email"
                    size="lg"
                    placeholder="juan@email.com"
                    class="w-full"
                    required
                  />
                </div>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2">
                    Teléfono *
                  </label>
                  <UInput
                    v-model="state.phone"
                    size="lg"
                    placeholder="+56 9 1234 5678"
                    class="w-full"
                    required
                  />
                </div>

                <div>
                  <label class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2">
                    Número de personas *
                  </label>
                  <UInput
                    v-model.number="state.numberOfPeople"
                    type="number"
                    min="1"
                    max="20"
                    size="lg"
                    placeholder="2"
                    class="w-full"
                    required
                  />
                </div>
              </div>

              <!-- Tour Details -->
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2">
                    Fecha preferida *
                  </label>
                  <UInput
                    v-model="state.preferredDate"
                    type="date"
                    size="lg"
                    class="w-full"
                    required
                  />
                </div>

                <div>
                  <label class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2">
                    Horario preferido
                  </label>
                  <USelect
                    v-model="state.preferredTime"
                    :items="timeOptions"
                    option-attribute="label"
                    value-attribute="value"
                    size="lg"
                    class="w-full"
                  />
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2">
                  Tipo de tour *
                </label>
                <USelect
                  v-model="state.tourType"
                  :items="tourTypeOptions"
                  option-attribute="label"
                  value-attribute="value"
                  size="lg"
                  class="w-full"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2">
                  Presupuesto estimado (opcional)
                </label>
                <UInput
                  v-model="state.budget"
                  size="lg"
                  placeholder="$100.000 - $200.000 CLP"
                  class="w-full"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2">
                  Solicitudes especiales
                </label>
                <UTextarea
                  v-model="state.specialRequests"
                  :rows="4"
                  size="lg"
                  placeholder="Cuéntanos sobre intereses específicos, necesidades especiales, objetivos fotográficos, etc."
                  class="w-full"
                />
              </div>

              <div class="pt-4">
                <UButton
                  type="submit"
                  size="lg"
                  color="primary"
                  :loading="state.loading"
                  block
                >
                  {{ state.loading ? 'Enviando solicitud...' : 'Enviar solicitud' }}
                </UButton>
              </div>
            </form>
          </UCard>

          <!-- Contact Info -->
          <div class="mt-12 text-center">
            <p class="text-neutral-600 dark:text-neutral-400">
              Completa el formulario y nos pondremos en contacto contigo a la brevedad para diseñar tu experiencia perfecta
            </p>
          </div>
        </div>
      </UContainer>
    </section>

    <!-- Popular Private Tours -->
    <section class="py-16 bg-white dark:bg-neutral-900">
      <UContainer>
        <h2 class="text-3xl font-bold text-neutral-900 dark:text-white mb-12 text-center">
          Tours Privados Populares
        </h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-5xl mx-auto">
          <UCard>
            <div class="text-center p-4">
              <UIcon name="i-lucide-telescope" class="w-12 h-12 text-primary-600 dark:text-primary-400 mx-auto mb-4" />
              <h3 class="text-xl font-semibold text-neutral-900 dark:text-white mb-2">
                Astrofotografía Avanzada
              </h3>
              <p class="text-neutral-600 dark:text-neutral-400 text-sm mb-4">
                Sesión nocturna dedicada a capturar el cielo del Atacama con equipo profesional
              </p>
              <p class="text-2xl font-bold text-primary-600 dark:text-primary-400">
                Desde $250.000
              </p>
            </div>
          </UCard>

          <UCard>
            <div class="text-center p-4">
              <UIcon name="i-lucide-heart" class="w-12 h-12 text-primary-600 dark:text-primary-400 mx-auto mb-4" />
              <h3 class="text-xl font-semibold text-neutral-900 dark:text-white mb-2">
                Experiencia Romántica
              </h3>
              <p class="text-neutral-600 dark:text-neutral-400 text-sm mb-4">
                Cena bajo las estrellas con observación privada y fotografía de pareja
              </p>
              <p class="text-2xl font-bold text-primary-600 dark:text-primary-400">
                Desde $300.000
              </p>
            </div>
          </UCard>

          <UCard>
            <div class="text-center p-4">
              <UIcon name="i-lucide-users-round" class="w-12 h-12 text-primary-600 dark:text-primary-400 mx-auto mb-4" />
              <h3 class="text-xl font-semibold text-neutral-900 dark:text-white mb-2">
                Tour Familiar
              </h3>
              <p class="text-neutral-600 dark:text-neutral-400 text-sm mb-4">
                Experiencia educativa y divertida diseñada para niños y adultos
              </p>
              <p class="text-2xl font-bold text-primary-600 dark:text-primary-400">
                Desde $200.000
              </p>
            </div>
          </UCard>
        </div>
      </UContainer>
    </section>
  </div>
</template>
