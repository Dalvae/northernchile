# Test Mode Guide - Segregating Test Payments from Production

## Overview

This guide explains how the **Test Mode** system works to prevent test transactions from contaminating your production financial reports and accounting.

## Problem

When testing payment integrations in a live environment, test transactions can:
- ❌ Pollute financial reports
- ❌ Inflate transaction counts
- ❌ Confuse accountants
- ❌ Make it hard to see real revenue

## Solution: Automatic Test Detection

The system **automatically detects** when you're using TEST credentials and marks all payments as test data.

### How Detection Works

#### Global Configuration (Recommended)

Use the **explicit test mode flag** to control whether payments are marked as test:

```bash
PAYMENT_TEST_MODE=true   → All payments marked as test
PAYMENT_TEST_MODE=false  → All payments are real
```

**This is the most reliable method** and works with any credentials.

#### Automatic Detection (Fallback)

If `PAYMENT_TEST_MODE` is not set, the system falls back to automatic detection:

**Transbank:**
```bash
TRANSBANK_ENVIRONMENT=INTEGRATION  → is_test = TRUE (fallback)
TRANSBANK_ENVIRONMENT=PRODUCTION   → is_test = FALSE (fallback)
```

**Mercado Pago:**
```bash
# Uses global PAYMENT_TEST_MODE setting
# No automatic detection based on credentials
```

**⚠️ Important:** Mercado Pago credentials (both test and production) may start with `APP-`. Always use `PAYMENT_TEST_MODE` to control test mode explicitly.

## Features

### 1. Automatic Marking

When a payment is created:
```java
payment.setTest(isTestMode()); // Automatically set based on credentials
```

No manual intervention needed!

### 2. Visual Indicators (Frontend)

Test payments show a **warning badge**:

```
┌─────────────────────────────┐
│  🧪 MODO TEST               │
│                             │
│  [QR Code Here]             │
│                             │
│  Este es un pago de prueba  │
└─────────────────────────────┘
```

### 3. Filter Test Payments (Backend)

**Get only real payments:**
```java
List<Payment> realPayments = paymentRepository.findByIsTest(false);
```

**Get only test payments:**
```java
List<Payment> testPayments = paymentRepository.findByIsTest(true);
```

### 4. Admin Endpoints

#### View Test Payments
```http
GET /api/payments/test
Authorization: Bearer {admin_token}

Response:
{
  "count": 15,
  "payments": [...]
}
```

**Access:** `ROLE_SUPER_ADMIN` or `ROLE_PARTNER_ADMIN`

#### Delete All Test Payments
```http
DELETE /api/payments/test
Authorization: Bearer {super_admin_token}

Response:
{
  "message": "Test payments deleted successfully",
  "deletedCount": 15
}
```

**Access:** `ROLE_SUPER_ADMIN` only (destructive operation)

⚠️ **Warning:** This permanently deletes test payment records. Use with caution!

## Database Schema

### Added Column

```sql
ALTER TABLE payments ADD COLUMN is_test BOOLEAN NOT NULL DEFAULT FALSE;
```

### Indexes

Consider adding an index for performance:
```sql
CREATE INDEX idx_payments_is_test ON payments(is_test);
```

## Financial Reporting

### Exclude Test Payments from Reports

When generating financial reports, **always filter out test data**:

```java
// ✅ Correct - exclude test payments
public BigDecimal getTotalRevenue() {
    return paymentRepository.findByIsTest(false)
        .stream()
        .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
        .map(Payment::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
}

// ❌ Wrong - includes test data
public BigDecimal getTotalRevenue() {
    return paymentRepository.findAll() // Includes test!
        .stream()
        .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
        .map(Payment::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
}
```

### SQL Query Example

```sql
-- Get total real revenue
SELECT SUM(amount)
FROM payments
WHERE is_test = FALSE
  AND status = 'COMPLETED';

-- Get test transaction count
SELECT COUNT(*)
FROM payments
WHERE is_test = TRUE;
```

## Testing Workflow

