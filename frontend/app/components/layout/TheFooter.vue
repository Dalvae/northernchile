<script setup lang="ts">
const { t } = useI18n()
const localePath = useLocalePath()

const legalLinks = computed(() => [
  { label: 'Términos y Condiciones', to: '/terms' },
  { label: 'Política de Privacidad', to: '/privacy' },
  { label: 'Políticas de Cancelación', to: '/cancellation-policy' }
])
</script>

<template>
  <!-- CAMBIO 1: Usamos bg-neutral-950 en lugar de la variable hardcoded -->
  <footer
    class="bg-neutral-950 border-t border-white/10 pt-12 pb-6 relative overflow-hidden"
  >
    <!-- Efecto de estrellas (se mantiene) -->
    <div
      class="estrellas-atacama absolute inset-0 opacity-30 pointer-events-none"
    />

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8 mb-8">
        <!-- 1. Marca y Copyright -->
        <div class="space-y-4">
          <div class="flex items-center gap-2">
            <UIcon
              name="i-lucide-telescope"
              class="w-6 h-6 text-primary"
            />
            <span class="font-display font-bold text-xl text-white">Northern Chile</span>
          </div>
          <p class="text-sm text-neutral-400 max-w-xs">
            Experiencias astronómicas premium bajo los cielos más claros del
            planeta. San Pedro de Atacama, Chile.
          </p>
        </div>

        <!-- 2. Enlaces Legales (Los avisos que mencionaste) -->
        <div class="flex flex-col gap-2">
          <h4 class="font-semibold text-white mb-2">
            Legal
          </h4>
          <NuxtLink
            v-for="link in legalLinks"
            :key="link.to"
            :to="localePath(link.to)"
            class="text-sm text-neutral-400 hover:text-primary transition-colors w-fit"
          >
            {{ link.label }}
          </NuxtLink>
        </div>

        <!-- 3. Redes Sociales -->
        <div class="flex flex-col md:items-end gap-4">
          <h4 class="font-semibold text-white md:text-right">
            Síguenos
          </h4>
          <div class="flex gap-2">
            <UButton
              variant="ghost"
              color="neutral"
              icon="i-simple-icons-instagram"
              to="https://instagram.com"
              target="_blank"
              aria-label="Instagram"
              class="hover:text-primary hover:bg-white/5"
            />
            <UButton
              variant="ghost"
              color="neutral"
              icon="i-simple-icons-facebook"
              to="https://facebook.com"
              target="_blank"
              aria-label="Facebook"
              class="hover:text-primary hover:bg-white/5"
            />
            <UButton
              variant="ghost"
              color="neutral"
              icon="i-simple-icons-tripadvisor"
              to="#"
              target="_blank"
              aria-label="TripAdvisor"
              class="hover:text-primary hover:bg-white/5"
            />
          </div>
        </div>
      </div>

      <div
        class="border-t border-white/10 pt-6 flex flex-col md:flex-row justify-between items-center gap-4 text-xs text-neutral-500"
      >
        <p>
          © {{ new Date().getFullYear() }} Northern Chile. Todos los derechos
          reservados.
        </p>

        <!-- CAMBIO 2: Tu firma -->
        <div class="flex items-center gap-1">
          <span>Made by</span>
          <a
            href="https://portfolio.dalvae.cl/"
            target="_blank"
            rel="noopener noreferrer"
            class="font-medium text-neutral-300 hover:text-primary transition-colors flex items-center gap-1"
          >
            dalvae
            <UIcon
              name="i-lucide-sparkles"
              class="w-3 h-3"
            />
          </a>
        </div>
      </div>
    </div>
  </footer>
</template>
