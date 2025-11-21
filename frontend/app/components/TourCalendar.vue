<script setup lang="ts">
import FullCalendar from "@fullcalendar/vue3";
import dayGridPlugin from "@fullcalendar/daygrid";
import interactionPlugin from "@fullcalendar/interaction";
import type { CalendarOptions, EventClickArg } from "@fullcalendar/core";
import esLocale from "@fullcalendar/core/locales/es";
import ptLocale from "@fullcalendar/core/locales/pt";
import enLocale from "@fullcalendar/core/locales/en-gb";
import type { TourRes } from "api-client";

interface TourSchedule {
  id: string;
  startDatetime: string;
  maxParticipants: number;
  availableSpots?: number;
  bookedParticipants?: number;
  status: string;
  tour: TourRes;
}

interface LunarPhase {
  date: string;
  phaseName: string;
  illumination: number;
}

interface WeatherDay {
  date: string;
  maxWindKph: number;
  cloudCover: number;
  chanceOfRain: number;
}

interface Props {
  tours: TourRes[];
  showLegend?: boolean;
  height?: string;
  initialView?: string;
}

const props = withDefaults(defineProps<Props>(), {
  showLegend: true,
  height: "auto",
  initialView: "dayGridMonth",
});

const emit = defineEmits<{
  scheduleClick: [schedule: TourSchedule, tour: TourRes];
}>();

const router = useRouter();
const { locale, t } = useI18n();
const config = useRuntimeConfig();

const schedules = ref<TourSchedule[]>([]);
const lunarData = ref<LunarPhase[]>([]);
const weatherData = ref<WeatherDay[]>([]);
const loading = ref(false);

const weatherMap = ref(new Map<string, any>());
const lunarMap = ref(new Map<string, LunarPhase>());

async function fetchSchedules() {
  try {
    const start = new Date();
    start.setHours(0, 0, 0, 0);

    const end = new Date();
    end.setDate(end.getDate() + 90);

    const formatDate = (date: Date) => date.toISOString().split("T")[0];

    const allSchedules = await Promise.all(
      props.tours.map(async (tour) => {
        try {
          const response = await $fetch<Array<Omit<TourSchedule, "tour">>>(
            `${config.public.apiBase}/api/tours/${tour.id}/schedules`,
            {
              params: {
                start: formatDate(start),
                end: formatDate(end),
              },
            },
          );
          return response.map((s) => ({ ...s, tour }));
        } catch {
          return [];
        }
      }),
    );

    schedules.value = allSchedules.flat();
  } catch (e) {
    console.error("Failed to fetch schedules", e);
    schedules.value = [];
  }
}

async function fetchLunarData() {
  try {
    const start = new Date();
    start.setHours(0, 0, 0, 0);

    const end = new Date();
    end.setDate(end.getDate() + 90);

    const formatDate = (date: Date) => date.toISOString().split("T")[0];

    const response = await $fetch<LunarPhase[]>(
      `${config.public.apiBase}/api/lunar/calendar`,
      {
        params: {
          startDate: formatDate(start),
          endDate: formatDate(end),
        },
      },
    );

    lunarData.value = response;

    lunarMap.value.clear();
    response.forEach((phase) => lunarMap.value.set(phase.date, phase));
  } catch (e) {
    console.error("Failed to fetch lunar data", e);
    lunarData.value = [];
  }
}

async function fetchWeatherData() {
  try {
    interface WeatherResponse {
      daily?: Array<{
        dt: number;
        windSpeed?: number;
        clouds?: number;
        pop?: number;
        temp?: {
          day: number;
          min: number;
          max: number;
        };
        weather?: Array<{
          main: string;
          description: string;
          icon: string;
        }>;
      }>;
    }

    const response = await $fetch<WeatherResponse>(
      `${config.public.apiBase}/api/weather/forecast`,
    );

    const weatherArray: WeatherDay[] = [];
    if (response?.daily) {
      for (const day of response.daily) {
        const date = new Date(day.dt * 1000).toISOString().split("T")[0];
        weatherArray.push({
          date,
          maxWindKph: (day.windSpeed || 0) / 0.514444,
          cloudCover: day.clouds || 0,
          chanceOfRain: (day.pop || 0) * 100,
        });
      }
    }

    weatherData.value = weatherArray;

    weatherMap.value.clear();
    if (response?.daily) {
      response.daily.forEach((day: any) => {
        const date = new Date(day.dt * 1000).toISOString().split("T")[0];
        weatherMap.value.set(date, {
          date,
          maxWindKph: (day.windSpeed || 0) / 0.514444,
          cloudCover: day.clouds || 0,
          chanceOfRain: (day.pop || 0) * 100,
          temp: day.temp,
          weather: day.weather,
        });
      });
    }
  } catch (e) {
    console.error("Failed to fetch weather data", e);
    weatherData.value = [];
  }
}

