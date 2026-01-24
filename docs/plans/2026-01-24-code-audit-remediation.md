# Code Audit Remediation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Fix 53 code quality, performance, and architecture issues identified in the full code audit.

**Architecture:** Six phases organized by priority and domain. Each phase can be executed independently. Critical backend issues first, then frontend, then performance optimizations.

**Tech Stack:** Spring Boot 3.5.7, Java 21, Nuxt 3, Vue 3, TypeScript, PostgreSQL

---

## Phase 1: Critical Backend Issues (5 issues)

### Task 1.1: Extract Booking Creation from PaymentSessionService

**Files:**
- Create: `backend/src/main/java/com/northernchile/api/booking/BookingCreationService.java`
- Modify: `backend/src/main/java/com/northernchile/api/payment/PaymentSessionService.java:618-720`

**Problem:** PaymentSessionService is a 737-line god class that handles payment orchestration AND booking creation AND email notifications. The `createBookingsFromSession()` method (lines 618-720) should be in a dedicated service.

**Step 1: Create BookingCreationService interface**

```java
package com.northernchile.api.booking;

import com.northernchile.api.model.User;
import com.northernchile.api.payment.model.PaymentSession;
import java.util.List;
import java.util.UUID;

public interface BookingCreationService {
    List<UUID> createBookingsFromPaymentSession(PaymentSession session, User user);
}
```

**Step 2: Create BookingCreationServiceImpl**

Extract the `createBookingsFromSession()` method from PaymentSessionService into a new implementation class. Move lines 618-720 to the new service.

**Step 3: Inject BookingCreationService into PaymentSessionService**

Replace the inline booking creation with a call to the new service.

**Step 4: Run tests**

Run: `docker-compose exec backend mvn test -Dtest=PaymentSessionServiceTest`

**Step 5: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/booking/BookingCreationService.java
git add backend/src/main/java/com/northernchile/api/booking/BookingCreationServiceImpl.java
git add backend/src/main/java/com/northernchile/api/payment/PaymentSessionService.java
git commit -m "refactor: extract BookingCreationService from PaymentSessionService"
```

---

### Task 1.2: Remove @Lazy Circular Dependency

**Files:**
- Modify: `backend/src/main/java/com/northernchile/api/payment/PaymentSessionService.java:74`
- Modify: `backend/src/main/java/com/northernchile/api/booking/BookingService.java`

**Problem:** PaymentSessionService uses `@Lazy BookingService` (line 74) indicating a circular dependency. After Task 1.1, the email notification should be triggered via an event instead.

**Step 1: Create BookingCreatedEvent**

```java
package com.northernchile.api.booking.event;

import java.util.UUID;

public record BookingCreatedEvent(UUID bookingId) {}
```

**Step 2: Create BookingEventPublisher**

```java
package com.northernchile.api.booking.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class BookingEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    public BookingEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publishBookingCreated(UUID bookingId) {
        eventPublisher.publishEvent(new BookingCreatedEvent(bookingId));
    }
}
```

**Step 3: Create BookingEventListener in BookingService**

```java
@EventListener
@Async
public void handleBookingCreated(BookingCreatedEvent event) {
    Booking booking = bookingRepository.findByIdWithDetails(event.bookingId())
        .orElseThrow(() -> new IllegalStateException("Booking not found: " + event.bookingId()));
    sendBookingConfirmationNotifications(booking);
}
```

**Step 4: Remove @Lazy from PaymentSessionService constructor**

Remove line 74's `@Lazy` annotation and replace direct `bookingService.sendBookingConfirmationNotifications()` call with event publishing.

**Step 5: Compile and test**

Run: `docker-compose exec backend mvn compile`

**Step 6: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/booking/event/
git add backend/src/main/java/com/northernchile/api/payment/PaymentSessionService.java
git add backend/src/main/java/com/northernchile/api/booking/BookingService.java
git commit -m "refactor: replace @Lazy with event-driven booking notifications"
```

---

### Task 1.3: Fix Controller-to-Repository Direct Access

**Files:**
- Modify: `backend/src/main/java/com/northernchile/api/tour/TourController.java:83-88`
- Modify: `backend/src/main/java/com/northernchile/api/tour/TourService.java`

**Problem:** `TourController.getTourByIdPublic()` calls `tourRepository.findByIdNotDeleted()` directly, bypassing business logic in TourService.

**Step 1: Add method to TourService**

```java
public TourRes getTourByIdPublic(UUID id) {
    Tour tour = tourRepository.findByIdNotDeleted(id)
        .orElseThrow(() -> new EntityNotFoundException("Tour not found with id: " + id));
    TourRes tourRes = tourMapper.toTourRes(tour);
    return populateSingleTourImages(tourRes);
}
```

**Step 2: Update TourController to use service**

```java
@GetMapping("/tours/{id}")
public ResponseEntity<TourRes> getTourByIdPublic(@PathVariable UUID id) {
    TourRes tourRes = tourService.getTourByIdPublic(id);
    return ResponseEntity.ok(tourRes);
}
```

**Step 3: Remove tourRepository injection from TourController if now unused**

**Step 4: Run tests**

Run: `docker-compose exec backend mvn test -Dtest=TourControllerTest`

**Step 5: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/tour/TourController.java
git add backend/src/main/java/com/northernchile/api/tour/TourService.java
git commit -m "fix: route TourController.getTourByIdPublic through service layer"
```

---

### Task 1.4: Batch Schedule Generation Queries

**Files:**
- Modify: `backend/src/main/java/com/northernchile/api/tour/TourScheduleGeneratorService.java:82-104`
- Modify: `backend/src/main/java/com/northernchile/api/tour/TourScheduleRepository.java`

**Problem:** The schedule generator runs 90 queries per tour (4,500+ queries for 50 tours) checking if schedules exist one-by-one in a loop (lines 89-103).

**Step 1: Add batch query to TourScheduleRepository**

```java
@Query("SELECT ts.tour.id, FUNCTION('DATE', ts.startDatetime) FROM TourSchedule ts " +
       "WHERE ts.tour.id IN :tourIds AND ts.startDatetime BETWEEN :start AND :end")
