<script setup lang="ts">
definePageMeta({
  layout: 'admin'
})

useHead({
  title: 'Configuración - Admin - Northern Chile'
})

const toast = useToast()
const { fetchAdminSettings } = useAdminData()

const { data: settings, pending, error, refresh } = await useAsyncData(
  'system-settings',
  async () => {
    try {
      const response = await fetchAdminSettings()
      return response
    } catch (err) {
      console.error('Error fetching settings:', err)
      toast.add({
        color: 'error',
        title: 'Error',
        description: 'No se pudieron cargar las configuraciones'
      })
      throw err
    }
  },
  {
    server: false,
    lazy: true
  }
)
</script>

<template>
  <div class="p-6">
    <!-- Header -->
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-default">
        Configuración del Sistema
      </h1>
      <p class="text-sm text-muted mt-1">
        Vista de solo lectura de la configuración actual del sistema
      </p>
    </div>

    <!-- Loading state -->
    <div
      v-if="pending"
      class="flex justify-center items-center py-12"
    >
      <UIcon
        name="i-lucide-loader-2"
        class="w-8 h-8 animate-spin text-primary"
      />
    </div>

    <!-- Error state -->
    <div
      v-else-if="error"
      class="text-center py-12"
    >
      <UIcon
        name="i-lucide-alert-circle"
        class="w-12 h-12 text-error mx-auto mb-4"
      />
      <p class="text-default">
        Error al cargar las configuraciones
      </p>
      <UButton
        color="primary"
        variant="soft"
        class="mt-4"
        @click="() => refresh()"
      >
        Reintentar
      </UButton>
    </div>

    <!-- Settings Display -->
    <div
      v-else-if="settings"
      class="space-y-6"
    >
      <!-- Weather Alerts -->
      <div class="bg-elevated rounded-lg p-6 border border-default">
        <div class="flex items-center gap-3 mb-4">
          <UIcon
            name="i-lucide-cloud"
            class="w-5 h-5 text-info"
          />
          <h3 class="text-lg font-semibold text-default">
            Alertas Climáticas
          </h3>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-default mb-2">
              Umbral de Viento (nudos)
            </label>
            <UInput
              :model-value="settings.weatherAlerts.windThreshold"
              type="number"
              size="lg"
              disabled
            />
            <p class="text-xs text-muted mt-1">
              Se genera alerta si el viento supera este valor
            </p>
          </div>
          <div>
            <label class="block text-sm font-medium text-default mb-2">
              Umbral de Cobertura Nubosa (%)
            </label>
            <UInput
              :model-value="settings.weatherAlerts.cloudCoverThreshold"
              type="number"
              size="lg"
              disabled
            />
            <p class="text-xs text-muted mt-1">
              Se genera alerta si las nubes superan este porcentaje
            </p>
          </div>
          <div>
            <label class="block text-sm font-medium text-default mb-2">
              Umbral de Probabilidad de Lluvia (%)
            </label>
            <UInput
              :model-value="settings.weatherAlerts.rainProbabilityThreshold"
              type="number"
              size="lg"
              disabled
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-default mb-2">
              Frecuencia de Verificación (horas)
            </label>
            <UInput
              :model-value="settings.weatherAlerts.checkFrequency"
              type="number"
              size="lg"
              disabled
            />
          </div>
        </div>
      </div>

      <!-- Booking Settings -->
      <div class="bg-elevated rounded-lg p-6 border border-default">
        <div class="flex items-center gap-3 mb-4">
          <UIcon
            name="i-lucide-book-marked"
            class="w-5 h-5 text-primary"
          />
          <h3 class="text-lg font-semibold text-default">
            Configuración de Reservas
          </h3>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-default mb-2">
              Ventana de Cancelación (horas)
            </label>
            <UInput
              :model-value="settings.bookingSettings.cancellationWindowHours"
              type="number"
              size="lg"
              disabled
            />
            <p class="text-xs text-muted mt-1">
              Horas antes del tour para permitir cancelación
            </p>
          </div>
          <div>
            <label class="block text-sm font-medium text-default mb-2">
              Reserva Máxima Adelantada (días)
            </label>
            <UInput
              :model-value="settings.bookingSettings.maxAdvanceBookingDays"
              type="number"
              size="lg"
              disabled
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-default mb-2">
              Anticipación Mínima (horas)
            </label>
            <UInput
              :model-value="settings.bookingSettings.minAdvanceBookingHours"
              type="number"
              size="lg"
              disabled
            />
            <p class="text-xs text-muted mt-1">
              Horas mínimas de anticipación para reservar
            </p>
          </div>
          <div class="flex items-center gap-3">
            <UCheckbox
              :model-value="settings.bookingSettings.autoConfirmBookings"
              label="Confirmar reservas automáticamente"
              disabled
            />
          </div>
        </div>
      </div>

      <!-- Astronomical Tours -->
      <div class="bg-elevated rounded-lg p-6 border border-default">
        <div class="flex items-center gap-3 mb-4">
          <UIcon
            name="i-lucide-moon"
            class="w-5 h-5 text-tertiary"
          />
          <h3 class="text-lg font-semibold text-default">
            Tours Astronómicos
          </h3>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-default mb-2">
              Umbral de Iluminación Lunar (%)
            </label>
            <UInput
              :model-value="settings.astronomicalTours.moonIlluminationThreshold"
              type="number"
              size="lg"
              disabled
            />
            <p class="text-xs text-muted mt-1">
              No se crean tours astronómicos si supera este valor
            </p>
          </div>
          <div>
            <label class="block text-sm font-medium text-default mb-2">
              Días de Generación Adelantada
            </label>
            <UInput
              :model-value="settings.astronomicalTours.scheduleGenerationDaysAhead"
              type="number"
              size="lg"
              disabled
            />
          </div>
          <div class="flex items-center gap-3">
            <UCheckbox
              :model-value="settings.astronomicalTours.autoBlockFullMoon"
              label="Bloquear automáticamente durante luna llena"
              disabled
            />
          </div>
        </div>
      </div>

      <!-- Notifications -->
      <div class="bg-elevated rounded-lg p-6 border border-default">
        <div class="flex items-center gap-3 mb-4">
          <UIcon
            name="i-lucide-bell"
            class="w-5 h-5 text-warning"
          />
          <h3 class="text-lg font-semibold text-default">
            Notificaciones
          </h3>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="flex items-center gap-3">
            <UCheckbox
              :model-value="settings.notifications.emailEnabled"
              label="Notificaciones por Email habilitadas"
              disabled
            />
          </div>
          <div class="flex items-center gap-3">
            <UCheckbox
              :model-value="settings.notifications.smsEnabled"
              label="Notificaciones por SMS habilitadas"
              disabled
            />
          </div>
          <div class="flex items-center gap-3">
            <UCheckbox
              :model-value="settings.notifications.sendBookingConfirmation"
              label="Enviar confirmación de reserva"
              disabled
            />
          </div>
          <div class="flex items-center gap-3">
            <UCheckbox
              :model-value="settings.notifications.sendCancellationNotice"
              label="Enviar aviso de cancelación"
              disabled
            />
          </div>
          <div class="flex items-center gap-3">
            <UCheckbox
              :model-value="settings.notifications.sendWeatherAlerts"
              label="Enviar alertas climáticas"
              disabled
            />
          </div>
        </div>
      </div>

      <!-- Payments -->
      <div class="bg-elevated rounded-lg p-6 border border-default">
        <div class="flex items-center gap-3 mb-4">
          <UIcon
            name="i-lucide-credit-card"
            class="w-5 h-5 text-success"
          />
          <h3 class="text-lg font-semibold text-default">
            Pagos
          </h3>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="flex items-center gap-3">
            <UCheckbox
              :model-value="settings.payments.mockPaymentMode"
              label="Modo de pago simulado (para pruebas)"
              disabled
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-default mb-2">
              Moneda
            </label>
            <UInput
              :model-value="settings.payments.currency"
              size="lg"
              disabled
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-default mb-2">
              Tasa de Impuesto (%)
            </label>
            <UInput
              :model-value="settings.payments.taxRate"
              type="number"
              size="lg"
              disabled
            />
            <p class="text-xs text-muted mt-1">
              IVA aplicado a todas las reservas
            </p>
          </div>
          <div>
            <label class="block text-sm font-medium text-default mb-2">
              Depósito Inicial (%)
            </label>
            <UInput
              :model-value="settings.payments.depositPercentage"
              type="number"
              size="lg"
              disabled
            />
            <p class="text-xs text-muted mt-1">
              Porcentaje a pagar al momento de reservar (0 = pago completo)
            </p>
          </div>
        </div>
      </div>

      <!-- Info Notice -->
      <div class="bg-info/10 border border-info/20 rounded-lg p-4">
        <div class="flex gap-3">
          <UIcon
            name="i-lucide-info"
            class="w-5 h-5 text-info flex-shrink-0 mt-0.5"
          />
          <div>
            <p class="text-sm font-medium text-info mb-1">
              Configuración del Sistema
            </p>
            <p class="text-sm text-default">
              Esta página muestra la configuración actual del sistema. Los valores se configuran mediante variables de entorno
              y archivos de configuración del backend. Para modificar estos valores, edita el archivo <code class="bg-neutral-100 dark:bg-neutral-800 px-1 rounded">.env</code>
              y reinicia la aplicación.
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
