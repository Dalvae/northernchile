<script setup lang="ts">
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import listPlugin from '@fullcalendar/list'
import type { CalendarOptions, EventClickArg } from '@fullcalendar/core'
import esLocale from '@fullcalendar/core/locales/es'
import ptLocale from '@fullcalendar/core/locales/pt'
import enLocale from '@fullcalendar/core/locales/en-gb'
import type { TourRes, TourScheduleRes } from 'api-client'
import { getLocalDateString, unixToDateString, instantToChileLocalString } from '~/utils/dateUtils'
import type { MoonPhase } from '~/composables/useCalendarData'
import { logger } from '~/utils/logger'

interface WeatherDay {
  date: string
  maxWindKph: number
  cloudCover: number
  chanceOfRain: number
}

interface Props {
  tours: TourRes[]
  showLegend?: boolean
  height?: string
  initialView?: string
  preloadedSchedules?: TourScheduleRes[]
}

const props = withDefaults(defineProps<Props>(), {
  showLegend: true,
  height: 'auto',
  initialView: 'dayGridMonth',
  preloadedSchedules: undefined
})

// Emit schedule and optional tour (lookup from props.tours by tourId)
const emit = defineEmits<{
  scheduleClick: [schedule: TourScheduleRes, tour: TourRes | undefined]
}>()

const _router = useRouter()
const { locale, t } = useI18n()

const schedules = ref<TourScheduleRes[]>([])
const lunarData = ref<MoonPhase[]>([])
const weatherData = ref<WeatherDay[]>([])
const loading = ref(false)

interface WeatherDayData {
  date: string
  maxWindKph: number
  cloudCover: number
  chanceOfRain: number
  temp?: {
    day: number
    min: number
    max: number
  }
  weather?: Array<{
    main: string
    description: string
    icon: string
  }>
}

const weatherMap = ref(new Map<string, WeatherDayData>())
const lunarMap = ref(new Map<string, MoonPhase>())

// Detect mobile
const isMobile = ref(false)

function handleResize() {
  isMobile.value = window.innerWidth < 768
}