List<Object[]> findExistingScheduleDates(
    @Param("tourIds") List<UUID> tourIds,
    @Param("start") Instant start,
    @Param("end") Instant end
);
```

**Step 2: Refactor generateSchedulesAutomatically()**

```java
@Transactional
public void generateSchedulesAutomatically() {
    LocalDate today = LocalDate.now(DateTimeUtils.CHILE_ZONE);
    LocalDate endDate = today.plusDays(DAYS_AHEAD_ALL);

    List<Tour> recurringTours = tourRepository.findByRecurringTrueAndStatus(TourStatus.PUBLISHED);
    if (recurringTours.isEmpty()) return;

    // Batch load ALL existing schedules for 90-day window
    List<UUID> tourIds = recurringTours.stream().map(Tour::getId).toList();
    Instant startInstant = today.atStartOfDay(DateTimeUtils.CHILE_ZONE).toInstant();
    Instant endInstant = endDate.atStartOfDay(DateTimeUtils.CHILE_ZONE).toInstant();

    Set<String> existingSchedules = tourScheduleRepository
        .findExistingScheduleDates(tourIds, startInstant, endInstant)
        .stream()
        .map(row -> row[0] + "_" + row[1]) // tourId_date
        .collect(Collectors.toSet());

    // Cache parsed cron rules
    Map<UUID, Cron> cronCache = new HashMap<>();

    int schedulesCreated = 0;
    for (Tour tour : recurringTours) {
        for (LocalDate date = today; date.isBefore(endDate); date = date.plusDays(1)) {
            String key = tour.getId() + "_" + date;
            if (existingSchedules.contains(key)) continue;

            if (shouldGenerateScheduleForDate(tour, date, cronCache)) {
                createScheduleInstance(tour, date);
                schedulesCreated++;
            }
        }
    }
    logger.info("Generated {} schedules", schedulesCreated);
}
```

**Step 3: Update matchesCronRule to use cache**

```java
private boolean matchesCronRule(LocalDate date, String recurrenceRule, Map<UUID, Cron> cache, UUID tourId) {
    if (recurrenceRule == null || recurrenceRule.isEmpty()) return true;

    Cron cron = cache.computeIfAbsent(tourId, id -> {
        try {
            return cronParser.parse(recurrenceRule);
        } catch (Exception e) {
            logger.warn("Invalid cron rule for tour {}: {}", id, recurrenceRule);
            return null;
        }
    });

    if (cron == null) return true; // Invalid cron, default to daily
    // ... rest of logic
}
```

**Step 4: Run tests**

Run: `docker-compose exec backend mvn test -Dtest=TourScheduleGeneratorServiceTest`

**Step 5: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/tour/TourScheduleGeneratorService.java
git add backend/src/main/java/com/northernchile/api/tour/TourScheduleRepository.java
git commit -m "perf: batch load existing schedules to eliminate N+1 queries"
```

---

### Task 1.5: Batch TourReminderService Saves

**Files:**
- Modify: `backend/src/main/java/com/northernchile/api/notification/TourReminderService.java:66-77`

**Problem:** Each booking calls `bookingRepository.save(booking)` inside a loop (line 71), causing N separate UPDATE queries.

**Step 1: Refactor to batch saves outside loop**

```java
@Transactional
public void sendTourReminders() {
    // ... existing code to find bookings ...

    List<Booking> sentReminders = new ArrayList<>();

    for (Booking booking : upcomingBookings) {
        try {
            sendReminderEmail(booking);
            booking.setReminderSentAt(Instant.now());
            sentReminders.add(booking);
            successCount++;
        } catch (Exception e) {
            log.error("Failed to send reminder for booking: {}", booking.getId(), e);
            failCount++;
        }
    }

    // Batch save all successful reminders
    if (!sentReminders.isEmpty()) {
        bookingRepository.saveAll(sentReminders);
    }

    log.info("Tour reminders: {} sent, {} failed", successCount, failCount);
}
```

**Step 2: Run tests**

Run: `docker-compose exec backend mvn test -Dtest=TourReminderServiceTest`

**Step 3: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/notification/TourReminderService.java
git commit -m "perf: batch save booking reminders instead of N individual saves"
```

---

## Phase 2: Critical Frontend Issues (4 issues)

### Task 2.1: Fix Deep Watcher on Tours Array

**Files:**
- Modify: `frontend/app/components/TourCalendar.vue:549-557`

**Problem:** Deep watching `props.tours` array causes expensive object traversal on every property change.

**Step 1: Change deep watcher to shallow comparison**

Replace lines 549-557:

```typescript
// Before: { deep: true } causes full object traversal
watch(
  () => props.tours,
  async () => {
    if (props.tours.length > 0) {
      await fetchSchedules()
    }
  },
  { deep: true }
)

