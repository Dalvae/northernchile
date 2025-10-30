# Plan de Implementación Frontend - Northern Chile
## ⚠️ VERSIÓN FREE - Sin componentes Nuxt UI Pro

## Componentes PRO a EVITAR:
- ❌ `UDashboardGroup`
- ❌ `UDashboardSidebar`
- ❌ `UDashboardNavbar`
- ❌ `UDashboardPanel`
- ❌ `UDashboardToolbar`
- ❌ Todos los `UDashboard*`
- ❌ `UPage*` (pueden ser Pro)
- ❌ `UAuthForm` (Pro)
- ❌ `UBlogPost*` (Pro)
- ❌ `UChangelog*` (Pro)
- ❌ `UChat*` (Pro)
- ❌ `UContent*` (Pro)
- ❌ `UPricing*` (Pro)

## Componentes FREE disponibles:
- ✅ `UCard`
- ✅ `UButton`
- ✅ `UModal`
- ✅ `USlideover`
- ✅ `UTable`
- ✅ `UNavigationMenu`
- ✅ `UBreadcrumb`
- ✅ `UDropdownMenu`
- ✅ `UColorModeButton`
- ✅ `UInput`, `UTextarea`, `USelect`, `USelectMenu`
- ✅ `UForm`, `UFormField`
- ✅ `UBadge`, `UChip`
- ✅ `UAlert`
- ✅ `UAccordion`
- ✅ `UAvatar`, `UAvatarGroup`
- ✅ `UCalendar`
- ✅ `UCarousel`
- ✅ `UTabs`
- ✅ `UPagination`
- ✅ `UProgress`
- ✅ `USkeleton`
- ✅ `UTooltip`
- ✅ `UPopover`
- ✅ `UContextMenu`
- ✅ `UDrawer`
- ✅ Todos los form inputs

---

## FASE 1: ADMIN PANEL COMPLETO

### 1.1 Layout Admin - IMPLEMENTACIÓN CUSTOM ✅ PARCIAL
**Estado actual:** Usa layout simple con sidebar, necesita mejoras

**Nueva implementación SIN componentes Pro:**

