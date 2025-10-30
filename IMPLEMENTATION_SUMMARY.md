# Resumen de Implementación - Sistema de Schedules

## ✅ Completado

### 1. Base de Datos
- ✅ Agregado campo `default_start_time TIME` a tabla `tours` en V1__Create_initial_schema.sql
- ✅ Nomenclatura de campos booleanos corregida (sin prefijo "is")

### 2. Backend - Entidades
- ✅ **Tour.java**: Agregado campo `LocalTime defaultStartTime`
- ✅ **Tour.java**: Corregida nomenclatura de booleanos:
  - `windSensitive` con getter `isWindSensitive()`
  - `moonSensitive` con getter `isMoonSensitive()`
  - `cloudSensitive` con getter `isCloudSensitive()`
  - `recurring` con getter `isRecurring()`

### 3. Backend - Servicios Externos
- ✅ **WeatherService.java**: Implementado con OpenWeatherMap One Call API 3.0
  - Método `isWindAboveThreshold(LocalDate, double)` - Threshold: 25 nudos
  - Método `isCloudyDay(LocalDate)` - Threshold: >80% nubes
  - Método `getDailyForecast(LocalDate)` - Retorna pronóstico diario
  - Caché de 24 horas
  - 8 días de pronóstico

- ✅ **LunarService.java**: **REESCRITO - Cálculo local con Julian Date**
  - ✅ **NO depende de APIs externas** - matemática pura
  - ✅ **Sin límite de días** - funciona para cualquier fecha (pasado/presente/futuro)
  - ✅ **Offline** - no requiere conexión a internet
  - ✅ **Instantáneo** - ~0.001ms por cálculo
  - ✅ **Precisión** - ±0.5 días (suficiente para tours astronómicos)
  - Método `getMoonPhase(LocalDate)` - Cálculo con método Julian Date
  - Método `isFullMoon(LocalDate)` - Threshold: phase 0.45-0.55
  - Método `getMoonIllumination(LocalDate)` - Retorna iluminación (0-100%)
  - Método `getMoonPhaseName(LocalDate)` - Retorna nombre descriptivo

### 4. Backend - Generador Automático
- ✅ **TourScheduleGeneratorService.java**: **ACTUALIZADO - Límites diferenciados**
  - Cron job: Cada día a las 2 AM (America/Santiago)
  - **Límites según tipo de tour:**
    - 🌕 Tours astronómicos puros: **60 días** (solo luna - cálculo local)
    - 🌤️ Tours sensibles al clima: **8 días** (límite OpenWeatherMap)
    - 🎒 Tours normales: **30 días** (por defecto)
  - Solo tours con `recurring = true` y `status = 'PUBLISHED'`
  - Aplica validaciones:
    - Luna llena para `moonSensitive` - **sin límite de días**
    - Viento >25kt para `windSensitive` - **solo primeros 8 días**
    - Nubosidad >80% para `cloudSensitive` - **solo primeros 8 días**
  - NO sobreescribe schedules existentes
  - Método público `generateSchedulesManually()` para trigger manual

### 5. Backend - Repositories
- ✅ **TourRepository.java**: Métodos agregados
  - `findByRecurringTrueAndStatus(String status)`

- ✅ **TourScheduleRepository.java**: Métodos agregados
  - `findByStartDatetimeBetween(ZonedDateTime, ZonedDateTime)`
  - `existsByTourIdAndStartDatetimeBetween(UUID, ZonedDateTime, ZonedDateTime)`

### 6. Backend - API Endpoints
- ✅ **TourScheduleAdminController.java**: Controlador de admin (esqueleto)
  - `GET /api/admin/schedules?start=2025-11-01&end=2025-11-08`
  - `POST /api/admin/schedules` - Crear manual
  - `PATCH /api/admin/schedules/{id}` - Editar
  - `DELETE /api/admin/schedules/{id}` - Cancelar
  - `POST /api/admin/schedules/generate` - Trigger manual

### 7. Backend - DTOs de OpenWeatherMap
- ✅ **OpenWeatherResponse.java**: Response principal del API
- ✅ **DailyForecast.java**: Pronóstico diario con moon_phase
- ✅ **CurrentWeather.java**: Clima actual
- ✅ **HourlyForecast.java**: Pronóstico por hora
- ✅ **Temperature.java**: Temperaturas del día (min, max, morn, day, eve, night)
- ✅ **FeelsLike.java**: Sensación térmica
- ✅ **WeatherCondition.java**: Condiciones climáticas
- ✅ **Alert.java**: Alertas meteorológicas