// After: Watch only the array reference and length
watch(
  () => [props.tours, props.tours.length] as const,
  async ([newTours], [oldTours]) => {
    // Only refetch if tours array changed (not internal properties)
    if (newTours !== oldTours && newTours.length > 0) {
      await fetchSchedules()
    }
  }
)
```

**Step 2: Test calendar functionality**

Open the tours page and verify the calendar still updates when tours are loaded.

**Step 3: Commit**

```bash
git add frontend/app/components/TourCalendar.vue
git commit -m "perf: replace deep watcher with shallow comparison on tours array"
```

---

### Task 2.2: Split useAdminData Mega-Composable

**Files:**
- Modify: `frontend/app/composables/useAdminData.ts`
- Create: `frontend/app/composables/admin/useAdminTours.ts`
- Create: `frontend/app/composables/admin/useAdminSchedules.ts`
- Create: `frontend/app/composables/admin/useAdminBookings.ts`
- Create: `frontend/app/composables/admin/useAdminUsers.ts`
- Create: `frontend/app/composables/admin/useAdminMedia.ts`
- Create: `frontend/app/composables/admin/useAdminReports.ts`

**Problem:** `useAdminData.ts` is a 275-line mega-composable with 50+ functions. Hard to maintain and navigate.

**Step 1: Create shared base fetch options**

Create `frontend/app/composables/admin/useAdminFetch.ts`:

```typescript
export const useAdminFetch = () => {
  const baseFetchOptions = {
    credentials: 'include' as RequestCredentials
  }

  const jsonHeaders = {
    'Content-Type': 'application/json'
  }

  return { baseFetchOptions, jsonHeaders }
}
```

**Step 2: Extract tour functions to useAdminTours.ts**

```typescript
// frontend/app/composables/admin/useAdminTours.ts
import type { TourCreateReq, TourUpdateReq, TourRes } from 'api-client'
import { useAdminFetch } from './useAdminFetch'

export const useAdminTours = () => {
  const { baseFetchOptions, jsonHeaders } = useAdminFetch()

  return {
    fetchAdminTours: () =>
      $fetch<TourRes[]>('/api/admin/tours', { ...baseFetchOptions }),
    createAdminTour: (tourData: TourCreateReq) =>
      $fetch<TourRes>('/api/admin/tours', {
        method: 'POST',
        body: tourData,
        headers: jsonHeaders,
        ...baseFetchOptions
      }),
    // ... rest of tour functions
  }
}
```

**Step 3: Extract schedule functions to useAdminSchedules.ts**

**Step 4: Extract booking functions to useAdminBookings.ts**

**Step 5: Extract user functions to useAdminUsers.ts**

**Step 6: Extract media functions to useAdminMedia.ts**

**Step 7: Extract reports functions to useAdminReports.ts**

**Step 8: Update useAdminData.ts to re-export all**

```typescript
// frontend/app/composables/useAdminData.ts
// Re-export for backwards compatibility
export { useAdminTours } from './admin/useAdminTours'
export { useAdminSchedules } from './admin/useAdminSchedules'
export { useAdminBookings } from './admin/useAdminBookings'
export { useAdminUsers } from './admin/useAdminUsers'
export { useAdminMedia } from './admin/useAdminMedia'
export { useAdminReports } from './admin/useAdminReports'

// Composite export for existing code
export const useAdminData = () => ({
  ...useAdminTours(),
  ...useAdminSchedules(),
  ...useAdminBookings(),
  ...useAdminUsers(),
  ...useAdminMedia(),
  ...useAdminReports()
})
```

**Step 9: Run typecheck**

Run: `cd frontend && pnpm typecheck`

**Step 10: Commit**

```bash
git add frontend/app/composables/admin/
git add frontend/app/composables/useAdminData.ts
git commit -m "refactor: split useAdminData into feature-specific composables"
```

---

### Task 2.3: Fix Watch Cleanup in useAdminTourForm

**Files:**
- Modify: `frontend/app/composables/useAdminTourForm.ts:153-163`

**Problem:** The setTimeout for auto-save is never cleaned up on unmount, causing potential memory leaks.

**Step 1: Replace manual timeout with useDebounceFn**

```typescript
// Before (lines 153-163):
let saveTimeout: ReturnType<typeof setTimeout> | null = null
watch(
  () => state,
  () => {
    if (!props.tour) {
      if (saveTimeout) clearTimeout(saveTimeout)
      saveTimeout = setTimeout(saveDraft, 1000)
    }
  },
  { deep: true }
)

// After:
import { useDebounceFn } from '@vueuse/core'

const debouncedSaveDraft = useDebounceFn(() => {
  if (!props.tour) {
    saveDraft()
  }
}, 1000)

watch(
  () => state,
  () => debouncedSaveDraft(),
  { deep: true }
)
```

**Step 2: Run typecheck**

Run: `cd frontend && pnpm typecheck`

**Step 3: Commit**

```bash
git add frontend/app/composables/useAdminTourForm.ts
git commit -m "fix: replace manual setTimeout with useDebounceFn for proper cleanup"
```

---

### Task 2.4: Fix SSR Boundary Violation in checkout.vue

**Files:**
- Modify: `frontend/app/pages/checkout.vue:64-67`

**Problem:** Cart empty check runs at module level during SSR when cart store isn't initialized.

**Step 1: Move redirect to onMounted**

```typescript
// Before (lines 64-67):
// Redirect if cart is empty
if (cartStore.cart.items.length === 0) {
  router.push(localePath('/cart'))
}

// After:
onMounted(() => {
  // Only redirect on client after hydration
  if (cartStore.cart.items.length === 0) {
    router.push(localePath('/cart'))
  }
})
```

**Step 2: Run dev server and test checkout page**

Run: `cd frontend && pnpm dev`

**Step 3: Commit**

```bash
git add frontend/app/pages/checkout.vue
git commit -m "fix: move cart empty check to onMounted for SSR compatibility"
```

---

## Phase 3: Database & Performance (8 issues)

### Task 3.1: Add Database Indexes

**Files:**
- Create: `backend/src/main/resources/db/migration/V2__add_performance_indexes.sql`

**Problem:** Several frequently filtered columns lack indexes.

**Step 1: Create migration file**

```sql
-- V2__add_performance_indexes.sql
-- Indexes for booking queries
CREATE INDEX IF NOT EXISTS idx_booking_status ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_booking_status_reminder ON bookings(status, reminder_sent_at);
CREATE INDEX IF NOT EXISTS idx_booking_created_at ON bookings(created_at);