### Development Environment

1. **Use TEST credentials**
```bash
# .env
TRANSBANK_ENVIRONMENT=INTEGRATION
MERCADOPAGO_ACCESS_TOKEN=TEST-1234567890-123456-abcdef...
```

2. **Make test payments**
   - All marked as `is_test = true` automatically
   - Frontend shows "TEST MODE" badge
   - Safe to test freely

3. **Clean up periodically**
```bash
curl -X DELETE https://your-api.com/api/payments/test \
  -H "Authorization: Bearer ${SUPER_ADMIN_TOKEN}"
```

### Production Environment

1. **Use PRODUCTION credentials**
```bash
# .env
TRANSBANK_ENVIRONMENT=PRODUCTION
MERCADOPAGO_ACCESS_TOKEN=APP-1234567890-123456-abcdef...
```

2. **All payments are real**
   - Marked as `is_test = false` automatically
   - No TEST badge shown
   - Included in financial reports

## Best Practices

### DO ✅

- Always use TEST credentials in development/staging
- Clean up test data regularly (weekly/monthly)
- Filter `is_test = false` in financial queries
- Document which reports exclude test data
- Use separate databases for dev/staging/production (recommended)

### DON'T ❌

- Mix TEST and PRODUCTION credentials
- Manually change `is_test` flags
- Delete real payments accidentally
- Forget to filter test data in reports
- Test with real credit cards

## Environment Setup

### Development (Testing)
```bash
# Explicitly enable test mode
PAYMENT_TEST_MODE=true

# Use test credentials from Mercado Pago dashboard
TRANSBANK_ENVIRONMENT=INTEGRATION
TRANSBANK_COMMERCE_CODE=597055555532
TRANSBANK_API_KEY=579B532A7440BB0C9079DED94D31EA1615BACEB56610332264630D42D0A36B1C

MERCADOPAGO_ACCESS_TOKEN=APP-... # Get from "Credenciales de prueba"
MERCADOPAGO_PUBLIC_KEY=APP-...   # Get from "Credenciales de prueba"
```

### Staging (Testing with Real Credentials - Optional)
```bash
# Enable test mode even with production credentials
PAYMENT_TEST_MODE=true

# Use production credentials but mark as test
TRANSBANK_ENVIRONMENT=PRODUCTION
TRANSBANK_COMMERCE_CODE=your_real_code
TRANSBANK_API_KEY=your_real_key

MERCADOPAGO_ACCESS_TOKEN=APP-... # Production credentials
MERCADOPAGO_PUBLIC_KEY=APP-...   # Production credentials
```

### Production (Real Transactions)
```bash
# Disable test mode for real transactions
PAYMENT_TEST_MODE=false

# Use production credentials
TRANSBANK_ENVIRONMENT=PRODUCTION
TRANSBANK_COMMERCE_CODE=your_real_code
TRANSBANK_API_KEY=your_real_key

MERCADOPAGO_ACCESS_TOKEN=APP-... # From "Credenciales de producción"
MERCADOPAGO_PUBLIC_KEY=APP-...   # From "Credenciales de producción"
```

## Monitoring

### Grafana/Prometheus Metrics

Track test vs real payments:
```
# Real payments count
sum(payments{is_test="false",status="COMPLETED"})

# Test payments count
sum(payments{is_test="true"})

# Alert if test payments in production
alert: TestPaymentsInProduction
expr: sum(payments{is_test="true",environment="production"}) > 0
```

### Database Queries

Check for test pollution:
```sql
-- Daily test payment count
SELECT DATE(created_at), COUNT(*)
FROM payments
WHERE is_test = TRUE
GROUP BY DATE(created_at)
ORDER BY DATE(created_at) DESC
LIMIT 30;

-- Ratio of test to real payments
SELECT
  SUM(CASE WHEN is_test THEN 1 ELSE 0 END) as test_count,
  SUM(CASE WHEN NOT is_test THEN 1 ELSE 0 END) as real_count,
  ROUND(100.0 * SUM(CASE WHEN is_test THEN 1 ELSE 0 END) / COUNT(*), 2) as test_percentage
FROM payments;
```

