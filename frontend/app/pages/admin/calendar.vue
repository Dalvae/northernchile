<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <div>
        <h1 class="text-2xl font-bold text-neutral-900 dark:text-white">
          Calendario de Tours
        </h1>
        <p class="text-sm text-neutral-600 dark:text-neutral-400 mt-1">
          Gestiona schedules con información climática y lunar
        </p>
      </div>

      <div class="flex gap-2">
        <!-- Badge de alertas -->
        <UButton
          v-if="pendingAlerts > 0"
          color="error"
          variant="soft"
          :to="'/admin/alerts'"
          icon="i-heroicons-exclamation-triangle"
        >
          {{ pendingAlerts }} {{ pendingAlerts === 1 ? 'Alerta' : 'Alertas' }}
        </UButton>

        <!-- Botón generar schedules -->
        <UButton
          color="primary"
          icon="i-heroicons-plus"
          @click="generateSchedules"
          :loading="generating"
        >
          Generar Schedules
        </UButton>
      </div>
    </div>

    <!-- Leyenda -->
    <div class="mb-4 p-4 bg-neutral-50 dark:bg-neutral-800 rounded-lg">
      <div class="flex flex-wrap gap-4 text-sm">
        <div class="flex items-center gap-2">
          <span class="text-lg">🌑🌒🌓🌔🌕🌖🌗🌘</span>
          <span class="text-neutral-700 dark:text-neutral-300">Fases lunares</span>
        </div>
        <div class="flex items-center gap-2">
          <UBadge color="error" variant="soft" size="xs">💨 Viento</UBadge>
          <span class="text-neutral-700 dark:text-neutral-300">&gt;25 nudos</span>
        </div>
        <div class="flex items-center gap-2">
          <UBadge color="warning" variant="soft" size="xs">☁️ Nublado</UBadge>
          <span class="text-neutral-700 dark:text-neutral-300">&gt;80%</span>
        </div>
        <div class="flex items-center gap-2">
          <UBadge color="info" variant="soft" size="xs">🌧️ Lluvia</UBadge>
          <span class="text-neutral-700 dark:text-neutral-300">Probabilidad &gt;50%</span>
        </div>
        <div class="flex items-center gap-2">
          <UBadge color="tertiary" variant="soft" size="xs">🌕</UBadge>
          <span class="text-neutral-700 dark:text-neutral-300">Luna llena</span>
        </div>
      </div>
    </div>

    <!-- Calendario -->
    <div class="bg-white dark:bg-neutral-900 rounded-lg shadow-sm p-4">
      <FullCalendar
        v-if="calendarOptions"
        :options="calendarOptions"
      />
    </div>

    <!-- Modal de schedule (crear/editar) -->
    <UModal v-model:open="showScheduleModal">
      <template #content>
        <div class="p-6">
          <!-- Header -->
          <div class="flex justify-between items-center pb-4 border-b border-neutral-200 dark:border-neutral-700">
            <h3 class="text-xl font-semibold text-neutral-900 dark:text-white">
              {{ isEditMode ? 'Editar Schedule' : 'Crear Schedule' }}
            </h3>
            <UButton
              icon="i-lucide-x"
              color="neutral"
              variant="ghost"
              size="sm"
              @click="closeScheduleModal"
            />
          </div>

          <!-- Form -->
          <form @submit.prevent="saveSchedule" class="space-y-4 py-4">
            <!-- Tour Selection -->
            <div>
              <label class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2">
                Tour <span class="text-error">*</span>
              </label>
              <USelect
                v-model="scheduleForm.tourId"
                :items="tourOptions"
                option-attribute="label"
                value-attribute="value"
                placeholder="Selecciona un tour"
                size="lg"
                :disabled="isEditMode"
                class="w-full"
              />
              <p v-if="formErrors.tourId" class="mt-1 text-sm text-error">
                {{ formErrors.tourId }}
              </p>
            </div>

            <!-- Date and Time -->
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2">
                  Fecha <span class="text-error">*</span>
                </label>
                <UInput
                  v-model="scheduleForm.date"
                  type="date"
                  size="lg"
                  class="w-full"
                />
                <p v-if="formErrors.date" class="mt-1 text-sm text-error">
                  {{ formErrors.date }}
                </p>
              </div>
              <div>
                <label class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2">
                  Hora <span class="text-error">*</span>
                </label>
                <UInput
                  v-model="scheduleForm.time"
                  type="time"
                  size="lg"
                  class="w-full"
                />
                <p v-if="formErrors.time" class="mt-1 text-sm text-error">
                  {{ formErrors.time }}
                </p>
              </div>
            </div>

            <!-- Max Participants -->
            <div>
              <label class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2">
                Cupos Máximos <span class="text-error">*</span>
              </label>
              <UInput
                v-model.number="scheduleForm.maxParticipants"
                type="number"
                min="1"
                max="100"
                size="lg"
                placeholder="Ej: 15"
                class="w-full"
              />
              <p v-if="formErrors.maxParticipants" class="mt-1 text-sm text-error">
                {{ formErrors.maxParticipants }}
              </p>
            </div>

            <!-- Guide Selection (Optional) -->
            <div>
              <label class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2">
                Guía Asignado (Opcional)
              </label>
              <USelect
                v-model="scheduleForm.assignedGuideId"
                :items="guideOptions"
                option-attribute="label"
                value-attribute="value"
                placeholder="Sin guía asignado"
                size="lg"
                class="w-full"
              />
            </div>

            <!-- Status (only in edit mode) -->
            <div v-if="isEditMode">
              <label class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2">
                Estado
              </label>
              <USelect
                v-model="scheduleForm.status"
                :items="statusOptions"
                option-attribute="label"
                value-attribute="value"
                size="lg"
                class="w-full"
              />
            </div>
          </form>

          <!-- Footer -->
          <div class="flex justify-end gap-2 pt-4 border-t border-neutral-200 dark:border-neutral-700">
            <UButton
              color="neutral"
              variant="outline"
              @click="closeScheduleModal"
            >
              Cancelar
            </UButton>
            <UButton
              color="primary"
              :loading="savingSchedule"
              @click="saveSchedule"
            >
              {{ isEditMode ? 'Actualizar' : 'Crear' }}
            </UButton>
          </div>
        </div>
      </template>
    </UModal>
  </div>
