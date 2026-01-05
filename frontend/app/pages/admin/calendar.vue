<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <div>
        <h1 class="text-2xl font-bold text-default">
          Calendario de Tours
        </h1>
        <p class="mt-1 text-sm text-muted">
          Gestiona schedules con información climática y lunar
        </p>
        <p class="mt-1 text-sm text-info-600 dark:text-info-400">
          💡 Haz clic en cualquier fecha del calendario para crear un schedule manualmente
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
          {{ pendingAlerts }} {{ pendingAlerts === 1 ? "Alerta" : "Alertas" }}
        </UButton>

        <!-- Botón generar schedules -->
        <UButton
          color="primary"
          icon="i-heroicons-plus"
          :loading="generating"
          @click="generateSchedules"
        >
          Generar Schedules
        </UButton>
      </div>
    </div>

    <!-- Leyenda -->
    <div class="p-4 mb-4 rounded-lg bg-elevated border border-default">
      <div class="flex flex-wrap gap-4 text-sm">
        <div class="flex items-center gap-2">
          <span class="text-lg">🌑🌒🌓🌔🌕🌖🌗🌘</span>
          <span class="text-default">Fases lunares</span>
        </div>
        <div class="flex items-center gap-2">
          <UBadge
            color="error"
            variant="soft"
            size="xs"
          >
            💨 Viento
          </UBadge>
          <span class="text-muted">&gt;25 nudos</span>
        </div>
        <div class="flex items-center gap-2">
          <UBadge
            color="warning"
            variant="soft"
            size="xs"
          >
            ☁️ Nublado
          </UBadge>
          <span class="text-muted">&gt;80%</span>
        </div>
        <div class="flex items-center gap-2">
          <UBadge
            color="info"
            variant="soft"
            size="xs"
          >
            🌧️ Lluvia
          </UBadge>
          <span class="text-muted">Probabilidad &gt;50%</span>
        </div>
      </div>
    </div>

    <!-- Calendario -->
    <div class="p-4 rounded-lg bg-elevated border border-default">
      <FullCalendar
        v-if="calendarOptions"
        :options="calendarOptions"
      />
    </div>

    <!-- Modal de schedule (crear/editar) -->
    <UModal v-model:open="showScheduleModal">
      <template #content>
        <div class="p-6 bg-default min-h-screen">
          <!-- Header -->
          <div
            class="flex items-center justify-between pb-4 border-b border-default"
          >
            <div class="flex items-center gap-3">
              <h3
                class="text-xl font-semibold text-default"
              >
                {{ isEditMode ? "Editar Schedule" : "Crear Schedule" }}
              </h3>
              <!-- Ver participantes button -->
              <UButton
                v-if="isEditMode && selectedSchedule"
                color="info"
                variant="soft"
                size="sm"
                icon="i-lucide-users"
                :to="`/admin/schedules/${selectedSchedule.id}/participants`"
              >
                Ver Participantes
              </UButton>
            </div>
            <UButton
              icon="i-lucide-x"
              color="neutral"
              variant="ghost"
              size="sm"
              @click="closeScheduleModal"
            />
          </div>

          <!-- Form -->
          <form
            class="space-y-4 py-4"
            @submit.prevent="saveSchedule"
          >
            <!-- Tour Selection -->
            <div>
              <label
                class="block text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-2"
              >
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
              <p
                v-if="formErrors.tourId"
                class="mt-1 text-sm text-error"
              >
                {{ formErrors.tourId }}
              </p>
            </div>

            <!-- Date and Time -->
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label
                  class="block text-sm font-medium text-default mb-2"
                >
                  Fecha <span class="text-error">*</span>
                </label>
                <UInput
                  v-model="scheduleForm.date"
                  type="date"
                  size="lg"
                  class="w-full"
                />
                <p
                  v-if="formErrors.date"
                  class="mt-1 text-sm text-error"
                >
                  {{ formErrors.date }}
                </p>
              </div>
              <div>
                <label
                  class="block text-sm font-medium text-default mb-2"
                >
                  Hora <span class="text-error">*</span>
                </label>
                <UInput
                  v-model="scheduleForm.time"
                  type="time"
                  size="lg"
                  class="w-full"
                />
                <p
                  v-if="formErrors.time"
                  class="mt-1 text-sm text-error"
                >
                  {{ formErrors.time }}
                </p>
              </div>
            </div>

            <!-- Max Participants -->
            <div>
              <label
                class="block text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-2"
              >
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
              <p
                v-if="formErrors.maxParticipants"
                class="mt-1 text-sm text-error"
              >
                {{ formErrors.maxParticipants }}
              </p>
            </div>

            <!-- Status (only in edit mode) -->
            <div v-if="isEditMode">
              <label
                class="block text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-2"
              >
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
          <div
            class="flex justify-end gap-2 pt-4 border-t border-default"
          >
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
              {{ isEditMode ? "Actualizar" : "Crear" }}
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
import interactionPlugin, { type DateClickArg } from '@fullcalendar/interaction'
import type {
  CalendarOptions,
  EventClickArg
} from '@fullcalendar/core'
import esLocale from '@fullcalendar/core/locales/es'
import type { TourRes, TourScheduleRes, TourScheduleCreateReq, WeatherAlert } from 'api-client'
import type { DailyWeather, MoonPhase } from '~/composables/useCalendarData'
import { getLocalDateString, CHILE_TIMEZONE } from '~/utils/dateUtils'

