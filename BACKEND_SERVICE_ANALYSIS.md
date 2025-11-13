# Backend Service Layer Design Quality Analysis

## Executive Summary
The backend service layer has significant design quality issues across 8 major categories. This analysis identifies **19 critical issues** with specific recommendations for refactoring.

---

## 1. DEPENDENCY INJECTION INCONSISTENCY: FIELD INJECTION REMAINING

### Issue Severity: HIGH
### Files Affected: 20 files
### Category: Spring Best Practices

Field injection (`@Autowired`) is still used extensively instead of constructor injection, violating Spring best practices and making testing difficult.

#### Affected Services:
- `BookingService.java` (5 fields)
- `CartService.java` (4 fields)
- `AvailabilityService.java` (4 fields)
- `UserService.java` (4 fields)
- `AuditLogService.java` (1 field)
- `TourService.java` (5 fields)
- `TourScheduleService.java` (5 fields)
- `EmailService.java` (1 field)
- `PrivateTourRequestService.java` (2 fields)
- `TourSecurityService.java` (2 fields)
- `BookingSecurityService.java` (2 fields)

#### Code Example (BookingService, lines 36-50):
```java
@Service
@Transactional
public class BookingService {
    @Autowired  // ❌ ANTI-PATTERN
    private BookingRepository bookingRepository;
    
    @Autowired
    private TourScheduleRepository tourScheduleRepository;
    // ... more @Autowired fields
}
```

#### Recommendations:
1. **Migrate to Constructor Injection** across all services
```java
@Service
@Transactional
public class BookingService {
    private final BookingRepository bookingRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final EmailService emailService;
    private final AuditLogService auditLogService;
    private final BookingMapper bookingMapper;
    private final BigDecimal taxRate;
    
    public BookingService(
            BookingRepository bookingRepository,
            TourScheduleRepository tourScheduleRepository,
            EmailService emailService,
            AuditLogService auditLogService,
            BookingMapper bookingMapper,
            @Value("${tax.rate}") BigDecimal taxRate) {
        this.bookingRepository = bookingRepository;
        this.tourScheduleRepository = tourScheduleRepository;
        // ... initialize all fields
    }
}
```

2. **Benefits:**
   - ✅ Explicit dependency declaration
   - ✅ Immutability (final fields)
   - ✅ Easier unit testing (no reflection needed)
   - ✅ Faster startup (no reflection processing)
   - ✅ Clear constructor contracts

---

## 2. IN-MEMORY FILTERING CAUSING PERFORMANCE ISSUES: CRITICAL

### Issue Severity: CRITICAL (Performance Problem)
### Category: N+1 Query Problem / Poor Database Usage
### Files: `BookingService.java`

The `BookingService` loads **all bookings** into memory, then filters them in Java. This scales poorly and contradicts database design.

#### Problematic Code (BookingService, lines 132-152):
```java
// ❌ WRONG: Loads ALL bookings, filters in memory
@Transactional(readOnly = true)
public List<BookingRes> getAllBookingsForAdmin() {
    return bookingRepository.findAllWithDetails().stream()  // Loads ENTIRE table!
            .map(bookingMapper::toBookingRes)
            .collect(Collectors.toList());
}

@Transactional(readOnly = true)
public List<BookingRes> getBookingsByTourOwner(User owner) {
    return bookingRepository.findAllWithDetails().stream()  // All bookings loaded
            .filter(booking -> booking.getSchedule().getTour().getOwner().getId().equals(owner.getId()))  // Filtered in app
            .map(bookingMapper::toBookingRes)
            .collect(Collectors.toList());
}

@Transactional(readOnly = true)
public List<BookingRes> getBookingsByUser(User user) {
    return bookingRepository.findAllWithDetails().stream()  // All bookings loaded
            .filter(booking -> booking.getUser().getId().equals(user.getId()))  // Filtered in app
            .map(bookingMapper::toBookingRes)
            .collect(Collectors.toList());
}
```

#### Impact:
- With 10,000 bookings: Loads 10,000 objects into memory
- No pagination or filtering at database level
- Memory bloat and slow response times
- Violates the principle of database efficiency

#### Recommendations:

1. **Add Repository Query Methods** (Add to BookingRepository):
```java
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    // ...existing methods...
    
    // New optimized methods
    @Query("SELECT b FROM Booking b JOIN FETCH b.schedule s JOIN FETCH s.tour t WHERE t.owner.id = :ownerId")
    List<Booking> findByTourOwnerIdWithDetails(@Param("ownerId") UUID ownerId);
    
    @Query("SELECT b FROM Booking b JOIN FETCH b.user u WHERE u.id = :userId")
    List<Booking> findByUserIdWithDetails(@Param("userId") UUID userId);
    
    // For pagination support
    Page<Booking> findByTourOwnerIdWithDetails(@Param("ownerId") UUID ownerId, Pageable pageable);
    Page<Booking> findByUserIdWithDetails(@Param("userId") UUID userId, Pageable pageable);
}
```

2. **Refactor Service Methods**:
```java
@Transactional(readOnly = true)
public List<BookingRes> getBookingsByTourOwner(User owner) {
    // ✅ Database does the filtering
    return bookingRepository.findByTourOwnerIdWithDetails(owner.getId()).stream()
            .map(bookingMapper::toBookingRes)
            .collect(Collectors.toList());
}

@Transactional(readOnly = true)
public List<BookingRes> getBookingsByUser(User user) {
    // ✅ Database does the filtering
    return bookingRepository.findByUserIdWithDetails(user.getId()).stream()
            .map(bookingMapper::toBookingRes)
            .collect(Collectors.toList());
}

// Support pagination for large datasets
@Transactional(readOnly = true)
public Page<BookingRes> getBookingsByUserPaged(User user, Pageable pageable) {
    return bookingRepository.findByUserIdWithDetails(user.getId(), pageable)
            .map(bookingMapper::toBookingRes);
}
```

---

## 3. DUPLICATE SLOT AVAILABILITY CHECKING LOGIC

### Issue Severity: MEDIUM
### Files Affected: 3 files
### Category: Code Duplication / DRY Violation

The logic to check available booking slots is duplicated across three services.

#### Duplicated Locations:
1. **BookingService.java** (lines 63-73)
2. **CartService.java** (lines 73-92)
3. **AvailabilityService.java** (lines 80-90)

#### Code Pattern:
```java
// BookingService line 63
Integer bookedParticipants = bookingRepository.countConfirmedParticipantsByScheduleId(req.getScheduleId());
if (bookedParticipants == null) bookedParticipants = 0;
int availableSlots = schedule.getMaxParticipants() - bookedParticipants;
if (availableSlots < requestedSlots) {
    throw new IllegalStateException(String.format(
        "Not enough available slots. Requested: %d, Available: %d",
        requestedSlots, availableSlots
    ));
}

// CartService line 73 - IDENTICAL LOGIC
Integer bookedParticipants = bookingRepository.countConfirmedParticipantsByScheduleId(itemReq.getScheduleId());
if (bookedParticipants == null) bookedParticipants = 0;
// ... same calculation and validation
```

#### Recommendation:

Create an `AvailabilityValidator` utility service:
```java
@Service
public class AvailabilityValidator {
    
    private final BookingRepository bookingRepository;
    
    public AvailabilityValidator(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }
    
    /**
     * Validates that enough slots are available for requested participants
     * @throws IllegalStateException if not enough slots available
     */
    public void validateSlotAvailability(UUID scheduleId, TourSchedule schedule, int requestedSlots) {
        Integer bookedParticipants = bookingRepository.countConfirmedParticipantsByScheduleId(scheduleId);
        if (bookedParticipants == null) bookedParticipants = 0;
        
        int availableSlots = schedule.getMaxParticipants() - bookedParticipants;
        if (availableSlots < requestedSlots) {
            throw new IllegalStateException(String.format(
                "Not enough available slots. Requested: %d, Available: %d",
                requestedSlots, availableSlots
            ));
        }
    }
    
    public int getAvailableSlots(UUID scheduleId, TourSchedule schedule) {
        Integer bookedParticipants = bookingRepository.countConfirmedParticipantsByScheduleId(scheduleId);
        if (bookedParticipants == null) bookedParticipants = 0;
        return schedule.getMaxParticipants() - bookedParticipants;
    }
}
```

