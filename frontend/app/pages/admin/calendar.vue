<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">
          Calendario de Tours
        </h1>
        <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">
          Gestiona schedules con información climática y lunar
        </p>
      </div>

      <div class="flex gap-2">
        <!-- Badge de alertas -->
        <UButton
          v-if="pendingAlerts > 0"
          color="red"
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
    <div class="mb-4 p-4 bg-gray-50 dark:bg-gray-800 rounded-lg">
      <div class="flex flex-wrap gap-4 text-sm">
        <div class="flex items-center gap-2">
          <span class="text-lg">🌑🌒🌓🌔🌕🌖🌗🌘</span>
          <span class="text-gray-700 dark:text-gray-300">Fases lunares</span>
        </div>
        <div class="flex items-center gap-2">
          <UBadge color="red" variant="soft" size="xs">💨 Viento</UBadge>
          <span class="text-gray-700 dark:text-gray-300">&gt;25 nudos</span>
        </div>
        <div class="flex items-center gap-2">
          <UBadge color="yellow" variant="soft" size="xs">☁️ Nublado</UBadge>
          <span class="text-gray-700 dark:text-gray-300">&gt;80%</span>
        </div>
        <div class="flex items-center gap-2">
          <UBadge color="blue" variant="soft" size="xs">🌧️ Lluvia</UBadge>
          <span class="text-gray-700 dark:text-gray-300">Probabilidad &gt;50%</span>
        </div>
        <div class="flex items-center gap-2">
          <UBadge color="purple" variant="soft" size="xs">🌕</UBadge>
          <span class="text-gray-700 dark:text-gray-300">Luna llena</span>
        </div>
      </div>
    </div>

    <!-- Calendario -->
    <div class="bg-white dark:bg-gray-900 rounded-lg shadow-sm p-4">
      <FullCalendar
        v-if="calendarOptions"
        :options="calendarOptions"
      />
    </div>

    <!-- Modal de schedule (crear/editar) -->
    <UModal v-model="showScheduleModal">
      <UCard>
        <template #header>
          <h3 class="text-lg font-semibold">
            {{ selectedSchedule ? 'Editar Schedule' : 'Crear Schedule' }}
          </h3>
        </template>

        <div class="space-y-4">
          <p class="text-sm text-gray-600 dark:text-gray-400">
            Funcionalidad de crear/editar schedules pendiente de implementación
          </p>
          <!-- TODO: Formulario de schedule -->
        </div>

        <template #footer>
          <div class="flex justify-end gap-2">
            <UButton color="gray" variant="ghost" @click="showScheduleModal = false">
              Cancelar
            </UButton>
            <UButton color="primary">
              Guardar
            </UButton>
          </div>
        </template>
      </UCard>
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

// Estado
const calendarData = ref<any>(null)
const showScheduleModal = ref(false)
const selectedSchedule = ref<any>(null)
const generating = ref(false)
const pendingAlerts = ref(0)

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
  selectedSchedule.value = info.event.extendedProps.schedule
  showScheduleModal.value = true
}

// Click en día vacío
const handleDateClick = (info: DateClickArg) => {
  selectedSchedule.value = null
  showScheduleModal.value = true
  // TODO: Pre-rellenar con la fecha clickeada
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
          <span class="text-gray-500">${moonPhase.illumination}%</span>
        `
        weatherInfo.appendChild(moonDiv)
      }

      // Clima
      if (dayWeather) {
        const tempDiv = document.createElement('div')
        tempDiv.className = 'flex items-center gap-1'
        tempDiv.innerHTML = `
          <span>${getWeatherIcon(dayWeather.weather[0]?.main)}</span>
          <span class="text-gray-700 dark:text-gray-300">
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