definePageMeta({
  layout: 'admin'
})

useHead({
  title: 'Calendario - Admin - Northern Chile'
})

const { locale } = useI18n()
const toast = useToast()

const { fetchCalendarData, hasAdverseConditions, getWeatherIcon }
  = useCalendarData()

const { fetchAdminTours } = useAdminData()
const { formatLocalTime } = useDateTime()

// Calendar data interface
interface CalendarDataResponse {
  schedules: TourScheduleRes[]
  moonPhases: Map<string, MoonPhase>
  weather: Map<string, DailyWeather>
  alerts: Map<string, WeatherAlert[]>
  allAlerts?: WeatherAlert[]
}

// Estado
const calendarData = ref<CalendarDataResponse | null>(null)
const showScheduleModal = ref(false)
const selectedSchedule = ref<TourScheduleRes | null>(null)
const generating = ref(false)
const savingSchedule = ref(false)
const pendingAlerts = ref(0)

// Form state
const scheduleForm = ref({
  tourId: '',
  date: '',
  time: '',
  maxParticipants: 10,
  status: 'OPEN'
})

const formErrors = ref<Record<string, string>>({})

// Tours and guides data
const { data: toursData } = await useAsyncData(
  'admin-tours-for-schedule',
  () => fetchAdminTours(),
  {
    server: false,
    lazy: true
  }
)

// Computed options for selects
const tourOptions = computed(() => {
  const list = Array.isArray(toursData.value) ? toursData.value : (toursData.value as unknown as { data?: TourRes[] })?.data || []
  return list
    .filter((tour: TourRes) => tour.status === 'PUBLISHED')
    .map((tour: TourRes) => ({
      value: tour.id,
      label:
        tour.nameTranslations?.[locale.value]
        || tour.nameTranslations?.es
        || ''
    }))
})

const statusOptions = [
  { value: 'OPEN', label: 'Abierto' },
  { value: 'CANCELLED', label: 'Cancelado' },
  { value: 'CLOSED', label: 'Cerrado' }
]

const isEditMode = computed(() => !!selectedSchedule.value)

// Map de tours por ID para acceso rápido
const toursMap = computed(() => {
  const list = Array.isArray(toursData.value) ? toursData.value : (toursData.value as unknown as { data?: TourRes[] })?.data || []
  const map = new Map<string, TourRes>()
  list.forEach((tour: TourRes) => {
    if (tour.id) {
      map.set(tour.id, tour)
    }
  })
  return map
})

// Watch para pre-llenar la hora cuando se selecciona un tour
watch(() => scheduleForm.value.tourId, (newTourId) => {
  if (!newTourId || isEditMode.value) return // No modificar en modo edición

  const selectedTour = toursMap.value.get(newTourId)
  if (selectedTour?.defaultStartTime) {
    scheduleForm.value.time = formatLocalTime(selectedTour.defaultStartTime)
  }
})

// Rango de fechas del calendario
const startDate = ref('')
const endDate = ref('')

// Inicializar fechas
onMounted(() => {
  const today = new Date()
  startDate.value = getLocalDateString(today)

  // Mostrar próximos 60 días (máximo para tours astronómicos)
  const end = new Date(today)
  end.setDate(end.getDate() + 60)
  endDate.value = getLocalDateString(end)

  loadCalendarData()
})

// Cargar datos del calendario
const loadCalendarData = async () => {
  try {
    const data = await fetchCalendarData(startDate.value, endDate.value) as CalendarDataResponse
    calendarData.value = data
    pendingAlerts.value = Array.isArray(data.allAlerts)
      ? data.allAlerts.filter((a: WeatherAlert) => a.status === 'PENDING').length
      : 0
  } catch (error) {
    console.error('Error loading calendar data:', error)
    toast.add({
      title: 'Error',
      description: 'No se pudieron cargar los datos del calendario',
      color: 'error'
    })
  }
}

