# Payment Testing Guide

Complete guide for testing payment integrations in development/sandbox mode.

## 🔧 Development Mode Configuration

### Transbank (Chile)

**Environment:** INTEGRATION (pre-configured by default)
```properties
transbank.environment=INTEGRATION
transbank.commerce-code=597055555532
transbank.api-key=579B532A7440BB0C9079DED94D31EA1615BACEB56610332264630D42D0A36B1C
```

**Note:** The SDK has these integration credentials pre-configured when using `WebpayPlus.configureForTesting()`.

### Mercado Pago (Latin America)

**Environment:** TEST/SANDBOX
```properties
mercadopago.access-token=TEST-ACCESS-TOKEN  # Must start with "TEST-"
mercadopago.public-key=TEST-PUBLIC-KEY      # Must start with "TEST-"
```

**Getting Test Credentials:**
1. Go to https://www.mercadopago.com/developers/panel
2. Navigate to "Your integrations" → "Test accounts"
3. Create two test accounts: one seller, one buyer
4. Get test credentials (Access Token starts with `TEST-`)

---

## 💳 Transbank Test Cards

### Credit Cards

#### ✅ Approved Transaction (VISA)
```
Card Number: 4051 8856 0044 6623
Expiration:  Any future date (e.g., 12/25)
CVV:         123
```

#### ❌ Rejected Transaction (MASTERCARD)
```
Card Number: 5186 0595 5959 0568
Expiration:  Any future date (e.g., 12/25)
CVV:         123
```

### Debit Cards

Same card numbers as credit cards:
- **Success:** 4051 8856 0044 6623
- **Failure:** 5186 0595 5959 0568

### Authentication Form

When Transbank shows an authentication form (RUT and password):
```
RUT:      11.111.111-1
Password: 123
```

**Note:** All transactions in INTEGRATION environment are automatically approved if you use the success card.

### Testing Flow

1. **Initialize Payment:**
   ```bash
   POST /api/payments/init
   {
     "bookingId": "uuid",
     "provider": "TRANSBANK",
     "paymentMethod": "WEBPAY",
     "amount": 50000,
     "currency": "CLP",
     "returnUrl": "http://localhost:3000/payment/success"
   }
   ```

2. **Response:** You'll receive a `paymentUrl` and `token`

3. **Redirect:** User goes to `paymentUrl`, enters test card, completes authentication

4. **Callback:** Transbank redirects to your return URL with `token_ws`

5. **Confirm Payment:**
   ```bash
   GET /api/payments/confirm?token_ws={token}
   ```

---

## 💳 Mercado Pago Test Cards

### Argentina

| Type | Brand | Number | CVV | Expiry |
|------|-------|--------|-----|--------|
| Credit | Mastercard | 5031 7557 3453 0604 | 123 | 11/25 |
| Credit | Visa | 4509 9535 6623 3704 | 123 | 11/25 |
| Credit | Amex | 3711 803032 57522 | 1234 | 11/25 |
| Debit | Mastercard | 5287 3383 1025 3304 | 123 | 11/25 |
| Debit | Visa | 4002 7686 9439 5619 | 123 | 11/25 |

### Brazil (For PIX and Cards)

| Type | Brand | Number | CVV | Expiry |
|------|-------|--------|-----|--------|
| Credit | Mastercard | 5031 4332 1540 6351 | 123 | 11/25 |
| Credit | Visa | 4235 6477 2802 5682 | 123 | 11/25 |

### Chile

| Type | Brand | Number | CVV | Expiry |
|------|-------|--------|-----|--------|
| Credit | Mastercard | 5416 7526 0258 2580 | 123 | 11/25 |
| Credit | Visa | 4168 8188 4444 7115 | 123 | 11/25 |

### Mexico

| Type | Brand | Number | CVV | Expiry |
|------|-------|--------|-----|--------|
| Credit | Mastercard | 5474 9254 3267 0366 | 123 | 11/25 |
| Credit | Visa | 4075 5957 1648 3764 | 123 | 11/25 |

### Test Scenarios (Cardholder Names)

Use these names to simulate different payment outcomes:

| Name | Result | Status |
|------|--------|--------|
| **APRO** | Approved | Payment successful |
| **OTHE** | Rejected | General error |
| **CONT** | Pending | Payment pending |
| **FUND** | Rejected | Insufficient funds |
| **SECU** | Rejected | Invalid security code |
| **CALL** | Rejected | Card needs authorization |
| **EXPI** | Rejected | Card expired |
| **FORM** | Rejected | Form error |

**Example:**
```
Name:     APRO
Surname:  USER
CPF/DNI:  12345678
Email:    test_user_123456789@testuser.com
```

---

## 🇧🇷 PIX Testing (Brazil)

### Important Notes

1. **PIX in sandbox is instant:** Payment status changes to `approved` immediately
2. **No QR code scanning:** In test mode, you don't actually scan the QR code
3. **Automatic approval:** Creating a PIX payment automatically approves it after a few seconds
4. **Test accounts required:** Must use test seller/buyer accounts

### Testing PIX Flow