```vue
<!-- layouts/admin.vue -->
<template>
  <div class="min-h-screen flex bg-neutral-50 dark:bg-neutral-900">
    <!-- Sidebar -->
    <aside
      :class="[
        'fixed inset-y-0 left-0 z-40 transition-transform duration-300 ease-in-out',
        'bg-white dark:bg-neutral-800 border-r border-neutral-200 dark:border-neutral-700',
        'flex flex-col',
        sidebarOpen ? 'translate-x-0' : '-translate-x-full',
        'lg:translate-x-0 lg:static lg:w-64'
      ]"
    >
      <!-- Header del sidebar -->
      <div class="h-16 flex items-center justify-between px-4 border-b border-neutral-200 dark:border-neutral-700">
        <NuxtLink to="/admin" class="flex items-center gap-2 font-bold text-lg">
          <UIcon name="i-lucide-shield-check" class="w-5 h-5 text-primary" />
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
            <div class="flex items-center gap-3 p-2 rounded-lg hover:bg-neutral-100 dark:hover:bg-neutral-700 cursor-pointer">
              <UAvatar
                :alt="authStore.user?.fullName"
                size="sm"
              />
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium truncate">
                  {{ authStore.user?.fullName }}
                </p>
                <p class="text-xs text-neutral-500 truncate">
                  {{ authStore.user?.role }}
                </p>
              </div>
              <UIcon name="i-lucide-chevron-up" class="w-4 h-4" />
            </div>
          </template>
        </UDropdownMenu>
      </div>
    </aside>

    <!-- Overlay para mobile -->
    <div
      v-if="sidebarOpen"
      class="fixed inset-0 bg-neutral-900/50 z-30 lg:hidden"
      @click="sidebarOpen = false"
    />

    <!-- Main content -->
    <div class="flex-1 flex flex-col min-w-0">
      <!-- Top navbar -->
      <header class="h-16 bg-white dark:bg-neutral-800 border-b border-neutral-200 dark:border-neutral-700 flex items-center px-4 gap-4">
        <!-- Hamburger menu mobile -->
        <UButton
          icon="i-lucide-menu"
          color="neutral"
          variant="ghost"
          class="lg:hidden"
          @click="sidebarOpen = true"
        />

        <!-- Breadcrumbs -->
        <UBreadcrumb :items="breadcrumbItems" class="flex-1" />

        <!-- Actions -->
        <div class="flex items-center gap-2">
          <!-- Color mode toggle -->
          <UColorModeButton />
          
          <!-- Notifications (futuro) -->
          <UButton
            icon="i-lucide-bell"
            color="neutral"
            variant="ghost"
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

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

const authStore = useAuthStore()
const route = useRoute()

// Sidebar state (mobile)
const sidebarOpen = ref(false)

// Close sidebar on route change (mobile)
watch(() => route.path, () => {
  sidebarOpen.value = false
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
    label: 'Usuarios',
    icon: 'i-lucide-users',
    to: '/admin/users'
  },
  {
    label: 'Alertas',
    icon: 'i-lucide-alert-triangle',
    to: '/admin/alerts'
  },
  {
    label: 'Tours Privados',
    icon: 'i-lucide-mail',
    to: '/admin/private-requests'
  },
  {
    label: 'Reportes',
    icon: 'i-lucide-bar-chart',
    to: '/admin/reports'
  },
  {
    label: 'Configuración',
    icon: 'i-lucide-settings',
    to: '/admin/settings'
  }
]

// User menu items
const userMenuItems = [
  [{
    label: 'Mi Perfil',
    icon: 'i-lucide-user',
    to: '/profile'
  }],
  [{
    label: 'Cerrar Sesión',
    icon: 'i-lucide-log-out',
    click: () => authStore.logout()
  }]
]

// Breadcrumbs dinámicos
const breadcrumbItems = computed(() => {
  const path = route.path
  const segments = path.split('/').filter(Boolean)
  
  const items = [{ label: 'Inicio', to: '/admin' }]
  
  let currentPath = ''
  segments.slice(1).forEach(segment => {
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
```

**Tareas:**
- [ ] Implementar layout custom responsive con sidebar colapsable
- [ ] Agregar breadcrumbs dinámicos
- [ ] Implementar user dropdown menu
- [ ] Agregar color mode toggle
- [ ] Hacer sidebar sticky y responsive
- [ ] Agregar overlay en mobile

---

### 1.2 Dashboard Stats Card Component - CUSTOM

Crear componente reutilizable para stats:

```vue
<!-- components/admin/StatsCard.vue -->
<template>
  <UCard>
    <div class="flex items-start justify-between">
      <div class="flex-1">
        <p class="text-sm font-medium text-neutral-600 dark:text-neutral-400">
          {{ label }}
        </p>
        <p class="mt-2 text-3xl font-semibold text-neutral-900 dark:text-white">
          {{ formattedValue }}
        </p>
        
        <!-- Cambio vs período anterior -->
        <div v-if="change !== undefined" class="mt-2 flex items-center gap-1">
          <UIcon
            :name="change >= 0 ? 'i-lucide-trending-up' : 'i-lucide-trending-down'"
            :class="change >= 0 ? 'text-success' : 'text-error'"
            class="w-4 h-4"
          />
          <span
            :class="change >= 0 ? 'text-success' : 'text-error'"
            class="text-sm font-medium"
          >
            {{ Math.abs(change) }}%
          </span>
          <span class="text-xs text-neutral-500">vs período anterior</span>
        </div>
      </div>
      
      <!-- Icon -->
      <div
        :class="[
          'p-3 rounded-lg',
          iconColor || 'bg-primary/10'
        ]"
      >
        <UIcon
          :name="icon"
          :class="iconTextColor || 'text-primary'"
          class="w-6 h-6"
        />
      </div>
    </div>
    
    <!-- Loading skeleton -->
    <USkeleton v-if="loading" class="h-20 mt-4" />
  </UCard>
</template>

<script setup lang="ts">
const props = defineProps<{
  label: string
  value: number | string
  icon: string
  change?: number
  iconColor?: string
  iconTextColor?: string
  loading?: boolean
  formatter?: (value: number | string) => string
}>()

const formattedValue = computed(() => {
  if (props.formatter) {
    return props.formatter(props.value)
  }
  return props.value.toString()
})
</script>
```