async function fetchCalendarData() {
  loading.value = true;
  try {
    await Promise.all([fetchSchedules(), fetchLunarData(), fetchWeatherData()]);
  } finally {
    loading.value = false;
  }
}

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
  };
  return moonPhases[phaseName] || "🌑";
}

// Get tour color
function getTourColor(tourId: string): string {
  const colors = [
    "var(--color-atacama-copper-500)",
    "var(--color-atacama-sky-500)",
    "var(--color-atacama-dorado-500)",
    "var(--color-atacama-copper-700)",
    "var(--color-atacama-shadow-500)",
    "var(--color-atacama-lagoon-500)",
    "var(--color-atacama-oxide-500)",
  ];

  const index = props.tours.findIndex((t) => t.id === tourId);
  return colors[index % colors.length]!;
}

interface CalendarEvent {
  id?: string;
  title: string;
  start: string;
  allDay: boolean;
  display?: string;
  backgroundColor?: string;
  borderColor?: string;
  textColor?: string;
  extendedProps?: Record<string, unknown>;
}

const calendarEvents = computed(() => {
  const events: CalendarEvent[] = [];

  schedules.value.forEach((schedule) => {
    const startDate = new Date(schedule.startDatetime);
    const tourName =
      schedule.tour?.nameTranslations?.[locale.value] ||
      schedule.tour?.nameTranslations?.es ||
      "Tour";

    events.push({
      id: schedule.id,
      title: `${tourName} - ${startDate.toLocaleTimeString(locale.value, {
        hour: "2-digit",
        minute: "2-digit",
      })}`,
      start: schedule.startDatetime,
      allDay: false,
      backgroundColor: getTourColor(schedule.tour?.id),
      borderColor: getTourColor(schedule.tour?.id),
      textColor: "var(--color-atacama-oxide-50)",
      extendedProps: {
        type: "schedule",
        schedule: schedule,
        tour: schedule.tour,
        availableSpots: schedule.availableSpots || schedule.maxParticipants,
        bookedParticipants: schedule.bookedParticipants || 0,
        maxParticipants: schedule.maxParticipants,
        status: schedule.status,
      },
    });
  });

  lunarData.value.forEach((lunar) => {
    events.push({
      title: getMoonEmoji(lunar.phaseName),
      start: lunar.date,
      allDay: true,
      display: "background",
      backgroundColor: "transparent",
      textColor: "var(--color-atacama-shadow-500)",

      extendedProps: {
        type: "lunar",
        phase: lunar.phaseName,
        illumination: lunar.illumination,
      },
    });
  });

  weatherData.value.forEach((weather) => {
    const hasHighWind = weather.maxWindKph > 25;
    const hasHighCloudCover = weather.cloudCover > 80;
    const hasRain = weather.chanceOfRain > 50;

    if (hasHighWind || hasHighCloudCover || hasRain) {
      let weatherIcon = "";
      if (hasHighWind) weatherIcon += "💨";
      if (hasHighCloudCover) weatherIcon += "☁️";
      if (hasRain) weatherIcon += "🌧️";

      events.push({
        title: weatherIcon,
        start: weather.date,
        allDay: true,
        display: "background",
        backgroundColor: "transparent",
        textColor: "var(--color-atacama-copper-700)",

        extendedProps: {
          type: "weather",
          wind: weather.maxWindKph,
          cloudCover: weather.cloudCover,
          rain: weather.chanceOfRain,
        },
      });
    }
  });

  return events;
});

