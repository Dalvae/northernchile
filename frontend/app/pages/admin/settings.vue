<script setup lang="ts">
definePageMeta({
  layout: 'admin'
})

const toast = useToast()
const { fetchAdminSettings, updateAdminSettings } = useAdminData()

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

// Form state - debe coincidir con la estructura del API
const settingsForm = ref<any>({
  weatherAlerts: {
    windThreshold: 25,
    cloudCoverThreshold: 80,
    rainProbabilityThreshold: 50,
    checkFrequency: 3
  },
  bookingSettings: {
    cancellationWindowHours: 24,
    maxAdvanceBookingDays: 90,
    minAdvanceBookingHours: 2,
    autoConfirmBookings: false
  },
  astronomicalTours: {
    moonIlluminationThreshold: 90,
    autoBlockFullMoon: true,
    scheduleGenerationDaysAhead: 90
  },
  notifications: {
    emailEnabled: true,
    smsEnabled: false,
    sendBookingConfirmation: true,
    sendCancellationNotice: true,
    sendWeatherAlerts: true
  },
  payments: {
    mockPaymentMode: false,
    currency: 'CLP',
    taxRate: 19,
    depositPercentage: 0
  }
})

// Initialize form when settings load
watch(settings, (newSettings) => {
  if (newSettings) {
    settingsForm.value = JSON.parse(JSON.stringify(newSettings))
  }
}, { immediate: true })

// Save settings
const saving = ref(false)
const saveSettings = async () => {
  try {
    saving.value = true

    await updateAdminSettings(settingsForm.value)

    toast.add({
      color: 'success',
      title: 'Configuración guardada',
      description: 'Las configuraciones se han actualizado correctamente'
    })

    await refresh()
  } catch (err) {
    console.error('Error saving settings:', err)
    toast.add({
      color: 'error',
      title: 'Error',
      description: 'No se pudieron guardar las configuraciones'
    })
  } finally {
    saving.value = false
  }
}

// Reset to defaults
const resetSettings = () => {
  if (settings.value) {
    settingsForm.value = JSON.parse(JSON.stringify(settings.value))
    toast.add({
      color: 'info',
      title: 'Cambios descartados',
      description: 'Se han restaurado las configuraciones guardadas'
    })
  }
}
</script>

<template>
  <div class="p-6">
    <!-- Header -->
    <div class="mb-6">
       <h1 class="text-2xl font-bold text-default">
        Configuración del Sistema
      </h1>
       <p class="text-sm text-muted mt-1">
        Ajusta los parámetros operacionales del sistema
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

    <!-- Settings Form -->
    <div
      v-else-if="settingsForm"
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
              v-model.number="settingsForm.weatherAlerts.windThreshold"
              type="number"
              size="lg"
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
              v-model.number="settingsForm.weatherAlerts.cloudCoverThreshold"
              type="number"
              size="lg"
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
              v-model.number="settingsForm.weatherAlerts.rainProbabilityThreshold"
              type="number"
              size="lg"
            />
          </div>
          <div>
             <label class="block text-sm font-medium text-default mb-2">
              Frecuencia de Verificación (horas)
            </label>
            <UInput
              v-model.number="settingsForm.weatherAlerts.checkFrequency"
              type="number"
              size="lg"
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
              v-model.number="settingsForm.bookingSettings.cancellationWindowHours"
              type="number"
              size="lg"
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
              v-model.number="settingsForm.bookingSettings.maxAdvanceBookingDays"
              type="number"
              size="lg"
            />
          </div>
          <div>
             <label class="block text-sm font-medium text-default mb-2">
              Anticipación Mínima (horas)
            </label>
            <UInput
              v-model.number="settingsForm.bookingSettings.minAdvanceBookingHours"
              type="number"
              size="lg"
            />
             <p class="text-xs text-muted mt-1">
              Horas mínimas de anticipación para reservar
            </p>
          </div>
          <div class="flex items-center gap-3">
            <UCheckbox
              v-model="settingsForm.bookingSettings.autoConfirmBookings"
              label="Confirmar reservas automáticamente"
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
              v-model.number="settingsForm.astronomicalTours.moonIlluminationThreshold"
              type="number"
              size="lg"
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
              v-model.number="settingsForm.astronomicalTours.scheduleGenerationDaysAhead"
              type="number"
              size="lg"
            />
          </div>
          <div class="flex items-center gap-3">
            <UCheckbox
              v-model="settingsForm.astronomicalTours.autoBlockFullMoon"
              label="Bloquear automáticamente durante luna llena"
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
              v-model="settingsForm.notifications.emailEnabled"
              label="Notificaciones por Email habilitadas"
            />
          </div>
          <div class="flex items-center gap-3">
            <UCheckbox
              v-model="settingsForm.notifications.smsEnabled"
              label="Notificaciones por SMS habilitadas"
            />
          </div>
          <div class="flex items-center gap-3">
            <UCheckbox
              v-model="settingsForm.notifications.sendBookingConfirmation"
              label="Enviar confirmación de reserva"
            />
          </div>
          <div class="flex items-center gap-3">
            <UCheckbox
              v-model="settingsForm.notifications.sendCancellationNotice"
              label="Enviar aviso de cancelación"
            />
          </div>
          <div class="flex items-center gap-3">
            <UCheckbox
              v-model="settingsForm.notifications.sendWeatherAlerts"
              label="Enviar alertas climáticas"
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
              v-model="settingsForm.payments.mockPaymentMode"
              label="Modo de pago simulado (para pruebas)"
            />
          </div>
          <div>
             <label class="block text-sm font-medium text-default mb-2">
              Moneda
            </label>
            <UInput
              v-model="settingsForm.payments.currency"
              size="lg"
              disabled
            />
          </div>
          <div>
             <label class="block text-sm font-medium text-default mb-2">
              Tasa de Impuesto (%)
            </label>
            <UInput
              v-model.number="settingsForm.payments.taxRate"
              type="number"
              size="lg"
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
              v-model.number="settingsForm.payments.depositPercentage"
              type="number"
              size="lg"
            />
             <p class="text-xs text-muted mt-1">
              Porcentaje a pagar al momento de reservar (0 = pago completo)
            </p>
          </div>
        </div>
      </div>

      <!-- Actions -->
      <div class="flex justify-end gap-3">
        <UButton
          color="neutral"
          variant="ghost"
          icon="i-lucide-rotate-ccw"
          @click="resetSettings"
        >
          Descartar Cambios
        </UButton>
        <UButton
          color="primary"
          icon="i-lucide-save"
          :loading="saving"
          @click="saveSettings"
        >
          Guardar Configuración
        </UButton>
      </div>

      <!-- Warning Notice -->
      <div class="bg-warning/10 border border-warning/20 rounded-lg p-4">
        <div class="flex gap-3">
          <UIcon
            name="i-lucide-alert-triangle"
            class="w-5 h-5 text-warning flex-shrink-0 mt-0.5"
          />
          <div>
            <p class="text-sm font-medium text-warning mb-1">
              Nota de Implementación
            </p>
            <p class="text-sm text-default">
              Esta es una interfaz básica de configuración. En producción, estos valores deberían persistirse en base de datos
              y aplicarse dinámicamente en el sistema. Actualmente, algunas configuraciones requieren reiniciar la aplicación
              para tener efecto completo.
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