-- Indexes for tour schedule queries
CREATE INDEX IF NOT EXISTS idx_tour_schedule_status ON tour_schedules(status);
CREATE INDEX IF NOT EXISTS idx_tour_schedule_start ON tour_schedules(start_datetime);
CREATE INDEX IF NOT EXISTS idx_tour_schedule_tour_start ON tour_schedules(tour_id, start_datetime);

-- Indexes for tour queries
CREATE INDEX IF NOT EXISTS idx_tour_status ON tours(status);
CREATE INDEX IF NOT EXISTS idx_tour_deleted_at ON tours(deleted_at);
CREATE INDEX IF NOT EXISTS idx_tour_status_deleted ON tours(status, deleted_at);

-- Indexes for cart cleanup
CREATE INDEX IF NOT EXISTS idx_cart_expires_at ON carts(expires_at);

-- Indexes for user lookups
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_provider_id ON users(provider_id);
```

**Step 2: Restart backend to apply migration**

Run: `docker-compose restart backend`

**Step 3: Verify indexes**

Run: `docker-compose exec database psql -U user -d northernchile_db -c "\\di"`

**Step 4: Commit**

```bash
git add backend/src/main/resources/db/migration/V2__add_performance_indexes.sql
git commit -m "perf: add database indexes for frequently filtered columns"
```

---

### Task 3.2: Add @BatchSize to Entity Relationships

**Files:**
- Modify: `backend/src/main/java/com/northernchile/api/model/TourSchedule.java`
- Modify: `backend/src/main/java/com/northernchile/api/model/Booking.java`
- Modify: `backend/src/main/java/com/northernchile/api/model/Tour.java`

**Problem:** Lazy-loaded relationships without @BatchSize cause N+1 queries.

**Step 1: Add @BatchSize to TourSchedule.tour**

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "tour_id", nullable = false)
@BatchSize(size = 20)
private Tour tour;
```

**Step 2: Add @BatchSize to Booking.participants**

```java
@OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
@BatchSize(size = 50)
private List<Participant> participants = new ArrayList<>();
```

**Step 3: Add @BatchSize to Tour.owner**

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "owner_id")
@BatchSize(size = 20)
private User owner;
```

**Step 4: Add import for @BatchSize**

```java
import org.hibernate.annotations.BatchSize;
```

**Step 5: Compile and test**

Run: `docker-compose exec backend mvn compile`

**Step 6: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/model/TourSchedule.java
git add backend/src/main/java/com/northernchile/api/model/Booking.java
git add backend/src/main/java/com/northernchile/api/model/Tour.java
git commit -m "perf: add @BatchSize to lazy relationships to reduce N+1 queries"
```

---

### Task 3.3: Fix ManifestService N+1 for Participants

**Files:**
- Modify: `backend/src/main/java/com/northernchile/api/tour/ManifestService.java:56-74`
- Modify: `backend/src/main/java/com/northernchile/api/booking/BookingRepository.java`

**Problem:** `findByScheduleId()` doesn't eagerly load participants, causing N+1.

**Step 1: Add repository method with eager fetch**

```java
@Query("SELECT b FROM Booking b " +
       "LEFT JOIN FETCH b.participants " +
       "WHERE b.schedule.id = :scheduleId AND b.status = :status")
List<Booking> findByScheduleIdAndStatusWithParticipants(
    @Param("scheduleId") UUID scheduleId,
    @Param("status") BookingStatus status
);
```

**Step 2: Update ManifestService to use new method**

```java
List<Booking> confirmedBookings = bookingRepository
    .findByScheduleIdAndStatusWithParticipants(schedule.getId(), BookingStatus.CONFIRMED);
```

**Step 3: Run tests**

Run: `docker-compose exec backend mvn test -Dtest=ManifestServiceTest`

**Step 4: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/tour/ManifestService.java
git add backend/src/main/java/com/northernchile/api/booking/BookingRepository.java
git commit -m "perf: eager load participants in ManifestService to fix N+1"
```

---

### Task 3.4: Optimize WeatherService Multiple Stream Passes

**Files:**
- Modify: `backend/src/main/java/com/northernchile/api/external/WeatherService.java:124-198`

**Problem:** `processForecastData()` iterates the same collection 7+ times with separate stream operations.

**Step 1: Refactor to single-pass aggregation**

```java
private DayForecast processForecastData(List<ForecastItem> items) {
    if (items.isEmpty()) {
        return DayForecast.empty();
    }

    // Single pass through data
    double tempSum = 0, tempMax = Double.MIN_VALUE, tempMin = Double.MAX_VALUE;
    double windSum = 0, windMax = Double.MIN_VALUE;
    double cloudsSum = 0, rainSum = 0;
    int count = items.size();

    for (ForecastItem item : items) {
        double temp = item.main().temp();
        tempSum += temp;
        tempMax = Math.max(tempMax, item.main().tempMax());
        tempMin = Math.min(tempMin, item.main().tempMin());

        double wind = item.wind().speed();
        windSum += wind;
        windMax = Math.max(windMax, wind);

        cloudsSum += item.clouds().all();
        rainSum += item.rain() != null ? item.rain().threeHour() : 0;
    }

    return new DayForecast(
        tempSum / count,  // tempDay
        tempMax,
        tempMin,
        windSum / count,  // avgWind
        windMax,
        cloudsSum / count,  // avgClouds
        rainSum
    );
}
```

**Step 2: Run tests**

Run: `docker-compose exec backend mvn test -Dtest=WeatherServiceTest`

**Step 3: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/external/WeatherService.java
git commit -m "perf: single-pass aggregation in WeatherService.processForecastData"
```

---

### Task 3.5: Move DateTimeFormatters to Static Fields