---

### 1.3 Dashboard con Stats Reales

```vue
<!-- pages/admin/index.vue -->
<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-neutral-900 dark:text-white">
        Dashboard
      </h1>
      <p class="mt-1 text-sm text-neutral-600 dark:text-neutral-400">
        Resumen de tu negocio
      </p>
    </div>

    <!-- Filtro de período -->
    <div class="flex gap-2">
      <UButton
        v-for="period in periods"
        :key="period.value"
        :label="period.label"
        :color="selectedPeriod === period.value ? 'primary' : 'neutral'"
        :variant="selectedPeriod === period.value ? 'solid' : 'outline'"
        size="sm"
        @click="selectedPeriod = period.value"
      />
    </div>

    <!-- Stats Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <AdminStatsCard
        label="Reservas"
        :value="stats.bookings.total"
        :change="stats.bookings.change"
        icon="i-lucide-calendar-check"
        :loading="loadingStats"
      />
      
      <AdminStatsCard
        label="Ingresos"
        :value="stats.revenue.total"
        :change="stats.revenue.change"
        icon="i-lucide-dollar-sign"
        :formatter="formatCurrency"
        icon-color="bg-success/10"
        icon-text-color="text-success"
        :loading="loadingStats"
      />
      
      <AdminStatsCard
        label="Tours Activos"
        :value="stats.activeTours"
        icon="i-lucide-map"
        icon-color="bg-info/10"
        icon-text-color="text-info"
        :loading="loadingStats"
      />
      
      <AdminStatsCard
        label="Alertas Pendientes"
        :value="stats.pendingAlerts"
        icon="i-lucide-alert-triangle"
        icon-color="bg-warning/10"
        icon-text-color="text-warning"
        :loading="loadingStats"
      />
    </div>

    <!-- Charts and Tables -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Gráfico de reservas (usar Chart.js) -->
      <UCard>
        <template #header>
          <h3 class="text-lg font-semibold">Reservas por Día</h3>
        </template>
        <canvas ref="bookingsChart" />
      </UCard>

      <!-- Reservas recientes -->
      <UCard>
        <template #header>
          <h3 class="text-lg font-semibold">Reservas Recientes</h3>
        </template>
        <UTable
          :data="recentBookings"
          :columns="bookingsColumns"
        />
      </UCard>
    </div>

    <!-- Alertas -->
    <UCard v-if="pendingAlerts.length > 0">
      <template #header>
        <h3 class="text-lg font-semibold">Alertas Climáticas Pendientes</h3>
      </template>
      <div class="space-y-2">
        <UAlert
          v-for="alert in pendingAlerts"
          :key="alert.id"
          :color="getAlertColor(alert.severity)"
          :title="alert.title"
          :description="alert.description"
        />
      </div>
    </UCard>
  </div>
</template>
```

---

### 1.4 Todas las Páginas Admin - IMPLEMENTACIÓN FREE

**Patrón general para todas las páginas:**

```vue
<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold">Título</h1>
        <p class="text-sm text-neutral-600 dark:text-neutral-400">
          Descripción
        </p>
      </div>
      <div class="flex gap-2">
        <!-- Actions -->
      </div>
    </div>

    <!-- Filters -->
    <UCard>
      <div class="flex flex-wrap gap-4">
        <UInput placeholder="Buscar..." class="w-full sm:w-64" />
        <USelectMenu :items="[]" placeholder="Filtro 1" />
        <USelectMenu :items="[]" placeholder="Filtro 2" />
      </div>
    </UCard>

    <!-- Main content (Table, Grid, etc) -->
    <UCard>
      <UTable :data="[]" :columns="[]" />
      <div class="flex justify-between items-center mt-4 pt-4 border-t">
        <p class="text-sm text-neutral-600">
          Mostrando {{ from }} - {{ to }} de {{ total }}
        </p>
        <UPagination
          v-model="page"
          :total="totalPages"
        />
      </div>
    </UCard>
  </div>
</template>
```

