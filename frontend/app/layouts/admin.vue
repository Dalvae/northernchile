<script setup lang="ts">
const open = ref(false);

const links = [
  [
    {
      label: "Dashboard",
      icon: "i-heroicons-home",
      to: "/admin",
      exact: true,
    },
    {
      label: "Tours",
      icon: "i-heroicons-map",
      to: "/admin/tours",
    },
    {
      label: "Calendario",
      icon: "i-heroicons-calendar-days",
      to: "/admin/calendar",
    },
    {
      label: "Reservas",
      icon: "i-heroicons-bookmark",
      to: "/admin/bookings",
    },
    {
      label: "Usuarios",
      icon: "i-heroicons-users",
      to: "/admin/users",
    },
  ],
];
</script>

<template>
  <UDashboardGroup>
    <UDashboardSidebar v-model:open="open" collapsible resizable>
      <template #header="{ collapsed }">
        <div class="flex items-center gap-2">
          <UIcon
            name="i-heroicons-chart-bar"
            class="w-6 h-6 text-primary-600"
          />
          <span v-if="!collapsed" class="font-bold">Admin Panel</span>
        </div>
      </template>

      <template #default="{ collapsed }">
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
            <p class="text-sm font-medium truncate">Administrador</p>
          </div>
        </div>
      </template>
    </UDashboardSidebar>

    <div class="flex-1">
      <slot />
    </div>
  </UDashboardGroup>
</template>