**Files:**
- Modify: `backend/src/main/java/com/northernchile/api/booking/BookingService.java:73-81`
- Modify: `backend/src/main/java/com/northernchile/api/notification/TourReminderService.java:87-93`
- Modify: `backend/src/main/java/com/northernchile/api/tour/ManifestService.java`

**Problem:** DateTimeFormatters are created repeatedly in methods called in loops.

**Step 1: Add static formatters to each service**

```java
private static final DateTimeFormatter DATE_FORMATTER =
    DateTimeFormatter.ofPattern("yyyy-MM-dd")
        .withZone(DateTimeUtils.CHILE_ZONE);

private static final DateTimeFormatter TIME_FORMATTER =
    DateTimeFormatter.ofPattern("HH:mm")
        .withZone(DateTimeUtils.CHILE_ZONE);
```

**Step 2: Replace inline formatter creation with static references**

**Step 3: Compile**

Run: `docker-compose exec backend mvn compile`

**Step 4: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/booking/BookingService.java
git add backend/src/main/java/com/northernchile/api/notification/TourReminderService.java
git add backend/src/main/java/com/northernchile/api/tour/ManifestService.java
git commit -m "perf: move DateTimeFormatters to static fields"
```

---

### Task 3.6: Add API Cache Strategy to TourCalendar

**Files:**
- Modify: `frontend/app/components/TourCalendar.vue:87-220`

**Problem:** Calendar data fetched on every mount without checking if data is recent.

**Step 1: Add cache timestamp tracking**

```typescript
const CACHE_DURATION_MS = 5 * 60 * 1000 // 5 minutes
const lastFetchTime = ref<number>(0)

async function fetchCalendarData(force = false) {
  const now = Date.now()

  // Skip fetch if data is recent (unless forced)
  if (!force && lastFetchTime.value && (now - lastFetchTime.value) < CACHE_DURATION_MS) {
    return
  }

  loading.value = true
  try {
    await Promise.all([fetchSchedules(), fetchLunarData(), fetchWeatherData()])
    lastFetchTime.value = now
  } finally {
    loading.value = false
  }
}
```

**Step 2: Update exposed refresh to force fetch**

```typescript
defineExpose({
  refresh: () => fetchCalendarData(true)
})
```

**Step 3: Test calendar refresh behavior**

**Step 4: Commit**

```bash
git add frontend/app/components/TourCalendar.vue
git commit -m "perf: add 5-minute cache to TourCalendar data fetching"
```

---

### Task 3.7: Fix Cart Store Deep Clone

**Files:**
- Modify: `frontend/app/stores/cart.ts:125,210`

**Problem:** `JSON.parse(JSON.stringify())` is used for deep cloning, which is slow.

**Step 1: Replace with structuredClone**

```typescript
// Before:
const previousCart = JSON.parse(JSON.stringify(_cart.value)) as CartRes

// After:
const previousCart = structuredClone(_cart.value)
```

**Step 2: Run typecheck**

Run: `cd frontend && pnpm typecheck`

**Step 3: Commit**

```bash
git add frontend/app/stores/cart.ts
git commit -m "perf: use structuredClone instead of JSON.parse/stringify in cart store"
```

---

### Task 3.8: Split calendarOptions Computed in Admin Calendar

**Files:**
- Modify: `frontend/app/pages/admin/calendar.vue:582-751`

**Problem:** 150+ line computed property rebuilds entirely on any data change.

**Step 1: Extract event mapping to separate computed**

```typescript
// Extract schedule events processing
const scheduleEvents = computed(() => {
  if (!calendarData.value?.schedules) return []

  return calendarData.value.schedules.map((schedule: TourScheduleRes) => {
    const start = new Date(schedule.startDatetime)
    const startInChile = instantToChileLocalString(schedule.startDatetime)
    // ... rest of schedule mapping
    return {
      id: schedule.id,
      title: tourName,
      start: startInChile,
      // ... rest of properties
    }
  })
})

// Main calendar options (now simpler)
const calendarOptions = computed<CalendarOptions | null>(() => {
  if (!calendarData.value) return null

  return {
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin, listPlugin],
    // ... static options
    events: scheduleEvents.value
  }
})
```

**Step 2: Run typecheck**

Run: `cd frontend && pnpm typecheck`

**Step 3: Commit**

```bash
git add frontend/app/pages/admin/calendar.vue
git commit -m "perf: extract event mapping from calendarOptions computed"
```

---

## Phase 4: Backend Code Quality (10 issues)

### Task 4.1: Fix MapStruct Field Injection

**Files:**
- Modify: `backend/src/main/java/com/northernchile/api/tour/TourMapper.java:29-32`
- Modify: `backend/src/main/java/com/northernchile/api/media/mapper/MediaMapper.java:18-21`

**Problem:** MapStruct mappers use `@Autowired` setter injection instead of proper MapStruct patterns.

**Step 1: Update TourMapper to use @Mapping with expression**

For S3 URL generation, use MapStruct's expression feature instead of injecting service:

```java
@Mapper(componentModel = "spring", uses = {S3StorageService.class})
public interface TourMapper {
    // Use @Context for services needed during mapping
    TourRes toTourRes(Tour tour, @Context S3StorageService s3Service);
}
```

**Step 2: Compile and test**

Run: `docker-compose exec backend mvn compile`

**Step 3: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/tour/TourMapper.java
git add backend/src/main/java/com/northernchile/api/media/mapper/MediaMapper.java
git commit -m "fix: use MapStruct @Context instead of @Autowired for service injection"
```

---

### Task 4.2: Fix Unchecked Casts in WebhookController

**Files:**
- Modify: `backend/src/main/java/com/northernchile/api/payment/WebhookController.java:54-78`

**Problem:** JSON payload casts without type safety.