---

## FASE 2: FRONTEND PÚBLICO - SIN COMPONENTES PRO

### 2.1 Homepage

**Implementación con componentes FREE:**

```vue
<template>
  <div>
    <!-- Hero Section - CUSTOM -->
    <section class="relative h-screen">
      <img
        src="/images/hero-bg.jpg"
        alt="Atacama"
        class="absolute inset-0 w-full h-full object-cover"
      />
      <div class="absolute inset-0 bg-neutral-900/50" />
      
      <div class="relative h-full flex items-center justify-center">
        <div class="max-w-4xl mx-auto px-4 text-center text-white">
          <h1 class="text-5xl md:text-6xl font-bold mb-4">
            Descubre las Estrellas del Desierto
          </h1>
          <p class="text-xl md:text-2xl mb-8">
            Tours astronómicos en San Pedro de Atacama
          </p>
          <div class="flex gap-4 justify-center">
            <UButton
              label="Ver Tours"
              size="xl"
              color="primary"
              to="/tours"
            />
            <UButton
              label="Contactar"
              size="xl"
              color="neutral"
              variant="outline"
              to="/contact"
            />
          </div>
        </div>
      </div>
    </section>

    <!-- Tours Destacados - con UCarousel (FREE) -->
    <section class="py-16 bg-white dark:bg-neutral-900">
      <div class="max-w-7xl mx-auto px-4">
        <h2 class="text-3xl font-bold text-center mb-8">
          Tours Destacados
        </h2>
        <UCarousel :items="featuredTours">
          <template #default="{ item }">
            <TourCard :tour="item" />
          </template>
        </UCarousel>
      </div>
    </section>

    <!-- Features - Grid custom con UCard -->
    <section class="py-16 bg-neutral-50 dark:bg-neutral-800">
      <div class="max-w-7xl mx-auto px-4">
        <h2 class="text-3xl font-bold text-center mb-12">
          ¿Por qué elegirnos?
        </h2>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <UCard v-for="feature in features" :key="feature.title">
            <div class="text-center">
              <div class="mx-auto w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center mb-4">
                <UIcon :name="feature.icon" class="w-6 h-6 text-primary" />
              </div>
              <h3 class="text-lg font-semibold mb-2">{{ feature.title }}</h3>
              <p class="text-sm text-neutral-600 dark:text-neutral-400">
                {{ feature.description }}
              </p>
            </div>
          </UCard>
        </div>
      </div>
    </section>
  </div>
</template>
```

### 2.2 Tour Detail Page

```vue
<template>
  <div class="max-w-7xl mx-auto px-4 py-8">
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- Main Content -->
      <div class="lg:col-span-2 space-y-6">
        <!-- Gallery con UCarousel -->
        <UCarousel :items="tour.images">
          <template #default="{ item }">
            <img
              :src="item"
              alt="Tour"
              class="w-full h-96 object-cover rounded-lg"
            />
          </template>
        </UCarousel>

        <!-- Tabs con UTabs (FREE) -->
        <UTabs :items="tabs">
          <template #description>
            <div class="prose dark:prose-invert">
              {{ tour.description }}
            </div>
          </template>
          
          <template #included>
            <ul class="space-y-2">
              <li v-for="item in tour.included" class="flex gap-2">
                <UIcon name="i-lucide-check" class="text-success" />
                {{ item }}
              </li>
            </ul>
          </template>
          
          <template #faq>
            <UAccordion :items="tour.faq" />
          </template>
        </UTabs>
      </div>

      <!-- Sidebar -->
      <div class="space-y-4">
        <!-- Booking Card -->
        <UCard class="sticky top-4">
          <div class="space-y-4">
            <div>
              <p class="text-sm text-neutral-600">Desde</p>
              <p class="text-3xl font-bold">{{ formatPrice(tour.priceAdult) }}</p>
            </div>
            
            <!-- Date Picker con UCalendar (FREE) -->
            <UCalendar
              v-model="selectedDate"
              :is-date-disabled="isDateDisabled"
            />
            
            <UButton
              label="Reservar Ahora"
              color="primary"
              block
              size="lg"
              @click="bookTour"
            />
          </div>
        </UCard>
      </div>
    </div>
  </div>
</template>
```

