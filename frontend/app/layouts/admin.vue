<script setup lang="ts">
// Este ref controlará si el menú lateral está abierto en móvil
const isNavOpen = ref(false)

const links = [
  { label: 'Dashboard', icon: 'i-lucide-layout-dashboard', to: '/admin' },
  { label: 'Tours', icon: 'i-lucide-map', to: '/admin/tours' },
  { label: 'Calendario', icon: 'i-lucide-calendar-days', to: '/admin/calendar' },
  { label: 'Reservas', icon: 'i-lucide-book-marked', to: '/admin/bookings' },
  { label: 'Usuarios', icon: 'i-lucide-users', to: '/admin/users' } // Podemos ocultarlo con v-if si el rol no es SUPER_ADMIN
]
</script>

<template>
  <div>
    <!-- Header para Móvil con botón de Menú -->
    <div class="lg:hidden p-4 flex items-center justify-between border-b border-gray-800">
      <h1 class="text-xl font-bold">Admin Panel</h1>
      <UButton
        icon="i-lucide-menu"
        variant="ghost"
        color="gray"
        @click="isNavOpen = true"
      />
    </div>

    <UContainer class="flex items-start gap-8 py-8">
      <!-- Barra Lateral para Escritorio (Oculta en móvil) -->
      <aside class="w-1/5 hidden lg:block">
        <!-- VERIFICA QUE ESTE NOMBRE SEA EXACTO -->
        <UVerticalNavigation :links="links" />
      </aside>

      <!-- Menú Deslizante para Móvil -->
      <USlideover v-model="isNavOpen">
        <div class="p-4">
          <!-- Y ESTE TAMBIÉN -->
          <UVerticalNavigation :links="links" />
        </div>
      </USlideover>

      <!-- Contenido Principal de la Página -->
      <main class="w-full lg:w-4/5">
        <slot />
      </main>
    </UContainer>
  </div>
</template>