<script setup lang="ts">
import FullCalendar from "@fullcalendar/vue3"
import dayGridPlugin from "@fullcalendar/daygrid"
import interactionPlugin from "@fullcalendar/interaction"
import type { CalendarOptions, EventClickArg } from "@fullcalendar/core"
import esLocale from "@fullcalendar/core/locales/es"
import ptLocale from "@fullcalendar/core/locales/pt"
import enLocale from "@fullcalendar/core/locales/en-gb"
import type { TourRes } from "~/lib/api-client"

interface Props {
  tours: TourRes[]
  showLegend?: boolean
  height?: string
  initialView?: string
}

const props = withDefaults(defineProps<Props>(), {
  showLegend: true,
  height: "auto",
  initialView: "dayGridMonth",
})

const emit = defineEmits<{
  scheduleClick: [schedule: any, tour: any]
}>()

const router = useRouter()
const { locale, t } = useI18n()
const config = useRuntimeConfig()

// Calendar data
const schedules = ref<any[]>([])
const lunarData = ref<any[]>([])
const weatherData = ref<any[]>([])
const loading = ref(false)

// Fetch all schedules for the given tours
async function fetchSchedules() {
  try {
    const start = new Date()
    start.setHours(0, 0, 0, 0) // Desde hoy a las 00:00

    const end = new Date()
    end.setDate(end.getDate() + 90)

    const formatDate = (date: Date) => date.toISOString().split("T")[0]

    const allSchedules = await Promise.all(
      props.tours.map(async (tour) => {
        try {
          const response = await $fetch(`${config.public.apiBase}/api/tours/${tour.id}/schedules`, {
            params: {
              start: formatDate(start),
              end: formatDate(end),
            },
          })
          return (response as any[]).map(s => ({ ...s, tour }))
        } catch {
          return []
        }
      })
    )

    schedules.value = allSchedules.flat()
  } catch (e: any) {
    console.error("Failed to fetch schedules", e)
    schedules.value = []
  }
}

// Fetch lunar phases
async function fetchLunarData() {
  try {
    const start = new Date()
    start.setHours(0, 0, 0, 0)

    const end = new Date()
    end.setDate(end.getDate() + 90)

    const formatDate = (date: Date) => date.toISOString().split("T")[0]

    const response = await $fetch(`${config.public.apiBase}/api/lunar/calendar`, {
      params: {
        startDate: formatDate(start),
        endDate: formatDate(end),
      },
    })

    lunarData.value = response as any[]
  } catch (e: any) {
    console.error("Failed to fetch lunar data", e)
    lunarData.value = []
  }
}

// Fetch weather forecast
async function fetchWeatherData() {
  try {
    const response = await $fetch(`${config.public.apiBase}/api/weather/forecast`)

    // Parse weather response
    const weatherArray: any[] = []
    if ((response as any)?.daily) {
      for (const day of (response as any).daily) {
        const date = new Date(day.dt * 1000).toISOString().split("T")[0]
        weatherArray.push({
          date,
          maxWindKph: (day.windSpeed || 0) / 0.514444,
          cloudCover: day.clouds || 0,
          chanceOfRain: (day.pop || 0) * 100,
        })
      }
    }

    weatherData.value = weatherArray
  } catch (e: any) {
    console.error("Failed to fetch weather data", e)
    weatherData.value = []
  }
}

async function fetchCalendarData() {
  loading.value = true
  try {
    await Promise.all([
      fetchSchedules(),
      fetchLunarData(),
      fetchWeatherData(),
    ])
  } finally {
    loading.value = false
  }
}

// Get moon emoji for phase
function getMoonEmoji(phaseName: string): string {
  const moonPhases: Record<string, string> = {
    "New Moon": "🌑",
    "Waxing Crescent": "🌒",
    "First Quarter": "🌓",
    "Waxing Gibbous": "🌔",
    "Full Moon": "🌕",
    "Waning Gibbous": "🌖",
    "Last Quarter": "🌗",
    "Waning Crescent": "🌘",
  }
  return moonPhases[phaseName] || "🌑"
}

// Get tour color
function getTourColor(tourId: string): string {
  const colors = [
    "#10b981", // green
    "#3b82f6", // blue
    "#f59e0b", // amber
    "#ef4444", // red
    "#8b5cf6", // purple
    "#ec4899", // pink
    "#14b8a6", // teal
  ]

  const index = props.tours.findIndex(t => t.id === tourId)
  return colors[index % colors.length]
}