**Step 1: Create typed DTOs for webhook payloads**

```java
public record MercadoPagoWebhookPayload(
    String action,
    String type,
    MercadoPagoWebhookData data
) {}

public record MercadoPagoWebhookData(
    String id
) {}
```

**Step 2: Use ObjectMapper with type safety**

```java
MercadoPagoWebhookPayload payload = objectMapper.readValue(rawBody, MercadoPagoWebhookPayload.class);
```

**Step 3: Compile and test**

Run: `docker-compose exec backend mvn compile`

**Step 4: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/payment/WebhookController.java
git add backend/src/main/java/com/northernchile/api/payment/dto/
git commit -m "fix: add type-safe DTOs for webhook payloads"
```

---

### Task 4.3: Add Null Checks in RefundService

**Files:**
- Modify: `backend/src/main/java/com/northernchile/api/payment/RefundService.java:133`

**Problem:** Chained method calls without null checks.

**Step 1: Add defensive null checks**

```java
// Before:
String tourName = booking.getSchedule().getTour().getNameTranslations()
    .getOrDefault("es", "Tour").toString();

// After:
String tourName = Optional.ofNullable(booking.getSchedule())
    .map(TourSchedule::getTour)
    .map(Tour::getNameTranslations)
    .map(translations -> translations.getOrDefault("es", "Tour"))
    .orElse("Tour");
```

**Step 2: Compile**

Run: `docker-compose exec backend mvn compile`

**Step 3: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/payment/RefundService.java
git commit -m "fix: add null-safe chain in RefundService.getTourName"
```

---

### Task 4.4: Replace Hardcoded Status Strings in Queries

**Files:**
- Modify: `backend/src/main/java/com/northernchile/api/booking/BookingRepository.java:24,27,119,138`

**Problem:** String literal 'CONFIRMED' in JPQL queries instead of enum.

**Step 1: Use SpEL for enum reference**

```java
// Before:
@Query("SELECT COUNT(p) FROM Participant p WHERE p.booking.schedule.id = :scheduleId AND p.booking.status = 'CONFIRMED'")

// After:
@Query("SELECT COUNT(p) FROM Participant p WHERE p.booking.schedule.id = :scheduleId AND p.booking.status = com.northernchile.api.model.BookingStatus.CONFIRMED")
```

**Alternative:** Use parameter binding:

```java
@Query("SELECT COUNT(p) FROM Participant p WHERE p.booking.schedule.id = :scheduleId AND p.booking.status = :status")
int countConfirmedParticipantsByScheduleId(@Param("scheduleId") UUID scheduleId, @Param("status") BookingStatus status);
```

**Step 2: Compile**

Run: `docker-compose exec backend mvn compile`

**Step 3: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/booking/BookingRepository.java
git commit -m "fix: use enum parameters instead of hardcoded status strings in queries"
```

---

### Task 4.5: Add @Transactional(readOnly=true) to Read Methods

**Files:**
- Modify: `backend/src/main/java/com/northernchile/api/booking/BookingService.java`

**Problem:** Class-level `@Transactional` means read-only queries open read-write transactions.

**Step 1: Add readOnly=true to getter methods**

```java
@Transactional(readOnly = true)
public List<BookingRes> getAllBookings() { ... }

@Transactional(readOnly = true)
public BookingRes getBookingById(UUID id) { ... }

@Transactional(readOnly = true)
public List<BookingRes> getBookingsByUser(UUID userId) { ... }
```

**Step 2: Compile**

Run: `docker-compose exec backend mvn compile`

**Step 3: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/booking/BookingService.java
git commit -m "perf: add @Transactional(readOnly=true) to read-only methods"
```

---

### Task 4.6: Create Parameter Object for EmailService

**Files:**
- Create: `backend/src/main/java/com/northernchile/api/notification/dto/BookingEmailData.java`
- Modify: `backend/src/main/java/com/northernchile/api/notification/EmailService.java:70-72`

**Problem:** `sendBookingConfirmationEmail` has 9 parameters.

**Step 1: Create parameter record**

```java
public record BookingEmailData(
    String toEmail,
    String customerName,
    String bookingId,
    String tourName,
    String tourDate,
    String tourTime,
    int participantCount,
    String totalAmount,
    String languageCode
) {}
```

**Step 2: Update method signature**

```java
public CompletableFuture<Void> sendBookingConfirmationEmail(BookingEmailData data) {
    // Use data.toEmail(), data.customerName(), etc.
}
```

**Step 3: Update callers**

**Step 4: Compile**

Run: `docker-compose exec backend mvn compile`

**Step 5: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/notification/dto/BookingEmailData.java
git add backend/src/main/java/com/northernchile/api/notification/EmailService.java
git commit -m "refactor: create BookingEmailData record to replace 9-parameter method"
```

---

### Task 4.7: Externalize SESSION_EXPIRATION_MINUTES

**Files:**
- Modify: `backend/src/main/java/com/northernchile/api/payment/PaymentSessionService.java:53`
- Modify: `backend/src/main/java/com/northernchile/api/config/properties/PaymentProperties.java`

**Problem:** Hardcoded magic number for session expiration.

**Step 1: Add to PaymentProperties**

```java
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {
    private int sessionExpirationMinutes = 30;
    // getter/setter
}
```

**Step 2: Inject and use in PaymentSessionService**

```java
private final PaymentProperties paymentProperties;

// In createSession():
session.setExpiresAt(Instant.now().plus(
    paymentProperties.getSessionExpirationMinutes(), ChronoUnit.MINUTES));
```

**Step 3: Compile**

Run: `docker-compose exec backend mvn compile`

**Step 4: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/payment/PaymentSessionService.java
git add backend/src/main/java/com/northernchile/api/config/properties/PaymentProperties.java
git commit -m "config: externalize payment session expiration to properties"
```

