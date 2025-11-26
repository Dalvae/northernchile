# Payment Configuration Guide

This guide provides detailed instructions for configuring payment integrations in Northern Chile Tours platform.

## Table of Contents

- [Overview](#overview)
- [Transbank (Webpay Plus) - Chile](#transbank-webpay-plus---chile)
- [Mercado Pago - Latin America (PIX, Cards)](#mercado-pago---latin-america-pix-cards)
- [Webhooks Configuration](#webhooks-configuration)
- [Testing Payments](#testing-payments)
- [Refund Policy](#refund-policy)
- [Commission Rates](#commission-rates)

## Overview

Northern Chile Tours supports multiple payment providers:

1. **Transbank (Webpay Plus)** - Primary processor for Chilean customers
2. **Mercado Pago** - International processor with PIX support for Brazilian customers
3. **Stripe** - Planned for future implementation

## Transbank (Webpay Plus) - Chile

### Credentials

Transbank uses a two-tier environment system:

#### Integration Environment (Testing)
- **Purpose:** Development and testing
- **Commerce Code:** `597055555532` (pre-configured in SDK)
- **API Key:** `579B532A7440BB0C9079DED94D31EA1615BACEB56610332264630D42D0A36B1C`
- **Environment:** `INTEGRATION`

#### Production Environment
- **Purpose:** Live transactions
- **Credentials:** Request from Transbank after account approval
- **Environment:** `PRODUCTION`

### Environment Variables

```bash
# For Testing
TRANSBANK_COMMERCE_CODE=597055555532
TRANSBANK_API_KEY=579B532A7440BB0C9079DED94D31EA1615BACEB56610332264630D42D0A36B1C
TRANSBANK_ENVIRONMENT=INTEGRATION

# For Production
TRANSBANK_COMMERCE_CODE=your_production_commerce_code
TRANSBANK_API_KEY=your_production_api_key
TRANSBANK_ENVIRONMENT=PRODUCTION
```

### How It Works

1. Customer selects Webpay Plus payment method
2. Backend creates transaction and receives redirect URL + token
3. Customer is redirected to Transbank's secure payment page
4. Customer completes payment with Chilean credit/debit card
5. Transbank redirects back to `/api/payments/confirm?token_ws={token}`
6. Backend confirms transaction and updates booking status

### Test Cards

In INTEGRATION mode, use these test cards:

**Successful Payment:**
- Card: VISA `4051885600446623`
- CVV: `123`
- Expiry: Any future date
- RUT: `11.111.111-1`
- Password: `123`

**Failed Payment:**
- Card: Mastercard `5186059559590568`
- CVV: `123`
- Expiry: Any future date
- RUT: `11.111.111-1`
- Password: `123`

### Commission Rates

- **Debit Cards:** ~0.82% + UF
- **Credit Cards:** ~1.9% + UF
- **Dynamic Pricing:** Rates adjust after 2 months based on transaction volume
- **No Monthly Fees:** Only pay commission per transaction

Variable by business category (MCC code). Use [Transbank's simulator](https://publico.transbank.cl/tarifas) for exact rates.

## Mercado Pago - Latin America (PIX, Cards)

### Credentials

Mercado Pago provides separate credentials for testing and production:

#### Test Environment
- **Access Token:** Starts with `TEST-` (e.g., `TEST-1234567890-123456-abcdef...`)
- **Public Key:** Starts with `TEST-` (e.g., `TEST-abcdef123456...`)
- **Dashboard:** [https://www.mercadopago.com/developers/panel/app](https://www.mercadopago.com/developers/panel/app)

#### Production Environment
- **Access Token:** Starts with `APP-` (e.g., `APP-1234567890-123456-abcdef...`)
- **Public Key:** Starts with `APP-` (e.g., `APP-abcdef123456...`)

### Environment Variables

```bash
# For Testing
MERCADOPAGO_ACCESS_TOKEN=TEST-1234567890-123456-abcdefghijklmnop123456789
MERCADOPAGO_PUBLIC_KEY=TEST-abcdef123456-7890abcd-efgh-ijkl-mnop-qrstuvwxyz12

# For Production
MERCADOPAGO_ACCESS_TOKEN=APP-1234567890-123456-abcdefghijklmnop123456789
MERCADOPAGO_PUBLIC_KEY=APP-abcdef123456-7890abcd-efgh-ijkl-mnop-qrstuvwxyz12
```

### Getting Credentials

1. Go to [Mercado Pago Developers](https://www.mercadopago.com/developers)
2. Sign in with your Mercado Pago account
3. Navigate to **Your applications**
4. Create a new application or select existing one
5. Copy **Access Token** (for backend) and **Public Key** (for frontend)
6. Use TEST credentials for development, APP credentials for production

### PIX Payment Method (Brazil)

PIX is Brazil's instant payment system - like a QR code-based bank transfer.

#### How PIX Works

1. Customer selects PIX payment method
2. Backend creates payment with Mercado Pago
3. Mercado Pago generates:
   - **QR Code** (PNG image as base64)
   - **PIX Code** (string for copy-paste, e.g., `00020126600014br.gov.bcb.pix0117...`)
   - **Expiration Time** (default 30 minutes, configurable)
4. Frontend displays QR code and copy-paste code
5. Customer scans QR or pastes code in their bank app
6. Payment is instant - webhook notifies our backend
7. Booking is automatically confirmed

#### PIX Features

- **Instant Payment:** Confirmed in seconds
- **24/7 Availability:** Works on weekends and holidays
- **No Card Required:** Direct bank transfer
- **Low Fees:** Lower than card transactions
- **QR Code or Copy-Paste:** Two ways to pay

#### PIX Configuration

```java
// Default expiration: 30 minutes
// Configurable via PaymentInitReq.expirationMinutes
PaymentInitReq request = new PaymentInitReq();
request.setPaymentMethod(PaymentMethod.PIX);
request.setExpirationMinutes(30); // Optional, default 30
```

#### Frontend Display

The frontend automatically displays:
- QR Code image (scannable)
- PIX copy-paste code (for manual entry)
- Countdown timer until expiration
- Real-time status updates via polling

### Test PIX Payments

In TEST mode, PIX payments are **automatically approved** after creation. You don't need a real Brazilian bank account.

### Commission Rates

**PIX (Brazil):**
- Standard: ~1.59% (lower than cards)
- Instant confirmation
- No chargeback risk

**Credit Cards (Chile/International):**
- QR Code: 2.6% + IVA
- Point (credit): 2.79% + IVA
- Links: 3.19% + IVA

**Debit Cards:**
- Point: 2.95% + IVA
- Links: 2.89% + IVA

## Webhooks Configuration

Webhooks allow payment providers to notify our system when payment status changes.

### Webhook URLs

Your backend must be publicly accessible. Configure these URLs in payment provider dashboards:

```
https://your-domain.com/api/webhooks/mercadopago
https://your-domain.com/api/webhooks/transbank  (not used - Transbank uses redirect)
```

### Mercado Pago Webhook Setup

1. Go to [Mercado Pago Developers](https://www.mercadopago.com/developers)
2. Select your application
3. Click **Webhooks** in left menu
4. Add webhook URL: `https://your-domain.com/api/webhooks/mercadopago`
5. Select events to receive:
   - `payment` - Payment status updates
   - `merchant_order` - Order updates

### Webhook Security

Our system implements multiple security measures:

1. **Signature Verification:** Validates HMAC-SHA256 signature
2. **Timestamp Validation:** Rejects webhooks older than 5 minutes
3. **Duplicate Detection:** Prevents processing same webhook twice
4. **Rate Limiting:** Protects against spam attacks

Configuration:
```bash
# Optional - defaults to payment provider secrets
MERCADOPAGO_WEBHOOK_SECRET=your_webhook_secret
TRANSBANK_WEBHOOK_SECRET=your_webhook_secret
```

If not set, uses:
- Mercado Pago: `MERCADOPAGO_ACCESS_TOKEN`
- Transbank: `TRANSBANK_API_KEY`

### Transbank Webhooks

Transbank does **NOT** use webhooks. It uses a redirect-based flow where the customer is redirected back to your site after payment.

## Testing Payments

### Transbank Testing

See [Test Cards](#test-cards) section above.

### Mercado Pago Testing

**PIX:**
- Automatically approved in TEST mode
- No real bank account needed
- Instant confirmation

**Credit Cards (TEST mode):**

Test by cardholder name:
- `APRO` - Approved
- `CONT` - Pending (requires contingency)
- `CALL` - Declined (call to authorize)
- `FUND` - Declined (insufficient funds)
- `SECU` - Declined (invalid security code)
- `EXPI` - Declined (expired card)
- `FORM` - Declined (form error)
- `OTHE` - Declined (general error)

**Example:**
- Card: `5031 7557 3453 0604` (Mastercard)
- Name: `APRO`
- CVV: `123`
- Expiry: `11/25`
- Result: Approved

See complete testing guide: `backend/docs/PAYMENT_TESTING.md`

## Refund Policy

### 24-Hour Cancellation Policy

Our platform implements a **24-hour advance cancellation policy**:

- ✅ **Allowed:** Refunds if cancelled **more than 24 hours** before tour starts
- ❌ **Blocked:** Refunds if cancelled **less than 24 hours** before tour starts

### Refund Implementation

The system automatically validates the 24-hour window:

```java
// In PaymentService.canRefund()
Instant tourStart = payment.getBooking().getTourSchedule().getStartDatetime();
Instant now = Instant.now();
long hoursUntilTour = ChronoUnit.HOURS.between(now, tourStart);

if (hoursUntilTour < 24) {
    throw new IllegalStateException(
        "Cannot refund payment less than 24 hours before tour starts. " +
        "Hours remaining: " + hoursUntilTour
    );
}
```

### Refund Methods

**Transbank:**
- Full or partial refunds supported
- Processed via `transaction.refund(token, amount)`
- Refund appears in customer's bank account in 3-5 business days

**Mercado Pago:**
- Full or partial refunds supported
- Processed via `PaymentRefundClient.refund(paymentId, amount)`
- Refund timing depends on original payment method:
  - PIX: 1-2 business days
  - Credit Card: 7-14 business days (depends on card issuer)

### Admin Access

Only users with `ROLE_SUPER_ADMIN` or `ROLE_PARTNER_ADMIN` can process refunds:

```
POST /api/payments/{paymentId}/refund
Authorization: Bearer {admin_jwt_token}
Content-Type: application/json

{
  "amount": 10000  // Optional - omit for full refund
}
```

## Commission Rates Summary

| Provider | Method | Rate | Notes |
|----------|--------|------|-------|
| Transbank | Debit | ~0.82% + UF | Variable by MCC |
| Transbank | Credit | ~1.9% + UF | Variable by MCC |
| Mercado Pago | PIX | ~1.59% | Brazil only |
| Mercado Pago | Credit Card | 2.79-3.19% + IVA | Chile |
| Mercado Pago | Debit Card | 2.89-2.95% + IVA | Chile |

**Important Notes:**
- Transbank rates are dynamic and adjust after 2 months based on volume
- Mercado Pago rates include IVA (Chilean VAT)
- PIX has lower rates but is Brazil-only
- International cards have higher processing fees

## Environment Variables Checklist

Before deploying to production, ensure all these variables are set:

### Required
- ✅ `PAYMENT_TEST_MODE` - **NEW:** Set to `true` for testing, `false` for production
- ✅ `TRANSBANK_COMMERCE_CODE`
- ✅ `TRANSBANK_API_KEY`
- ✅ `TRANSBANK_ENVIRONMENT`
- ✅ `MERCADOPAGO_ACCESS_TOKEN`
- ✅ `MERCADOPAGO_PUBLIC_KEY`

### Optional
- `MERCADOPAGO_WEBHOOK_SECRET` (defaults to access token)
- `TRANSBANK_WEBHOOK_SECRET` (defaults to API key)

### Security
- ✅ `JWT_SECRET` (for authentication)
- ✅ `ALLOWED_REDIRECT_DOMAINS` (whitelist for payment redirects)

### Test Mode Configuration

**⚠️ IMPORTANT:** Mercado Pago credentials (both test and production) may start with `APP-`.

**To test without contaminating your accounting:**
```bash
PAYMENT_TEST_MODE=true  # Mark all payments as test
```

**For production (real transactions):**
```bash
PAYMENT_TEST_MODE=false  # All payments are real
```

See `backend/docs/TEST_MODE_GUIDE.md` for complete test mode documentation.

## Troubleshooting

### Transbank Issues

**Problem:** Transaction fails with "Invalid signature"
- **Solution:** Verify `TRANSBANK_API_KEY` matches your environment

**Problem:** Redirect URL rejected
- **Solution:** Add your domain to `ALLOWED_REDIRECT_DOMAINS`

### Mercado Pago Issues

**Problem:** PIX QR code not displaying
- **Solution:** Ensure `qrCode` is returned as data URI with base64 prefix

**Problem:** Webhook not received
- **Solution:** Verify webhook URL is publicly accessible and added to Mercado Pago dashboard

**Problem:** Payment approved but booking not confirmed
- **Solution:** Check webhook signature verification and logs

### General Issues

**Problem:** Refund blocked despite being >24 hours
- **Solution:** Check timezone configuration - tour times must be in `America/Santiago`

**Problem:** Payment appears stuck in PENDING
- **Solution:** Check webhook logs - may need to manually query payment status

## Support Resources

- **Transbank:** [Developer Portal](https://www.transbankdevelopers.cl)
- **Mercado Pago:** [Developer Portal](https://www.mercadopago.com/developers)
- **PIX Documentation:** [Mercado Pago PIX Guide](https://www.mercadopago.com.br/developers/en/docs/checkout-api/integration-configuration/pix-integration)

---

**Last Updated:** 2025-01-26
**Version:** 1.0.0