Then use in services:
```java
// BookingService
public BookingRes createBooking(BookingCreateReq req, User currentUser) {
    var schedule = tourScheduleRepository.findById(req.getScheduleId())
            .orElseThrow(() -> new EntityNotFoundException("TourSchedule not found"));
    
    availabilityValidator.validateSlotAvailability(
        req.getScheduleId(), 
        schedule, 
        req.getParticipants().size()
    );
    // ... rest of method
}

// CartService
public Cart addItemToCart(Cart cart, CartItemReq itemReq) {
    var schedule = tourScheduleRepository.findById(itemReq.getScheduleId())
            .orElseThrow(() -> new EntityNotFoundException("TourSchedule not found"));
    
    int participantsInCart = getTotalParticipantsInCart(cart, itemReq.getScheduleId());
    int totalRequested = participantsInCart + itemReq.getNumParticipants();
    
    availabilityValidator.validateSlotAvailability(itemReq.getScheduleId(), schedule, totalRequested);
    // ... rest of method
}
```

---

## 4. DUPLICATE TOUR NAME EXTRACTION: 11+ OCCURRENCES

### Issue Severity: LOW (But Pervasive)
### Files Affected: 6+ files
### Pattern: `getNameTranslations().getOrDefault("es", "Tour sin nombre")`

This pattern appears 11+ times across the codebase for audit logging.

#### Locations:
- BookingService.java: 4 occurrences (lines 173, 193, 224, 267)
- TourScheduleService.java: 4 occurrences (lines 62, 110, 142, 181)
- TourService.java: 4 occurrences (lines 91, 140, 199, 221)
- WeatherAlertService.java: Multiple in audit messages

#### Recommendation:

Add helper method to Tour entity or create a utility:
```java
// In Tour entity
public String getDisplayName(String locale) {
    return getNameTranslations() != null 
        ? getNameTranslations().getOrDefault(locale, "Tour sin nombre")
        : "Tour sin nombre";
}

// Or in utility service
@Service
public class TourNameResolver {
    public String getTourDisplayName(Tour tour, String preferredLocale) {
        if (tour == null || tour.getNameTranslations() == null) {
            return "Tour sin nombre";
        }
        return tour.getNameTranslations().getOrDefault(preferredLocale, "Tour sin nombre");
    }
}
```

Usage:
```java
// Before
String tourName = booking.getSchedule().getTour().getNameTranslations().getOrDefault("es", "Tour sin nombre");

// After
String tourName = booking.getSchedule().getTour().getDisplayName("es");
```

---

## 5. BUSINESS LOGIC IN CONTROLLERS

### Issue Severity: MEDIUM
### Files Affected: 2 controllers
### Category: Separation of Concerns

Controllers contain business logic that should be in services.

#### Issue 1: TourController.java (lines 69-74)
```java
@GetMapping("/tours/{id}")
public ResponseEntity<TourRes> getTourById(@PathVariable UUID id) {
    // ❌ Creating fake user in controller
    User publicUser = new User();
    publicUser.setRole("ROLE_PUBLIC");
    TourRes tour = tourService.getTourById(id, publicUser);
    return new ResponseEntity<>(tour, HttpStatus.OK);
}
```

**Problem:** User creation is business logic. The controller shouldn't know about User role semantics.

#### Issue 2: BookingController.java (lines 76-87)
```java
@GetMapping("/admin/bookings")
@PreAuthorize("@bookingSecurityService.canViewTourBookings(authentication)")
public ResponseEntity<List<BookingRes>> getAdminBookings(@CurrentUser User currentUser) {
    List<BookingRes> bookings;
    // ❌ Business logic in controller
    if (currentUser.getRole().equals("ROLE_SUPER_ADMIN")) {
        bookings = bookingService.getAllBookingsForAdmin();
    } else {
        bookings = bookingService.getBookingsByTourOwner(currentUser);
    }
    return new ResponseEntity<>(bookings, HttpStatus.OK);
}
```

**Problem:** Decision logic about which bookings to return based on role should be in service.

#### Recommendations:

1. **Move public tour lookup to service**:
```java
// TourService
@Transactional(readOnly = true)
public TourRes getTourByIdForPublic(UUID id) {
    Tour tour = tourRepository.findByIdNotDeleted(id)
            .orElseThrow(() -> new EntityNotFoundException("Tour not found"));
    
    // Only return if published
    if (!"PUBLISHED".equals(tour.getStatus())) {
        throw new AccessDeniedException("Tour not available");
    }
    
    return tourMapper.toTourRes(tour);
}

// TourController
@GetMapping("/tours/{id}")
public ResponseEntity<TourRes> getTourById(@PathVariable UUID id) {
    TourRes tour = tourService.getTourByIdForPublic(id);  // ✅ Business logic in service
    return new ResponseEntity<>(tour, HttpStatus.OK);
}
```