## Troubleshooting

### Issue: Real payments marked as test

**Cause:** Using TEST credentials in production

**Solution:**
1. Verify environment variables
2. Check `TRANSBANK_ENVIRONMENT` and `MERCADOPAGO_ACCESS_TOKEN`
3. Restart backend after fixing

### Issue: Test payments not marked

**Cause:** Database column missing or default value issue

**Solution:**
```sql
-- Check if column exists
SELECT column_name, data_type, column_default
FROM information_schema.columns
WHERE table_name = 'payments' AND column_name = 'is_test';

-- Add column if missing (Hibernate will auto-create, but manual is safer)
ALTER TABLE payments ADD COLUMN IF NOT EXISTS is_test BOOLEAN NOT NULL DEFAULT FALSE;
```

### Issue: Can't delete test payments

**Cause:** Foreign key constraints

**Solution:** Test payments with associated bookings cannot be deleted. Options:
1. Soft delete (add `deleted_at` timestamp)
2. Cascade delete bookings (dangerous)
3. Mark bookings as test too

## API Reference

### Get Test Payments
```http
GET /api/payments/test
```

**Response:**
```json
{
  "count": 5,
  "payments": [
    {
      "id": "uuid",
      "amount": 10000,
      "status": "COMPLETED",
      "isTest": true,
      "provider": "MERCADOPAGO",
      "paymentMethod": "PIX",
      "createdAt": "2025-01-26T10:00:00Z"
    }
  ]
}
```

### Delete Test Payments
```http
DELETE /api/payments/test
```

**Response:**
```json
{
  "message": "Test payments deleted successfully",
  "deletedCount": 5
}
```

## Frontend Integration

### TypeScript Type
```typescript
export interface PaymentInitRes {
  paymentId: string
  status: PaymentStatus
  isTest?: boolean // NEW
  // ... other fields
}
```

### Display Test Badge
```vue
<div v-if="payment.isTest" class="test-badge">
  <UIcon name="i-lucide-flask-conical" />
  <span>{{ t('payment.test_mode') }}</span>
</div>
```

### Filter Test Payments in UI
```typescript
// Admin dashboard - separate tabs
const realPayments = computed(() =>
  payments.value.filter(p => !p.isTest)
)

const testPayments = computed(() =>
  payments.value.filter(p => p.isTest)
)
```

## Migration Guide

If you have existing payments in production:

1. **Mark existing as real:**
```sql
UPDATE payments SET is_test = FALSE WHERE is_test IS NULL;
```

2. **Identify test patterns:**
```sql
-- Mark known test emails/amounts
UPDATE payments
SET is_test = TRUE
WHERE booking_id IN (
  SELECT id FROM bookings WHERE user_email LIKE '%test%'
);
```

3. **Manual review:**
```sql
-- Review suspicious payments
SELECT * FROM payments
WHERE amount < 1000 -- Small test amounts
  AND created_at > NOW() - INTERVAL '30 days'
ORDER BY created_at DESC;
```

## Security Considerations

1. **Admin-only access:** Test data endpoints require admin authentication
2. **Audit logging:** All test data deletions are logged with `log.warn()`
3. **Production safeguards:** Never expose test flag modification API
4. **Monitoring:** Alert on unexpected test payments in production

## Compliance & Auditing

### PCI-DSS Compliance
- Test data clearly separated from production
- No real card data in test transactions
- Audit trail of test data cleanup

### Financial Audits
- Reports explicitly exclude test data
- Test transactions clearly labeled
- Cleanup operations logged

## Summary

✅ **Automatic detection** - No manual flags needed
✅ **Visual indicators** - Clear TEST badges in UI
✅ **Easy cleanup** - Single API call to delete all test data
✅ **Safe reports** - Filter out test payments in queries
✅ **Production-ready** - Works with live testing

---

**Last Updated:** 2025-01-26
**Version:** 1.0.0