onMounted(async () => {
  handleResize()
  window.addEventListener('resize', handleResize)
  await fetchCalendarData()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

async function fetchSchedules() {
  // Use preloaded schedules if available
  if (props.preloadedSchedules && props.preloadedSchedules.length > 0) {
    schedules.value = props.preloadedSchedules
    return
  }

  try {
    const start = new Date()
    start.setHours(0, 0, 0, 0)

    const end = new Date()
    end.setDate(end.getDate() + 90)

    // Extract tour IDs to filter schedules
    const tourIds = props.tours.map(tour => tour.id)

    // Build query string with multiple tourIds parameters
    const params = new URLSearchParams()
    params.append('start', getLocalDateString(start))
    params.append('end', getLocalDateString(end))
    tourIds.forEach(id => params.append('tourIds', id))

    // Single API call to fetch all schedules for all tours at once
    const response = await $fetch<TourScheduleRes[]>(
      `/api/tours/schedules/all?${params.toString()}`
    )

    schedules.value = response
  } catch (e) {
    logger.error('Failed to fetch schedules', e)
    schedules.value = []
  }
}

async function fetchLunarData() {
  try {
    const start = new Date()
    start.setHours(0, 0, 0, 0)

    const end = new Date()
    end.setDate(end.getDate() + 90)

    const response = await $fetch<MoonPhase[]>(
      '/api/lunar/calendar',
      {
        params: {
          startDate: getLocalDateString(start),
          endDate: getLocalDateString(end)
        }
      }
    )

    lunarData.value = response

    lunarMap.value.clear()
    response.forEach((phase) => {
      if (phase.date) {
        lunarMap.value.set(phase.date, phase)
      }
    })
  } catch (e) {
    logger.error('Failed to fetch lunar data', e)
    lunarData.value = []
  }
}

async function fetchWeatherData() {
  try {
    interface WeatherResponse {
      daily?: Array<{
        dt: number
        windSpeed?: number
        clouds?: number
        pop?: number
        temp?: {
          day: number
          min: number
          max: number
        }
        weather?: Array<{
          main: string
          description: string
          icon: string
        }>
      }>
    }

    const response = await $fetch<WeatherResponse>(
      '/api/weather/forecast'
    )

    const weatherArray: WeatherDay[] = []
    if (response?.daily) {
      for (const day of response.daily) {
        const date = unixToDateString(day.dt)
        weatherArray.push({
          date,
          maxWindKph: (day.windSpeed || 0) / 0.514444,
          cloudCover: day.clouds || 0,
          chanceOfRain: (day.pop || 0) * 100
        })
      }
    }

    weatherData.value = weatherArray

    weatherMap.value.clear()
    if (response?.daily) {
      response.daily.forEach((day) => {
        const date = unixToDateString(day.dt)
        weatherMap.value.set(date, {
          date,
          maxWindKph: (day.windSpeed || 0) / 0.514444,
          cloudCover: day.clouds || 0,
          chanceOfRain: (day.pop || 0) * 100,
          temp: day.temp,
          weather: day.weather
        })
      })
    }
  } catch (e) {
    logger.error('Failed to fetch weather data', e)
    weatherData.value = []
  }
}

async function fetchCalendarData() {
  loading.value = true
  try {
    await Promise.all([fetchSchedules(), fetchLunarData(), fetchWeatherData()])
  } finally {
    loading.value = false
  }
}

function getMoonEmoji(phaseName: string): string {
  const moonPhases: Record<string, string> = {
    'New Moon': '🌑',
    'Waxing Crescent': '🌒',
    'First Quarter': '🌓',
    'Waxing Gibbous': '🌔',
    'Full Moon': '🌕',
    'Waning Gibbous': '🌖',
    'Last Quarter': '🌗',
    'Waning Crescent': '🌘'
  }
  return moonPhases[phaseName] || '🌑'
}

// Get tour color
function getTourColor(tourId: string): string {
  const colors = [
    'var(--color-atacama-copper-500)',
    'var(--color-atacama-sky-500)',
    'var(--color-atacama-dorado-500)',
    'var(--color-atacama-copper-700)',
    'var(--color-atacama-shadow-500)',
    'var(--color-atacama-lagoon-500)',
    'var(--color-atacama-oxide-500)'
  ]

  const index = props.tours.findIndex(t => t.id === tourId)
  return colors[index % colors.length]!
}

interface CalendarEvent {
  id?: string
  title: string
  start: string
  allDay: boolean
  display?: string
  backgroundColor?: string
  borderColor?: string
  textColor?: string
  extendedProps?: Record<string, unknown>
}

const calendarEvents = computed(() => {
  const events: CalendarEvent[] = []

  schedules.value.forEach((schedule) => {
    const startDate = new Date(schedule.startDatetime)

    // Convert UTC to Chile local time for correct calendar day placement
    // FullCalendar uses the date in the ISO string to determine which day to show the event
    const startInSantiago = instantToChileLocalString(schedule.startDatetime)

    // Use tourNameTranslations from TourScheduleRes directly (no need for tour.nameTranslations)
    const tourName
      = schedule.tourNameTranslations?.[locale.value]
        || schedule.tourNameTranslations?.es
        || schedule.tourName
        || 'Tour'

    events.push({
      id: schedule.id,
      title: `${tourName} - ${startDate.toLocaleTimeString(locale.value, {
        hour: '2-digit',
        minute: '2-digit',
        timeZone: 'America/Santiago'
      })}`,
      start: startInSantiago,
      allDay: false,
      backgroundColor: getTourColor(schedule.tourId),
      borderColor: getTourColor(schedule.tourId),
      textColor: 'var(--color-atacama-oxide-50)',
      extendedProps: {
        type: 'schedule',
        schedule: schedule,
        tourId: schedule.tourId, // Pass tourId instead of full tour object
        availableSpots: schedule.availableSpots || schedule.maxParticipants,
        bookedParticipants: schedule.bookedParticipants,
        maxParticipants: schedule.maxParticipants,
        status: schedule.status
      }
    })
  })

  lunarData.value.forEach((lunar) => {
    events.push({
      title: getMoonEmoji(lunar.phaseName || ''),
      start: lunar.date || '',
      allDay: true,
      display: 'background',
      backgroundColor: 'transparent',
      textColor: 'var(--color-atacama-shadow-500)',

      extendedProps: {
        type: 'lunar',
        phase: lunar.phaseName,
        illumination: lunar.illumination
      }
    })
  })

  weatherData.value.forEach((weather) => {
    const hasHighWind = weather.maxWindKph > 25
    const hasHighCloudCover = weather.cloudCover > 80
    const hasRain = weather.chanceOfRain > 50

    if (hasHighWind || hasHighCloudCover || hasRain) {
      let weatherIcon = ''
      if (hasHighWind) weatherIcon += '💨'
      if (hasHighCloudCover) weatherIcon += '☁️'
      if (hasRain) weatherIcon += '🌧️'

      events.push({
        title: weatherIcon,
        start: weather.date,
        allDay: true,
        display: 'background',
        backgroundColor: 'transparent',
        textColor: 'var(--color-atacama-copper-700)',

        extendedProps: {
          type: 'weather',
          wind: weather.maxWindKph,
          cloudCover: weather.cloudCover,
          rain: weather.chanceOfRain
        }
      })
    }
  })

  return events
})

const calendarOptions = computed<CalendarOptions>(() => ({
  plugins: [dayGridPlugin, interactionPlugin, listPlugin],
  initialView: isMobile.value ? 'listMonth' : props.initialView,
  timeZone: 'America/Santiago',
  locale:
    locale.value === 'es'
      ? esLocale
      : locale.value === 'pt'
        ? ptLocale
        : enLocale,
  headerToolbar: isMobile.value
    ? {
        left: 'prev,next',
        center: 'title',
        right: ''
      }
    : {
        left: 'prev,next today',
        center: 'title',
        right: 'dayGridMonth,listMonth'
      },
  events: calendarEvents.value,
  eventClick: handleEventClick,
  eventContent: renderEventContent,
  height: isMobile.value ? 'auto' : props.height,
  eventDisplay: 'block',
  displayEventTime: true,
  eventTimeFormat: {
    hour: '2-digit',
    minute: '2-digit',
    meridiem: false
  },
  buttonText: {
    today: t('common.today')
  },
  listDayFormat: { weekday: 'long', day: 'numeric', month: 'long' },
  listDaySideFormat: false,

  dayCellContent: (arg) => {
    const date = getLocalDateString(arg.date)
    const moon = lunarMap.value.get(date)
    const weather = weatherMap.value.get(date)

    const container = document.createElement('div')
    container.className = 'flex flex-col justify-between h-full p-1'

    const dayNumber = document.createElement('div')
    dayNumber.className
      = 'text-right font-semibold text-neutral-700 dark:text-neutral-300 text-sm'
    dayNumber.innerText = arg.dayNumberText
    container.appendChild(dayNumber)

    // Simplify for mobile - only show day number
    if (isMobile.value) {
      return { domNodes: [container] }
    }

    const infoBox = document.createElement('div')
    infoBox.className = 'flex justify-between items-end mt-auto'

    if (weather && weather.temp) {
      const wData = weather
      const weatherDiv = document.createElement('div')
      weatherDiv.className = 'text-xs text-neutral-500 flex flex-col'

      const icon = wData.weather?.[0]?.main === 'Clear' ? '☀️' : '☁️'

      const iconSpan = document.createElement('span')
      iconSpan.textContent = icon
      weatherDiv.appendChild(iconSpan)

      const tempSpan = document.createElement('span')
      tempSpan.className = 'font-mono'
      tempSpan.textContent = `${Math.round(wData.temp?.day || 0)}°`
      weatherDiv.appendChild(tempSpan)

      infoBox.appendChild(weatherDiv)
    }

    if (moon) {
      const moonDiv = document.createElement('div')
      moonDiv.className = 'text-xs text-neutral-500 flex flex-col items-end'

      const moonIconSpan = document.createElement('span')
      moonIconSpan.className = 'text-base'
      moonIconSpan.textContent = getMoonEmoji(moon.phaseName || '')
      moonDiv.appendChild(moonIconSpan)

      const illumSpan = document.createElement('span')
      illumSpan.className = 'scale-75 origin-right'
      illumSpan.textContent = `${moon.illumination}%`
      moonDiv.appendChild(illumSpan)

      infoBox.appendChild(moonDiv)
    }

    container.appendChild(infoBox)
    return { domNodes: [container] }
  }
}))

interface EventContentArg {
  event: {
    title: string
    extendedProps: Record<string, unknown>
    backgroundColor?: string
  }
  timeText: string
  view: {
    type: string
  }
}

function renderEventContent(arg: EventContentArg) {
  const eventType = arg.event.extendedProps.type

  // VISTA DE LISTA (MÓVIL)
  if (arg.view.type.includes('list')) {
    if (eventType === 'schedule') {
      const available = arg.event.extendedProps.availableSpots as number
      const max = arg.event.extendedProps.maxParticipants as number
      const color = arg.event.backgroundColor
      const titleParts = arg.event.title.split(' - ')
      const tourName = titleParts[0]

      return {
        html: `
          <div class="flex items-center justify-between w-full py-1 pr-2">
            <div class="flex items-center gap-3">
              <div class="w-1 h-8 rounded-full" style="background-color: ${color}"></div>
              <div class="flex flex-col">
                <span class="font-bold text-sm text-neutral-800 dark:text-white line-clamp-1">
                  ${tourName}
                </span>
                <span class="text-xs text-neutral-500 flex items-center gap-1">
                  🕒 ${arg.timeText}
                </span>
              </div>
            </div>

            <div class="flex flex-col items-end">
               <span class="text-xs font-medium px-2 py-0.5 rounded-full ${
                  available > 0
                    ? 'bg-success-100 text-success-700 dark:bg-success-900/30 dark:text-success-400'
                    : 'bg-error-100 text-error-700 dark:bg-error-900/30 dark:text-error-400'
                }">
                 ${available}/${max} cupos
               </span>
            </div>
          </div>
        `
      }
    }
    // Ocultar eventos de clima/luna en la lista
    return { domNodes: [] }
  }

  // VISTA DE GRILLA (DESKTOP)
  if (eventType === 'schedule') {
    const availableSpots = arg.event.extendedProps.availableSpots
    const maxParticipants = arg.event.extendedProps.maxParticipants
    const titleParts = arg.event.title.split(' - ')
    const tourName = titleParts[0]

    return {
      html: `
        <div class="fc-event-main-frame" style="padding: 1px 3px; font-size: 0.7rem; line-height: 1.2;">
          <div style="font-weight: 500; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
            ${arg.timeText} • ${tourName}
          </div>
          <div style="font-size: 0.65rem; opacity: 0.85; white-space: nowrap;">
            ${availableSpots}/${maxParticipants} cupos
          </div>
        </div>
      `
    }
  }

  return {
    html: `<div style="font-size: 0.7rem; padding: 1px;">${arg.timeText} ${arg.event.title}</div>`
  }
}

function handleEventClick(info: EventClickArg) {
  const eventType = info.event.extendedProps.type

  if (eventType === 'schedule') {
    const schedule = info.event.extendedProps.schedule as TourScheduleRes
    // Lookup full tour by tourId from props.tours (if needed by consumer)
    const tour = props.tours.find(t => t.id === schedule.tourId)
    emit('scheduleClick', schedule, tour)
  }
}

watch(
  () => props.tours,
  async () => {
    if (props.tours.length > 0) {
      await fetchSchedules()
    }
  },
  { deep: true }
)

defineExpose({
  refresh: fetchCalendarData
})
</script>

<template>
  <div>
    <!-- Legend (Hidden on mobile) -->
    <div
      v-if="showLegend && !isMobile"
      class="mb-4 p-4 bg-neutral-50 dark:bg-neutral-800 rounded-lg"
    >
      <h3 class="text-sm font-semibold text-neutral-900 dark:text-white mb-3">
        {{ t("schedule.legend_title") || "Leyenda" }}
      </h3>
      <div class="space-y-3">
        <!-- Tour legend -->
        <div v-if="tours.length > 0">
          <p class="text-xs font-medium text-neutral-600 dark:text-neutral-300 mb-2">
            {{ t("tours.calendar.tours") || "Tours" }}
          </p>
          <div class="flex flex-wrap gap-3">
            <div
              v-for="tour in tours.slice(0, 7)"
              :key="tour.id"
              class="flex items-center gap-2"
            >
              <div
                class="w-4 h-4 rounded"
                :style="{ backgroundColor: getTourColor(tour.id!) }"
              />
              <span class="text-sm text-neutral-700 dark:text-neutral-200">
                {{ tour.nameTranslations?.[locale] || tour.nameTranslations?.es }}
              </span>
            </div>
          </div>
        </div>

        <!-- Other legend items -->
        <div>
          <p class="text-xs font-medium text-neutral-600 dark:text-neutral-300 mb-2">
            {{ t("tours.calendar.conditions") || "Condiciones" }}
          </p>
          <div class="flex flex-wrap gap-4 text-sm">
            <div class="flex items-center gap-2">
              <span class="text-lg">🌑🌒🌓🌔🌕🌖🌗🌘</span>
              <span class="text-neutral-700 dark:text-neutral-200">
                {{ t("schedule.legend.moon_phases") || "Fases lunares" }}
              </span>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-lg">💨</span>
              <span class="text-neutral-700 dark:text-neutral-200">
                {{ t("schedule.legend.high_wind") || "Viento fuerte" }}
              </span>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-lg">☁️</span>
              <span class="text-neutral-700 dark:text-neutral-200">
                {{ t("schedule.legend.cloudy") || "Nublado" }}
              </span>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-lg">🌧️</span>
              <span class="text-neutral-700 dark:text-neutral-200">
                {{ t("schedule.legend.rain") || "Lluvia" }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Calendar -->
    <div class="bg-white dark:bg-neutral-800 rounded-lg shadow-sm p-2 sm:p-4 tour-calendar-container">
      <FullCalendar
        v-if="calendarOptions"
        :options="calendarOptions"
      />
    </div>

    <!-- Slot for additional info -->
    <slot name="info" />
  </div>
</template>

<style>
/* FullCalendar custom styles */
.tour-calendar-container .fc {
  --fc-border-color: var(--ui-border-muted);
  --fc-button-bg-color: var(--ui-primary);
  --fc-button-border-color: var(--ui-primary);
  --fc-button-hover-bg-color: color-mix(
    in srgb,
    var(--ui-primary) 85%,
    var(--ui-bg) 15%
  );
  --fc-button-hover-border-color: var(--ui-primary);
  --fc-button-active-bg-color: color-mix(
    in srgb,
    var(--ui-primary) 70%,
    var(--ui-bg) 30%
  );
  --fc-button-active-border-color: var(--ui-primary);
  --fc-today-bg-color: var(--ui-bg-muted);
}

/* Header background (toolbar with prev/next/today buttons) */
.tour-calendar-container .fc .fc-toolbar {
  background-color: var(--ui-primary);
  padding: 1rem;
  border-radius: 0.5rem 0.5rem 0 0;
  margin-bottom: 0;
  display: flex;
  align-items: center;
}

.tour-calendar-container .fc .fc-toolbar-chunk {
  flex: 1;
}

.tour-calendar-container .fc .fc-toolbar-chunk:nth-child(2) {
  text-align: center;
}

/* Header text color (title and buttons) */
.tour-calendar-container .fc .fc-toolbar-title,
.tour-calendar-container .fc .fc-button {
  color: white !important;
  text-transform: capitalize;
  font-weight: 600;
}

.tour-calendar-container .fc .fc-button {
  padding: 0.5rem 1rem;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.tour-calendar-container .fc .fc-button:hover {
  background-color: rgba(255, 255, 255, 0.2) !important;
  border-color: rgba(255, 255, 255, 0.5);
}

/* Day names header (lun, mar, mié, etc.) */
.tour-calendar-container .fc .fc-col-header-cell {
  background-color: var(--ui-primary);
  border-color: var(--ui-primary);
  padding: 0.75rem 0.5rem;
}

.tour-calendar-container .fc .fc-col-header-cell-cushion {
  color: white !important;
  font-weight: 600;
  text-decoration: none;
}

/* Day cells */
.tour-calendar-container .fc .fc-daygrid-day {
  background-color: var(--ui-bg);
}

/* Day numbers */
.tour-calendar-container .fc .fc-daygrid-day-number {
  color: var(--ui-text);
  padding: 0.5rem;
  font-weight: 500;
}

/* Events */
.tour-calendar-container .fc .fc-event {
  cursor: pointer;
  border-radius: 0.25rem;
  padding: 2px 4px;
  font-size: 0.7rem;
  margin-bottom: 1px;
  line-height: 1.2;
  min-height: auto;
}

.tour-calendar-container .fc .fc-event:hover {
  opacity: 0.8;
}

/* Prevent events from growing too tall */
.tour-calendar-container .fc .fc-daygrid-event-harness {
  margin-top: 1px;
}

.tour-calendar-container .fc .fc-event-main {
  padding: 0;
}

/* Background events (lunar, weather) */
.tour-calendar-container .fc .fc-bg-event {
  opacity: 0.7;
}

/* ESTILOS ESPECÍFICOS PARA LA VISTA DE LISTA (MÓVIL) */
.fc-list {
  border: none !important;
}

.fc-list-day-cushion {
  background-color: var(--ui-bg-muted) !important;
  padding: 12px 16px !important;
}

.fc-list-day-text,
.fc-list-day-side-text {
  font-weight: 700;
  color: var(--ui-primary);
  text-transform: capitalize;
  font-size: 1rem;
}

.fc-list-event:hover td {
  background-color: transparent !important;
}

.fc-list-event-time {
  display: none; /* Lo manejamos en el HTML custom */
}

.fc-list-event-graphic {
  display: none; /* Ocultamos el puntito por defecto */
}

.fc-list-event-title {
  padding: 8px 0 !important;
  border-bottom: 1px solid var(--ui-border-muted);
}

/* Mobile responsive styles */
@media (max-width: 768px) {
  .tour-calendar-container {
    padding: 0 !important;
    background: transparent !important;
    box-shadow: none !important;
  }

  .tour-calendar-container .fc .fc-toolbar {
    background-color: var(--ui-primary);
    padding: 0.75rem 1rem;
    border-radius: 0.5rem 0.5rem 0 0;
  }

  .tour-calendar-container .fc .fc-toolbar-title {
    font-size: 1.2rem !important;
    color: white !important;
  }

  .tour-calendar-container .fc .fc-button {
    color: white !important;
    padding: 0.5rem 0.75rem;
    font-size: 0.875rem;
  }
}
</style>