const calendarOptions = computed<CalendarOptions>(() => ({
  plugins: [dayGridPlugin, interactionPlugin],
  initialView: props.initialView,
  locale:
    locale.value === "es"
      ? esLocale
      : locale.value === "pt"
        ? ptLocale
        : enLocale,
  headerToolbar: {
    left: "prev,next today",
    center: "title",
    right: "",
  },
  events: calendarEvents.value,
  eventClick: handleEventClick,
  eventContent: renderEventContent,
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

  dayCellContent: (arg) => {
    const date = arg.date.toISOString().split("T")[0];
    const moon = lunarMap.value.get(date);
    const weather = weatherMap.value.get(date); // any para acceder a temp y weather

    const container = document.createElement("div");
    container.className = "flex flex-col justify-between h-full p-1";

    const dayNumber = document.createElement("div");
    dayNumber.className =
      "text-right font-semibold text-neutral-700 dark:text-neutral-300 text-sm";
    dayNumber.innerText = arg.dayNumberText;
    container.appendChild(dayNumber);

    const infoBox = document.createElement("div");
    infoBox.className = "flex justify-between items-end mt-auto";

    if (weather && (weather as any).temp) {
      const wData = weather as any;
      const weatherDiv = document.createElement("div");
      weatherDiv.className = "text-xs text-neutral-500 flex flex-col";

      const icon = wData.weather?.[0]?.main === "Clear" ? "☀️" : "☁️";

      weatherDiv.innerHTML = `
        <span>${icon}</span>
        <span class="font-mono">${Math.round(wData.temp.day)}°</span>
      `;
      infoBox.appendChild(weatherDiv);
    }

    if (moon) {
      const moonDiv = document.createElement("div");
      moonDiv.className = "text-xs text-neutral-500 flex flex-col items-end";
      moonDiv.innerHTML = `
        <span class="text-base">${getMoonEmoji(moon.phaseName)}</span>
        <span class="scale-75 origin-right">${moon.illumination}%</span>
      `;
      infoBox.appendChild(moonDiv);
    }

    container.appendChild(infoBox);
    return { domNodes: [container] };
  },
}));

interface EventContentArg {
  event: {
    title: string;
    extendedProps: Record<string, unknown>;
  };
  timeText: string;
}

function renderEventContent(arg: EventContentArg) {
  const eventType = arg.event.extendedProps.type;

  if (eventType === "schedule") {
    const availableSpots = arg.event.extendedProps.availableSpots;
    const maxParticipants = arg.event.extendedProps.maxParticipants;

    const titleParts = arg.event.title.split(" - ");
    const tourName = titleParts[0];

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
      `,
    };
  }

  return {
    html: `<div style="font-size: 0.7rem; padding: 1px;">${arg.timeText} ${arg.event.title}</div>`,
  };
}

function handleEventClick(info: EventClickArg) {
  const eventType = info.event.extendedProps.type;

  if (eventType === "schedule") {
    const schedule = info.event.extendedProps.schedule;
    const tour = info.event.extendedProps.tour;
    emit("scheduleClick", schedule, tour);
  }
}

watch(
  () => props.tours,
  async () => {
    if (props.tours.length > 0) {
      await fetchSchedules();
    }
  },
  { deep: true },
);

onMounted(async () => {
  await fetchCalendarData();
});

defineExpose({
  refresh: fetchCalendarData,
});
</script>

<template>
  <div>
    <div v-if="loading" class="text-center py-12">
      <UIcon
        name="i-lucide-loader-2"
        class="w-8 h-8 animate-spin text-primary mx-auto"
      />
      <p class="mt-4 text-neutral-600 dark:text-neutral-300">
        {{ t("tours.calendar.loading") || "Cargando calendario..." }}
      </p>
    </div>

    <div v-else>
      <!-- Legend -->
      <div
        v-if="showLegend"
        class="mb-4 p-4 bg-neutral-50 dark:bg-neutral-800 rounded-lg"
      >
        <h3 class="text-sm font-semibold text-neutral-900 dark:text-white mb-3">
          {{ t("schedule.legend_title") || "Leyenda" }}
        </h3>
        <div class="space-y-3">
          <!-- Tour legend -->
          <div v-if="tours.length > 0">
            <p
              class="text-xs font-medium text-neutral-600 dark:text-neutral-300 mb-2"
            >
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
                  {{
                    tour.nameTranslations?.[locale] || tour.nameTranslations?.es
                  }}
                </span>
              </div>
            </div>
          </div>

          <!-- Other legend items -->
          <div>
            <p
              class="text-xs font-medium text-neutral-600 dark:text-neutral-300 mb-2"
            >
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
      <div
        class="bg-white dark:bg-neutral-800 rounded-lg shadow-sm p-4 tour-calendar-container"
      >
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
</style>
