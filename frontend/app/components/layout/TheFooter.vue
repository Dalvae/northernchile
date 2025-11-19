<template>

  <footer class="bg-[var(--color-paranal-950)] border-t border-white/10 py-8 relative overflow-hidden">
    <div class="estrellas-atacama absolute inset-0 opacity-50 pointer-events-none" />
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
      <div class="flex flex-col md:flex-row justify-between items-center gap-4">
        <div class="flex items-center gap-2">
          <UIcon name="i-lucide-telescope" class="w-5 h-5 text-primary" />
          <p class="text-sm text-neutral-300">
            © {{ new Date().getFullYear() }} Northern Chile. <span class="hidden sm:inline">Bajo los cielos de Atacama.</span>
          </p>
        </div>

        <div class="flex items-center gap-4">
          <!-- Social Links (Placeholders) -->
          <UButton
            variant="ghost"
            color="white"
            icon="i-lucide-instagram"
            to="https://instagram.com"
            target="_blank"
            aria-label="Instagram"
            class="hover:text-primary hover:bg-white/5"
          />
          <UButton
            variant="ghost"
            color="white"
            icon="i-lucide-facebook"
            to="https://facebook.com"
            target="_blank"
            aria-label="Facebook"
            class="hover:text-primary hover:bg-white/5"
          />
        </div>
      </div>
    </div>
  </footer>
</template>
