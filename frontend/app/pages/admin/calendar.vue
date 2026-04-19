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
        <UButton
          color="secondary"
          variant="soft"
          icon="i-lucide-calendar-range"
          @click="openBulkScheduleModal"
        >
          Crear en Lote
        </UButton>

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
                {{ scheduleModalTitle }}
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

          <div
            v-if="!isEditMode"
            class="flex gap-2 pt-4"
          >
            <UButton
              type="button"
              color="primary"
              :variant="scheduleModalMode === 'single' ? 'solid' : 'soft'"
              @click="setScheduleModalMode('single')"
            >
              Individual
            </UButton>
            <UButton
              type="button"
              color="secondary"
              :variant="scheduleModalMode === 'bulk' ? 'solid' : 'soft'"
              @click="setScheduleModalMode('bulk')"
            >
              En Lote
            </UButton>
          </div>

          <!-- Form -->
          <form
            class="space-y-4 py-4"
            @submit.prevent="handleScheduleSubmit"
          >
            <template v-if="scheduleModalMode === 'bulk'">
              <div>
                <label
                  class="block text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-2"
                >
                  Tour <span class="text-error">*</span>
                </label>
                <USelect
                  v-model="bulkScheduleForm.tourId"
                  :items="tourOptions"
                  option-attribute="label"
                  value-attribute="value"
                  placeholder="Selecciona un tour"
                  size="lg"
                  class="w-full"
                />
                <p
                  v-if="formErrors.tourId"
                  class="mt-1 text-sm text-error"
                >
                  {{ formErrors.tourId }}
                </p>
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label
                    class="block text-sm font-medium text-default mb-2"
                  >
                    Desde <span class="text-error">*</span>
                  </label>
                  <UInput
                    v-model="bulkScheduleForm.startDate"
                    type="date"
                    size="lg"
                    class="w-full"
                  />
                  <p
                    v-if="formErrors.startDate"
                    class="mt-1 text-sm text-error"
                  >
                    {{ formErrors.startDate }}
                  </p>
                </div>
                <div>
                  <label
                    class="block text-sm font-medium text-default mb-2"
                  >
                    Hasta <span class="text-error">*</span>
                  </label>
                  <UInput
                    v-model="bulkScheduleForm.endDate"
                    type="date"
                    size="lg"
                    class="w-full"
                  />
                  <p
                    v-if="formErrors.endDate"
                    class="mt-1 text-sm text-error"
                  >
                    {{ formErrors.endDate }}
                  </p>
                </div>
              </div>

              <div>
                <label
                  class="block text-sm font-medium text-default mb-2"
                >
                  Días a crear <span class="text-error">*</span>
                </label>
                <div class="flex flex-wrap gap-2">
                  <UButton
                    v-for="weekday in weekdayOptions"
                    :key="weekday.value"
                    type="button"
                    size="sm"
                    :color="isBulkWeekdaySelected(weekday.value) ? 'primary' : 'neutral'"
                    :variant="isBulkWeekdaySelected(weekday.value) ? 'solid' : 'outline'"
                    @click="toggleBulkWeekday(weekday.value)"
                  >
                    {{ weekday.label }}
                  </UButton>
                </div>

                <div class="flex gap-2 mt-3">
                  <UButton
                    type="button"
                    size="xs"
                    color="neutral"
                    variant="ghost"
                    @click="selectAllBulkWeekdays"
                  >
                    Todos
                  </UButton>
                  <UButton
                    type="button"
                    size="xs"
                    color="neutral"
                    variant="ghost"
                    @click="clearBulkWeekdays"
                  >
                    Limpiar
                  </UButton>
                </div>

                <p
                  v-if="formErrors.weekdays"
                  class="mt-1 text-sm text-error"
                >
                  {{ formErrors.weekdays }}
                </p>
                <p class="mt-2 text-xs text-muted">
                  {{ bulkSelectionSummary }}
                </p>
                <p class="mt-1 text-xs text-muted">
                  Los schedules existentes para el mismo tour y hora se omitirán automáticamente.
                </p>
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label
                    class="block text-sm font-medium text-default mb-2"
                  >
                    Hora <span class="text-error">*</span>
                  </label>
                  <UInput
                    v-model="bulkScheduleForm.time"
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
                <div>
                  <label
                    class="block text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-2"
                  >
                    Cupos Máximos <span class="text-error">*</span>
                  </label>
                  <UInput
                    v-model.number="bulkScheduleForm.maxParticipants"
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
              </div>
            </template>

            <template v-else>
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
            </template>
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
              @click="handleScheduleSubmit"
            >
              {{ scheduleSubmitLabel }}
            </UButton>
          </div>
        </div>
      </template>
    </UModal>
  </div>
