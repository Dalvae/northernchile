# Manejo de Timezone en Northern Chile

## Problema

Chile tiene **horario de verano (DST - Daylight Saving Time)** y las reglas han cambiado varias veces:

- **Horario estándar (invierno)**: UTC-4 (CLT - Chile Standard Time)
- **Horario de verano**: UTC-3 (CLST - Chile Summer Time)
- **Cambios DST**: Aproximadamente primer sábado de abril/septiembre
- **Historia**: Las reglas cambiaron en 2015, 2016, 2018, y 2019

## Solución Implementada

### Backend (Spring Boot + Java)

#### ✅ Almacenamiento: `Instant` (UTC)

**SIEMPRE** almacenar timestamps como `Instant` en la base de datos:

```java
// ✅ CORRECTO - Almacena en UTC
@Column(nullable = false, updatable = false)
private Instant createdAt;

@Column(nullable = false)
private Instant startDatetime;
```

```java
// ❌ INCORRECTO - NO usar LocalDateTime en DB
private LocalDateTime startDatetime; // Pierde información de timezone!
```

#### ✅ Lógica de negocio: `ZoneId.of("America/Santiago")`

**SIEMPRE** usar la constante `ZONE_ID` para conversiones:

```java
private static final ZoneId ZONE_ID = ZoneId.of("America/Santiago");

// Convertir Instant → LocalDate (para comparaciones de calendario)
LocalDate date = LocalDate.ofInstant(schedule.getStartDatetime(), ZONE_ID);

// Convertir LocalDate → Instant (para almacenar en DB)
Instant instant = localDate.atStartOfDay(ZONE_ID).toInstant();
```

**¿Por qué funciona?** Java automáticamente aplica las reglas DST de Chile:

```java
// VERANO (enero 2025): UTC-3
LocalDate.of(2025, 1, 15)
    .atStartOfDay(ZONE_ID)
    .toInstant()
// → 2025-01-15T03:00:00Z (UTC)

// INVIERNO (julio 2025): UTC-4
LocalDate.of(2025, 7, 15)
    .atStartOfDay(ZONE_ID)
    .toInstant()
// → 2025-07-15T04:00:00Z (UTC)
```

#### ✅ API REST: ISO 8601 Strings

Enviar/recibir fechas como strings ISO 8601 en JSON:

```json
{
  "startDate": "2025-01-15",
  "createdAt": "2025-01-15T03:00:00Z"
}
```

```java
// Parsear fechas del frontend
Instant start = LocalDate.parse(startDate)
    .atStartOfDay(ZONE_ID)
    .toInstant();

// Para rangos inclusivos, sumar 1 día al endDate
Instant end = LocalDate.parse(endDate)
    .plusDays(1)
    .atStartOfDay(ZONE_ID)
    .toInstant();
```

### Frontend (Nuxt 3 + Vue)

#### ✅ Recibir timestamps como ISO strings

```typescript
interface TourSchedule {
  startDatetime: string  // "2025-01-15T03:00:00Z"
}

// Convertir a Date nativo de JavaScript
const date = new Date(schedule.startDatetime)

// Mostrar en hora local del usuario
const formatted = date.toLocaleString('es-CL', {
  timeZone: 'America/Santiago',
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  hour: '2-digit',
  minute: '2-digit'
})
```

#### ✅ Enviar fechas al backend

```typescript
// Para date pickers (solo fecha, sin hora)
const dateString = selectedDate.toISOString().split('T')[0]  // "2025-01-15"

// Para timestamps completos
const timestamp = new Date().toISOString()  // "2025-01-15T03:00:00.000Z"
```

## Comparación con Predicciones Externas

### Weather API

```java
// Weather API retorna epoch timestamp
long weatherEpoch = 1736899200L;

// Convertir a LocalDate para comparar con schedule
LocalDate weatherDate = Instant.ofEpochSecond(weatherEpoch)
    .atZone(ZONE_ID)  // ← Usar America/Santiago
    .toLocalDate();

LocalDate scheduleDate = LocalDate.ofInstant(schedule.getStartDatetime(), ZONE_ID);

// Comparar
if (weatherDate.equals(scheduleDate)) {
    // Las predicciones calzan con el schedule
}
```

### Lunar Service

```java
// LunarService ya trabaja con LocalDate directamente
public boolean isFullMoon(LocalDate date) {
    double phase = getMoonPhase(date);
    return phase >= 0.45 && phase <= 0.55;
}

// Uso desde AvailabilityService
LocalDate scheduleDate = LocalDate.ofInstant(schedule.getStartDatetime(), ZONE_ID);
boolean isFullMoon = lunarService.isFullMoon(scheduleDate);
```

## Edge Cases Importantes

### 1. Cambio de Horario a Medianoche