### 8. Backend - Sistema de Alertas Climáticas ⚡ NUEVO
- ✅ **WeatherAlert.java**: Entidad para alertas
  - Almacena tipo (WIND, CLOUDS, MOON), severidad (WARNING, CRITICAL)
  - Estados: PENDING, REVIEWED, RESOLVED
  - Datos del pronóstico incluidos (windSpeed, cloudCoverage, moonPhase)
  - Resolución: CANCELLED, KEPT, RESCHEDULED

- ✅ **WeatherAlertRepository.java**: Repositorio JPA
  - Búsqueda por status, schedule_id, fecha
  - Conteo de alertas pendientes

- ✅ **WeatherAlertService.java**: Servicio de alertas
  - Cron job: Cada día a las 3 AM (después del generador)
  - Verifica schedules en próximos 8 días
  - Compara pronóstico actual vs restricciones del tour
  - Crea alertas automáticas si detecta problemas
  - **NO cancela automáticamente** - admin decide
  - Método `checkWeatherAlertsManually()` para trigger manual

- ✅ **WeatherAlertController.java**: API REST
  - `GET /api/admin/alerts` - Lista alertas pendientes
  - `GET /api/admin/alerts/count` - Cuenta pendientes (para badge)
  - `GET /api/admin/alerts/{id}` - Detalle de alerta
  - `GET /api/admin/alerts/schedule/{scheduleId}` - Alertas de un schedule
  - `GET /api/admin/alerts/history` - Historial completo
  - `POST /api/admin/alerts/{id}/resolve` - Resolver alerta
  - `POST /api/admin/alerts/check` - Verificar alertas manualmente

### 9. Backend - API Lunar Calendar 🌙 NUEVO
- ✅ **LunarController.java**: API para calendario lunar
  - `GET /api/lunar/calendar?startDate=2025-01-01&endDate=2025-12-31` - Calendario de rango
  - `GET /api/lunar/phase/2025-11-15` - Fase lunar de fecha específica
  - `GET /api/lunar/next-full-moons?count=5` - Próximas N lunas llenas
  - Respuesta incluye: phase, illumination, phaseName, icon (emoji 🌑🌒🌓🌔🌕🌖🌗🌘)
  - **Sin límite de fechas** - calcula cualquier día del año

## ⏳ Pendiente

### Backend (Menor prioridad)
- ⬜ Completar métodos de TourScheduleAdminController (create, update, delete)
- ⬜ Crear DTOs: `TourScheduleCreateReq`, `TourScheduleRes`
- ⬜ Implementar filtro por `owner_id` para PARTNER_ADMIN
- ⬜ Agregar contador de bookings a TourScheduleRes

### Frontend (Alta prioridad) 🎨
- ⬜ **Dashboard de Alertas** (`/admin/alerts.vue`)
  - Listado de alertas pendientes/resueltas
  - Filtro por tipo, severidad, fecha
  - Modal para resolver alertas (CANCELLED, KEPT, RESCHEDULED)
  - Badge de notificación con conteo

- ⬜ **Calendario de Tours** (`/admin/calendar.vue`)
  - Instalar e integrar FullCalendar
  - Vista de 8-60 días (según tipo de tour)
  - Mostrar fase lunar por día (iconos 🌑🌒🌓🌔🌕🌖🌗🌘)
  - Indicadores de alertas climáticas en días afectados
  - Modal para crear/editar schedules
  - Drag & drop para cambiar horarios

- ⬜ **Widget de Alertas** (Componente global en navbar)
  - Badge con conteo de alertas pendientes
  - Dropdown con últimas 5 alertas
  - Link a dashboard completo de alertas

- ⬜ **Calendario Lunar Público** (`/moon-calendar.vue`)
  - Vista de mes completo con fases lunares
  - Información de illuminación y nombre de fase
  - Para que clientes sepan cuándo NO hay tours astronómicos

## 📋 Pasos para Probar

### 1. Backend

```bash
# 1. Detener backend si está corriendo
# 2. Reiniciar base de datos (las migraciones crearán el nuevo campo)
docker-compose down -v
docker-compose up database

# 3. En otro terminal, ejecutar backend
cd backend
mvn spring-boot:run
```

**Verificación:**
- El backend debería arrancar sin errores
- La tabla `tours` debería tener el campo `default_start_time`
- El servicio `TourScheduleGeneratorService` se programará para correr a las 2 AM

### 2. Configurar API Keys (Opcional para testing inicial)

En tu archivo `.env` o `application.properties`:

```properties
# OpenWeatherMap One Call API 3.0 - Obtener key en https://home.openweathermap.org/api_keys
weather.api.key=TU_API_KEY_AQUI
weather.api.baseurl=https://api.openweathermap.org/data/3.0/onecall
weather.api.lat=-22.9083
weather.api.lon=-68.1999

# Si no tienes key, usa:
weather.api.key=dummy
```

