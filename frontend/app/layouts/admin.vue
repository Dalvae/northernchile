<script setup lang="ts">
import type { NavigationMenuItem } from '@nuxt/ui'
import { useAuthStore } from '~/stores/auth';

const authStore = useAuthStore()
const open = ref(false)

const links: NavigationMenuItem[][] = [
  [{
    label: 'Dashboard',
    icon: 'i-lucide-layout-dashboard',
    to: '/admin',
    exact: true
  }, {
    label: 'Reservas',
    icon: 'i-lucide-book-marked',
    to: '/admin/bookings'
  }, {
    label: 'Tours',
    icon: 'i-lucide-map',
    to: '/admin/tours'
  }, {
    label: 'Calendario',
    icon: 'i-lucide-calendar-days',
    to: '/admin/calendar'
  }, {
    label: 'Usuarios',
    icon: 'i-lucide-users',
    to: '/admin/users'
  }]
]
</script>

<template>
  <UDashboardGroup unit="rem">
    <UDashboardSidebar
      v-model:open="open"
      collapsible
      resizable
      class="bg-elevated/25"
      :ui="{ footer: 'lg:border-t lg:border-default' }"
    >
      <template #header="{ collapsed }">
        <NuxtLink to="/admin" class="flex items-center gap-2 font-bold text-xl">
           <UIcon
            name="i-lucide-shield-check"
            class="w-6 h-6 text-primary"
          />
          <span v-if="!collapsed">Admin Panel</span>
        </NuxtLink>
      </template>

      <template #default="{ collapsed }">
        <UDashboardSearchButton :collapsed="collapsed" class="bg-transparent ring-default" />
        <UNavigationMenu
          :collapsed="collapsed"
          :items="links[0]"
          orientation="vertical"
          tooltip
          popover
        />
      </template>

      <template #footer="{ collapsed }">
         <div class="flex items-center gap-2 p-2">
          <UAvatar
            size="sm"
            src="https://avatars.githubusercontent.com/u/739984?v=4"
          />
          <div v-if="!collapsed" class="flex-1 min-w-0">
            <p class="text-sm font-medium truncate">{{ authStore.user?.fullName || 'Administrador' }}</p>
            <p class="text-xs text-gray-400 truncate">{{ authStore.user?.email }}</p>
          </div>
           <UButton
            v-if="!collapsed"
            icon="i-lucide-log-out"
            color="gray"
            variant="ghost"
            aria-label="Salir"
            @click="authStore.logout()"
           />
        </div>
      </template>
    </UDashboardSidebar>

    <slot />
  </UDashboardGroup>
</template>