// Convert schedules to FullCalendar events
const calendarEvents = computed(() => {
  const events: any[] = []

  // Add tour schedules
  schedules.value.forEach((schedule) => {
    const startDate = new Date(schedule.startDatetime)
    const tourName = schedule.tour?.nameTranslations?.[locale.value] ||
                     schedule.tour?.nameTranslations?.es ||
                     "Tour"

    events.push({
      id: schedule.id,
      title: `${tourName} - ${startDate.toLocaleTimeString(locale.value, {
        hour: "2-digit",
        minute: "2-digit"
      })}`,
      start: schedule.startDatetime,
      allDay: false,
      backgroundColor: getTourColor(schedule.tour?.id),
      borderColor: getTourColor(schedule.tour?.id),
      textColor: "#ffffff",
      extendedProps: {
        type: "schedule",
        schedule: schedule,
        tour: schedule.tour,
        availableSpots: schedule.maxParticipants,
        status: schedule.status,
      },
    })
  })

  // Add lunar phases as background events
  lunarData.value.forEach((lunar) => {
    events.push({
      title: getMoonEmoji(lunar.phaseName),
      start: lunar.date,
      allDay: true,
      display: "background",
      backgroundColor: "transparent",
      textColor: "#6b7280",
      extendedProps: {
        type: "lunar",
        phase: lunar.phaseName,
        illumination: lunar.illumination,
      },
    })
  })

  // Add weather alerts as background events
  weatherData.value.forEach((weather) => {
    const hasHighWind = weather.maxWindKph > 25
    const hasHighCloudCover = weather.cloudCover > 80
    const hasRain = weather.chanceOfRain > 50

    if (hasHighWind || hasHighCloudCover || hasRain) {
      let weatherIcon = ""
      if (hasHighWind) weatherIcon += "💨"
      if (hasHighCloudCover) weatherIcon += "☁️"
      if (hasRain) weatherIcon += "🌧️"

      events.push({
        title: weatherIcon,
        start: weather.date,
        allDay: true,
        display: "background",
        backgroundColor: "transparent",
        textColor: "#ef4444",
        extendedProps: {
          type: "weather",
          wind: weather.maxWindKph,
          cloudCover: weather.cloudCover,
          rain: weather.chanceOfRain,
        },
      })
    }
  })

  return events
})

// FullCalendar options
const calendarOptions = computed<CalendarOptions>(() => ({
  plugins: [dayGridPlugin, interactionPlugin],
  initialView: props.initialView,
  locale: locale.value === "es" ? esLocale : locale.value === "pt" ? ptLocale : enLocale,
  headerToolbar: {
    left: "prev,next today",
    center: "title",
    right: "",
  },
  events: calendarEvents.value,
  eventClick: handleEventClick,
  height: props.height,
  eventDisplay: "block",
  displayEventTime: true,
  eventTimeFormat: {
    hour: "2-digit",
    minute: "2-digit",
    meridiem: false,
  },
  buttonText: {
    today: t("common.today"),
  },
}))

// Handle event click
function handleEventClick(info: EventClickArg) {
  const eventType = info.event.extendedProps.type

  if (eventType === "schedule") {
    const schedule = info.event.extendedProps.schedule
    const tour = info.event.extendedProps.tour
    emit("scheduleClick", schedule, tour)
  }
}

// Watch tours prop and refetch when it changes
watch(() => props.tours, async () => {
  if (props.tours.length > 0) {
    await fetchSchedules()
  }
}, { deep: true })

// Load calendar data on mount
onMounted(async () => {
  await fetchCalendarData()
})

// Expose refresh method
defineExpose({
  refresh: fetchCalendarData,
})
</script>