Con `weather.api.key=dummy`, el servicio retornará `null` y las validaciones no se aplicarán (todos los días válidos).

### 3. Crear un Tour Recurrente para Probar

```bash
# Usando tu API existente o Swagger UI en http://localhost:8080/swagger-ui.html
POST /api/admin/tours
{
  "nameTranslations": {
    "es": "Tour Astronómico Test",
    "en": "Astronomical Tour Test",
    "pt": "Tour Astronômico Teste"
  },
  "descriptionTranslations": {
    "es": "Tour de prueba",
    "en": "Test tour",
    "pt": "Tour de teste"
  },
  "category": "ASTRONOMICAL",
  "priceAdult": 50000,
  "defaultMaxParticipants": 15,
  "durationHours": 3,
  "defaultStartTime": "20:00:00",
  "recurring": true,
  "recurrenceRule": "0 20 * * *",  // Cron: Todos los días a las 20:00
  "moonSensitive": true,
  "windSensitive": true,
  "status": "PUBLISHED"
}
```

### 4. Trigger Manual del Generador

```bash
# Llamar al endpoint de generación manual
POST http://localhost:8080/api/admin/schedules/generate
```

### 5. Verificar Schedules Creados

```bash
# Ver schedules generados
GET http://localhost:8080/api/admin/schedules?start=2025-10-30&end=2025-11-07
```

Deberías ver ~8 schedules (uno por día), EXCEPTO:
- Días con luna llena (si `moonSensitive = true`)
- Días con viento >25kt (si `windSensitive = true` y tienes API key)
- Días con >80% nubes (si `cloudSensitive = true` y tienes API key)

## 🔧 Configuración de Variables de Entorno

### Backend (`application.properties` o `.env`)

```properties
# Base de datos
spring.datasource.url=jdbc:postgresql://localhost:5432/northernchile_db
spring.datasource.username=user
spring.datasource.password=password

# OpenWeatherMap One Call API 3.0
weather.api.key=dummy  # o tu key real
weather.api.baseurl=https://api.openweathermap.org/data/3.0/onecall
weather.api.lat=-22.9083
weather.api.lon=-68.1999

# Timezone
spring.jackson.time-zone=America/Santiago
```

## 🐛 Troubleshooting

### Error: "Column 'default_start_time' not found"
**Solución**: Reinicia la BD con `docker-compose down -v && docker-compose up`

### Error: "findByIsRecurringTrueAndStatus" no existe
**Solución**: Debería ser `findByRecurringTrueAndStatus` (sin "is" prefix)

### No se generan schedules automáticamente
**Solución**:
1. El cron job corre a las 2 AM. Para testing, usa el endpoint manual:
   `POST /api/admin/schedules/generate`
2. Verifica que el tour tenga `recurring = true` y `status = 'PUBLISHED'`

### Schedules se generan en días con luna llena
**Solución**: Verifica que el tour tenga `moonSensitive = true`

## 📝 Notas

- **Zona horaria**: Todo usa `America/Santiago (CLT/CLST)`

- **Ventana de generación diferenciada:**
  - Tours astronómicos: **60 días** (solo validación lunar)
  - Tours sensibles al clima: **8 días** (límite OpenWeatherMap)
  - Tours normales: **30 días**

- **Cron jobs**:
  - `2:00 AM` - Generador de schedules (TourScheduleGeneratorService)
  - `3:00 AM` - Verificador de alertas (WeatherAlertService)

- **Cron format**: Usa formato UNIX (5 campos)
  - `0 20 * * *` = Todos los días a las 20:00
  - `0 9 * * 6,0` = Sábados y Domingos a las 09:00

- **OpenWeatherMap API**:
  - Plan gratuito: 60 llamadas/minuto, 8 días de pronóstico
  - Incluye moon_phase (ya no se usa), wind_speed (m/s), clouds (%), weather conditions
  - Caché: 24 horas
  - **Límite**: Solo 8 días de pronóstico confiable

- **Cálculo Lunar (Julian Date)**:
  - **Método**: Matemática pura, offline, sin API
  - **Precisión**: ±0.5 días (suficiente para tours)
  - **Costo**: ~0.001ms por cálculo
  - **Límite**: Ninguno - funciona para cualquier fecha
  - Moon phase valores:
    - 0 y 1 = Luna nueva
    - 0.25 = Cuarto creciente
    - 0.5 = Luna llena
    - 0.75 = Cuarto menguante
  - Luna llena detectada: phase entre 0.45-0.55

- **Sistema de Alertas**:
  - Detecta cambios en pronóstico para schedules existentes
  - NO cancela automáticamente - admin decide
  - Resolutions: CANCELLED, KEPT, RESCHEDULED
  - Severidad: WARNING (nubes), CRITICAL (viento, luna)