</template>

<script setup lang="ts">
import FullCalendar from '@fullcalendar/vue3'
import logger from '~/utils/logger'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin, { type DateClickArg } from '@fullcalendar/interaction'
import type {
  CalendarOptions,
  EventClickArg
} from '@fullcalendar/core'
import esLocale from '@fullcalendar/core/locales/es'
import type { TourRes, TourScheduleRes, TourScheduleCreateReq, TourScheduleCreateReqStatusEnum, WeatherAlertRes } from 'api-client'
import type { DailyWeather, MoonPhase } from '~/composables/useCalendarData'
import { getLocalDateString, CHILE_TIMEZONE, instantToChileLocalString } from '~/utils/dateUtils'

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

const { formatLocalTime } = useDateTime()
const { fetchAdminSchedules } = useAdminData()

const MAX_SCHEDULE_WINDOW_DAYS = 365
const BULK_CREATE_BATCH_SIZE = 8
const DEFAULT_BULK_WEEKDAYS = [1, 2, 3, 4, 5, 6, 0]

type ScheduleModalMode = 'single' | 'bulk'

// Calendar data interface
interface CalendarDataResponse {
  schedules: TourScheduleRes[]
  moonPhases: Map<string, MoonPhase>
  weather: Map<string, DailyWeather>
  alerts: Map<string, WeatherAlert[]>
  allAlerts?: WeatherAlertRes[]
}

// Estado
const calendarData = ref<CalendarDataResponse | null>(null)
const showScheduleModal = ref(false)
const selectedSchedule = ref<TourScheduleRes | null>(null)
const generating = ref(false)
const savingSchedule = ref(false)
const pendingAlerts = ref(0)
const scheduleModalMode = ref<ScheduleModalMode>('single')

const createEmptyScheduleForm = () => ({
  tourId: '',
  date: '',
  time: '',
  maxParticipants: 10,
  status: 'OPEN'
})

const createEmptyBulkScheduleForm = () => {
  const today = getLocalDateString(new Date())

  return {
    tourId: '',
    startDate: today,
    endDate: today,
    time: '',
    maxParticipants: 10,
    weekdays: [...DEFAULT_BULK_WEEKDAYS]
  }
}

// Form state
const scheduleForm = ref(createEmptyScheduleForm())
const bulkScheduleForm = ref(createEmptyBulkScheduleForm())
const formErrors = ref<Record<string, string>>({})

// Tours and guides data
const { data: toursData } = useAdminToursData()

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
const weekdayOptions = [
  { value: 1, label: 'Lun' },
  { value: 2, label: 'Mar' },
  { value: 3, label: 'Mié' },
  { value: 4, label: 'Jue' },
  { value: 5, label: 'Vie' },
  { value: 6, label: 'Sáb' },
  { value: 0, label: 'Dom' }
]

const isEditMode = computed(() => !!selectedSchedule.value)
const scheduleModalTitle = computed(() => {
  if (isEditMode.value) return 'Editar Schedule'
  return scheduleModalMode.value === 'bulk'
    ? 'Crear Schedules en Lote'
    : 'Crear Schedule'
})
const scheduleSubmitLabel = computed(() => {
  if (isEditMode.value) return 'Actualizar'
  return scheduleModalMode.value === 'bulk'
    ? 'Crear en Lote'
    : 'Crear'
})

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