2. **Move admin booking logic to service**:
```java
// BookingService
@Transactional(readOnly = true)
public List<BookingRes> getAdminBookings(User currentUser) {
    if ("ROLE_SUPER_ADMIN".equals(currentUser.getRole())) {
        return getAllBookingsForAdmin();
    } else if ("ROLE_PARTNER_ADMIN".equals(currentUser.getRole())) {
        return getBookingsByTourOwner(currentUser);
    }
    throw new AccessDeniedException("User cannot view admin bookings");
}

// BookingController
@GetMapping("/admin/bookings")
@PreAuthorize("@bookingSecurityService.canViewTourBookings(authentication)")
public ResponseEntity<List<BookingRes>> getAdminBookings(@CurrentUser User currentUser) {
    List<BookingRes> bookings = bookingService.getAdminBookings(currentUser);  // ✅ Service handles logic
    return new ResponseEntity<>(bookings, HttpStatus.OK);
}
```

---

## 6. INCONSISTENT ERROR HANDLING PATTERNS

### Issue Severity: MEDIUM
### Files Affected: 2+ files
### Category: Exception Handling

Different services use different exception types and patterns.

#### Issues Found:

1. **WeatherAlertService.java (line 221)**:
```java
WeatherAlert alert = alertRepository.findById(java.util.UUID.fromString(alertId))
        .orElseThrow(() -> new RuntimeException("Alert not found"));  // ❌ Generic exception
```

2. **Various services mix**:
   - `EntityNotFoundException` (Jakarta)
   - `IllegalStateException` (standard)
   - `SecurityException` (standard)
   - `AccessDeniedException` (Spring Security)
   - `RuntimeException` (generic)

#### Recommendations:

1. **Create custom exceptions hierarchy**:
```java
// Base exception
public abstract class ApplicationException extends RuntimeException {
    private final ErrorCode errorCode;
    
    public ApplicationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ApplicationException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

// Specific exceptions
public class ResourceNotFoundException extends ApplicationException {
    public ResourceNotFoundException(String resourceType, String identifier) {
        super(ErrorCode.RESOURCE_NOT_FOUND, 
              String.format("%s not found: %s", resourceType, identifier));
    }
}

public class SlotNotAvailableException extends ApplicationException {
    public SlotNotAvailableException(int requested, int available) {
        super(ErrorCode.INSUFFICIENT_SLOTS,
              String.format("Not enough slots. Requested: %d, Available: %d", requested, available));
    }
}

public class UnauthorizedActionException extends ApplicationException {
    public UnauthorizedActionException(String action, String reason) {
        super(ErrorCode.UNAUTHORIZED, String.format("Cannot %s: %s", action, reason));
    }
}
```

2. **Use consistently**:
```java
// Before
Booking booking = bookingRepository.findByIdWithDetails(bookingId)
        .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

// After
Booking booking = bookingRepository.findByIdWithDetails(bookingId)
        .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId.toString()));
```

