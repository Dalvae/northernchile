<script setup lang="ts">
import { useAuthStore } from '~/stores/auth';

const authStore = useAuthStore();

const links = [
  {
    label: 'Dashboard',
    icon: 'i-lucide-layout-dashboard',
    to: '/admin',
  }, {
    label: 'Reservas',
    icon: 'i-lucide-book-marked',
    to: '/admin/bookings',
  }, {
    label: 'Tours',
    icon: 'i-lucide-map',
    to: '/admin/tours',
  }, {
    label: 'Calendario',
    icon: 'i-lucide-calendar-days',
    to: '/admin/calendar',
  }, {
    label: 'Usuarios',
    icon: 'i-lucide-users',
    to: '/admin/users',
  },
];
</script>

<template>
  <div class="min-h-screen flex">
    <!-- Barra Lateral (Sidebar) -->
    <aside class="w-64 flex-shrink-0 border-r border-gray-200 dark:border-gray-800 flex flex-col">
      <!-- Encabezado del Sidebar -->
      <div class="h-16 flex-shrink-0 px-4 flex items-center gap-2">
        <NuxtLink to="/admin" class="flex items-center gap-2 font-bold text-xl">
          <UIcon name="i-lucide-shield-check" class="w-6 h-6 text-primary" />
          <span>Admin Panel</span>
        </NuxtLink>
      </div>

      <!-- Menú de Navegación -->
      <div class="flex-1 overflow-y-auto p-4">
        <UNavigationMenu :links="links" orientation="vertical" />
      </div>

      <!-- Pie de página del Sidebar -->
      <div class="p-4 border-t border-gray-200 dark:border-gray-800">
        <div class="flex items-center gap-2">
          <UAvatar v-if="authStore.user" size="sm" :alt="authStore.user.fullName" />
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium truncate">{{ authStore.user?.fullName || 'Administrador' }}</p>
            <p class="text-xs text-gray-400 truncate">{{ authStore.user?.email }}</p>
          </div>
          <UButton
            icon="i-lucide-log-out"
            color="gray"
            variant="ghost"
            aria-label="Salir"
            @click="authStore.logout()"
          />
        </div>
      </div>
    </aside>

    <!-- Contenido Principal -->
    <main class="flex-1 overflow-y-auto">
      <div class="p-4 sm:p-6 lg:p-8">
        <slot />
      </div>
    </main>
  </div>
</template>