---

### Task 4.8: Fix Cart.equals() to Use ID Only

**Files:**
- Modify: `backend/src/main/java/com/northernchile/api/model/Cart.java:97-107`

**Problem:** equals() compares lazy-loaded collections, causing LazyInitializationException.

**Step 1: Simplify equals/hashCode to ID-based**

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Cart cart = (Cart) o;
    return id != null && Objects.equals(id, cart.id);
}

@Override
public int hashCode() {
    return getClass().hashCode();
}
```

**Step 2: Compile**

Run: `docker-compose exec backend mvn compile`

**Step 3: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/model/Cart.java
git commit -m "fix: use ID-only equals/hashCode in Cart entity"
```

---

### Task 4.9: Use Custom Exception Instead of IllegalArgumentException

**Files:**
- Modify: `backend/src/main/java/com/northernchile/api/payment/PaymentService.java:43,68`

**Problem:** Uses generic IllegalArgumentException instead of domain exception.

**Step 1: Replace with ResourceNotFoundException**

```java
// Before:
.orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

// After:
.orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
```

**Step 2: Compile**

Run: `docker-compose exec backend mvn compile`

**Step 3: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/payment/PaymentService.java
git commit -m "fix: use ResourceNotFoundException instead of IllegalArgumentException"
```

---

### Task 4.10: Add Webhook Request Validation

**Files:**
- Modify: `backend/src/main/java/com/northernchile/api/payment/WebhookController.java:45-48`

**Problem:** Webhook endpoint accepts any payload without size or content validation.

**Step 1: Add request body size limit**

```java
@PostMapping(value = "/mercadopago", consumes = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<Map<String, String>> handleMercadoPagoWebhook(
        @RequestBody @Size(max = 65536) String rawBody,  // 64KB limit
        @RequestHeader(value = "x-signature", required = false) String xSignature,
        @RequestHeader(value = "x-request-id", required = false) String requestId) {

    if (rawBody == null || rawBody.isBlank()) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Empty request body"));
    }
    // ... rest of method
}
```

**Step 2: Compile**

Run: `docker-compose exec backend mvn compile`

**Step 3: Commit**

```bash
git add backend/src/main/java/com/northernchile/api/payment/WebhookController.java
git commit -m "fix: add request body validation to webhook endpoints"
```

---

## Phase 5: Frontend Code Quality (8 issues)

### Task 5.1: Replace console.error with Logger

**Files:**
- Modify: `frontend/app/composables/useApiError.ts:240`
- Modify: `frontend/app/components/admin/media/GalleryManager.vue:63,88`

**Problem:** console.error used in production code.

**Step 1: Import and use logger**

```typescript
import { logger } from '~/utils/logger'

// Before:
console.error('Validation errors:', apiError.errors)

// After:
if (import.meta.dev) {
  logger.error('Validation errors:', apiError.errors)
}
```

**Step 2: Run lint**

Run: `cd frontend && pnpm lint`

**Step 3: Commit**

```bash
git add frontend/app/composables/useApiError.ts
git add frontend/app/components/admin/media/GalleryManager.vue
git commit -m "fix: replace console.error with logger utility"
```

---

### Task 5.2: Add i18n to GalleryManager Error Messages

**Files:**
- Modify: `frontend/app/components/admin/media/GalleryManager.vue`

**Problem:** Hardcoded Spanish error messages.

**Step 1: Add i18n keys**

```typescript
const { t } = useI18n()

// Before:
toast.add({ color: 'error', title: 'Error al cargar galería' })