// Generar schedules
const generateSchedules = async () => {
  try {
    generating.value = true
    await $fetch('/api/admin/schedules/generate', {
      method: 'POST'
    })

    toast.add({
      title: 'Schedules generados',
      description: 'Los schedules se han generado correctamente',
      color: 'success'
    })

    // Recargar datos
    await loadCalendarData()
  } catch (error) {
    console.error('Error generating schedules:', error)
    toast.add({
      title: 'Error',
      description: 'No se pudieron generar los schedules',
      color: 'error'
    })
  } finally {
    generating.value = false
  }
}

// Click en evento (schedule)
const handleEventClick = (info: EventClickArg) => {
  const schedule = info.event.extendedProps.schedule as TourScheduleRes
  selectedSchedule.value = schedule

  // Fill form with schedule data
  // startDatetime is an Instant (ISO with Z), parse it correctly
  const scheduleDate = new Date(schedule.startDatetime)
  scheduleForm.value = {
    tourId: schedule.tourId,
    date: getLocalDateString(scheduleDate),
    time: scheduleDate.toLocaleTimeString('es-CL', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
      timeZone: CHILE_TIMEZONE
    }),
    maxParticipants: schedule.maxParticipants || 10,
    status: schedule.status || 'OPEN'
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
    time: '', // Se llenará automáticamente cuando se seleccione un tour
    maxParticipants: 10,
    status: 'OPEN'
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
    status: 'OPEN'
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

  if (
    !scheduleForm.value.maxParticipants
    || scheduleForm.value.maxParticipants < 1
  ) {
    formErrors.value.maxParticipants = 'Debe ser al menos 1'
  }

  return Object.keys(formErrors.value).length === 0
}

