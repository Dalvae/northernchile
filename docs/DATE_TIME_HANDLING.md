# Date/Time Handling Guide - Northern Chile

## Overview

This document defines the **standardized contracts** for date/time serialization between backend (Spring Boot) and frontend (Nuxt 3). Following these contracts ensures consistent data exchange and prevents timezone-related bugs.

---

## Type Definitions

### 1. **Instant** - Timezone-Aware Timestamps

**Backend Type:** `java.time.Instant`
**Database:** `TIMESTAMP` (stored as UTC)
**JSON Format:** ISO-8601 string with UTC timezone

#### Serialization Format
```json
{
  "createdAt": "2025-01-15T10:30:00Z",
  "startDatetime": "2025-01-15T14:00:00.123Z"
}
```

#### Use Cases
- `TourSchedule.startDatetime` - When a tour starts
- `Booking.createdAt` - When a booking was created
- `Booking.updatedAt` - When a booking was last modified
- Any timestamp requiring timezone awareness

#### Frontend Handling
```typescript
// Parsing
const date = new Date(response.startDatetime); // Works with ISO-8601

// Displaying
const formatter = new Intl.DateTimeFormat('es-CL', {
  dateStyle: 'full',
  timeStyle: 'short',
  timeZone: 'America/Santiago'
});
console.log(formatter.format(date)); // "miércoles, 15 de enero de 2025, 14:00"
```

#### Backend Examples
```java
// Entity
@Column(nullable = false)
private Instant startDatetime;

// Controller
@GetMapping
public ResponseEntity<TourScheduleRes> getSchedule() {
    schedule.setStartDatetime(Instant.now());
    return ResponseEntity.ok(schedule);
}
```

---

### 2. **LocalDate** - Date Without Time

**Backend Type:** `java.time.LocalDate`
**Database:** `DATE`
**JSON Format:** ISO-8601 date string (YYYY-MM-DD)

#### Serialization Format
```json
{
  "dateOfBirth": "1990-05-15",
  "tourDate": "2025-01-20"
}
```

#### Use Cases
- `User.dateOfBirth` - User's birth date
- `Participant.dateOfBirth` - Participant's birth date
- `BookingRes.tourDate` - The date of the tour (for display purposes)
- Any date without time context

#### Frontend Handling
```typescript
// HTML date input (native support)
<input type="date" v-model="participant.dateOfBirth" />
// Value is automatically "YYYY-MM-DD" format

// Parsing
const date = participant.dateOfBirth; // "2025-01-15"
const parts = date.split('-'); // ["2025", "01", "15"]

// Sending to backend
const payload = {
  dateOfBirth: "2025-01-15" // Send as string, NOT Date object
};
```

#### Backend Examples
```java
// Entity
@Column(nullable = false)
private LocalDate dateOfBirth;

// DTO
public class ParticipantReq {
    @NotNull
    private LocalDate dateOfBirth; // Jackson deserializes "2025-01-15" automatically
}
```

⚠️ **Important:** Do NOT convert LocalDate to JavaScript `Date` objects when sending to backend, as this introduces timezone issues.

---

### 3. **LocalTime** - Time Without Date

**Backend Type:** `java.time.LocalTime`
**Database:** `TIME`
**JSON Format:** ISO-8601 time string (HH:mm:ss)

#### Serialization Format
```json
{
  "defaultStartTime": "14:30:00",
  "tourStartTime": "10:00:00"
}
```

#### Use Cases
- `Tour.defaultStartTime` - Default time a tour starts
- `BookingRes.tourStartTime` - Display time for a tour
- Any time-of-day without date context

#### Frontend Handling
```typescript
// HTML time input (supports HH:mm)
<input type="time" v-model="timeValue" />
// Browser sends "14:30" (HH:mm)

// Converting to backend format
const timeWithSeconds = `${timeValue}:00`; // "14:30:00"

// Parsing from backend
const time = response.defaultStartTime; // "14:30:00"
const displayTime = time.slice(0, 5); // "14:30" for HTML input
```

#### Backend Examples
```java
// Entity
@Column
private LocalTime defaultStartTime;

// Controller
tour.setDefaultStartTime(LocalTime.of(14, 30)); // 14:30:00
```

---

## Backend Configuration

### Jackson Configuration
Location: `backend/src/main/java/com/northernchile/api/config/JacksonConfig.java`

```java
// LocalDate serialization: YYYY-MM-DD
javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE));

// LocalTime serialization: HH:mm:ss
javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ISO_LOCAL_TIME));

// Instant serialization: ISO-8601 UTC
javaTimeModule.addSerializer(Instant.class, InstantSerializer.INSTANCE);
```

### application.properties
```properties
# Serialize dates as ISO-8601 strings (not timestamps)
spring.jackson.serialization.write-dates-as-timestamps=false

# Application timezone (used for display, not storage)
spring.jackson.time-zone=America/Santiago

# Database storage: ALL timestamps stored as UTC
spring.jpa.properties.hibernate.jdbc.time_zone=UTC
```

---

## Frontend Best Practices

### ✅ DO

```typescript
// Parse Instant to Date for display
const bookingDate = new Date(booking.createdAt);

// Send LocalDate as string
const participant = {
  dateOfBirth: "1990-05-15" // String, not Date object
};

// Use Intl.DateTimeFormat for localized formatting
const formatter = new Intl.DateTimeFormat('es-CL', {
  dateStyle: 'medium',
  timeStyle: 'short'
});
```

### ❌ DON'T