</template>

<script setup lang="ts">
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import type { CalendarOptions, EventClickArg, DateClickArg } from '@fullcalendar/core'
import esLocale from '@fullcalendar/core/locales/es'

definePageMeta({
  layout: 'admin',
  middleware: ['auth']
})

const config = useRuntimeConfig()
const { locale } = useI18n()
const toast = useToast()

const {
  fetchCalendarData,
  hasAdverseConditions,
  getWeatherIcon
} = useCalendarData()

const { fetchAdminTours } = useAdminData()

// Estado
const calendarData = ref<any>(null)
const showScheduleModal = ref(false)
const selectedSchedule = ref<any>(null)
const generating = ref(false)
const savingSchedule = ref(false)
const pendingAlerts = ref(0)

// Form state
const scheduleForm = ref({
  tourId: '',
  date: '',
  time: '',
  maxParticipants: 10,
  assignedGuideId: null as string | null,
  status: 'ACTIVE'
})

const formErrors = ref<Record<string, string>>({})

// Tours and guides data
const { data: toursData } = await useAsyncData('admin-tours-for-schedule', () => fetchAdminTours(), {
  server: false,
  lazy: true
})

// Computed options for selects
const tourOptions = computed(() => {
  if (!toursData.value?.data) return []
  return toursData.value.data
    .filter((tour: any) => tour.status === 'PUBLISHED')
    .map((tour: any) => ({
      value: tour.id,
      label: tour.nameTranslations[locale.value] || tour.nameTranslations.es || tour.name
    }))
})

const guideOptions = computed(() => [
  { value: null, label: 'Sin guía asignado' },
  // TODO: Fetch actual guides from API
])

const statusOptions = [
  { value: 'ACTIVE', label: 'Activo' },
  { value: 'CANCELLED', label: 'Cancelado' },
  { value: 'CLOSED', label: 'Cerrado' }
]

const isEditMode = computed(() => !!selectedSchedule.value)

// Rango de fechas del calendario
const startDate = ref('')
const endDate = ref('')

// Inicializar fechas
onMounted(() => {
  const today = new Date()
  startDate.value = today.toISOString().split('T')[0]

  // Mostrar próximos 60 días (máximo para tours astronómicos)
  const end = new Date(today)
  end.setDate(end.getDate() + 60)
  endDate.value = end.toISOString().split('T')[0]

  loadCalendarData()
})

// Cargar datos del calendario
const loadCalendarData = async () => {
  try {
    const data = await fetchCalendarData(startDate.value, endDate.value)
    calendarData.value = data
    pendingAlerts.value = data.allAlerts.filter((a: any) => a.status === 'PENDING').length
  } catch (error) {
    console.error('Error loading calendar data:', error)
    toast.add({
      title: 'Error',
      description: 'No se pudieron cargar los datos del calendario',
      color: 'red'
    })
  }
}

// Generar schedules
const generateSchedules = async () => {
  try {
    generating.value = true
    await $fetch(`${config.public.apiBaseUrl}/admin/schedules/generate`, {
      method: 'POST'
    })

    toast.add({
      title: 'Schedules generados',
      description: 'Los schedules se han generado correctamente',
      color: 'green'
    })

    // Recargar datos
    await loadCalendarData()
  } catch (error) {
    console.error('Error generating schedules:', error)
    toast.add({
      title: 'Error',
      description: 'No se pudieron generar los schedules',
      color: 'red'
    })
  } finally {
    generating.value = false
  }
}