1. **Initialize PIX Payment:**
   ```bash
   POST /api/payments/init
   {
     "bookingId": "uuid",
     "provider": "MERCADOPAGO",
     "paymentMethod": "PIX",
     "amount": 250.00,
     "currency": "BRL",
     "userEmail": "test@example.com",
     "expirationMinutes": 30
   }
   ```

2. **Response:** You'll receive:
   - `qrCode`: Base64 QR code image
   - `pixCode`: Copy-paste payment code
   - `paymentId`: Internal payment ID
   - `expiresAt`: Expiration timestamp

3. **Webhook Simulation:**
   In test mode, Mercado Pago will send a webhook to your configured URL when the payment is "approved" (happens automatically):

   ```bash
   POST /api/webhooks/mercadopago
   {
     "action": "payment.updated",
     "data": {
       "id": "123456789"  # Mercado Pago payment ID
     }
   }
   ```

4. **Check Status:**
   ```bash
   GET /api/payments/{paymentId}/status
   ```

### Manual Testing in Mercado Pago Panel

1. Go to https://www.mercadopago.com.br/developers/panel
2. Navigate to "Your integrations" → "Test accounts"
3. Use the test buyer account to complete the payment
4. In sandbox, PIX payments are instantly approved

---

## 🧪 Testing Webhooks Locally

### Using ngrok (Recommended)

1. **Install ngrok:**
   ```bash
   npm install -g ngrok
   ```

2. **Start your backend:**
   ```bash
   docker-compose up
   ```

3. **Create tunnel:**
   ```bash
   ngrok http 8080
   ```

4. **Configure webhook URL in Mercado Pago:**
   - URL: `https://your-ngrok-url.ngrok.io/api/webhooks/mercadopago`
   - Events: Payment updates

### Manual Webhook Testing

Use curl to simulate webhook:

```bash
# Mercado Pago webhook
curl -X POST http://localhost:8080/api/webhooks/mercadopago \
  -H "Content-Type: application/json" \
  -d '{
    "action": "payment.updated",
    "data": {
      "id": "123456789"
    }
  }'
```

---

## 📝 Testing Checklist

### Transbank (Webpay Plus)

- [ ] Successful payment with approved card
- [ ] Failed payment with rejected card
- [ ] User cancels payment
- [ ] Payment timeout
- [ ] Invalid token handling
- [ ] Refund approved payment

### Mercado Pago - Credit Card

- [ ] Approved payment (APRO)
- [ ] Rejected - Insufficient funds (FUND)
- [ ] Rejected - Invalid CVV (SECU)
- [ ] Rejected - Expired card (EXPI)
- [ ] Pending payment (CONT)

### Mercado Pago - PIX

- [ ] QR code generation
- [ ] PIX code copy-paste
- [ ] Payment expiration (30 min)
- [ ] Webhook notification received
- [ ] Booking status updated to CONFIRMED
- [ ] Expired payment handling

---

## 🚨 Common Issues

### Transbank

**Issue:** "Invalid commerce code"
- **Solution:** Ensure `transbank.environment=INTEGRATION` in application.properties

**Issue:** "Transaction not found"
- **Solution:** Token expires after 5 minutes. Complete payment flow quickly.

**Issue:** "Authentication failed"
- **Solution:** Use RUT `11.111.111-1` and password `123` in the auth form

### Mercado Pago

**Issue:** "Invalid credentials"
- **Solution:** Ensure access token starts with `TEST-` prefix

**Issue:** "Webhook not received"
- **Solution:** Use ngrok or similar tunnel for local testing, or check Mercado Pago logs in developer panel

**Issue:** "PIX QR code not showing"
- **Solution:** Remove `/sandbox` from URL if using production credentials accidentally

**Issue:** "Test account can't buy from itself"
- **Solution:** Create separate seller and buyer test accounts

---

## 🔐 Production Setup

### Transbank

1. Request production credentials at https://www.transbankdevelopers.cl
2. Complete certification process
3. Update environment variables:
   ```properties
   TRANSBANK_ENVIRONMENT=PRODUCTION
   TRANSBANK_COMMERCE_CODE=your_production_code
   TRANSBANK_API_KEY=your_production_key
   ```

### Mercado Pago

1. Complete account verification in Mercado Pago
2. Get production credentials from developer panel
3. Update environment variables:
   ```properties
   MERCADOPAGO_ACCESS_TOKEN=APP-your_production_token
   MERCADOPAGO_PUBLIC_KEY=APP-your_production_key
   ```
4. Configure production webhook URL
5. Enable payment methods in your account

---

## 📚 Additional Resources

- **Transbank Docs:** https://www.transbankdevelopers.cl
- **Mercado Pago Docs:** https://www.mercadopago.com/developers
- **PIX Documentation:** https://www.bcb.gov.br/estabilidadefinanceira/pix
- **Test Cards Repository:** https://github.com/TransbankDevelopers/transbank-webpay-credenciales

---

## 🆘 Support

If you encounter issues:

1. Check logs in `backend` container: `docker logs northernchile-backend-1`
2. Review Swagger UI at `http://localhost:8080/swagger-ui.html`
3. Check provider dashboards:
   - Transbank: No dashboard for integration environment
   - Mercado Pago: https://www.mercadopago.com/developers/panel

For production issues, contact provider support directly.
