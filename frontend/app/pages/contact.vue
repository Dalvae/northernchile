<script setup lang="ts">
const { t } = useI18n()
const toast = useToast()

useSeoMeta({
  title: 'Contacto - Northern Chile',
  description:
    'Contáctanos para consultas sobre tours astronómicos, reservas especiales y más información sobre nuestros servicios.',
  ogTitle: 'Contacto - Northern Chile',
  ogDescription: 'Contáctanos para consultas sobre tours astronómicos',
  twitterCard: 'summary_large_image'
})

const state = reactive({
  name: '',
  email: '',
  phone: '',
  subject: '',
  message: '',
  loading: false
})

const contactInfo = [
  {
    icon: 'i-lucide-map-pin',
    title: 'Ubicación',
    details: ['San Pedro de Atacama', 'Región de Antofagasta, Chile']
  },
  {
    icon: 'i-lucide-phone',
    title: 'Teléfono',
    details: ['Disponible vía formulario', 'Te contactaremos a la brevedad']
  },
  {
    icon: 'i-lucide-mail',
    title: 'Email',
    details: ['info@northernchile.cl', 'reservas@northernchile.cl']
  },
  {
    icon: 'i-lucide-clock',
    title: 'Horarios',
    details: ['Lun - Dom: 9:00 - 20:00', 'Respuesta rápida por formulario']
  }
]

const faqs = [
  {
    question: '¿Necesito experiencia previa en astronomía?',
    answer:
      'No, nuestros tours están diseñados para todos los niveles. Nuestros guías explicarán todo lo necesario.'
  },
  {
    question: '¿Qué pasa si hay mal clima?',
    answer:
      'Monitoreamos constantemente las condiciones climáticas. Si se cancela por mal clima, ofrecemos reprogramación o reembolso completo.'
  },
  {
    question: '¿Los tours incluyen transporte?',
    answer:
      'Sí, todos nuestros tours incluyen transporte desde tu alojamiento en San Pedro de Atacama.'
  },
  {
    question: '¿Puedo hacer una reserva privada?',
    answer:
      'Sí, ofrecemos tours privados. Contáctanos para coordinar una experiencia personalizada.'
  }
]