---

## COMPONENTES CUSTOM A CREAR

### 1. Sidebar Collapsible (reemplaza UDashboardSidebar)
```vue
<!-- components/admin/AdminSidebar.vue -->
- Estado collapsed
- Responsive (drawer en mobile)
- Transiciones suaves
```

### 2. Stats Card (reemplaza UDashboardStats)
```vue
<!-- components/admin/StatsCard.vue -->
- Icon customizable
- Valor + cambio porcentual
- Loading state con USkeleton
```

### 3. Page Header (reemplaza UPageHeader)
```vue
<!-- components/common/PageHeader.vue -->
- Título + descripción
- Breadcrumbs opcionales
- Actions slot
```

### 4. Empty State (usar UEmpty que es FREE)
```vue
<!-- Usar UEmpty directamente -->
<UEmpty
  icon="i-lucide-inbox"
  title="No hay datos"
  description="Comienza creando tu primer item"
/>
```

---

## STACK TECNOLÓGICO ACTUALIZADO

### Componentes UI
- **Nuxt UI v4 FREE**: Solo componentes base
- **NO usar**: Dashboard, Page, Auth, Blog, Changelog, Content, Pricing components
- **Custom components**: Para layouts complejos

### Charts
- **Chart.js** + **vue-chartjs**: Para gráficos
- Alternativa: **ECharts** + **vue-echarts**

### Todo lo demás igual que antes
- Nuxt 3, Vue 3, Composition API
- TypeScript
- Pinia
- i18n
- Zod
- FullCalendar
- etc.

---

## ORDEN DE IMPLEMENTACIÓN ACTUALIZADO

### Sprint 1 (Admin Layout & Core)
1. **Layout admin CUSTOM** (sin UDashboard*)
2. **StatsCard component**
3. **Dashboard con stats reales**
4. **Mejorar página Tours**

### Sprint 2 (Admin CRUD)
5. **Calendario - modals**
6. **Reservas completa**
7. **Usuarios completa**

### Sprint 3 (Admin Avanzado)
8. **Alertas**
9. **Tours Privados**
10. **Reportes**
11. **Configuración**

### Sprint 4+ (Frontend Público)
12-20. **Todo el frontend público** (sin componentes Pro)

---

## NOTAS IMPORTANTES

1. **Verificar qué tienes instalado:**
   ```bash
   cat package.json | grep "@nuxt/ui"
   ```
   Si solo tienes `@nuxt/ui` (sin `-pro`), solo tienes FREE

2. **Componentes seguros (100% FREE):**
   - Form inputs (todos)
   - Table
   - Card
   - Button
   - Modal, Slideover, Drawer
   - Navigation, Breadcrumb
   - Todos los overlays
   - Calendar
   - Carousel
   - Tabs, Accordion
   - etc.

3. **Para Dashboard:**
   - Construir layout custom con flex/grid
   - Usar UCard para containers
   - Crear componentes custom para stats
   - Usar UNavigationMenu para sidebar

4. **Para Homepage:**
   - Hero section: HTML/CSS custom
   - Features: Grid de UCard
   - Carousel: UCarousel (FREE)
   - Sin componentes UPage*

---

**Versión:** 2.0 - FREE ONLY  
**Fecha:** 2025-10-30  
**Cambios:** Eliminados TODOS los componentes Pro, implementaciones custom
