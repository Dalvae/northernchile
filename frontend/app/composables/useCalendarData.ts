/**
 * Composable para datos del calendario de tours
 *
 * Centraliza:
 * - Schedules de tours
 * - Fase lunar por día
 * - Pronóstico climático (viento, nubes, lluvia)
 * - Alertas meteorológicas
 */

import { unixToDateString } from '~/utils/dateUtils'

export interface MoonPhase {
  date: string
  phase: number // 0.0-1.0
  illumination: number // 0-100%
  phaseName: string
  isFullMoon: boolean
  icon: string // emoji 🌑🌒🌓🌔🌕🌖🌗🌘
}

export interface DailyWeather {
  date: string
  temp: {
    min: number
    max: number
    day: number
  }
  windSpeed: number // m/s
  windGust?: number // m/s
  clouds: number // %
  pop: number // probability of precipitation (0-1)
  weather: {
    id: number
    main: string // Rain, Snow, Clouds, Clear
    description: string
    icon: string
  }[]
}

export interface TourSchedule {
  id: string
  tourId: string
  tourName: string
  tourNameTranslations: Record<string, string>
  tourDurationHours: number
  startDatetime: string
  maxParticipants: number
  currentBookings?: number
  status: 'OPEN' | 'CLOSED' | 'CANCELLED'
  assignedGuideId?: string | null
  assignedGuideName?: string | null
  createdAt: string
}

export interface WeatherAlert {
  id: string
  scheduleId: string
  alertType: 'WIND' | 'CLOUDS' | 'MOON'
  severity: 'WARNING' | 'CRITICAL'
  message: string
  createdAt: string
  status: 'PENDING' | 'REVIEWED' | 'RESOLVED'
  windSpeed?: number
  cloudCoverage?: number
  moonPhase?: number
}

