<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

const authStore = useAuthStore()
const route = useRoute()
const config = useRuntimeConfig()

// Sidebar state (mobile)
const sidebarOpen = ref(false)

// Close sidebar on route change (mobile)
watch(
  () => route.path,
  () => {
    sidebarOpen.value = false
  }
)

// Fetch pending alerts count
const { data: alertsCount, refresh: refreshAlertsCount } = await useAsyncData(
  'pending-alerts-count',
  async () => {
    try {
      const token = import.meta.client ? localStorage.getItem('auth_token') : null
      const response = await $fetch<{ pending: number }>(
        `${config.public.apiBase}/api/admin/alerts/count`,
        {
          headers: token
            ? {
                Authorization: `Bearer ${token}`
              }
            : {}
        }
      )
      return response.pending || 0
    } catch (error) {
      console.error('Error fetching alerts count:', error)
      return 0
    }
  },
  {
    server: false,
    lazy: true
  }
)

// Refresh alerts count every 5 minutes
const alertsRefreshInterval = setInterval(() => {
  refreshAlertsCount()
}, 5 * 60 * 1000)

// Cleanup interval on unmount
onUnmounted(() => {
  clearInterval(alertsRefreshInterval)
})

// Navigation links
const navigationLinks = [
  {
    label: 'Dashboard',
    icon: 'i-lucide-layout-dashboard',
    to: '/admin'
  },
  {
    label: 'Tours',
    icon: 'i-lucide-map',
    to: '/admin/tours'
  },
  {
    label: 'Calendario',
    icon: 'i-lucide-calendar-days',
    to: '/admin/calendar'
  },
  {
    label: 'Reservas',
    icon: 'i-lucide-book-marked',
    to: '/admin/bookings'
  },
  {
    label: 'Tours Privados',
    icon: 'i-lucide-star',
    to: '/admin/private-requests'
  },
  {
    label: 'Usuarios',
    icon: 'i-lucide-users',
    to: '/admin/users'
  },
  {
    label: 'Reportes',
    icon: 'i-lucide-bar-chart-3',
    to: '/admin/reports'
  },
  {
    label: 'Configuración',
    icon: 'i-lucide-settings',
    to: '/admin/settings'
  }
]

// Handle logout
async function handleLogout() {
  await authStore.logout()
}

// User menu items
const userMenuItems = [
  [
    {
      label: 'Mi Perfil',
      icon: 'i-lucide-user',
      to: '/profile'
    }
  ],
  [
    {
      label: 'Cerrar Sesión',
      icon: 'i-lucide-log-out',
      onClick: handleLogout
    }
  ]
]

// Breadcrumbs dinámicos
const breadcrumbItems = computed(() => {
  const path = route.path
  const segments = path.split('/').filter(Boolean)

  const items = [{ label: 'Inicio', to: '/admin' }]

  let currentPath = ''
  segments.slice(1).forEach((segment) => {
    currentPath += `/${segment}`
    items.push({
      label: formatSegment(segment),
      to: `/admin${currentPath}`
    })
  })

  return items
})

function formatSegment(segment: string) {
  const labels: Record<string, string> = {
    'tours': 'Tours',
    'bookings': 'Reservas',
    'users': 'Usuarios',
    'calendar': 'Calendario',
    'alerts': 'Alertas',
    'private-requests': 'Tours Privados',
    'reports': 'Reportes',
    'settings': 'Configuración'
  }
  return labels[segment] || segment
}
</script>