```typescript
// DON'T convert LocalDate to Date object
const participant = {
  dateOfBirth: new Date("1990-05-15") // ❌ Introduces timezone issues
};

// DON'T manually parse timezone
const localDate = new Date(utcDate).toLocaleDateString(); // ❌ Unreliable

// DON'T hardcode timezone offsets
const date = new Date(timestamp + 3 * 60 * 60 * 1000); // ❌ Breaks with DST
```

---

## Common Patterns

### Pattern 1: Creating a TourSchedule from LocalDate + LocalTime

**Backend**
```java
// Combine LocalDate + LocalTime → Instant
LocalDate date = LocalDate.parse("2025-01-15");
LocalTime time = LocalTime.parse("14:30:00");
ZoneId zone = ZoneId.of("America/Santiago");

Instant startDatetime = ZonedDateTime.of(date, time, zone).toInstant();
schedule.setStartDatetime(startDatetime); // Stored as UTC in DB
```

**Frontend**
```typescript
// Send separate date and time, let backend combine
const payload = {
  tourDate: "2025-01-15",    // LocalDate
  tourTime: "14:30:00",      // LocalTime
  tourId: "uuid-here"
};
```

### Pattern 2: Displaying Instant in User's Timezone

**Frontend**
```typescript
const formatDateTime = (instant: string) => {
  const date = new Date(instant);
  return new Intl.DateTimeFormat('es-CL', {
    day: '2-digit',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    timeZone: 'America/Santiago'
  }).format(date);
};

// Usage
formatDateTime("2025-01-15T14:30:00Z"); // "15 de enero de 2025, 14:30"
```

### Pattern 3: Date Range Queries

**Frontend**
```typescript
// Send LocalDate strings for date-only queries
const params = {
  startDate: "2025-01-01",
  endDate: "2025-01-31"
};
```

**Backend**
```java
@GetMapping
public ResponseEntity<List<BookingRes>> getBookings(
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
) {
    // Convert LocalDate to Instant for database query
    Instant start = startDate.atStartOfDay(ZoneId.of("America/Santiago")).toInstant();
    Instant end = endDate.plusDays(1).atStartOfDay(ZoneId.of("America/Santiago")).toInstant();

    List<Booking> bookings = bookingRepository.findByCreatedAtBetween(start, end);
    return ResponseEntity.ok(bookings.stream().map(this::toDto).toList());
}
```

---

## Testing

### Backend Unit Test Example
```java
@Test
void testDateTimeSerialization() throws Exception {
    TourSchedule schedule = new TourSchedule();
    schedule.setStartDatetime(Instant.parse("2025-01-15T14:30:00Z"));

    String json = objectMapper.writeValueAsString(schedule);

    assertThat(json).contains("2025-01-15T14:30:00Z");
}

@Test
void testLocalDateSerialization() throws Exception {
    User user = new User();
    user.setDateOfBirth(LocalDate.parse("1990-05-15"));

    String json = objectMapper.writeValueAsString(user);

    assertThat(json).contains("1990-05-15");
}
```

### Frontend Test Example
```typescript
describe('Date handling', () => {
  it('should parse Instant correctly', () => {
    const instant = "2025-01-15T14:30:00Z";
    const date = new Date(instant);
    expect(date.toISOString()).toBe(instant);
  });

  it('should send LocalDate as string', () => {
    const participant = {
      dateOfBirth: "1990-05-15"
    };
    expect(typeof participant.dateOfBirth).toBe('string');
  });
});
```

---

## Troubleshooting

### Issue: Dates showing wrong day in frontend

**Cause:** Converting LocalDate string to Date object
**Solution:** Keep LocalDate as string, only convert to Date for display if needed

```typescript
// ❌ Wrong
const birthDate = new Date("1990-05-15"); // May show May 14 due to timezone

// ✅ Correct
const birthDate = "1990-05-15"; // Keep as string
```

### Issue: TourSchedule times off by several hours

**Cause:** Frontend not handling UTC offset correctly
**Solution:** Use Intl.DateTimeFormat with explicit timezone

```typescript
// ❌ Wrong
const date = new Date(schedule.startDatetime).toLocaleString();

// ✅ Correct
const formatter = new Intl.DateTimeFormat('es-CL', {
  timeZone: 'America/Santiago'
});
const date = formatter.format(new Date(schedule.startDatetime));
```

### Issue: Backend validation fails on date format

**Cause:** Frontend sending Date object instead of string
**Solution:** Always send dates as formatted strings

```typescript
// ❌ Wrong
const payload = {
  dateOfBirth: new Date("1990-05-15")
};

// ✅ Correct
const payload = {
  dateOfBirth: "1990-05-15"
};
```

---

## Migration Checklist

When updating existing date/time code:

- [ ] Verify all `Instant` fields stored as UTC in database
- [ ] Check all `LocalDate` fields use ISO format (YYYY-MM-DD)
- [ ] Ensure frontend sends LocalDate as string, not Date object
- [ ] Update all date displays to use Intl.DateTimeFormat
- [ ] Remove manual timezone calculations
- [ ] Add @DateTimeFormat annotations to controller parameters
- [ ] Test with different timezones (especially DST transitions)
- [ ] Update API documentation with date format examples

---

## References

- [ISO-8601 Standard](https://en.wikipedia.org/wiki/ISO_8601)
- [Java 8 Date/Time API](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html)
- [Jackson Java Time Module](https://github.com/FasterXML/jackson-modules-java8)
- [MDN: Intl.DateTimeFormat](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Intl/DateTimeFormat)
- [Spring Boot Jackson Config](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#application-properties.json.spring.jackson)
