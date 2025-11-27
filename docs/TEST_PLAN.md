# Test Plan - NorthernChile Tours Platform

Pre-production comprehensive test plan covering unit tests, integration tests, E2E tests, and manual verification procedures.

---

## Table of Contents

1. [Test Coverage Summary](#test-coverage-summary)
2. [Backend Unit Tests](#backend-unit-tests)
3. [Backend Integration Tests](#backend-integration-tests)
4. [Frontend E2E Tests](#frontend-e2e-tests)
5. [Manual Testing Procedures](#manual-testing-procedures)
6. [Edge Cases & Security Tests](#edge-cases--security-tests)
7. [Test Execution Checklist](#test-execution-checklist)

---

## Test Coverage Summary

### Current Test Status

| Area | Existing Tests | Tests Needed | Priority |
|------|----------------|--------------|----------|
| Availability Validator | 15 tests | - | Done |
| Payment Service | 5 tests | +10 | High |
| Reports Service | 12 tests | +3 | Medium |
| TimeZone Handling | 8 tests | - | Done |
| Booking Service | 0 tests | +15 | **Critical** |
| Cart Service | 0 tests | +10 | High |
| Tour Service | 0 tests | +8 | High |
| Auth/JWT | 0 tests | +12 | **Critical** |
| Email Service | 0 tests | +8 | High |
| Webhook Security | 0 tests | +6 | **Critical** |
| Frontend E2E | 0 tests | +20 | High |

### Commands

```bash
# Backend tests
cd backend && mvn test

# Single test class
mvn -Dtest=ClassName test

# Single test method
mvn -Dtest=ClassName#methodName test

# Frontend tests (when implemented)
cd frontend && pnpm test
```

---

## Backend Unit Tests

### 1. AvailabilityValidator (15 tests)

**File:** `backend/src/test/java/com/northernchile/api/availability/AvailabilityValidatorTest.java`

| Test | Status | Description |
|------|--------|-------------|
| `shouldShowAllSlotsAvailableWhenEmpty` | Done | No bookings/carts shows full capacity |
| `shouldCalculateAvailabilityWithBookingsOnly` | Done | Confirmed bookings reduce availability |
| `shouldCalculateAvailabilityWithCartsOnly` | Done | Cart items reduce availability |
| `shouldCalculateAvailabilityWithBookingsAndCarts` | Done | Combined calculation |
| `shouldPreventOverbookingWhenExceedingCapacity` | Done | Rejects when requested > available |
| `shouldPreventOverbookingWhenSoldOut` | Done | Rejects when 0 slots |
| `shouldAllowBookingLastAvailableSlot` | Done | Exact match allowed |
| `shouldExcludeCartWhenUpdating` | Done | Cart update excludes own cart |
| `shouldExcludeUserCartsWhenValidating` | Done | User's carts excluded |
| `shouldHandleNullCountsFromRepositories` | Done | Null treated as 0 |
| `shouldGetAvailabilityStatusWithoutRequest` | Done | Status check without validation |
| `shouldHandleZeroRequestedSlots` | Done | 0 slots always valid |
| `shouldCalculateErrorMessageWithCorrectCounts` | Done | Error message format |
| `shouldReturnNullErrorMessageWhenAvailable` | Done | No error when available |

---

### 2. TimeZoneVerification (8 tests)

**File:** `backend/src/test/java/com/northernchile/api/availability/TimeZoneVerificationTest.java`

| Test | Status | Description |
|------|--------|-------------|
| `shouldHandleSummerTimeInJanuary` | Done | UTC-3 in summer |
| `shouldHandleStandardTimeInJuly` | Done | UTC-4 in winter |
| `shouldDetectDSTTransitionDates` | Done | Finds April/September transitions |
| `shouldHandleDSTTransitionEdgeCase` | Done | Edge case at midnight |
| `shouldMaintainConsistencyInConversions` | Done | Instant <-> LocalDate |
| `shouldHandleMidnightEdgeCase` | Done | Midnight round-trip |
| `shouldVerifyTzdataVersion` | Done | tzdata database check |
| `shouldCompareWeatherPredictionsWithCorrectTimezone` | Done | Weather API compatibility |

---

### 3. PaymentService (5 existing + 10 needed)

**File:** `backend/src/test/java/com/northernchile/api/payment/PaymentServiceTest.java`

#### Existing Tests

| Test | Status | Description |
|------|--------|-------------|
| `testCreatePayment_Success` | Done | Transbank payment creation |
| `testCreatePayment_InvalidRequest_NullBookingId` | Done | Null booking validation |
| `testCreatePayment_InvalidRequest_NullProvider` | Done | Null provider validation |
| `testCreatePayment_InvalidRequest_NegativeAmount` | Done | Negative amount validation |
| `testCreatePayment_MercadoPago_PIX` | Done | PIX payment creation |

#### Tests to Add

| Test | Priority | Description |
|------|----------|-------------|
| `testCreatePayment_ZeroAmount` | High | Reject zero amount |
| `testCreatePayment_UnsupportedCurrency` | High | Only CLP/BRL allowed |
| `testConfirmPayment_Transbank_Success` | **Critical** | Webhook confirmation flow |
| `testConfirmPayment_Transbank_Failed` | **Critical** | Declined payment handling |
| `testConfirmPayment_MercadoPago_Success` | **Critical** | MP webhook confirmation |
| `testConfirmPayment_MercadoPago_Failed` | **Critical** | MP failed payment |
| `testRefundPayment_FullRefund` | High | Full refund processing |
| `testRefundPayment_PartialRefund` | Medium | Partial refund (if supported) |
| `testIdempotencyKey_DuplicatePrevented` | High | Same idempotency key rejected |
| `testPayment_ExpirationHandling` | Medium | Expired payment status update |

---

### 4. ReportsService (12 existing + 3 needed)

**File:** `backend/src/test/java/com/northernchile/api/reports/ReportsServiceTest.java`

#### Existing Tests

| Test | Status | Description |
|------|--------|-------------|
| `shouldCalculateOverviewWithCorrectMetrics` | Done | Overview calculations |
| `shouldCalculateAverageBookingValue` | Done | Average value |
| `shouldHandleEmptyBookingsList` | Done | Empty data handling |
| `shouldGroupBookingsByDay` | Done | Daily grouping |
| `shouldAggregateRevenueByDay` | Done | Revenue aggregation |
| `shouldGetTopToursSortedByBookingsCount` | Done | Top tours sorting |
| `shouldLimitTopToursToSpecifiedLimit` | Done | Limit parameter |
| `shouldCalculateParticipantsCountInTopTours` | Done | Participant counting |
| `shouldParseStartDateCorrectly` | Done | Date parsing |
| `shouldParseEndDateCorrectlyWithDayOffset` | Done | End date offset |
| `shouldUseDefaultDatesWhenNullProvided` | Done | Default date range |

#### Tests to Add

| Test | Priority | Description |
|------|----------|-------------|
| `testFinancialReport_MercadoPagoFees` | High | MP real fees extraction |
| `testFinancialReport_TransbankEstimatedFees` | High | TB fee estimation |
| `testFinancialReport_MixedProviders` | Medium | Combined fee report |

---

### 5. BookingService (NEW - 15 tests needed)

**File to create:** `backend/src/test/java/com/northernchile/api/booking/BookingServiceTest.java`

| Test | Priority | Description |
|------|----------|-------------|
| `testCreateBooking_Success` | **Critical** | Happy path booking creation |
| `testCreateBooking_NoAvailability` | **Critical** | Reject when sold out |
| `testCreateBooking_InvalidScheduleId` | High | Invalid schedule UUID |
| `testCreateBooking_PastSchedule` | High | Cannot book past dates |
| `testCreateBooking_RequiresParticipants` | High | At least 1 participant |
| `testCreateBooking_ValidatesContactInfo` | High | Email/phone required |
| `testConfirmBooking_AfterPayment` | **Critical** | Status PENDING -> CONFIRMED |
| `testCancelBooking_ByCustomer` | High | Customer cancellation |
| `testCancelBooking_ByAdmin` | High | Admin cancellation |
| `testCancelBooking_Refund` | High | Triggers refund process |
| `testGetBookingsByUser` | Medium | User booking list |
| `testGetBookingByConfirmationCode` | Medium | Lookup by code |
| `testBooking_OwnerAuthorization` | **Critical** | Only owner can view |
| `testBooking_SendsConfirmationEmail` | High | Email triggered |
| `testBooking_CalculatesTotalAmount` | High | Price calculation |

---

### 6. CartService (NEW - 10 tests needed)

**File to create:** `backend/src/test/java/com/northernchile/api/cart/CartServiceTest.java`

| Test | Priority | Description |
|------|----------|-------------|
| `testAddToCart_Success` | **Critical** | Add item to cart |
| `testAddToCart_ChecksAvailability` | **Critical** | Validates slots before adding |
| `testAddToCart_ExistingItem_UpdatesQuantity` | High | Increases participants |
| `testUpdateCartItem_Quantity` | High | Change participant count |
| `testRemoveFromCart` | High | Remove item |
| `testClearCart` | Medium | Clear all items |
| `testGetCart_BySession` | Medium | Anonymous cart |
| `testGetCart_ByUser` | Medium | Authenticated cart |
| `testMergeCart_OnLogin` | High | Merge session -> user cart |
| `testCart_ExpirationCleanup` | Medium | Old cart items removed |

---

### 7. TourService (NEW - 8 tests needed)

**File to create:** `backend/src/test/java/com/northernchile/api/tour/TourServiceTest.java`

| Test | Priority | Description |
|------|----------|-------------|
| `testGetPublishedTours` | High | Only published returned |
| `testGetTourBySlug` | High | Slug lookup |
| `testGetTourSchedules_FutureDates` | High | Only future schedules |
| `testGetTourSchedules_WithAvailability` | High | Includes slot counts |
| `testCreateTour_Admin` | Medium | Admin tour creation |
| `testUpdateTour_Admin` | Medium | Admin tour update |
| `testToggleTourStatus` | Medium | Publish/unpublish |
| `testTour_OwnerFilter_PartnerAdmin` | **Critical** | PARTNER_ADMIN sees own tours only |

---

### 8. AuthService & JWT (NEW - 12 tests needed)

**File to create:** `backend/src/test/java/com/northernchile/api/auth/AuthServiceTest.java`

| Test | Priority | Description |
|------|----------|-------------|
| `testLogin_Success` | **Critical** | Valid credentials |
| `testLogin_InvalidPassword` | **Critical** | Wrong password rejected |
| `testLogin_UserNotFound` | High | Unknown email |
| `testLogin_UnverifiedEmail` | High | Email not verified |
| `testRegister_Success` | **Critical** | New user creation |
| `testRegister_DuplicateEmail` | High | Email already exists |
| `testOAuth_GoogleLogin` | High | Google OAuth flow |
| `testJWT_TokenGeneration` | **Critical** | Valid JWT created |
| `testJWT_TokenValidation` | **Critical** | JWT signature validation |
| `testJWT_TokenExpiration` | High | Expired token rejected |
| `testJWT_RoleClaims` | **Critical** | Roles in JWT claims |
| `testPasswordReset_Flow` | Medium | Reset token generation/validation |

---

### 9. EmailService (NEW - 8 tests needed)

**File to create:** `backend/src/test/java/com/northernchile/api/service/EmailServiceTest.java`

| Test | Priority | Description |
|------|----------|-------------|
| `testSendBookingConfirmation` | High | Customer confirmation email |
| `testSendAdminNotification` | High | Admin new booking alert |
| `testSendCancellationEmail` | High | Cancellation notification |
| `testSendRefundConfirmation` | High | Refund email |
| `testSendPickupReminder` | Medium | 24h reminder |
| `testSendTourReminder` | Medium | Day before reminder |
| `testEmailLocalization_ES` | High | Spanish template |
| `testEmailLocalization_EN` | High | English template |

**Note:** Use MockMail or similar for testing. Real email tests are manual.

---

### 10. WebhookSecurityService (NEW - 6 tests needed)

**File to create:** `backend/src/test/java/com/northernchile/api/payment/WebhookSecurityServiceTest.java`

| Test | Priority | Description |
|------|----------|-------------|
| `testTransbank_ValidSignature` | **Critical** | Accept valid Transbank signature |
| `testTransbank_InvalidSignature` | **Critical** | Reject tampered payload |
| `testTransbank_ReplayAttack` | High | Reject old timestamps |
| `testMercadoPago_ValidHMAC` | **Critical** | Accept valid MP signature |
| `testMercadoPago_InvalidHMAC` | **Critical** | Reject invalid signature |
| `testMercadoPago_XSignatureHeader` | High | Parse x-signature header |

---

## Backend Integration Tests

### Database Integration Tests

**File to create:** `backend/src/test/java/com/northernchile/api/integration/`

| Test | Priority | Description |
|------|----------|-------------|
| `BookingRepositoryIntegrationTest` | High | Booking CRUD with real DB |
| `AvailabilityIntegrationTest` | **Critical** | Race condition prevention |
| `CartCleanupIntegrationTest` | Medium | Scheduled cleanup job |
| `TourSearchIntegrationTest` | Medium | Search with filters |

### API Integration Tests

| Test | Priority | Description |
|------|----------|-------------|
| `BookingFlowIntegrationTest` | **Critical** | Full booking flow |
| `PaymentCallbackIntegrationTest` | **Critical** | Payment -> Booking confirm |
| `AdminReportsIntegrationTest` | Medium | Reports with real data |
| `MultiCurrencyIntegrationTest` | High | CLP + BRL pricing |

---

## Frontend E2E Tests

### Test Framework: Playwright or Cypress (to be decided)

### 1. Public User Flows

| Test | Priority | Steps |
|------|----------|-------|
| `tour-listing-and-filter` | High | 1. Load home, 2. Apply filters, 3. Verify results |
| `tour-detail-view` | High | 1. Click tour, 2. See details/gallery/schedules |
| `calendar-navigation` | Medium | 1. Open calendar, 2. Navigate months, 3. Check availability |
| `language-switching` | Medium | 1. Switch ES->EN->PT, 2. Verify translations |
| `theme-switching` | Low | 1. Toggle dark/light mode |

### 2. Booking Flow (Critical)

| Test | Priority | Steps |
|------|----------|-------|
| `complete-booking-flow` | **Critical** | 1. Select tour, 2. Pick date, 3. Add participants, 4. Checkout, 5. Pay, 6. See confirmation |
| `booking-validation` | High | 1. Try booking with invalid data, 2. Verify error messages |
| `participant-form` | High | 1. Fill participant details, 2. Validate required fields |
| `sold-out-handling` | High | 1. Try booking sold-out date, 2. See unavailable message |

### 3. Cart Flow

| Test | Priority | Steps |
|------|----------|-------|
| `add-to-cart` | High | 1. Select tour/date, 2. Add to cart, 3. Verify cart badge |
| `update-cart-quantity` | High | 1. Change participant count, 2. Verify price update |
| `remove-from-cart` | High | 1. Remove item, 2. Verify empty cart |
| `cart-persistence` | Medium | 1. Add item, 2. Refresh page, 3. Cart remains |

### 4. Authentication Flow

| Test | Priority | Steps |
|------|----------|-------|
| `email-registration` | High | 1. Fill form, 2. Submit, 3. Check verification email |
| `login-logout` | High | 1. Login, 2. Verify user menu, 3. Logout |
| `google-oauth` | High | 1. Click Google, 2. Authorize, 3. Redirect back |
| `protected-routes` | **Critical** | 1. Access /profile without login, 2. Redirect to auth |

### 5. Profile Pages

| Test | Priority | Steps |
|------|----------|-------|
| `view-booking-history` | High | 1. Login, 2. Go to /profile/bookings, 3. See list |
| `booking-detail` | Medium | 1. Click booking, 2. See details |
| `profile-edit` | Medium | 1. Edit name/phone, 2. Save, 3. Verify update |

### 6. Admin Panel

| Test | Priority | Steps |
|------|----------|-------|
| `admin-login` | **Critical** | 1. Login as admin, 2. See admin dashboard |
| `tour-management` | High | 1. Create tour, 2. Edit, 3. Publish |
| `schedule-management` | High | 1. Add schedule, 2. Set capacity |
| `booking-management` | High | 1. View bookings, 2. Change status |
| `media-upload` | Medium | 1. Upload image, 2. See in gallery |
| `reports-view` | Medium | 1. Navigate reports, 2. Filter by date |

---

## Manual Testing Procedures

### Payment Gateway Testing

#### Transbank WebPay (Chile)

**Environment:** Integration/Sandbox

| Scenario | Test Card | Expected Result |
|----------|-----------|-----------------|
| Approved | 4051 8856 0044 6623 (CVV: 123, Exp: any future) | Payment approved, booking confirmed |
| Declined | 5186 0595 5959 0568 | Payment declined, error shown |
| Timeout | Let timeout expire | Session expired message |
| User Cancel | Click "Anular" | Return to cart |

**Verification Steps:**
1. Check payment record in database (status = APPROVED)
2. Check booking status updated (CONFIRMED)
3. Check confirmation email sent
4. Check admin notification sent

#### MercadoPago PIX (Brazil)

**Environment:** Sandbox

| Scenario | Test Action | Expected Result |
|----------|-------------|-----------------|
| PIX Generated | Create PIX payment | QR code displayed, copia-e-cola shown |
| PIX Approved | Approve in sandbox | Webhook received, booking confirmed |
| PIX Expired | Wait for expiration | Status updated to expired |
| PIX Cancelled | Cancel in sandbox | Status updated to cancelled |

**Verification Steps:**
1. Verify PIX code is valid format
2. Verify QR code is scannable
3. Check webhook logs for signature validation
4. Verify fee_details stored in payment record

---

### Email Testing

**Prerequisites:** Valid SMTP configuration (Gmail/SendGrid/SES)

| Email Type | Trigger Action | Verify |
|------------|----------------|--------|
| Booking Confirmation | Complete payment | Customer receives, links work |
| Admin New Booking | Complete payment | Admin receives alert |
| Booking Cancelled | Cancel booking | Customer notified |
| Refund Confirmation | Process refund | Amount shown correctly |
| Pickup Reminder | Cron job (24h before) | Correct date/time shown |
| Private Tour Quote | Submit private tour form | Admin receives request |
| Contact Form | Submit contact form | Admin receives message |

**Localization Check:**
- Send each email with `lang=es`, `lang=en`, `lang=pt`
- Verify dates use correct format for locale
- Verify currency format (CLP vs BRL)

---

### Mobile Responsiveness

| Page | Breakpoints to Test |
|------|---------------------|
| Home | 320px, 375px, 414px, 768px, 1024px |
| Tour List | Same |
| Tour Detail | Same + image gallery behavior |
| Booking Form | Same + form usability |
| Checkout | Same + payment button visibility |
| Admin Panel | Tablets (768px+) |

---

## Edge Cases & Security Tests

### Race Condition Testing (Overbooking Prevention)

```bash
# Simulate concurrent booking attempts
# 10 users trying to book last 5 slots simultaneously
for i in {1..10}; do
  curl -X POST /api/bookings \
    -H "Authorization: Bearer $TOKEN_$i" \
    -d '{"scheduleId": "...", "participants": 1}' &
done
wait

# Verify: Only 5 should succeed, 5 should fail with availability error
```

### DST Edge Cases

| Date | Test | Expected |
|------|------|----------|
| 2025-04-05 23:00 | Create booking | Correct Chile time |
| 2025-04-06 00:30 | Create booking | DST transition handled |
| 2025-09-06 23:00 | Create booking | DST transition handled |

### Security Tests

| Test | Priority | Method |
|------|----------|--------|
| XSS in participant names | **Critical** | Submit `<script>alert(1)</script>` |
| SQL Injection | **Critical** | Submit `'; DROP TABLE bookings;--` |
| JWT Tampering | **Critical** | Modify JWT payload, verify rejection |
| IDOR on bookings | **Critical** | Access booking with different user's token |
| Rate limiting | High | 100+ requests/minute, verify 429 |
| CORS policy | High | Request from unauthorized origin |
| File upload validation | High | Upload .exe, .php, oversized files |

### Input Validation

| Field | Invalid Inputs to Test |
|-------|------------------------|
| Email | `notanemail`, `a@b`, `<script>@x.com` |
| Phone | `abc`, `+1 (800) 555-TOUR` (letters) |
| Participant count | `-1`, `0`, `100`, `abc` |
| Dates | Past dates, invalid format, `2025-13-45` |
| Amount | Negative, zero, extremely large |

---

## Test Execution Checklist

### Pre-Release Checklist

- [ ] All unit tests pass (`mvn test`)
- [ ] All integration tests pass
- [ ] E2E tests pass on staging
- [ ] Manual payment tests completed (Transbank + MercadoPago)
- [ ] Email delivery verified for all templates
- [ ] Mobile responsive check completed
- [ ] Security scan completed (OWASP ZAP or similar)
- [ ] Performance check (load testing on critical endpoints)
- [ ] DST edge cases verified
- [ ] Error handling verified (graceful failures)
- [ ] i18n verified (es/en/pt)
- [ ] Cookie consent working
- [ ] Analytics tracking working

### Environment-Specific Tests

| Environment | Additional Tests |
|-------------|------------------|
| Staging | Full E2E, payment sandbox |
| Production | Smoke test, one real payment (small amount) |

---

## Appendix: Test Data

### Test Users

| Role | Email | Password | Notes |
|------|-------|----------|-------|
| Customer | test@customer.com | (create in staging) | Regular user |
| Admin | admin@northernchile.com | (see 1Password) | Full access |
| Partner Admin | partner@test.com | (create in staging) | Limited to own tours |

### Test Tours

Create in staging:
1. "Test Tour - High Availability" (100 slots)
2. "Test Tour - Low Availability" (5 slots)
3. "Test Tour - Sold Out" (0 slots)
4. "Test Tour - Multi-currency" (CLP + BRL pricing)

---

*Last updated: November 2025*