// Click en evento (schedule)
const handleEventClick = (info: EventClickArg) => {
  const schedule = info.event.extendedProps.schedule
  selectedSchedule.value = schedule

  // Fill form with schedule data
  const scheduleDate = new Date(schedule.startDatetime)
  scheduleForm.value = {
    tourId: schedule.tour.id,
    date: scheduleDate.toISOString().split('T')[0],
    time: scheduleDate.toTimeString().slice(0, 5),
    maxParticipants: schedule.maxParticipants,
    assignedGuideId: schedule.assignedGuideId || null,
    status: schedule.status
  }

  showScheduleModal.value = true
}

// Click en día vacío
const handleDateClick = (info: DateClickArg) => {
  selectedSchedule.value = null

  // Pre-fill with clicked date
  const clickedDate = info.dateStr
  scheduleForm.value = {
    tourId: '',
    date: clickedDate,
    time: '20:00', // Default time for astronomical tours
    maxParticipants: 10,
    assignedGuideId: null,
    status: 'ACTIVE'
  }

  showScheduleModal.value = true
}

// Close modal and reset form
const closeScheduleModal = () => {
  showScheduleModal.value = false
  selectedSchedule.value = null
  formErrors.value = {}
  scheduleForm.value = {
    tourId: '',
    date: '',
    time: '',
    maxParticipants: 10,
    assignedGuideId: null,
    status: 'ACTIVE'
  }
}

// Validate form
const validateForm = (): boolean => {
  formErrors.value = {}

  if (!scheduleForm.value.tourId) {
    formErrors.value.tourId = 'Debes seleccionar un tour'
  }

  if (!scheduleForm.value.date) {
    formErrors.value.date = 'La fecha es requerida'
  }

  if (!scheduleForm.value.time) {
    formErrors.value.time = 'La hora es requerida'
  }

  if (!scheduleForm.value.maxParticipants || scheduleForm.value.maxParticipants < 1) {
    formErrors.value.maxParticipants = 'Debe ser al menos 1'
  }

  return Object.keys(formErrors.value).length === 0
}

// Save schedule (create or update)
const saveSchedule = async () => {
  if (!validateForm()) return

  savingSchedule.value = true

  try {
    // Combine date and time into ISO datetime
    const datetime = new Date(`${scheduleForm.value.date}T${scheduleForm.value.time}:00`)
    const isoDatetime = datetime.toISOString()

    const payload = {
      tourId: scheduleForm.value.tourId,
      startDatetime: isoDatetime,
      maxParticipants: scheduleForm.value.maxParticipants,
      assignedGuideId: scheduleForm.value.assignedGuideId || null
    }

    if (isEditMode.value) {
      // Update existing schedule
      await $fetch(`${config.public.apiBaseUrl}/admin/schedules/${selectedSchedule.value.id}`, {
        method: 'PATCH',
        body: payload,
        credentials: 'include'
      })

      toast.add({
        title: 'Schedule actualizado',
        description: 'Los cambios se han guardado correctamente',
        color: 'success'
      })
    } else {
      // Create new schedule
      await $fetch(`${config.public.apiBaseUrl}/admin/schedules`, {
        method: 'POST',
        body: payload,
        credentials: 'include'
      })

      toast.add({
        title: 'Schedule creado',
        description: 'El schedule se ha creado correctamente',
        color: 'success'
      })
    }

    // Reload calendar data
    await loadCalendarData()
    closeScheduleModal()
  } catch (error: any) {
    console.error('Error saving schedule:', error)
    toast.add({
      title: 'Error',
      description: error.data?.message || 'No se pudo guardar el schedule',
      color: 'error'
    })
  } finally {
    savingSchedule.value = false
  }
}