export const useCalendarData = () => {

  /**
   * Obtiene fase lunar para un rango de fechas
   */
  const fetchMoonPhases = async (startDate: string, endDate: string): Promise<MoonPhase[]> => {
    try {
      const response = await $fetch<MoonPhase[]>('/api/lunar/calendar', {
        params: { startDate, endDate }
      })
      return response
    } catch (error) {
      console.error('Error fetching moon phases:', error)
      return []
    }
  }

  /**
   * Obtiene pronóstico climático (solo próximos 8 días desde OpenWeatherMap)
   */
  interface WeatherForecastResponse {
    daily?: Array<{
      dt: number
      temp: { min: number, max: number, day: number }
      windSpeed: number
      windGust?: number
      clouds: number
      pop: number
      weather: Array<{ id: number, main: string, description: string, icon: string }>
    }>
  }

  const fetchWeatherForecast = async (): Promise<Map<string, DailyWeather>> => {
    try {
      const response = await $fetch<WeatherForecastResponse>('/api/weather/forecast')

      // Convertir a Map por fecha
      const weatherMap = new Map<string, DailyWeather>()

      if (response?.daily) {
        for (const day of response.daily) {
          const date = unixToDateString(day.dt)
          weatherMap.set(date, {
            date,
            temp: day.temp,
            windSpeed: day.windSpeed,
            windGust: day.windGust,
            clouds: day.clouds,
            pop: day.pop,
            weather: day.weather
          })
        }
      }

      return weatherMap
    } catch (error) {
      console.error('Error fetching weather forecast:', error)
      return new Map()
    }
  }

  /**
   * Obtiene schedules de tours para un rango de fechas
   */
  const fetchSchedules = async (startDate: string, endDate: string): Promise<TourSchedule[]> => {
    try {
      const response = await $fetch<TourSchedule[]>(
        '/api/admin/schedules',
        {
          params: { start: startDate, end: endDate }
        }
      )
      return response
    } catch (error) {
      console.error('Error fetching schedules:', error)
      return []
    }
  }

  /**
   * Obtiene alertas meteorológicas pendientes
   */
  const fetchAlerts = async (): Promise<WeatherAlert[]> => {
    try {
      const response = await $fetch<WeatherAlert[]>('/api/admin/alerts')
      return response
    } catch (error) {
      console.error('Error fetching alerts:', error)
      return []
    }
  }

  /**
   * Obtiene todos los datos necesarios para el calendario
   * Usa el endpoint combinado que devuelve luna + weather en una sola llamada
   */
  const fetchCalendarData = async (startDate: string, endDate: string) => {
    try {
      interface CalendarDataResponse {
        moonPhases?: MoonPhase[]
        weather?: WeatherForecastResponse
      }

      // Llamada combinada al backend: fases lunares + pronóstico meteorológico
      const calendarResponse = await $fetch<CalendarDataResponse>('/api/calendar/data', {
        params: { startDate, endDate },
        credentials: 'include' // Auth is handled via HttpOnly cookie
      })

      // Obtener schedules y alertas (datos que cambian frecuentemente)
      const [schedules, alerts] = await Promise.all([
        fetchSchedules(startDate, endDate),
        fetchAlerts()
      ])

      // Crear mapa de fases lunares por fecha
      const moonMap = new Map<string, MoonPhase>()
      if (calendarResponse.moonPhases && Array.isArray(calendarResponse.moonPhases)) {
        calendarResponse.moonPhases.forEach(moon => moonMap.set(moon.date, moon))
      }

      // Convertir weather response a Map
      const weatherMap = new Map<string, DailyWeather>()
      if (calendarResponse.weather?.daily) {
        for (const day of calendarResponse.weather.daily) {
          const date = unixToDateString(day.dt)
          weatherMap.set(date, {
            date,
            temp: day.temp,
            windSpeed: day.windSpeed,
            windGust: day.windGust,
            clouds: day.clouds,
            pop: day.pop,
            weather: day.weather
          })
        }
      }

      // Crear mapa de alertas por schedule ID
      const alertMap = new Map<string, WeatherAlert[]>()
      if (Array.isArray(alerts)) {
        alerts.forEach((alert) => {
          if (!alertMap.has(alert.scheduleId)) {
            alertMap.set(alert.scheduleId, [])
          }
          alertMap.get(alert.scheduleId)!.push(alert)
        })
      }

      return {
        moonPhases: moonMap,
        weather: weatherMap,
        schedules: schedules || [],
        alerts: alertMap,
        allAlerts: alerts || []
      }
    } catch (error) {
      console.error('Error loading calendar data:', error)
      // Retornar datos vacíos en caso de error
      return {
        moonPhases: new Map(),
        weather: new Map(),
        schedules: [],
        alerts: new Map(),
        allAlerts: []
      }
    }
  }

  /**
   * Helpers para obtener iconos de clima
   */
  const getWeatherIcon = (weatherMain: string): string => {
    const icons: Record<string, string> = {
      Clear: '☀️',
      Clouds: '☁️',
      Rain: '🌧️',
      Drizzle: '🌦️',
      Thunderstorm: '⛈️',
      Snow: '❄️',
      Mist: '🌫️',
      Smoke: '💨',
      Haze: '🌫️',
      Dust: '💨',
      Fog: '🌫️',
      Sand: '💨',
      Ash: '💨',
      Squall: '💨',
      Tornado: '🌪️'
    }
    return icons[weatherMain] || '🌤️'
  }

  /**
   * Determina si hay condiciones adversas para un día
   */
  const hasAdverseConditions = (
    date: string,
    weather: Map<string, DailyWeather>,
    moonPhases: Map<string, MoonPhase>
  ): {
    hasWind: boolean
    hasClouds: boolean
    hasFullMoon: boolean
    hasRain: boolean
  } => {
    const dayWeather = weather.get(date)
    const moonPhase = moonPhases.get(date)

    const windThreshold = 25 * 0.514444 // 25 knots en m/s
    const cloudThreshold = 80 // %

    return {
      hasWind: dayWeather ? (dayWeather.windSpeed > windThreshold || (dayWeather.windGust || 0) > windThreshold) : false,
      hasClouds: dayWeather ? dayWeather.clouds > cloudThreshold : false,
      hasFullMoon: moonPhase?.isFullMoon || false,
      hasRain: dayWeather ? dayWeather.pop > 0.5 : false
    }
  }

  /**
   * Obtiene color de severidad para alertas
   */
  type AlertColor = 'error' | 'info' | 'success' | 'primary' | 'secondary' | 'tertiary' | 'warning' | 'neutral'

  const getAlertColor = (severity: string): AlertColor => {
    return severity === 'CRITICAL' ? 'error' : 'warning'
  }

  /**
   * Formatea velocidad de viento
   */
  const formatWindSpeed = (ms: number): string => {
    const knots = ms / 0.514444
    return `${Math.round(knots)} kt (${Math.round(ms)} m/s)`
  }

  return {
    fetchMoonPhases,
    fetchWeatherForecast,
    fetchSchedules,
    fetchAlerts,
    fetchCalendarData,
    getWeatherIcon,
    hasAdverseConditions,
    getAlertColor,
    formatWindSpeed
  }
}