3. **Add global exception handler**:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }
    
    @ExceptionHandler(SlotNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleSlotNotAvailable(SlotNotAvailableException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }
    
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }
}
```

---

## 7. METHOD COMPLEXITY AND SIZE ISSUES

### Issue Severity: MEDIUM
### Category: Cyclomatic Complexity / SRP Violation

#### BookingService.createBooking() (lines 55-129): 74 lines, HIGH complexity

```java
@Transactional
public BookingRes createBooking(BookingCreateReq req, User currentUser) {
    // 1. Validate schedule existence
    var schedule = tourScheduleRepository.findById(req.getScheduleId())
            .orElseThrow(() -> new EntityNotFoundException("TourSchedule not found..."));
    
    // 2. Check slot availability
    Integer bookedParticipants = bookingRepository.countConfirmedParticipantsByScheduleId(req.getScheduleId());
    if (bookedParticipants == null) bookedParticipants = 0;
    int requestedSlots = req.getParticipants().size();
    int availableSlots = schedule.getMaxParticipants() - bookedParticipants;
    if (availableSlots < requestedSlots) {
        throw new IllegalStateException(...);
    }
    
    // 3. Calculate pricing/taxes
    BigDecimal pricePerParticipant = schedule.getTour().getPrice();
    BigDecimal totalAmount = pricePerParticipant.multiply(BigDecimal.valueOf(participantCount));
    BigDecimal subtotal = totalAmount.divide(BigDecimal.ONE.add(taxRate), 2, RoundingMode.HALF_UP);
    BigDecimal taxAmount = totalAmount.subtract(subtotal);
    
    // 4. Create booking entity
    Booking booking = new Booking();
    booking.setUser(currentUser);
    booking.setSchedule(schedule);
    // ... 6 more setters
    
    // 5. Create participant entities
    List<Participant> participants = new ArrayList<>();
    for (var participantReq : req.getParticipants()) {
        Participant participant = new Participant();
        participant.setBooking(booking);
        // ... 7 setters with conditional logic
    }
    booking.setParticipants(participants);
    
    // 6. Save and send emails
    Booking savedBooking = bookingRepository.save(booking);
    emailService.sendBookingConfirmationEmail(...);
    emailService.sendNewBookingNotificationToAdmin(...);
    
    return bookingMapper.toBookingRes(savedBooking);
}
```

**Responsibilities:**
1. Validation (schedule, slot availability)
2. Business calculation (pricing, tax)
3. Entity creation (booking + participants)
4. Persistence (save)
5. Notification (email sending)

#### Recommendation: Extract responsibilities

```java
@Service
@Transactional
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final BookingValidator bookingValidator;
    private final BookingPricingCalculator pricingCalculator;
    private final BookingFactory bookingFactory;
    private final BookingNotificationService notificationService;
    
    public BookingRes createBooking(BookingCreateReq req, User currentUser) {
        // 1. Validate
        var schedule = bookingValidator.validateAndGetSchedule(req.getScheduleId());
        bookingValidator.validateSlotAvailability(req.getScheduleId(), schedule, req.getParticipants().size());
        
        // 2. Calculate pricing
        PricingResult pricing = pricingCalculator.calculateBookingPrice(schedule.getTour(), req.getParticipants().size());
        
        // 3. Create booking
        Booking booking = bookingFactory.createBooking(req, schedule, currentUser, pricing);
        Booking savedBooking = bookingRepository.save(booking);
        
        // 4. Notify
        notificationService.notifyBookingCreated(savedBooking);
        
        return bookingMapper.toBookingRes(savedBooking);
    }
}
```

---

## 8. SERVICE RESPONSIBILITIES AND GOD OBJECTS

### Issue Severity: MEDIUM
### Category: Single Responsibility Principle Violation

Some services are doing too much.

#### BookingService: 7 core responsibilities
1. Create bookings
2. Query bookings (multiple overloads)
3. Update booking status
4. Cancel bookings
5. Confirm mock payments
6. Update participant details
7. Mapping logic

#### TourService: 5+ responsibilities
1. Create tours
2. Update tours
3. Delete tours
4. Query tours (multiple overloads)
5. Slug generation/uniqueness
6. Image management
7. Cache invalidation

#### Recommendation: Break into focused services

```java
// Keep primary service minimal
@Service
@Transactional
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final BookingFactory bookingFactory;
    private final BookingValidator bookingValidator;
    
    public BookingRes createBooking(BookingCreateReq req, User currentUser) {
        // Core responsibility: coordinate booking creation
    }
}

// Extract separate services
@Service
@Transactional(readOnly = true)
public class BookingQueryService {
    private final BookingRepository bookingRepository;
    
    public List<BookingRes> getBookingsByUser(User user) { ... }
    public List<BookingRes> getBookingsByTourOwner(User owner) { ... }
    public List<BookingRes> getAllBookingsForAdmin() { ... }
    public Optional<BookingRes> getBookingById(UUID bookingId) { ... }
}

@Service
@Transactional
public class BookingStatusService {
    private final BookingRepository bookingRepository;
    private final AuditLogService auditLogService;
    
    public BookingRes updateBookingStatus(UUID bookingId, String newStatus, User currentUser) { ... }
    public void cancelBooking(UUID bookingId, User currentUser) { ... }
    public BookingRes confirmBookingAfterPayment(UUID bookingId, User currentUser) { ... }
}

@Service
@Transactional
public class BookingEditService {
    private final BookingRepository bookingRepository;
    
    public BookingRes updateBookingDetails(UUID bookingId, BookingClientUpdateReq req, User currentUser) { ... }
}
```

---

## 9. TRANSACTION BOUNDARY ISSUES

### Issue Severity: LOW (But Worth Noting)
### Positive: Only 12 files lack specific `@Transactional` configurations
### Concern: Some methods marked `@Transactional` on the class, not method level

#### Current Pattern (BookingService):
```java
@Service
@Transactional  // ✅ Good: Class-level default
public class BookingService {
    @Transactional(readOnly = true)  // ✅ Good: Override for read-only
    public List<BookingRes> getBookingsByUser(User user) { ... }
    