// Configuración de FullCalendar
const calendarOptions = computed<CalendarOptions | null>(() => {
  if (!calendarData.value) return null

  const { schedules, moonPhases, weather, alerts } = calendarData.value

  return {
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    initialView: 'dayGridMonth',
    locale: esLocale,
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay'
    },
    height: 'auto',
    editable: true,
    selectable: true,
    selectMirror: true,
    dayMaxEvents: true,
    weekends: true,
    eventClick: handleEventClick,
    dateClick: handleDateClick,

    // Eventos (schedules)
    events: schedules.map((schedule: any) => {
      const start = new Date(schedule.startDatetime)
      const end = new Date(start)
      end.setHours(start.getHours() + schedule.tour.durationHours)

      // Color según status
      let backgroundColor = '#10b981' // green
      if (schedule.status === 'CANCELLED') backgroundColor = '#ef4444' // red
      if (schedule.status === 'CLOSED') backgroundColor = '#6b7280' // gray

      // Verificar si tiene alertas
      const scheduleAlerts = alerts.get(schedule.id) || []
      const hasCriticalAlert = scheduleAlerts.some((a: any) =>
        a.severity === 'CRITICAL' && a.status === 'PENDING'
      )

      if (hasCriticalAlert) {
        backgroundColor = '#f59e0b' // orange/amber para alertas
      }

      return {
        id: schedule.id,
        title: schedule.tour.nameTranslations[locale.value] || schedule.tour.nameTranslations.es,
        start: start.toISOString(),
        end: end.toISOString(),
        backgroundColor,
        borderColor: backgroundColor,
        extendedProps: {
          schedule,
          alerts: scheduleAlerts
        }
      }
    }),

    // Contenido de cada día
    dayCellContent: (arg) => {
      const date = arg.date.toISOString().split('T')[0]
      const moonPhase = moonPhases.get(date)
      const dayWeather = weather.get(date)
      const conditions = hasAdverseConditions(date, weather, moonPhases)

      // Crear HTML personalizado para el día
      const container = document.createElement('div')
      container.className = 'flex flex-col h-full'

      // Número del día
      const dayNumber = document.createElement('div')
      dayNumber.className = 'text-right pr-2 pt-1'
      dayNumber.textContent = arg.dayNumberText
      container.appendChild(dayNumber)

      // Información meteorológica
      const weatherInfo = document.createElement('div')
      weatherInfo.className = 'flex-1 p-1 text-xs space-y-1'

      // Luna
      if (moonPhase) {
        const moonDiv = document.createElement('div')
        moonDiv.className = 'flex items-center gap-1'
        moonDiv.innerHTML = `
          <span class="text-base">${moonPhase.icon}</span>
          <span class="text-neutral-500">${moonPhase.illumination}%</span>
        `
        weatherInfo.appendChild(moonDiv)
      }

      // Clima
      if (dayWeather) {
        const tempDiv = document.createElement('div')
        tempDiv.className = 'flex items-center gap-1'
        tempDiv.innerHTML = `
          <span>${getWeatherIcon(dayWeather.weather[0]?.main)}</span>
          <span class="text-neutral-700 dark:text-neutral-300">
            ${Math.round(dayWeather.temp.max)}° / ${Math.round(dayWeather.temp.min)}°
          </span>
        `
        weatherInfo.appendChild(tempDiv)
      }

      // Badges de condiciones adversas
      const badgesDiv = document.createElement('div')
      badgesDiv.className = 'flex flex-wrap gap-1 mt-1'

      if (conditions.hasWind) {
        const badge = document.createElement('span')
        badge.className = 'px-1.5 py-0.5 bg-red-100 text-red-700 rounded-full text-xs'
        badge.textContent = '💨'
        badgesDiv.appendChild(badge)
      }

      if (conditions.hasClouds) {
        const badge = document.createElement('span')
        badge.className = 'px-1.5 py-0.5 bg-yellow-100 text-yellow-700 rounded-full text-xs'
        badge.textContent = '☁️'
        badgesDiv.appendChild(badge)
      }

      if (conditions.hasRain) {
        const badge = document.createElement('span')
        badge.className = 'px-1.5 py-0.5 bg-blue-100 text-blue-700 rounded-full text-xs'
        badge.textContent = '🌧️'
        badgesDiv.appendChild(badge)
      }

      if (conditions.hasFullMoon) {
        const badge = document.createElement('span')
        badge.className = 'px-1.5 py-0.5 bg-purple-100 text-purple-700 rounded-full text-xs'
        badge.textContent = '🌕'
        badgesDiv.appendChild(badge)
      }

      weatherInfo.appendChild(badgesDiv)
      container.appendChild(weatherInfo)

      return { domNodes: [container] }
    }
  }
})
</script>

<style>
/* Estilos para FullCalendar */
.fc {
  --fc-border-color: theme('colors.gray.200');
  --fc-button-bg-color: theme('colors.primary.500');
  --fc-button-border-color: theme('colors.primary.500');
  --fc-button-hover-bg-color: theme('colors.primary.600');
  --fc-button-hover-border-color: theme('colors.primary.600');
  --fc-button-active-bg-color: theme('colors.primary.700');
  --fc-button-active-border-color: theme('colors.primary.700');
  --fc-today-bg-color: theme('colors.primary.50');
}

.dark .fc {
  --fc-border-color: theme('colors.gray.700');
  --fc-today-bg-color: theme('colors.primary.900/20');
}

.fc-daygrid-day {
  min-height: 120px !important;
}

.fc-event {
  cursor: pointer;
  padding: 2px 4px;
  margin: 1px 0;
}

.fc-event:hover {
  opacity: 0.8;
}
</style>