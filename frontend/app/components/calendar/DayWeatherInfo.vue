<template>
  <div class="flex flex-col gap-1">
    <!-- Fase lunar -->
    <div
      v-if="moonPhase"
      class="flex items-center gap-1 text-xs"
    >
      <span class="text-lg">{{ moonPhase.icon }}</span>
      <span class="text-neutral-600 dark:text-neutral-400">{{ moonPhase.illumination }}%</span>
    </div>

    <!-- Clima -->
    <div
      v-if="weather"
      class="flex flex-col gap-1"
    >
      <!-- Temperatura -->
      <div class="flex items-center gap-1 text-xs">
        <span>{{ getWeatherIcon(weather.weather[0]?.main || '') }}</span>
        <span class="text-neutral-700 dark:text-neutral-200">
          {{ Math.round(weather.temp.max) }}° / {{ Math.round(weather.temp.min) }}°
        </span>
      </div>

      <!-- Alertas de condiciones adversas -->
      <div class="flex flex-wrap gap-1">
        <!-- Viento -->
        <UBadge
          v-if="conditions.hasWind"
          color="error"
          variant="soft"
          size="xs"
          class="rounded-full"
        >
          💨 Viento
        </UBadge>

        <!-- Nubes -->
        <UBadge
          v-if="conditions.hasClouds"
          color="warning"
          variant="soft"
          size="xs"
          class="rounded-full"
        >
          ☁️ Nublado
        </UBadge>

        <!-- Lluvia -->
        <UBadge
          v-if="conditions.hasRain"
          color="info"
          variant="soft"
          size="xs"
          class="rounded-full"
        >
          🌧️ Lluvia
        </UBadge>

        <!-- Luna llena -->
        <UBadge
          v-if="conditions.hasFullMoon"
          color="tertiary"
          variant="soft"
          size="xs"
          class="rounded-full"
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
