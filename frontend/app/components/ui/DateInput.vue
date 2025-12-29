<template>
  <!-- Force en-GB locale for DD/MM/YYYY format (international standard) -->
  <ConfigProvider locale="en-GB">
    <UInputDate
      v-model="dateValue"
      :size="size"
      :color="color"
      :variant="variant"
      :disabled="disabled"
      :max-value="maxDate"
      :min-value="minDate"
      :placeholder="placeholder"
      :leading-icon="leadingIcon"
      :class="props.class"
    >
      <template
        v-if="$slots.trailing"
        #trailing
      >
        <slot name="trailing" />
      </template>
    </UInputDate>
  </ConfigProvider>
</template>

<script setup lang="ts">
import { CalendarDate } from '@internationalized/date'
import { ConfigProvider } from 'reka-ui'

const props = withDefaults(defineProps<{
  /**
   * ISO date string (YYYY-MM-DD) or null
   */
  modelValue?: string | null
  /**
   * Size of the input
   */
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl'
  /**
   * Color variant
   */
  color?: 'primary' | 'secondary' | 'success' | 'info' | 'warning' | 'error' | 'neutral'
  /**
   * Style variant
   */
  variant?: 'outline' | 'soft' | 'subtle' | 'ghost' | 'none'
  /**
   * Whether input is disabled
   */
  disabled?: boolean
  /**
   * Max date as ISO string (YYYY-MM-DD)
   */
  max?: string
  /**
   * Min date as ISO string (YYYY-MM-DD)
   */
  min?: string
  /**
   * Placeholder date info
   */
  placeholder?: { year: number, month: number, day: number }
  /**
   * Leading icon
   */
  leadingIcon?: string
  /**
   * Custom class
   */
  class?: string
}>(), {
  size: 'md',
  color: 'primary',
  variant: 'outline',
  disabled: false,
  leadingIcon: 'i-lucide-calendar'
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | null): void
}>()

/**
 * Convert ISO string (YYYY-MM-DD) to CalendarDate
 */
function parseIsoDate(isoString: string | null | undefined): CalendarDate | null {
  if (!isoString) return null

  const parts = isoString.split('-')
  if (parts.length !== 3) return null

  const year = parseInt(parts[0], 10)
  const month = parseInt(parts[1], 10)
  const day = parseInt(parts[2], 10)

  if (isNaN(year) || isNaN(month) || isNaN(day)) return null

  try {
    return new CalendarDate(year, month, day)
  } catch {
    return null
  }
}

/**
 * Convert CalendarDate to ISO string (YYYY-MM-DD)
 */
function toIsoDate(date: CalendarDate | null): string | null {
  if (!date) return null

  const year = date.year.toString().padStart(4, '0')
  const month = date.month.toString().padStart(2, '0')
  const day = date.day.toString().padStart(2, '0')

  return `${year}-${month}-${day}`
}

// Internal CalendarDate value
const dateValue = computed<CalendarDate | null>({
  get() {
    return parseIsoDate(props.modelValue)
  },
  set(newValue) {
    emit('update:modelValue', toIsoDate(newValue))
  }
})

// Convert max prop to CalendarDate
const maxDate = computed(() => parseIsoDate(props.max))

// Convert min prop to CalendarDate
const minDate = computed(() => parseIsoDate(props.min))
</script>
