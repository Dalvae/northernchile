<script setup lang="ts">
const { locale } = useI18n()
const router = useRouter()

// Load bookings from localStorage
const bookings = ref<any[]>([])
const loading = ref(true)

onMounted(() => {
  if (process.client) {
    const stored = localStorage.getItem('local_bookings')
    if (stored) {
      try {
        bookings.value = JSON.parse(stored)
      } catch (e) {
        console.error('Failed to parse bookings:', e)
      }
    }
  }
  loading.value = false
})

// Format currency
function formatCurrency(amount: number) {
  return new Intl.NumberFormat('es-CL', {
    style: 'currency',
    currency: 'CLP'
  }).format(amount)
}

// Format date
function formatDate(dateString: string) {
  const date = new Date(dateString)
  return new Intl.DateTimeFormat(locale, {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

function formatShortDate(dateString: string) {
  const date = new Date(dateString)
  return new Intl.DateTimeFormat(locale, {
    day: 'numeric',
    month: 'short',
    year: 'numeric'
  }).format(date)
}

// Status colors
function getStatusColor(status: string) {
  const colors: Record<string, string> = {
    'CONFIRMED': 'success',
    'PENDING': 'warning',
    'CANCELLED': 'error',
    'COMPLETED': 'info'
  }
  return colors[status] || 'neutral'
}

// Status labels
function getStatusLabel(status: string) {
  const labels: Record<string, string> = {
    'CONFIRMED': 'Confirmada',
    'PENDING': 'Pendiente',
    'CANCELLED': 'Cancelada',
    'COMPLETED': 'Completada'
  }
  return labels[status] || status
}
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-neutral-900 py-12">
    <UContainer>
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-neutral-900 dark:text-white mb-2">
          Mis Reservas
        </h1>
        <p class="text-neutral-600 dark:text-neutral-400">
          Revisa y administra tus reservas
        </p>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="flex justify-center py-12">
        <UIcon name="i-lucide-loader-2" class="w-8 h-8 animate-spin text-primary" />
      </div>

      <!-- Empty State -->
      <div v-else-if="!bookings || bookings.length === 0" class="text-center py-12">
        <div class="w-20 h-20 bg-neutral-100 dark:bg-neutral-800 rounded-full flex items-center justify-center mx-auto mb-4">
          <UIcon name="i-lucide-calendar-x" class="w-12 h-12 text-neutral-400" />
        </div>
        <h3 class="text-xl font-semibold text-neutral-900 dark:text-white mb-2">
          No tienes reservas
        </h3>
        <p class="text-neutral-600 dark:text-neutral-400 mb-6">
          Aún no has realizado ninguna reserva. Explora nuestros tours y comienza tu aventura.
        </p>
        <UButton
          color="primary"
          size="lg"
          icon="i-lucide-telescope"
          to="/tours"
        >
          Explorar Tours
        </UButton>
      </div>

      <!-- Bookings List -->
      <div v-else class="space-y-6">
        <UCard
          v-for="booking in bookings"
          :key="booking.id"
        >
          <template #header>
            <div class="flex justify-between items-start">
              <div>
                <p class="text-sm text-neutral-500 dark:text-neutral-400">
                  Reserva #{{ booking.id }}
                </p>
                <p class="text-lg font-semibold text-neutral-900 dark:text-white mt-1">
                  {{ formatShortDate(booking.bookingDate) }}
                </p>
              </div>
              <UBadge :color="getStatusColor(booking.status)" size="lg">
                {{ getStatusLabel(booking.status) }}
              </UBadge>
            </div>
          </template>

          <div class="space-y-4">
            <!-- Items -->
            <div class="space-y-3">
              <div
                v-for="item in booking.items"
                :key="item.itemId"
                class="p-3 bg-neutral-50 dark:bg-neutral-800 rounded-lg"
              >
                <div class="flex justify-between items-start">
                  <div class="flex-1">
                    <p class="font-medium text-neutral-900 dark:text-white">
                      {{ item.tourName }}
                    </p>
                    <div class="mt-2 space-y-1 text-sm text-neutral-600 dark:text-neutral-400">
                      <p v-if="item.startDatetime" class="flex items-center gap-2">
                        <UIcon name="i-lucide-calendar" class="w-4 h-4" />
                        {{ formatDate(item.startDatetime) }}
                      </p>
                      <p class="flex items-center gap-2">
                        <UIcon name="i-lucide-users" class="w-4 h-4" />
                        {{ item.numParticipants }} {{ item.numParticipants === 1 ? 'persona' : 'personas' }}
                      </p>
                      <p v-if="item.durationHours" class="flex items-center gap-2">
                        <UIcon name="i-lucide-clock" class="w-4 h-4" />
                        {{ item.durationHours }} horas
                      </p>
                    </div>
                  </div>
                  <div class="text-right ml-4">
                    <p class="text-lg font-bold text-neutral-900 dark:text-white">
                      {{ formatCurrency(item.itemTotal) }}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Contact Info -->
            <UDivider />
            <div>
              <h4 class="text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2">
                Información de Contacto
              </h4>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
                <div class="flex items-center gap-2 text-neutral-600 dark:text-neutral-400">
                  <UIcon name="i-lucide-user" class="w-4 h-4" />
                  {{ booking.contact.fullName }}
                </div>
                <div class="flex items-center gap-2 text-neutral-600 dark:text-neutral-400">
                  <UIcon name="i-lucide-mail" class="w-4 h-4" />
                  {{ booking.contact.email }}
                </div>
                <div class="flex items-center gap-2 text-neutral-600 dark:text-neutral-400">
                  <UIcon name="i-lucide-phone" class="w-4 h-4" />
                  {{ booking.contact.countryCode }} {{ booking.contact.phone }}
                </div>
              </div>
            </div>

            <!-- Total -->
            <UDivider />
            <div class="space-y-2">
              <div class="flex justify-between text-sm text-neutral-600 dark:text-neutral-400">
                <span>Subtotal</span>
                <span>{{ formatCurrency(booking.subtotal) }}</span>
              </div>
              <div class="flex justify-between text-sm text-neutral-600 dark:text-neutral-400">
                <span>IVA (19%)</span>
                <span>{{ formatCurrency(booking.tax) }}</span>
              </div>
              <div class="flex justify-between text-lg font-bold text-neutral-900 dark:text-white pt-2 border-t border-neutral-200 dark:border-neutral-700">
                <span>Total</span>
                <span>{{ formatCurrency(booking.total) }}</span>
              </div>
            </div>
          </div>

          <template #footer>
            <div class="flex justify-between items-center">
              <div class="flex items-center gap-2 text-sm text-neutral-600 dark:text-neutral-400">
                <UIcon name="i-lucide-info" class="w-4 h-4" />
                <span>Cancelación gratuita hasta 24h antes</span>
              </div>
              <div class="flex gap-2">
                <UButton
                  color="neutral"
                  variant="outline"
                  size="sm"
                  icon="i-lucide-download"
                >
                  Descargar
                </UButton>
                <UButton
                  v-if="booking.status === 'CONFIRMED'"
                  color="error"
                  variant="soft"
                  size="sm"
                  icon="i-lucide-x"
                >
                  Cancelar
                </UButton>
              </div>
            </div>
          </template>
        </UCard>
      </div>
    </UContainer>
  </div>
</template>
