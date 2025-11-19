<script setup lang="ts">
// Accept critical prop from parent (nuxt-booster)
defineProps<{
  critical?: boolean
}>()

const items = [
  {
    title: 'Astronomía con Alma',
    description: 'No solo mirarás estrellas: aprenderás a leerlas. Nuestros guías, expertos en astronomía y cosmovisión andina, revelan los secretos del cosmos y cómo los antiguos pueblos de los Andes lo interpretaron. Una experiencia que combina ciencia, historia y emoción.',
    icon: 'i-lucide-stars',
    image: '/images/astro-experience.jpg', // Placeholder, handled by UI if missing
    color: 'text-primary-400',
    bg: 'bg-primary-500/10'
  },
  {
    title: 'Paisajes Vivos',
    description: 'Conecta con la geología viva. Desde los espejos de sal en las Lagunas Escondidas de Baltinache hasta la paleta mineral del Valle del Arcoíris. Camina sobre dunas doradas y toca capas sedimentarias que narran millones de años de historia.',
    icon: 'i-lucide-mountain',
    image: '/images/geo-experience.jpg',
    color: 'text-tertiary-400',
    bg: 'bg-tertiary-500/10'
  }
]
</script>

<template>
  <section class="relative py-24 overflow-hidden">
    <!-- Background Elements -->
    <div class="absolute inset-0 bg-neutral-950 z-0" />
    <div class="absolute top-0 left-0 w-full h-px bg-gradient-to-r from-transparent via-primary/50 to-transparent" />

    <UContainer class="relative z-10">
      <div class="text-center mb-20">
        <span class="inline-block py-1 px-3 rounded-full bg-primary/10 text-primary text-sm font-medium mb-4 tracking-wider uppercase">
          La Experiencia Northern Chile
        </span>
        <h2 class="text-4xl md:text-5xl font-display font-bold text-white mb-6 text-glow">
          Más que un tour, una conexión
        </h2>
        <p class="text-xl text-neutral-200 max-w-3xl mx-auto leading-relaxed">
          En el corazón del desierto más árido del mundo, te invitamos a mirar el universo con ojos nuevos.
          Donde la ciencia, la historia y la naturaleza se funden bajo un mismo cielo.
        </p>
      </div>

      <div class="space-y-24">
        <div
          v-for="(item, index) in items"
          :key="item.title"
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
                to="/about"
              >
                Saber más
                <UIcon
                  name="i-lucide-arrow-right"
                  class="ml-2 w-5 h-5 group-hover:translate-x-1 transition-transform"
                />
              </UButton>
            </div>
          </div>

          <!-- Visual/Image Placeholder -->
          <div class="flex-1 w-full">
            <div class="relative aspect-[4/3] rounded-2xl overflow-hidden group border border-white/10 bg-neutral-900">
              <!-- Decorative gradient overlay -->
              <div class="absolute inset-0 bg-gradient-to-tr from-neutral-900/80 via-transparent to-transparent z-10" />

              <!-- Abstract/Placeholder Visual since we might not have images yet -->
              <div class="absolute inset-0 flex items-center justify-center bg-neutral-800 group-hover:scale-105 transition-transform duration-700">
                <UIcon
                  :name="item.icon"
                  class="w-32 h-32 opacity-20"
                  :class="item.color"
                />
              </div>

              <!-- Content Overlay -->
              <div class="absolute bottom-0 left-0 p-8 z-20 w-full">
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
