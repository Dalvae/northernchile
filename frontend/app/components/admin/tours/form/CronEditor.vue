<script setup lang="ts">
import logger from '~/utils/logger'

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits(['update:modelValue'])

// Estados locales para la UI
const time = ref('20:00')
const mode = ref('daily') // 'daily' | 'custom'
const selectedDays = ref<number[]>([])

const daysOptions = [
  { label: 'L', value: 1, name: 'Lunes' },
  { label: 'M', value: 2, name: 'Martes' },
  { label: 'M', value: 3, name: 'Miércoles' },
  { label: 'J', value: 4, name: 'Jueves' },
  { label: 'V', value: 5, name: 'Viernes' },
  { label: 'S', value: 6, name: 'Sábado' },
  { label: 'D', value: 0, name: 'Domingo' }
]

// Generar el string CRON basado en la selección visual
function updateCron() {
  const [hours, minutes] = time.value.split(':')

  // Formato Spring: seg min hora dia-mes mes dia-sem
  let daysPart = '*'

  if (mode.value === 'custom' && selectedDays.value.length > 0) {
    // Ordenar y unir por comas (ej: 1,3,5)
    daysPart = selectedDays.value.sort().join(',')
  }

  const cron = `0 ${minutes} ${hours} * * ${daysPart}`
  emit('update:modelValue', cron)
}

// Parsear el string CRON entrante para llenar la UI
watch(() => props.modelValue, (newVal) => {
  if (!newVal) return
  try {
    const parts = newVal.split(' ')
    // Asumiendo formato: 0 min hour * * days
    if (parts.length >= 6) {
      const min = parts[1]?.padStart(2, '0') || '00'
      const hour = parts[2]?.padStart(2, '0') || '00'
      time.value = `${hour}:${min}`

      const days = parts[5]
      if (!days || days === '*' || days === '?') {
        mode.value = 'daily'
        selectedDays.value = []
      } else {
        mode.value = 'custom'
        selectedDays.value = days.split(',').map(Number)
      }
    }
  } catch (e) {
    logger.error('Error parsing cron', e)
  }
}, { immediate: true })

function toggleDay(day: number) {
  if (selectedDays.value.includes(day)) {
    selectedDays.value = selectedDays.value.filter(d => d !== day)
  } else {
    selectedDays.value.push(day)
  }
  updateCron()
}
</script>

<template>
  <div class="p-4 bg-neutral-50 dark:bg-neutral-900 rounded-lg border border-neutral-200 dark:border-neutral-800 space-y-4">
    <!-- 1. Selección de Hora -->
    <div class="flex items-center gap-4">
      <div class="flex-1">
        <label class="text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1 block">Hora de Inicio</label>
        <UInput
          v-model="time"
          type="time"
          size="lg"
          @change="updateCron"
        />
      </div>

      <!-- 2. Tipo de Frecuencia -->
      <div class="flex-1">
        <label class="text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1 block">Frecuencia</label>
        <div class="flex gap-2">
          <UButton
            :variant="mode === 'daily' ? 'solid' : 'outline'"
            :color="mode === 'daily' ? 'primary' : 'neutral'"
            size="sm"
            class="flex-1 justify-center"
            @click="() => { mode = 'daily'; updateCron() }"
          >
            Todos los días
          </UButton>
          <UButton
            :variant="mode === 'custom' ? 'solid' : 'outline'"
            :color="mode === 'custom' ? 'primary' : 'neutral'"
            size="sm"
            class="flex-1 justify-center"
            @click="() => { mode = 'custom'; updateCron() }"
          >
            Días específicos
          </UButton>
        </div>
      </div>
    </div>

    <!-- 3. Selector de Días (Solo si es custom) -->
    <div
      v-if="mode === 'custom'"
      class="space-y-2"
    >
      <label class="text-sm font-medium text-neutral-700 dark:text-neutral-300">Selecciona los días:</label>
      <div class="flex justify-between gap-2">
        <button
          v-for="day in daysOptions"
          :key="day.value"
          type="button"
          class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold transition-all"
          :class="selectedDays.includes(day.value)
            ? 'bg-primary text-white shadow-lg scale-105'
            : 'bg-white dark:bg-neutral-800 text-neutral-500 border border-neutral-200 dark:border-neutral-700 hover:border-primary'"
          :title="day.name"
          @click="toggleDay(day.value)"
        >
          {{ day.label }}
        </button>
      </div>
      <p
        v-if="selectedDays.length === 0"
        class="text-xs text-error"
      >
        Selecciona al menos un día
      </p>
    </div>

    <!-- Resumen Visual -->
    <div class="pt-2 border-t border-neutral-200 dark:border-neutral-700">
      <p class="text-xs text-neutral-500 font-mono flex items-center gap-2">
        <UIcon
          name="i-heroicons-code-bracket"
          class="w-3 h-3"
        />
        Regla generada: {{ modelValue }}
      </p>
    </div>
  </div>
</template>