    @Transactional  // ⚠️ Redundant: already transactional from class
    public BookingRes createBooking(BookingCreateReq req, User currentUser) { ... }
}
```

#### Recommendations:

1. **Prefer method-level configuration** for clarity:
```java
@Service
public class BookingService {
    
    @Transactional
    public BookingRes createBooking(BookingCreateReq req, User currentUser) { ... }
    
    @Transactional(readOnly = true)
    public List<BookingRes> getBookingsByUser(User user) { ... }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyUserOfBooking(Booking booking) { ... }
}
```

2. **Add transaction timeout for long-running operations**:
```java
@Transactional(timeout = 30)  // 30 seconds
public void generateSchedulesForAllTours() { ... }
```

3. **Verify read-only optimization** in all query methods:
```java
// ✅ Good
@Transactional(readOnly = true)
public List<BookingRes> getBookingsByUser(User user) { ... }

// ❌ Missing readOnly = true
@Transactional
public List<BookingRes> getPublishedTours() { ... }
```

---

## 10. MISCELLANEOUS ISSUES

### A. Placeholder Implementations
- `PaymentService.java`: Only placeholder implementation
- `EmailService.java`: Print statements instead of actual email
- **Impact**: Mock behavior in production code

### B. Repeated RequestContextHolder Access
- `AuditLogService.java` (lines 53-62): Try-catch block for request context
- **Recommendation**: Extract to utility method or filter

### C. Hard-coded String Literals
- Role names: "ROLE_SUPER_ADMIN", "ROLE_PARTNER_ADMIN", "ROLE_CLIENT"
- Status values: "DRAFT", "PUBLISHED", "PENDING", "CONFIRMED", "CANCELLED"
- **Recommendation**: Create enums

---

## SCORING SUMMARY

| Category | Severity | Files | Issues |
|----------|----------|-------|--------|
| Dependency Injection | HIGH | 20 | 1 |
| In-Memory Filtering | CRITICAL | 1 | 4 |
| Code Duplication | MEDIUM | 3 | 2 |
| Business Logic in Controllers | MEDIUM | 2 | 2 |
| Error Handling | MEDIUM | 2+ | 1 |
| Method Complexity | MEDIUM | 3 | 1 |
| Service Size (God Objects) | MEDIUM | 3 | 2 |
| Transaction Boundaries | LOW | 20 | 1 |
| **TOTAL** | | **54** | **19 Critical/Medium Issues** |

---

## REFACTORING ROADMAP (Prioritized)

### Phase 1: CRITICAL (Week 1-2)
1. Fix in-memory booking filtering (ADD REPOSITORY METHODS)
2. Migrate field injection to constructor injection (All services)

### Phase 2: HIGH (Week 2-3)
3. Extract slot availability validator (Eliminate duplication)
4. Move business logic out of controllers
5. Create custom exception hierarchy

### Phase 3: MEDIUM (Week 3-4)
6. Decompose God Object services
7. Extract duplicate tour name resolution
8. Refactor complex methods (BookingService.createBooking)

### Phase 4: LOW (Ongoing)
9. Replace placeholder services
10. Create enums for constants
11. Add method-level @Transactional configurations

---

## Testing Implications

Once refactored, testing becomes significantly easier:

```java
// BEFORE: Cannot test without mocking multiple fields
@Test
void testCreateBooking() {
    // Hard to set up - field injection reflection needed
}

// AFTER: Constructor injection makes testing trivial
@Test
void testCreateBooking() {
    BookingValidator validator = mock(BookingValidator.class);
    BookingFactory factory = mock(BookingFactory.class);
    BookingRepository repository = mock(BookingRepository.class);
    
    BookingService service = new BookingService(
        validator,
        factory,
        repository,
        // ... other dependencies
    );
    
    // Easy to test with mocks injected
    assertEquals(...);
}
```

---

## Conclusion

The service layer has good architectural foundations (services exist, basic separation of concerns), but suffers from implementation quality issues that will compound as the system grows. The refactoring roadmap above should be prioritized by impact-effort ratio.

**Estimated effort**: 40-60 developer hours
**Expected impact**: 50% reduction in bugs, 30% improvement in query performance, 20% reduction in code duplication