<template>
  <div>
    <div v-if="loading" class="text-center py-12">
      <UIcon
        name="i-lucide-loader-2"
        class="w-8 h-8 animate-spin text-primary mx-auto"
      />
      <p class="mt-4 text-neutral-600 dark:text-neutral-400">
        {{ t("tours.calendar.loading") || "Cargando calendario..." }}
      </p>
    </div>

    <div v-else>
      <!-- Legend -->
      <div v-if="showLegend" class="mb-4 p-4 bg-neutral-50 dark:bg-neutral-800 rounded-lg">
        <h3 class="text-sm font-semibold text-neutral-900 dark:text-white mb-3">
          {{ t("schedule.legend_title") || "Leyenda" }}
        </h3>
        <div class="space-y-3">
          <!-- Tour legend -->
          <div v-if="tours.length > 0">
            <p class="text-xs font-medium text-neutral-600 dark:text-neutral-400 mb-2">
              {{ t("tours.calendar.tours") || "Tours" }}
            </p>
            <div class="flex flex-wrap gap-3">
              <div v-for="tour in tours.slice(0, 7)" :key="tour.id" class="flex items-center gap-2">
                <div class="w-4 h-4 rounded" :style="{ backgroundColor: getTourColor(tour.id) }"></div>
                <span class="text-sm text-neutral-700 dark:text-neutral-300">
                  {{ tour.nameTranslations?.[locale] || tour.nameTranslations?.es }}
                </span>
              </div>
            </div>
          </div>

          <!-- Other legend items -->
          <div>
            <p class="text-xs font-medium text-neutral-600 dark:text-neutral-400 mb-2">
              {{ t("tours.calendar.conditions") || "Condiciones" }}
            </p>
            <div class="flex flex-wrap gap-4 text-sm">
              <div class="flex items-center gap-2">
                <span class="text-lg">🌑🌒🌓🌔🌕🌖🌗🌘</span>
                <span class="text-neutral-700 dark:text-neutral-300">
                  {{ t("schedule.legend.moon_phases") || "Fases lunares" }}
                </span>
              </div>
              <div class="flex items-center gap-2">
                <span class="text-lg">💨</span>
                <span class="text-neutral-700 dark:text-neutral-300">
                  {{ t("schedule.legend.high_wind") || "Viento fuerte" }}
                </span>
              </div>
              <div class="flex items-center gap-2">
                <span class="text-lg">☁️</span>
                <span class="text-neutral-700 dark:text-neutral-300">
                  {{ t("schedule.legend.cloudy") || "Nublado" }}
                </span>
              </div>
              <div class="flex items-center gap-2">
                <span class="text-lg">🌧️</span>
                <span class="text-neutral-700 dark:text-neutral-300">
                  {{ t("schedule.legend.rain") || "Lluvia" }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Calendar -->
      <div class="bg-white dark:bg-neutral-800 rounded-lg shadow-sm p-4 tour-calendar-container">
        <FullCalendar v-if="calendarOptions" :options="calendarOptions" />
      </div>

      <!-- Slot for additional info -->
      <slot name="info" />
    </div>
  </div>
</template>

<style>
/* FullCalendar custom styles */
.tour-calendar-container .fc {
  --fc-border-color: rgb(229 231 235);
  --fc-button-bg-color: rgb(14 165 233);
  --fc-button-border-color: rgb(14 165 233);
  --fc-button-hover-bg-color: rgb(2 132 199);
  --fc-button-hover-border-color: rgb(2 132 199);
  --fc-button-active-bg-color: rgb(3 105 161);
  --fc-button-active-border-color: rgb(3 105 161);
  --fc-today-bg-color: rgb(224 242 254);
}

.dark .tour-calendar-container .fc {
  --fc-border-color: rgb(64 64 64);
  --fc-today-bg-color: rgb(23 37 84);
}

/* Header background (toolbar with prev/next/today buttons) */
.tour-calendar-container .fc .fc-toolbar {
  background-color: rgb(14 165 233);
  padding: 1rem;
  border-radius: 0.5rem 0.5rem 0 0;
  margin-bottom: 0;
}

.dark .tour-calendar-container .fc .fc-toolbar {
  background-color: rgb(3 105 161);
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
  background-color: rgb(14 165 233);
  border-color: rgb(2 132 199);
  padding: 0.75rem 0.5rem;
}

.dark .tour-calendar-container .fc .fc-col-header-cell {
  background-color: rgb(3 105 161);
  border-color: rgb(7 89 133);
}

.tour-calendar-container .fc .fc-col-header-cell-cushion {
  color: white !important;
  font-weight: 600;
  text-decoration: none;
}

/* Day cells */
.tour-calendar-container .fc .fc-daygrid-day {
  background-color: white;
}

.dark .tour-calendar-container .fc .fc-daygrid-day {
  background-color: rgb(38 38 38);
}

/* Day numbers */
.tour-calendar-container .fc .fc-daygrid-day-number {
  color: rgb(64 64 64);
  padding: 0.5rem;
  font-weight: 500;
}

.dark .tour-calendar-container .fc .fc-daygrid-day-number {
  color: rgb(212 212 212);
}

/* Events */
.tour-calendar-container .fc .fc-event {
  cursor: pointer;
  border-radius: 0.25rem;
  padding: 0.25rem 0.5rem;
  font-size: 0.875rem;
  margin-bottom: 2px;
}

.tour-calendar-container .fc .fc-event:hover {
  opacity: 0.8;
}

/* Background events (lunar, weather) */
.tour-calendar-container .fc .fc-bg-event {
  opacity: 0.7;
}
</style>