async function submitForm() {
  state.loading = true
  try {
    // Simular envío (aquí iría la llamada real a API)
    await new Promise(resolve => setTimeout(resolve, 1500))

    toast.add({
      title: 'Mensaje enviado',
      description: 'Gracias por contactarnos. Te responderemos pronto.',
      color: 'success'
    })

    // Reset form
    state.name = ''
    state.email = ''
    state.phone = ''
    state.subject = ''
    state.message = ''
  } catch (error) {
    toast.add({
      title: 'Error',
      description: 'No se pudo enviar el mensaje. Intenta nuevamente.',
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
    <section
      class="relative py-20 bg-gradient-to-b from-neutral-900 to-neutral-800 text-white"
    >
      <UContainer>
        <div class="max-w-3xl mx-auto text-center">
          <h1 class="text-4xl md:text-5xl font-bold mb-6">
            Contáctanos
          </h1>
          <p class="text-xl text-neutral-300">
            Estamos aquí para responder tus preguntas y ayudarte a planificar tu
            aventura astronómica
          </p>
        </div>
      </UContainer>
    </section>

    <!-- Contact Info Grid -->
    <section class="py-16 bg-white dark:bg-neutral-800">
      <UContainer>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8 mb-16">
          <div
            v-for="info in contactInfo"
            :key="info.title"
            class="text-center"
          >
            <div
              class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-primary-100 dark:bg-primary-900/30 mb-4"
            >
              <UIcon
                :name="info.icon"
                class="w-8 h-8 text-primary-600 dark:text-primary-400"
              />
            </div>
            <h3
              class="text-lg font-semibold text-neutral-900 dark:text-white mb-3"
            >
              {{ info.title }}
            </h3>
            <div class="space-y-1">
              <p
                v-for="detail in info.details"
                :key="detail"
                class="text-neutral-600 dark:text-neutral-400 text-sm"
              >
                {{ detail }}
              </p>
            </div>
          </div>
        </div>

        <!-- Contact Form + Map -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-12">
          <!-- Form -->
          <div>
            <h2
              class="text-2xl font-bold text-neutral-900 dark:text-white mb-6"
            >
              Envíanos un mensaje
            </h2>
            <form
              class="space-y-6"
              @submit.prevent="submitForm"
            >
              <div>
                <label
                  class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2"
                >
                  Nombre completo
                </label>
                <UInput
                  v-model="state.name"
                  size="lg"
                  placeholder="Juan Pérez"
                  class="w-full"
                  required
                />
              </div>

              <div>
                <label
                  class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2"
                >
                  Email
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

              <div>
                <label
                  class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2"
                >
                  Teléfono
                </label>
                <UInput
                  v-model="state.phone"
                  size="lg"
                  placeholder="+56 9 1234 5678"
                  class="w-full"
                />
              </div>

              <div>
                <label
                  class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2"
                >
                  Asunto
                </label>
                <UInput
                  v-model="state.subject"
                  size="lg"
                  placeholder="Consulta sobre tours astronómicos"
                  class="w-full"
                  required
                />
              </div>

              <div>
                <label
                  class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2"
                >
                  Mensaje
                </label>
                <UTextarea
                  v-model="state.message"
                  :rows="6"
                  size="lg"
                  placeholder="Escribe tu mensaje aquí..."
                  class="w-full"
                  required
                />
              </div>

              <UButton
                type="submit"
                size="lg"
                color="primary"
                :loading="state.loading"
                block
              >
                {{ state.loading ? "Enviando..." : "Enviar mensaje" }}
              </UButton>
            </form>
          </div>

          <!-- Map Placeholder + Social -->
          <div>
            <h2
              class="text-2xl font-bold text-neutral-900 dark:text-white mb-6"
            >
              Encuéntranos
            </h2>

            <!-- Map placeholder -->
            <div
              class="w-full h-64 bg-neutral-200 dark:bg-neutral-800 rounded-xl mb-6 flex items-center justify-center overflow-hidden"
            >
              <div class="text-center p-8">
                <UIcon
                  name="i-lucide-map"
                  class="w-12 h-12 text-neutral-400 mx-auto mb-3"
                />
                <p class="text-neutral-600 dark:text-neutral-400 text-sm">
                  San Pedro de Atacama<br>
                  Región de Antofagasta, Chile
                </p>
              </div>
            </div>

            <!-- Social Media -->
            <div>
              <h3
                class="text-lg font-semibold text-neutral-900 dark:text-white mb-4"
              >
                Síguenos en redes sociales
              </h3>
              <div class="flex gap-4">
                <UButton
                  icon="i-simple-icons-facebook"
                  color="neutral"
                  variant="soft"
                  size="lg"
                  square
                  external
                  to="https://facebook.com"
                />
                <UButton
                  icon="i-simple-icons-instagram"
                  color="neutral"
                  variant="soft"
                  size="lg"
                  square
                  external
                  to="https://instagram.com"
                />
              </div>
            </div>
          </div>
        </div>
      </UContainer>
    </section>

    <!-- FAQs -->
    <section class="py-16 bg-neutral-50 dark:bg-neutral-800/50">
      <UContainer>
        <h2
          class="text-3xl font-bold text-neutral-900 dark:text-white mb-12 text-center"
        >
          Preguntas Frecuentes
        </h2>
        <div class="max-w-3xl mx-auto space-y-4">
          <UAccordion
            :items="
              faqs.map((faq) => ({ label: faq.question, content: faq.answer }))
            "
          />
        </div>
      </UContainer>
    </section>
  </div>
</template>