const applyTourDefaults = (tourId: string, target: { time: string, maxParticipants: number }) => {
  const selectedTour = toursMap.value.get(tourId)
  if (!selectedTour) return

  if (selectedTour.defaultStartTime) {
    target.time = formatLocalTime(selectedTour.defaultStartTime)
  }

  if (selectedTour.defaultMaxParticipants) {
    target.maxParticipants = selectedTour.defaultMaxParticipants
  }
}

// Watch para pre-llenar la hora y cupo cuando se selecciona un tour
watch(() => scheduleForm.value.tourId, (newTourId) => {
  if (!newTourId || isEditMode.value) return // No modificar en modo edición

  applyTourDefaults(newTourId, scheduleForm.value)
})

watch(() => bulkScheduleForm.value.tourId, (newTourId) => {
  if (!newTourId) return
  applyTourDefaults(newTourId, bulkScheduleForm.value)
})

// Rango de fechas del calendario
const startDate = ref('')
const endDate = ref('')

const formatTimeForPayload = (time: string) => (
  time.length === 5 ? `${time}:00` : time
)

const formatTimeForComparison = (time: string) => time.slice(0, 5)

const localDateStringToDate = (date: string) => new Date(`${date}T12:00:00`)
const getScheduleLimitDateString = () => {
  const maxDate = new Date()
  maxDate.setDate(maxDate.getDate() + MAX_SCHEDULE_WINDOW_DAYS)
  return getLocalDateString(maxDate)
}

const getChileTimeString = (value: string | Date) => {
  const date = value instanceof Date ? value : new Date(value)

  return date.toLocaleTimeString('es-CL', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
    timeZone: CHILE_TIMEZONE
  })
}

const buildScheduleKey = (tourId: string, date: string, time: string) =>
  `${tourId}|${date}|${formatTimeForComparison(time)}`

const bulkSelectedDates = computed(() => {
  const { startDate: rangeStart, endDate: rangeEnd, weekdays } = bulkScheduleForm.value

  if (!rangeStart || !rangeEnd || weekdays.length === 0) return []

  const start = localDateStringToDate(rangeStart)
  const end = localDateStringToDate(rangeEnd)

  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime()) || start > end) {
    return []
  }

  const dates: string[] = []
  const current = new Date(start)

  while (current <= end) {
    if (weekdays.includes(current.getDay())) {
      dates.push(getLocalDateString(current))
    }
    current.setDate(current.getDate() + 1)
  }

  return dates
})

const bulkSelectionSummary = computed(() => {
  if (!bulkScheduleForm.value.startDate || !bulkScheduleForm.value.endDate) {
    return 'Selecciona un rango para calcular las fechas a crear.'
  }

  const count = bulkSelectedDates.value.length
  if (count === 0) {
    return 'No hay fechas que coincidan con el rango y los días seleccionados.'
  }

  return `${count} ${count === 1 ? 'fecha coincide' : 'fechas coinciden'} con el rango y los días seleccionados.`
})

// Inicializar fechas
onMounted(() => {
  const today = new Date()
  startDate.value = getLocalDateString(today)

  // Mostrar el próximo año de schedules
  const end = new Date(today)
  end.setDate(end.getDate() + MAX_SCHEDULE_WINDOW_DAYS)
  endDate.value = getLocalDateString(end)

  loadCalendarData()
})

