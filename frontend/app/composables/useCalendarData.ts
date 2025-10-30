/**
 * Composable para datos del calendario de tours
 *
 * Centraliza:
 * - Schedules de tours
 * - Fase lunar por día
 * - Pronóstico climático (viento, nubes, lluvia)
 * - Alertas meteorológicas
 */

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
  startDatetime: string
  maxParticipants: number
  currentBookings: number
  status: 'OPEN' | 'CLOSED' | 'CANCELLED'
  tour: {
    id: string
    nameTranslations: Record<string, string>
    category: string
    durationHours: number
    windSensitive: boolean
    moonSensitive: boolean
    cloudSensitive: boolean
  }
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
  const config = useRuntimeConfig()
  const { locale } = useI18n()

  /**
   * Obtiene fase lunar para un rango de fechas
   */
  const fetchMoonPhases = async (startDate: string, endDate: string): Promise<MoonPhase[]> => {
    try {
      const response = await $fetch<MoonPhase[]>(`${config.public.apiBaseUrl}/lunar/calendar`, {
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
  const fetchWeatherForecast = async (): Promise<Map<string, DailyWeather>> => {
    try {
      const response = await $fetch<any>(`${config.public.apiBaseUrl}/weather/forecast`)

      // Convertir a Map por fecha
      const weatherMap = new Map<string, DailyWeather>()

      if (response?.daily) {
        for (const day of response.daily) {
          const date = new Date(day.dt * 1000).toISOString().split('T')[0]
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
        `${config.public.apiBaseUrl}/admin/schedules`,
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
      const response = await $fetch<WeatherAlert[]>(`${config.public.apiBaseUrl}/admin/alerts`)
      return response
    } catch (error) {
      console.error('Error fetching alerts:', error)
      return []
    }
  }

  /**
   * Obtiene todos los datos necesarios para el calendario
   */
  const fetchCalendarData = async (startDate: string, endDate: string) => {
    const [moonPhases, weather, schedules, alerts] = await Promise.all([
      fetchMoonPhases(startDate, endDate),
      fetchWeatherForecast(),
      fetchSchedules(startDate, endDate),
      fetchAlerts()
    ])

    // Crear mapa de fases lunares por fecha
    const moonMap = new Map<string, MoonPhase>()
    moonPhases.forEach(moon => moonMap.set(moon.date, moon))

    // Crear mapa de alertas por schedule ID
    const alertMap = new Map<string, WeatherAlert[]>()
    alerts.forEach(alert => {
      if (!alertMap.has(alert.scheduleId)) {
        alertMap.set(alert.scheduleId, [])
      }
      alertMap.get(alert.scheduleId)!.push(alert)
    })

    return {
      moonPhases: moonMap,
      weather,
      schedules,
      alerts: alertMap,
      allAlerts: alerts
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
  const getAlertColor = (severity: string): string => {
    return severity === 'CRITICAL' ? 'red' : 'yellow'
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