El cambio DST en Chile ocurre a las **00:00** (medianoche), creando:

- **"Spring forward"** (septiembre): 23:59:59 → 01:00:00 (se "pierde" 1 hora)
- **"Fall back"** (abril): 23:59:59 → 23:00:00 (se "repite" 1 hora)

```java
// Si un schedule está exactamente a las 00:00 del día del cambio:
LocalDate transitionDay = LocalDate.of(2025, 4, 6);  // Ejemplo
ZonedDateTime midnight = transitionDay.atStartOfDay(ZONE_ID);

// Java maneja esto automáticamente, ajustando al primer instante válido
```

**Recomendación**: Evitar crear schedules a medianoche durante días de transición DST.

### 2. Rangos Inclusivos

Al filtrar por rango de fechas, el `endDate` debe ser **exclusivo**:

```java
// Usuario selecciona: 2025-01-01 a 2025-01-31
Instant start = LocalDate.parse("2025-01-01").atStartOfDay(ZONE_ID).toInstant();
Instant end = LocalDate.parse("2025-01-31")
    .plusDays(1)  // ← Sumar 1 día para incluir todo el 31
    .atStartOfDay(ZONE_ID)
    .toInstant();

List<Booking> bookings = repository.findByCreatedAtBetween(start, end);
```

### 3. Comparación de Fechas

```java
// ✅ CORRECTO - Comparar como LocalDate
LocalDate date1 = LocalDate.ofInstant(instant1, ZONE_ID);
LocalDate date2 = LocalDate.ofInstant(instant2, ZONE_ID);
boolean sameDay = date1.equals(date2);

// ❌ INCORRECTO - Comparar Instants directamente puede fallar
boolean sameDay = instant1.equals(instant2);  // Compara timestamps exactos
```

## Verificación en Producción

### 1. Endpoint de Diagnóstico

Acceder a `GET /api/diagnostic/timezone` para ver:

```json
{
  "zoneId": "America/Santiago",
  "currentChileTime": "2025-01-15T17:30:00-03:00",
  "currentOffset": "-03:00",
  "isDaylightSavingTime": true,
  "nextTransitions": [
    {
      "dateTime": "2025-04-06T00:00:00-03:00",
      "offsetBefore": "-03:00",
      "offsetAfter": "-04:00",
      "type": "Fall back"
    }
  ]
}
```

### 2. Health Check

Verificar `GET /api/diagnostic/health/timezone`:

```json
{
  "status": "OK",
  "warnings": [],
  "currentOffset": "-03:00",
  "nextTransition": "2025-04-06T03:00:00Z"
}
```

Si aparece `"status": "WARNING"`, actualizar tzdata.

### 3. Tests Automatizados

Ejecutar `TimeZoneVerificationTest.java`:

```bash
mvn test -Dtest=TimeZoneVerificationTest
```

Verifica:
- Offset correcto en verano (-3) e invierno (-4)
- Transiciones DST detectadas
- Conversiones Instant ↔ LocalDate consistentes
- Comparación con weather/lunar predictions

## Actualización de tzdata

### Docker (Producción)

El `Dockerfile` ya incluye actualización automática:

```dockerfile
# Actualizar tzdata para asegurar horarios de Chile correctos
RUN apk add --no-cache tzdata
```

Al reconstruir la imagen Docker, se obtiene la última versión de tzdata.

### Desarrollo Local

#### macOS/Linux:

```bash
# Verificar versión actual
java -version

# Actualizar JDK (contiene tzdata actualizado)
sdk install java 21.0.x-tem
```

#### Docker Compose:

```bash
# Reconstruir imagen backend con tzdata actualizado
docker-compose build backend --no-cache
docker-compose up
```

## Checklist de Implementación

Cuando trabajes con timestamps:

- [ ] ¿Almacené como `Instant` en la base de datos?
- [ ] ¿Usé `ZoneId.of("America/Santiago")` para conversiones?
- [ ] ¿Definí `ZONE_ID` como constante en la clase?
- [ ] ¿Comparé fechas como `LocalDate` en lugar de `Instant`?
- [ ] ¿Sumé 1 día al `endDate` para rangos inclusivos?
- [ ] ¿Envié/recibí ISO 8601 strings en la API?
- [ ] ¿Probé con fechas en verano E invierno?
- [ ] ¿Verifiqué contra el endpoint de diagnóstico?

## Referencias

- [IANA Time Zone Database](https://www.iana.org/time-zones)
- [Chile DST History (Wikipedia)](https://en.wikipedia.org/wiki/Time_in_Chile)
- [Java ZoneId Documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/ZoneId.html)
- [ISO 8601 Standard](https://en.wikipedia.org/wiki/ISO_8601)