// After:
toast.add({ color: 'error', title: t('admin.media.error_loading_gallery') })
```

**Step 2: Add translations to locale files**

**Step 3: Commit**

```bash
git add frontend/app/components/admin/media/GalleryManager.vue
git add frontend/i18n/locales/*.json
git commit -m "i18n: add translations for GalleryManager error messages"
```

---

### Task 5.3: Fix ErrorBoundary Watch Cleanup

**Files:**
- Modify: `frontend/app/components/ErrorBoundary.vue:77`

**Problem:** Watch on router doesn't assign stop handle for cleanup.

**Step 1: Store and clean up watch**

```typescript
const router = useRouter()
let stopRouteWatch: (() => void) | null = null

onMounted(() => {
  stopRouteWatch = watch(() => router.currentRoute.value, () => {
    error.value = null
  })
})

onUnmounted(() => {
  if (stopRouteWatch) {
    stopRouteWatch()
  }
})
```

**Step 2: Run typecheck**

Run: `cd frontend && pnpm typecheck`

**Step 3: Commit**

```bash
git add frontend/app/components/ErrorBoundary.vue
git commit -m "fix: properly clean up route watcher in ErrorBoundary"
```

---

### Task 5.4: Fix Typed Emits in ParticipantForm

**Files:**
- Modify: `frontend/app/components/booking/ParticipantForm.vue:4-9`

**Problem:** Uses deprecated string array syntax for emits.

**Step 1: Update to typed syntax**

```typescript
// Before:
const emit = defineEmits(['update'])

// After:
const emit = defineEmits<{
  'update': [data: Partial<Participant>]
}>()
```

**Step 2: Run typecheck**

Run: `cd frontend && pnpm typecheck`

**Step 3: Commit**

```bash
git add frontend/app/components/booking/ParticipantForm.vue
git commit -m "fix: use typed defineEmits in ParticipantForm"
```

---

### Task 5.5: Add Error Handling to useCalendarData

**Files:**
- Modify: `frontend/app/composables/useCalendarData.ts:62-71`

**Problem:** Silent failure returns empty array without user feedback.

**Step 1: Add toast notification on error**

```typescript
const fetchMoonPhases = async (startDate: string, endDate: string): Promise<MoonPhase[]> => {
  const toast = useToast()

  try {
    const response = await $fetch<MoonPhase[]>('/api/lunar/calendar', {
      params: { startDate, endDate }
    })
    return response
  } catch (error) {
    logger.error('Error fetching moon phases:', error)
    toast.add({
      color: 'warning',
      title: t('calendar.moon_data_unavailable'),
      timeout: 3000
    })
    return []
  }
}
```

**Step 2: Run typecheck**

Run: `cd frontend && pnpm typecheck`

**Step 3: Commit**

```bash
git add frontend/app/composables/useCalendarData.ts
git commit -m "fix: add user feedback when moon phase data fails to load"
```

---

### Task 5.6: Fix File Export Timing in useAdminTourForm

**Files:**
- Modify: `frontend/app/composables/useAdminTourForm.ts:316-335`

**Problem:** `URL.revokeObjectURL()` called immediately without delay.

**Step 1: Add timeout before cleanup**

```typescript
const exportToJson = () => {
  const exportData = { ...state }
  const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  const tourName = state.nameTranslations?.es || state.contentKey || 'tour'
  const safeName = tourName.toLowerCase().replace(/[^a-z0-9]+/g, '-').substring(0, 50)
  a.download = `tour-${safeName}-${Date.now()}.json`
  document.body.appendChild(a)
  a.click()

  // Delay cleanup to ensure download starts
  setTimeout(() => {
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  }, 100)
}
```

**Step 2: Run typecheck**

Run: `cd frontend && pnpm typecheck`

**Step 3: Commit**

```bash
git add frontend/app/composables/useAdminTourForm.ts
git commit -m "fix: delay URL.revokeObjectURL to ensure download completes"
```

---

### Task 5.7: Fix Payment Callback Timeout Cleanup

**Files:**
- Modify: `frontend/app/pages/payment/callback.vue:62`

**Problem:** setTimeout without cleanup on unmount.

**Step 1: Store and clear timeout**

```typescript
const redirectTimeout = ref<ReturnType<typeof setTimeout> | null>(null)

// Where setTimeout is called:
redirectTimeout.value = setTimeout(() => router.push('/'), 2000)

onUnmounted(() => {
  if (redirectTimeout.value) {
    clearTimeout(redirectTimeout.value)
  }
})
```

**Step 2: Run typecheck**

Run: `cd frontend && pnpm typecheck`

**Step 3: Commit**

```bash
git add frontend/app/pages/payment/callback.vue
git commit -m "fix: clear redirect timeout on unmount in payment callback"
```

---

### Task 5.8: Extract ErrorResponse Interface

**Files:**
- Create: `frontend/app/types/error.ts`
- Modify: `frontend/app/composables/useAdminTourForm.ts:290`

**Problem:** Inline type assertion instead of proper interface.

**Step 1: Create error types file**

```typescript
// frontend/app/types/error.ts
export interface ApiErrorResponse {
  data?: {
    message?: string
    errors?: Record<string, string[]>
  }
  message?: string
  statusCode?: number
}
```

**Step 2: Import and use in composable**

```typescript
import type { ApiErrorResponse } from '~/types/error'

const apiError = error as ApiErrorResponse
```

**Step 3: Run typecheck**

Run: `cd frontend && pnpm typecheck`

**Step 4: Commit**

```bash
git add frontend/app/types/error.ts
git add frontend/app/composables/useAdminTourForm.ts
git commit -m "refactor: extract ApiErrorResponse interface to types file"
```

---

## Phase 6: Quick Wins (Remaining Low-Priority Items)

These can be done in any order as time permits:

1. **Fix GalleryManager confirm() usage** - Replace native `confirm()` with `useConfirmDialog()`
2. **Fix Tour.equals() to exclude translations** - Remove large JSONB columns from equals
3. **Add CookieBanner placeholder height** - Prevent CLS on hydration
4. **Consolidate admin store fetchTours** - Remove duplicate fetching pattern
5. **Add thread-local cleanup in AuditContext** - Handle error paths

---

## Phase 7: E2E Verification

### Task 7.1: Run E2E Tests to Verify All Changes

**Prerequisites:**
- Backend running: `docker-compose up backend`
- Frontend running: `cd frontend && pnpm dev`

**Test Credentials (Admin):**
- Email: `diego@example.com`
- Password: `pass456`
- Role: `ROLE_SUPER_ADMIN`

**Step 1: Run all E2E tests**

```bash
cd frontend && pnpm test:e2e
```

**Step 2: If tests fail, review failures**

Check which tests failed and verify they're not related to the code changes.

**Step 3: Run E2E with UI for debugging (if needed)**

```bash
cd frontend && pnpm test:e2e:ui
```

**Step 4: Manual admin verification**

1. Login as admin at `/auth` with credentials above
2. Navigate to `/admin` dashboard
3. Verify tour listing works
4. Verify calendar loads without errors
5. Verify media gallery loads
6. Check browser console for errors

**Step 5: Document any issues found**

If E2E tests reveal issues from the refactoring, create follow-up tasks.

---

## Summary

| Phase | Issues | Est. Complexity |
|-------|--------|-----------------|
| 1: Critical Backend | 5 | High |
| 2: Critical Frontend | 4 | Medium |
| 3: Database & Perf | 8 | Medium |
| 4: Backend Quality | 10 | Low-Medium |
| 5: Frontend Quality | 8 | Low |
| 6: Quick Wins | ~18 | Low |
| 7: E2E Verification | 1 | Low |
| **Total** | **54** | |

---

**Plan complete and saved to `docs/plans/2026-01-24-code-audit-remediation.md`.**

**Two execution options:**

1. **Subagent-Driven (this session)** - I dispatch fresh subagent per task, review between tasks, fast iteration

2. **Parallel Session (separate)** - Open new session with executing-plans, batch execution with checkpoints

**Which approach?**