<template>
  <div class="min-h-screen flex bg-neutral-50 dark:bg-neutral-800">
    <!-- Sidebar -->
    <aside
      :class="[
        'fixed inset-y-0 left-0 z-40 transition-transform duration-300 ease-in-out',
        'bg-white dark:bg-neutral-800 border-r border-neutral-200 dark:border-neutral-700',
        'flex flex-col w-64',
        sidebarOpen ? 'translate-x-0' : '-translate-x-full',
        'lg:translate-x-0 lg:static'
      ]"
    >
      <!-- Header del sidebar -->
      <div
        class="h-16 flex items-center justify-between px-4 border-b border-neutral-200 dark:border-neutral-700"
      >
        <NuxtLink
          to="/admin"
          class="flex items-center gap-2 font-bold text-lg text-neutral-900 dark:text-white"
        >
          <UIcon
            name="i-lucide-shield-check"
            class="w-5 h-5 text-primary"
          />
          <span>Admin Panel</span>
        </NuxtLink>
        <!-- Botón cerrar en mobile -->
        <UButton
          icon="i-lucide-x"
          color="neutral"
          variant="ghost"
          class="lg:hidden"
          @click="sidebarOpen = false"
        />
      </div>

      <!-- Navigation -->
      <nav class="flex-1 overflow-y-auto p-4">
        <UNavigationMenu
          :items="navigationLinks"
          orientation="vertical"
          :highlight="true"
        />
      </nav>

      <!-- User section -->
      <div class="p-4 border-t border-neutral-200 dark:border-neutral-700">
        <UDropdownMenu :items="userMenuItems">
          <template #default>
            <div
              class="flex items-center gap-3 p-2 rounded-lg hover:bg-neutral-100 dark:hover:bg-neutral-700 cursor-pointer transition-colors"
            >
              <UAvatar
                :alt="authStore.user?.fullName"
                size="sm"
              />
              <div class="flex-1 min-w-0">
                <p
                  class="text-sm font-medium truncate text-neutral-900 dark:text-white"
                >
                  {{ authStore.user?.fullName }}
                </p>
                <p class="text-xs text-neutral-500 truncate">
                  {{
                    authStore.user?.role?.includes("ROLE_SUPER_ADMIN")
                      ? "Super Admin"
                      : authStore.user?.role?.includes("ROLE_PARTNER_ADMIN")
                        ? "Partner Admin"
                        : "Usuario"
                  }}
                </p>
              </div>
              <UIcon
                name="i-lucide-chevron-up"
                class="w-4 h-4 text-neutral-400"
              />
            </div>
          </template>
        </UDropdownMenu>
      </div>
    </aside>

    <!-- Overlay para mobile -->
    <div
      v-if="sidebarOpen"
      class="fixed inset-0 bg-neutral-800/50 z-30 lg:hidden"
      @click="sidebarOpen = false"
    />

    <!-- Main content -->
    <div class="flex-1 flex flex-col min-w-0">
      <!-- Top navbar -->
      <header
        class="h-16 bg-white dark:bg-neutral-800 border-b border-neutral-200 dark:border-neutral-700 flex items-center px-4 gap-4 sticky top-0 z-20"
      >
        <!-- Hamburger menu mobile -->
        <UButton
          icon="i-lucide-menu"
          color="neutral"
          variant="ghost"
          class="lg:hidden"
          @click="sidebarOpen = true"
        />

        <!-- Breadcrumbs -->
        <UBreadcrumb
          :items="breadcrumbItems"
          class="flex-1"
        />

        <!-- Actions -->
        <div class="flex items-center gap-2">
          <!-- Alerts Badge -->
          <NuxtLink
            to="/admin/alerts"
            class="relative"
          >
            <UButton
              icon="i-lucide-alert-triangle"
              :color="alertsCount && alertsCount > 0 ? 'warning' : 'neutral'"
              :variant="alertsCount && alertsCount > 0 ? 'soft' : 'ghost'"
            >
              <span
                v-if="alertsCount && alertsCount > 0"
                class="ml-1"
              >
                {{ alertsCount }}
              </span>
            </UButton>
          </NuxtLink>

          <!-- Notifications (futuro) -->
          <UButton
            icon="i-lucide-bell"
            color="neutral"
            variant="ghost"
            disabled
          />
        </div>
      </header>

      <!-- Page content -->
      <main class="flex-1 overflow-y-auto">
        <div class="p-4 sm:p-6 lg:p-8">
          <slot />
        </div>
      </main>
    </div>
  </div>
</template>
