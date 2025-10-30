<template>
  <div class="flex flex-col gap-1">
    <!-- Fase lunar -->
    <div v-if="moonPhase" class="flex items-center gap-1 text-xs">
      <span class="text-lg">{{ moonPhase.icon }}</span>
      <span class="text-gray-600 dark:text-gray-400">{{ moonPhase.illumination }}%</span>
    </div>

    <!-- Clima -->
    <div v-if="weather" class="flex flex-col gap-1">
      <!-- Temperatura -->
      <div class="flex items-center gap-1 text-xs">
        <span>{{ getWeatherIcon(weather.weather[0]?.main) }}</span>
        <span class="text-gray-700 dark:text-gray-300">
          {{ Math.round(weather.temp.max) }}° / {{ Math.round(weather.temp.min) }}°
        </span>
      </div>

      <!-- Alertas de condiciones adversas -->
      <div class="flex flex-wrap gap-1">
        <!-- Viento -->
        <UBadge
          v-if="conditions.hasWind"
          color="red"
          variant="soft"
          size="xs"
          :ui="{ rounded: 'rounded-full' }"
        >
          💨 Viento
        </UBadge>

        <!-- Nubes -->
        <UBadge
          v-if="conditions.hasClouds"
          color="yellow"
          variant="soft"
          size="xs"
          :ui="{ rounded: 'rounded-full' }"
        >
          ☁️ Nublado
        </UBadge>

        <!-- Lluvia -->
        <UBadge
          v-if="conditions.hasRain"
          color="blue"
          variant="soft"
          size="xs"
          :ui="{ rounded: 'rounded-full' }"
        >
          🌧️ Lluvia
        </UBadge>

        <!-- Luna llena -->
        <UBadge
          v-if="conditions.hasFullMoon"
          color="purple"
          variant="soft"
          size="xs"
          :ui="{ rounded: 'rounded-full' }"
        >
          🌕 Luna llena
        </UBadge>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { MoonPhase, DailyWeather } from '~/composables/useCalendarData'

const props = defineProps<{
  date: string
  moonPhase?: MoonPhase
  weather?: DailyWeather
  conditions: {
    hasWind: boolean
    hasClouds: boolean
    hasFullMoon: boolean
    hasRain: boolean
  }
}>()

const { getWeatherIcon } = useCalendarData()
</script>
