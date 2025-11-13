# DTO and Mapper Analysis Report

## Executive Summary

**Total DTOs Found: 37 (27 in main packages + 10 external weather DTOs)**
**Total Mappers Found: 3 (all using MapStruct)**
**Overall Health: MODERATE - Multiple consistency and validation issues**

---

## 1. AUTH PACKAGE
**Location:** `/backend/src/main/java/com/northernchile/api/auth/dto/`
**DTOs: 4**

### Breakdown:

| DTO | Type | Validation | Status | Issues |
|-----|------|-----------|--------|--------|
| `AuthReq.java` | Request | None | UNUSED | ❌ Never imported; duplicate of LoginReq; no validation |
| `LoginReq.java` | Request | @NotBlank, @Email | ✅ Used | Good validation coverage |
| `RegisterReq.java` | Request | @NotBlank, @Email, @Size | ✅ Used | Strong validation |
| `AuthRes.java` | Response | None | ✅ Used | Correct (responses don't validate) |

### Issues:
- **UNUSED DTO**: `AuthReq` is completely unused - all auth uses either `LoginReq` or `RegisterReq`
- **Recommendation**: Delete `AuthReq.java`

---

## 2. BOOKING PACKAGE
**Location:** `/backend/src/main/java/com/northernchile/api/booking/`
**DTOs: 6**

### Breakdown:

| DTO | Type | Validation | Issues |
|-----|------|-----------|--------|
| `BookingCreateReq.java` | Request | @NotNull, @NotEmpty, @Valid | ✅ Good: Validates nested ParticipantReq list |
| `BookingUpdateReq.java` | Request | @NotBlank | ⚠️ Minimal: Only validates status, not specialRequests |
| `BookingClientUpdateReq.java` | Request | @NotNull, @Valid | ✅ Good: Uses nested ParticipantUpdateReq |
| `ParticipantReq.java` | Request | @NotBlank | ⚠️ Only fullName & documentId; missing phone/email validation |
| `BookingRes.java` | Response | None | ✅ Correct |
| `ParticipantRes.java` | Response | None | ✅ Correct |

### Mapper:
- **BookingMapper.java** (MapStruct, componentModel="spring")
  - Uses `@Mapper` with explicit `@Mapping` annotations
  - Custom helper methods for date/time conversions
  - Properly maps nested Participant lists

### Issues:
1. **Inconsistent validation between ParticipantReq and ParticipantUpdateReq**
   - ParticipantReq requires fullName & documentId
   - ParticipantUpdateReq doesn't validate these (only id required)
2. **BookingUpdateReq** missing validation for specialRequests field
3. **Nested ParticipantUpdateReq** is an inner class - hard to reuse

---

## 3. TOUR PACKAGE
**Location:** `/backend/src/main/java/com/northernchile/api/tour/dto/`
**DTOs: 8**

### Breakdown:

| DTO | Type | Validation | Issues |
|-----|------|-----------|--------|
| `TourCreateReq.java` | Request | Strong (@NotEmpty, @NotNull, @Valid) | ✅ Excellent validation |
| `TourUpdateReq.java` | Request | Some (@Valid on translations) | ⚠️ Less strict than Create (all fields optional) |
| `TourScheduleCreateReq.java` | Request | @NotNull, @Min(1) | ✅ Good |
| `TourRes.java` | Response | None | ✅ Correct |
| `TourScheduleRes.java` | Response | None | ✅ Correct |
| `TourImageRes.java` | Response | None | ✅ Correct |
| `ContentBlock.java` | Data | ❌ NONE | 🔴 Critical issues |
| `ItineraryItem.java` | Data | @NotBlank | ⚠️ Only time & description |

### ContentBlock.java Issues:
```java
❌ Missing validation annotations
❌ Missing equals() implementation
❌ Missing hashCode() implementation  
❌ Missing toString() implementation
❌ Used in TourCreateReq/TourUpdateReq with @Valid
```
**Recommendation**: Add @NotBlank validations, implement equals/hashCode/toString

### TourMapper:
- Uses `componentModel="spring"` and `unmappedTargetPolicy = IGNORE`
- Complex mapping with custom helpers for itinerary & equipment translations
- Handles localization in mapper (Locale parameter)

### Issues:
1. **DTO field naming inconsistency**:
   - TourCreateReq uses `@JsonProperty("isMoonSensitive")` with Boolean wrapper
   - TourRes uses `boolean` primitive type (not nullable)
2. **TourUpdateReq less strict than TourCreateReq** - should validate required fields consistently
3. **ContentBlock needs major improvements**

---

## 4. USER PACKAGE
**Location:** `/backend/src/main/java/com/northernchile/api/user/dto/`
**DTOs: 6**

### Breakdown:

| DTO | Type | Validation | Issues |
|-----|------|-----------|--------|
| `UserCreateReq.java` | Request | @NotBlank, @Email, @Size | ✅ Good |
| `UserRes.java` | Response | None | ✅ Correct |
| `UserUpdateReq.java` | Request | ❌ NONE | 🔴 All fields optional |
| `ProfileUpdateReq.java` | Request | ❌ NONE | 🔴 All fields optional |
| `PasswordChangeReq.java` | Request | @NotBlank, @Size | ✅ Good |
| `AdminPasswordChangeReq.java` | Request | @NotBlank, @Size | ✅ Good |

### UserMapper:
- Minimal: Only `toUserRes()` and list version
- All field mappings automatic (MapStruct convention)
- componentModel="spring"

### Issues:
1. **UserUpdateReq has NO validation** 
   - `fullName`, `nationality`, `phoneNumber`, `dateOfBirth`, `role` all optional
   - No @Size, @Email, or pattern validation
2. **ProfileUpdateReq (subset of UserUpdateReq) also has NO validation**
   - Should at least validate fullName if provided
3. **Duplicate DTOs**: ProfileUpdateReq excludes role - could use @Valid field validation instead

---

## 5. CART PACKAGE
**Location:** `/backend/src/main/java/com/northernchile/api/cart/dto/`
**DTOs: 3**

### Breakdown:

| DTO | Type | Validation | Issues |
|-----|------|-----------|--------|
| `CartItemReq.java` | Request | ❌ NONE | 🔴 Missing validation |
| `CartItemRes.java` | Response | None | ✅ Correct |
| `CartRes.java` | Response | None | ✅ Correct |

### CartItemReq Issues:
```java
❌ Missing @NotNull on scheduleId
❌ Missing @Min(1) on numParticipants
❌ No validation despite being core request
```

### Missing Mapper:
- **NO CartMapper exists** - Manual mapping is done in `CartService.toCartRes()`
- Manual mapping code (lines 140-168 in CartService):
  ```java
  // Manual mapping instead of using @Mapper
  CartRes res = new CartRes();
  res.setCartId(cart.getId());
  // ... more manual field setting
  ```

**Recommendation**: Create CartMapper interface to consolidate conversion logic

---

## 6. NAMING CONSISTENCY

### Issues Found:

#### Boolean Field Naming (TourRes):
```java
// INCONSISTENT - Using is prefix with boolean primitive
@JsonProperty("isMoonSensitive")
private boolean isMoonSensitive;  // Getter: isMoonSensitive()

// VS TourCreateReq uses Boolean wrapper
@JsonProperty("isMoonSensitive")
private Boolean isMoonSensitive;  // Getter: isMoonSensitive()
```

#### DTO Suffix Convention:
- ✅ Consistent: All request DTOs end with `Req`
- ✅ Consistent: All response DTOs end with `Res`
- ⚠️ Data DTOs: `ContentBlock`, `ItineraryItem` (no suffix) - could add `Dto` suffix

---

## 7. VALIDATION COVERAGE SUMMARY

### Request DTOs - Validation Status:

**Strong Validation (4/15):**
- LoginReq
- RegisterReq
- TourCreateReq
- UserCreateReq

**Moderate Validation (5/15):**
- BookingCreateReq
- BookingClientUpdateReq
- TourUpdateReq
- PasswordChangeReq
- AdminPasswordChangeReq

**Weak/No Validation (6/15):**
- AuthReq (UNUSED)
- BookingUpdateReq
- ParticipantReq
- CartItemReq (NO VALIDATION)
- UserUpdateReq (NO VALIDATION)
- ProfileUpdateReq (NO VALIDATION)

### Coverage Percentage:
- **Strong**: 27%
- **Moderate**: 33%
- **Weak/None**: 40%

---

## 8. MAPPER ANALYSIS

### All 3 Mappers Use MapStruct with componentModel="spring"

| Mapper | Interface | Approach | unmappedTargetPolicy | Issues |
|--------|-----------|----------|----------------------|--------|
| `BookingMapper` | ✅ Interface | @Mapping + custom helpers | DEFAULT | ✅ Good patterns |
| `TourMapper` | ✅ Interface | @Mappings + complex logic | IGNORE | ⚠️ Silently ignores unmapped fields |
| `UserMapper` | ✅ Interface | Convention-based | DEFAULT | ✅ Simple, correct |

### Missing Mappers:
- ❌ **CartMapper** - Manual code in service instead
- ⚠️ No mapper for Auth DTOs (not needed - only service operations)

### TourMapper ReportingPolicy.IGNORE Issue:
```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
```
**Impact**: Silently ignores fields that don't map, making it harder to catch mapping bugs

---

## 9. KEY FINDINGS & RECOMMENDATIONS

### Critical Issues (Fix Immediately):

1. **UNUSED DTO**
   - ❌ `AuthReq.java` - Never imported, never used
   - **Action**: Delete file
   - **Impact**: Reduces codebase clutter

2. **Missing Validation on CartItemReq**
   - ❌ `scheduleId` not validated
   - ❌ `numParticipants` not validated (no @Min(1))
   - **Action**: Add `@NotNull` and `@Min(1)` annotations
   - **Impact**: Prevents invalid cart items

3. **ContentBlock Missing Standard Methods**
   - ❌ No equals/hashCode/toString
   - ❌ No validation annotations
   - **Action**: Add missing methods and `@NotBlank` validations
   - **Impact**: Ensures proper object equality checks

4. **Missing CartMapper**
   - ❌ Manual mapping in CartService
   - **Action**: Create CartMapper interface
   - **Impact**: Centralized conversion logic, easier maintenance

### High Priority Issues:

5. **UserUpdateReq/ProfileUpdateReq No Validation**
   - ❌ All fields optional with no constraints
   - **Action**: Add @Size, optional @NotBlank, etc.
   - **Impact**: Prevents invalid user data

6. **TourMapper unmappedTargetPolicy=IGNORE**
   - ⚠️ Silently ignores field mapping errors
   - **Action**: Change to `ReportingPolicy.WARN` or `ERROR`
   - **Impact**: Catches mapping bugs earlier

7. **Inconsistent Boolean Nullable Types**
   - ⚠️ TourRes uses `boolean` primitives; TourCreateReq uses `Boolean` wrappers
   - **Action**: Standardize to `Boolean` (nullable) for consistency
   - **Impact**: Better null handling

### Medium Priority Issues:

8. **BookingUpdateReq Minimal Validation**
   - ⚠️ Only `status` validated; `specialRequests` not validated
   - **Action**: Add constraints to `specialRequests` if needed

9. **ParticipantReq Inconsistent with ParticipantUpdateReq**
   - ⚠️ Different validation levels for same entity
   - **Action**: Standardize validation rules

10. **ContentBlock/ItineraryItem Naming**
    - ⚠️ No DTO suffix, used as nested objects
    - **Action**: Consider renaming to `ContentBlockDto`, `ItineraryItemDto` for clarity

---

## 10. BEST PRACTICES CHECKLIST

| Practice | Status | Details |
|----------|--------|---------|
| Req/Res naming convention | ✅ Good | All followed |
| Validation on Request DTOs | ⚠️ Inconsistent | 60% coverage |
| Validation on Response DTOs | ✅ Correct | None (not needed) |
| Mapper usage | ⚠️ Incomplete | Missing CartMapper |
| MapStruct configuration | ⚠️ Inconsistent | TourMapper uses IGNORE policy |
| Field validation @Valid | ✅ Good | Nested objects validated |
| Equals/HashCode/ToString | ⚠️ Incomplete | Missing in ContentBlock |
| Null safety (Boolean vs boolean) | ⚠️ Inconsistent | Mixed approaches |

---

## 11. RECOMMENDED REFACTORING CHECKLIST

### Phase 1 (Immediate - Week 1):
- [ ] Delete `AuthReq.java`
- [ ] Add validation to `CartItemReq`
- [ ] Fix `ContentBlock` (add equals, hashCode, toString, validations)
- [ ] Add validation to `UserUpdateReq` and `ProfileUpdateReq`

### Phase 2 (High Priority - Week 2):
- [ ] Create `CartMapper` interface
- [ ] Change `TourMapper.unmappedTargetPolicy` to WARN
- [ ] Standardize boolean field types (Boolean vs boolean)
- [ ] Add validation constraints to `BookingUpdateReq`

### Phase 3 (Nice to Have - Week 3):
- [ ] Rename `ContentBlock` -> `ContentBlockDto`
- [ ] Rename `ItineraryItem` -> `ItineraryItemDto`
- [ ] Extract `ParticipantUpdateReq` from inner class
- [ ] Add optional field validation messages in Spanish/English

