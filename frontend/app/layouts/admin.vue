<script setup lang="ts">
import { useAuthStore } from "~/stores/auth";
const authStore = useAuthStore();

const links = [
  {
    label: "Dashboard",
    icon: "i-lucide-layout-dashboard",
    to: "/admin",
  },
  {
    label: "Reservas",
    icon: "i-lucide-book-marked",
    to: "/admin/bookings",
  },
  {
    label: "Tours",
    icon: "i-lucide-map",
    to: "/admin/tours",
  },
  {
    label: "Calendario",
    icon: "i-lucide-calendar-days",
    to: "/admin/calendar",
  },
  {
    label: "Usuarios",
    icon: "i-lucide-users",
    to: "/admin/users",
  },
];
</script>

<template>
  <div>
    <!-- Show the main layout ONLY if auth check is done and user is admin -->
    <div
      v-if="!authStore.loading && authStore.isAdmin"
      class="min-h-screen flex"
    >
      <aside
        class="w-64 flex-shrink-0 border-r border-gray-800 flex flex-col bg-gray-900"
      >
        <div class="h-16 flex-shrink-0 px-4 flex items-center gap-2">
          <NuxtLink
            to="/admin"
            class="flex items-center gap-2 font-bold text-xl"
          >
            <UIcon name="i-lucide-shield-check" class="w-6 h-6 text-primary" />
            <span>Admin Panel</span>
          </NuxtLink>
        </div>
        <div class="flex-1 overflow-y-auto p-4">
          <UNavigationMenu :items="links" orientation="vertical" />
        </div>
        <div class="p-4 border-t border-gray-800">
          <div class="flex items-center gap-2">
            <UAvatar
              v-if="authStore.user"
              size="sm"
              :alt="authStore.user.fullName"
            />
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium truncate">
                {{ authStore.user?.fullName || "Administrador" }}
              </p>
              <p class="text-xs text-gray-400 truncate">
                {{ authStore.user?.email }}
              </p>
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
      <main class="flex-1 overflow-y-auto">
        <div class="p-4 sm:p-6 lg:p-8">
          <slot />
        </div>
      </main>
    </div>

    <!-- Show a loading spinner while the auth state is being checked -->
    <div
      v-else-if="authStore.loading"
      class="min-h-screen flex items-center justify-center bg-gray-950"
    >
      <USpinner size="lg" />
    </div>

    <!--
      If not loading and not an admin, this div will be empty.
      The admin-auth.global.ts middleware is responsible for redirecting the user away,
      so we don't need to show an "Access Denied" message here.
    -->
  </div>
</template>