// Save schedule (create or update)
const saveSchedule = async () => {
  if (!validateForm()) return

  savingSchedule.value = true

  try {
    // Send date and time separately - backend handles Chile timezone conversion
    const payload: TourScheduleCreateReq & { status?: string, date?: string, time?: string } = {
      tourId: scheduleForm.value.tourId,
      date: scheduleForm.value.date,
      time: scheduleForm.value.time.length === 5 ? `${scheduleForm.value.time}:00` : scheduleForm.value.time,
      maxParticipants: scheduleForm.value.maxParticipants
    }

    // En modo edición, incluir el status
    if (isEditMode.value) {
      payload.status = scheduleForm.value.status

      // Update existing schedule
      await $fetch(
        `/api/admin/schedules/${selectedSchedule.value?.id}`,
        {
          method: 'PATCH',
          body: payload
        }
      )

      toast.add({
        title: 'Schedule actualizado',
        description: 'Los cambios se han guardado correctamente',
        color: 'success'
      })
    } else {
      // Create new schedule
      await $fetch('/api/admin/schedules', {
        method: 'POST',
        body: payload
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
  } catch (error: unknown) {
    console.error('Error saving schedule:', error)
    const apiError = error as { data?: { message?: string } }
    toast.add({
      title: 'Error',
      description: apiError.data?.message || 'No se pudo guardar el schedule',
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
    events: Array.isArray(schedules)
      ? schedules.map((schedule: TourScheduleRes) => {
          const start = new Date(schedule.startDatetime)

          // Color único para todos los tours activos
          let backgroundColor = 'var(--color-atacama-dorado-500)'

          // Verificar si tiene alertas críticas
          const scheduleAlerts = alerts?.get(schedule.id) ?? []
          const hasCriticalAlert = scheduleAlerts.some(
            (a: WeatherAlert) => a.severity === 'CRITICAL' && a.status === 'PENDING'
          )

          if (hasCriticalAlert) {
            backgroundColor = 'var(--color-warning-500)'
          }

          // El estado CLOSED y CANCELLED tiene prioridad sobre alertas
          if (schedule.status === 'CLOSED') {
            backgroundColor = 'var(--color-atacama-oxide-500)'
          }

          if (schedule.status === 'CANCELLED') {
            backgroundColor = 'var(--ui-error)'
          }

          // Formatear hora local (HH:mm)
          const timeStr = start.toLocaleTimeString('es-CL', { hour: '2-digit', minute: '2-digit', hour12: false })
          // Nombre truncado del tour + hora
          const tourName = schedule.tourNameTranslations?.[locale.value]
            || schedule.tourNameTranslations?.es
            || schedule.tourName
            || ''
          const truncatedName = tourName.length > 25 ? tourName.slice(0, 25) + '...' : tourName

          return {
            id: schedule.id,
            title: `${timeStr} - ${truncatedName}`,
            start: start,
            allDay: false,
            backgroundColor,
            borderColor: backgroundColor,
            extendedProps: {
              schedule,
              alerts: scheduleAlerts
            }
          }
        })
      : [],

    // Contenido de cada día
    dayCellContent: (arg) => {
      const date = getLocalDateString(arg.date)
      const moonPhase = moonPhases.get(date)
      const dayWeather = weather.get(date)
      const conditions = hasAdverseConditions(date, weather, moonPhases)

      // Crear HTML personalizado para el día
      const container = document.createElement('div')
      container.className = 'flex flex-col h-full p-1'

      // Número del día
      const dayNumber = document.createElement('div')
      dayNumber.className
        = 'text-right font-semibold text-default mb-1'
      dayNumber.textContent = arg.dayNumberText
      container.appendChild(dayNumber)

      // Información meteorológica y lunar
      const infoContainer = document.createElement('div')
      infoContainer.className = 'flex-1 space-y-1'

      // Luna - SIEMPRE mostrar si hay datos
      if (moonPhase) {
        const moonDiv = document.createElement('div')
        moonDiv.className = 'flex items-center gap-1'
        moonDiv.innerHTML = `
          <span class="text-lg">${moonPhase.icon}</span>
          <span class="text-xs text-muted">${moonPhase.illumination}%</span>
        `
        infoContainer.appendChild(moonDiv)
      }

      // Clima
      if (dayWeather) {
        const tempDiv = document.createElement('div')
        tempDiv.className = 'flex items-center gap-1'
        tempDiv.innerHTML = `
          <span class="text-base">${getWeatherIcon(
            dayWeather.weather[0]?.main || ''
          )}</span>
          <span class="text-xs text-muted">
            ${Math.round(dayWeather.temp.max)}°/${Math.round(
              dayWeather.temp.min
            )}°
          </span>
        `
        infoContainer.appendChild(tempDiv)
      }

      // Badges de condiciones adversas
      const badgesDiv = document.createElement('div')
      badgesDiv.className = 'flex flex-wrap gap-1 mt-1'

      if (conditions.hasWind) {
        const badge = document.createElement('span')
        badge.className = 'inline-block px-1 text-xs'
        badge.textContent = '💨'
        badgesDiv.appendChild(badge)
      }

      if (conditions.hasClouds) {
        const badge = document.createElement('span')
        badge.className = 'inline-block px-1 text-xs'
        badge.textContent = '☁️'
        badgesDiv.appendChild(badge)
      }

      if (conditions.hasRain) {
        const badge = document.createElement('span')
        badge.className = 'inline-block px-1 text-xs'
        badge.textContent = '🌧️'
        badgesDiv.appendChild(badge)
      }

      // No mostrar badge de luna llena porque ya se muestra la fase lunar arriba

      infoContainer.appendChild(badgesDiv)
      container.appendChild(infoContainer)

      return { domNodes: [container] }
    }
  }
})
</script>

<style>
/* Estilos para FullCalendar */
.fc {
  --fc-border-color: var(--ui-border-muted);
  --fc-button-bg-color: var(--ui-primary);
  --fc-button-border-color: var(--ui-primary);
  --fc-button-hover-bg-color: color-mix(in srgb, var(--ui-primary) 85%, var(--ui-bg) 15%);
  --fc-button-hover-border-color: var(--ui-primary);
  --fc-button-active-bg-color: color-mix(in srgb, var(--ui-primary) 70%, var(--ui-bg) 30%);
  --fc-button-active-border-color: var(--ui-primary);
  --fc-today-bg-color: var(--ui-bg-muted);
}

/* Header background (toolbar with prev/next/today buttons) */
.fc .fc-toolbar {
  background-color: var(--ui-primary);
  padding: 1rem;
  border-radius: 0.5rem 0.5rem 0 0;
  margin-bottom: 0;
}

/* Header text color (title and buttons) */
.fc .fc-toolbar-title,
.fc .fc-button {
  color: white !important;
  text-transform: capitalize;
  font-weight: 600;
}

.fc .fc-button {
  padding: 0.5rem 1rem;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.fc .fc-button:hover {
  background-color: rgba(255, 255, 255, 0.2) !important;
  border-color: rgba(255, 255, 255, 0.5);
}

/* Header de días (lun, mar, mié, etc.) */
.fc-col-header-cell {
  background-color: var(--ui-primary) !important;
  border-color: var(--ui-primary) !important;
  padding: 0.75rem 0.5rem !important;
  font-weight: 600 !important;
}

.fc-col-header-cell-cushion {
  color: white !important;
  font-weight: 600;
  text-decoration: none;
}

.fc-daygrid-day {
  min-height: 140px !important;
}

.fc-event {
  cursor: pointer;
  padding: 2px 4px;
  margin: 1px 0;
  font-size: 0.75rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fc-event:hover {
  opacity: 0.8;
}

.fc-event-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Estilos para el contenido personalizado de cada día */
.fc-daygrid-day-frame {
  position: relative;
}
</style>