// Cargar datos del calendario
const loadCalendarData = async () => {
  try {
    const data = await fetchCalendarData(startDate.value, endDate.value) as CalendarDataResponse
    calendarData.value = data
    pendingAlerts.value = Array.isArray(data.allAlerts)
      ? data.allAlerts.filter((a: WeatherAlertRes) => a.status === 'PENDING').length
      : 0
  } catch (error) {
    logger.error('Error loading calendar data:', error)
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
    logger.error('Error generating schedules:', error)
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
  scheduleModalMode.value = 'single'

  // Fill form with schedule data
  // startDatetime is an Instant (ISO with Z), parse it correctly
  const scheduleDate = new Date(schedule.startDatetime)
  scheduleForm.value = {
    tourId: schedule.tourId,
    date: getLocalDateString(scheduleDate),
    time: getChileTimeString(scheduleDate),
    maxParticipants: schedule.maxParticipants || 10,
    status: schedule.status || 'OPEN'
  }

  showScheduleModal.value = true
}

// Click en día vacío
const handleDateClick = (info: DateClickArg) => {
  selectedSchedule.value = null
  scheduleModalMode.value = 'single'

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

const openBulkScheduleModal = () => {
  const fallbackDate = startDate.value || getLocalDateString(new Date())

  selectedSchedule.value = null
  scheduleModalMode.value = 'bulk'
  formErrors.value = {}
  bulkScheduleForm.value = {
    ...createEmptyBulkScheduleForm(),
    startDate: fallbackDate,
    endDate: fallbackDate
  }
  showScheduleModal.value = true
}

const setScheduleModalMode = (mode: ScheduleModalMode) => {
  if (isEditMode.value || scheduleModalMode.value === mode) return

  if (mode === 'bulk') {
    bulkScheduleForm.value = {
      ...bulkScheduleForm.value,
      tourId: scheduleForm.value.tourId,
      startDate: scheduleForm.value.date || bulkScheduleForm.value.startDate,
      endDate: scheduleForm.value.date || bulkScheduleForm.value.endDate,
      time: scheduleForm.value.time,
      maxParticipants: scheduleForm.value.maxParticipants
    }
  } else {
    scheduleForm.value = {
      ...scheduleForm.value,
      tourId: bulkScheduleForm.value.tourId,
      date: bulkScheduleForm.value.startDate,
      time: bulkScheduleForm.value.time,
      maxParticipants: bulkScheduleForm.value.maxParticipants
    }
  }

  formErrors.value = {}
  scheduleModalMode.value = mode
}

const isBulkWeekdaySelected = (weekday: number) =>
  bulkScheduleForm.value.weekdays.includes(weekday)

const toggleBulkWeekday = (weekday: number) => {
  bulkScheduleForm.value.weekdays = isBulkWeekdaySelected(weekday)
    ? bulkScheduleForm.value.weekdays.filter(day => day !== weekday)
    : [...bulkScheduleForm.value.weekdays, weekday]
}

const selectAllBulkWeekdays = () => {
  bulkScheduleForm.value.weekdays = [...DEFAULT_BULK_WEEKDAYS]
}

const clearBulkWeekdays = () => {
  bulkScheduleForm.value.weekdays = []
}

// Close modal and reset form
const closeScheduleModal = () => {
  showScheduleModal.value = false
  selectedSchedule.value = null
  scheduleModalMode.value = 'single'
  formErrors.value = {}
  scheduleForm.value = createEmptyScheduleForm()
  bulkScheduleForm.value = createEmptyBulkScheduleForm()
}

// Validate form
const validateSingleForm = (): boolean => {
  formErrors.value = {}

  if (!scheduleForm.value.tourId) {
    formErrors.value.tourId = 'Debes seleccionar un tour'
  }

  if (!scheduleForm.value.date) {
    formErrors.value.date = 'La fecha es requerida'
  }

  if (scheduleForm.value.date && scheduleForm.value.date > getScheduleLimitDateString()) {
    formErrors.value.date = 'La fecha no puede superar un año desde hoy'
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

const validateBulkForm = (): boolean => {
  formErrors.value = {}

  if (!bulkScheduleForm.value.tourId) {
    formErrors.value.tourId = 'Debes seleccionar un tour'
  }

  if (!bulkScheduleForm.value.startDate) {
    formErrors.value.startDate = 'La fecha inicial es requerida'
  }

  if (!bulkScheduleForm.value.endDate) {
    formErrors.value.endDate = 'La fecha final es requerida'
  }

  if (
    bulkScheduleForm.value.startDate
    && bulkScheduleForm.value.endDate
    && bulkScheduleForm.value.startDate > bulkScheduleForm.value.endDate
  ) {
    formErrors.value.endDate = 'La fecha final debe ser igual o posterior a la inicial'
  }

  const maxScheduleDate = getScheduleLimitDateString()
  if (bulkScheduleForm.value.startDate && bulkScheduleForm.value.startDate > maxScheduleDate) {
    formErrors.value.startDate = 'La fecha inicial no puede superar un año desde hoy'
  }

  if (bulkScheduleForm.value.endDate && bulkScheduleForm.value.endDate > maxScheduleDate) {
    formErrors.value.endDate = 'La fecha final no puede superar un año desde hoy'
  }

  if (!bulkScheduleForm.value.time) {
    formErrors.value.time = 'La hora es requerida'
  }

  if (
    !bulkScheduleForm.value.maxParticipants
    || bulkScheduleForm.value.maxParticipants < 1
  ) {
    formErrors.value.maxParticipants = 'Debe ser al menos 1'
  }

  if (bulkScheduleForm.value.weekdays.length === 0) {
    formErrors.value.weekdays = 'Selecciona al menos un día'
  }

  if (bulkSelectedDates.value.length === 0 && !formErrors.value.weekdays) {
    formErrors.value.weekdays = 'El rango no genera fechas válidas con los días seleccionados'
  }

  return Object.keys(formErrors.value).length === 0
}

const extendCalendarRange = (rangeStart: string, rangeEnd: string) => {
  if (!startDate.value || rangeStart < startDate.value) {
    startDate.value = rangeStart
  }

  if (!endDate.value || rangeEnd > endDate.value) {
    endDate.value = rangeEnd
  }
}

const handleScheduleSubmit = async () => {
  if (scheduleModalMode.value === 'bulk' && !isEditMode.value) {
    await saveBulkSchedules()
    return
  }

  await saveSchedule()
}

// Save schedule (create or update)
const saveSchedule = async () => {
  if (!validateSingleForm()) return

  savingSchedule.value = true

  try {
    // Send date and time separately - backend handles Chile timezone conversion
    const payload: TourScheduleCreateReq & { date?: string, time?: string } = {
      tourId: scheduleForm.value.tourId,
      date: scheduleForm.value.date,
      time: formatTimeForPayload(scheduleForm.value.time),
      maxParticipants: scheduleForm.value.maxParticipants
    }

    // En modo edición, incluir el status
    if (isEditMode.value) {
      payload.status = scheduleForm.value.status as TourScheduleCreateReqStatusEnum

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
    logger.error('Error saving schedule:', error)
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

const saveBulkSchedules = async () => {
  if (!validateBulkForm()) return

  savingSchedule.value = true

  try {
    const candidateDates = [...bulkSelectedDates.value]
    const comparisonTime = formatTimeForComparison(bulkScheduleForm.value.time)
    const existingSchedules = await fetchAdminSchedules({
      start: bulkScheduleForm.value.startDate,
      end: bulkScheduleForm.value.endDate
    })

    const existingKeys = new Set(
      existingSchedules.map(schedule =>
        buildScheduleKey(
          schedule.tourId,
          getLocalDateString(new Date(schedule.startDatetime)),
          getChileTimeString(schedule.startDatetime)
        ))
    )

    const datesToCreate = candidateDates.filter(date =>
      !existingKeys.has(
        buildScheduleKey(bulkScheduleForm.value.tourId, date, comparisonTime)
      ))

    const skipped = candidateDates.length - datesToCreate.length

    if (datesToCreate.length === 0) {
      toast.add({
        title: 'Sin cambios',
        description: skipped > 0
          ? 'Todas las fechas seleccionadas ya tienen un schedule para ese tour y hora.'
          : 'No hay fechas válidas para crear.',
        color: 'warning'
      })
      return
    }

    let created = 0
    let failed = 0

    for (let index = 0; index < datesToCreate.length; index += BULK_CREATE_BATCH_SIZE) {
      const batch = datesToCreate.slice(index, index + BULK_CREATE_BATCH_SIZE)
      const results = await Promise.allSettled(
        batch.map(date =>
          $fetch('/api/admin/schedules', {
            method: 'POST',
            body: {
              tourId: bulkScheduleForm.value.tourId,
              date,
              time: formatTimeForPayload(bulkScheduleForm.value.time),
              maxParticipants: bulkScheduleForm.value.maxParticipants
            }
          })
        )
      )

      results.forEach((result) => {
        if (result.status === 'fulfilled') {
          created++
        } else {
          failed++
          logger.error('Error creating bulk schedule:', result.reason)
        }
      })
    }

    extendCalendarRange(
      bulkScheduleForm.value.startDate,
      bulkScheduleForm.value.endDate
    )
    await loadCalendarData()

    if (failed > 0) {
      toast.add({
        title: 'Creación parcial',
        description: `${created} creados, ${skipped} omitidos, ${failed} fallaron.`,
        color: 'warning'
      })
    } else {
      toast.add({
        title: 'Schedules creados',
        description: skipped > 0
          ? `${created} creados y ${skipped} omitidos por ya existir.`
          : `${created} ${created === 1 ? 'schedule creado' : 'schedules creados'} correctamente.`,
        color: 'success'
      })
    }

    closeScheduleModal()
  } catch (error: unknown) {
    logger.error('Error creating bulk schedules:', error)
    const apiError = error as { data?: { message?: string } }
    toast.add({
      title: 'Error',
      description: apiError.data?.message || 'No se pudieron crear los schedules en lote',
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
    displayEventTime: false, // Don't show time prefix (we include it in title)
    eventClick: handleEventClick,
    dateClick: handleDateClick,

    // Eventos (schedules)
    events: Array.isArray(schedules)
      ? schedules.map((schedule: TourScheduleRes) => {
          const start = new Date(schedule.startDatetime)

          // Convert UTC to Chile local time for correct calendar day placement
          const startInChile = instantToChileLocalString(schedule.startDatetime)

          // Color único para todos los tours activos
          let backgroundColor = 'var(--color-atacama-dorado-500)'

          // Verificar si tiene alertas críticas
          const scheduleAlerts = alerts?.get(schedule.id) ?? []
          const hasCriticalAlert = scheduleAlerts.some(
            (a: WeatherAlertRes) => a.severity === 'CRITICAL' && a.status === 'PENDING'
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
            start: startInChile,
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

        const moonIconSpan = document.createElement('span')
        moonIconSpan.className = 'text-lg'
        moonIconSpan.textContent = moonPhase.icon ?? ''
        moonDiv.appendChild(moonIconSpan)

        const moonIllumSpan = document.createElement('span')
        moonIllumSpan.className = 'text-xs text-muted'
        moonIllumSpan.textContent = `${moonPhase.illumination}%`
        moonDiv.appendChild(moonIllumSpan)

        infoContainer.appendChild(moonDiv)
      }

      // Clima
      if (dayWeather) {
        const tempDiv = document.createElement('div')
        tempDiv.className = 'flex items-center gap-1'

        const weatherIconSpan = document.createElement('span')
        weatherIconSpan.className = 'text-base'
        weatherIconSpan.textContent = getWeatherIcon(dayWeather.weather[0]?.main || '')
        tempDiv.appendChild(weatherIconSpan)

        const tempTextSpan = document.createElement('span')
        tempTextSpan.className = 'text-xs text-muted'
        tempTextSpan.textContent = `${Math.round(dayWeather.temp.max)}°/${Math.round(dayWeather.temp.min)}°`
        tempDiv.appendChild(tempTextSpan)

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
