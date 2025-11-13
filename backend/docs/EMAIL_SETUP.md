# Email Configuration with Google Workspace

This guide explains how to configure the email system for Northern Chile Tours using Google Workspace (Gmail).

## Overview

The application uses Spring Boot Mail with JavaMailSender and Thymeleaf templates to send:

1. **Email Verification** - Sent when users register (token valid for 24 hours)
2. **Password Reset** - Sent when users request password reset (token valid for 2 hours)
3. **Booking Confirmation** - Sent immediately after successful booking
4. **Tour Reminders** - Sent 24 hours before the tour starts (automated)

## Prerequisites

- Google Workspace account (or regular Gmail account)
- Domain configured with Google Workspace (e.g., northernchile.cl)
- Email address for sending (e.g., noreply@northernchile.cl)

## Step 1: Create App Password in Google

Since the application uses SMTP authentication, you need to create an **App-Specific Password** (not your regular password):

1. Go to [Google Account Settings](https://myaccount.google.com/)
2. Navigate to **Security** → **2-Step Verification** (enable if not already enabled)
3. Scroll down to **App passwords**
4. Click **Select app** → Choose **Mail**
5. Click **Select device** → Choose **Other** and name it "Northern Chile Backend"
6. Click **Generate**
7. Copy the 16-character password (format: `xxxx xxxx xxxx xxxx`)

## Step 2: Configure Environment Variables

Add the following variables to your `.env` file:

```bash
# Email Configuration (Google Workspace / Gmail SMTP)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=noreply@northernchile.cl
MAIL_PASSWORD=your_app_specific_password_here
MAIL_FROM_EMAIL=noreply@northernchile.cl
MAIL_FROM_NAME=Northern Chile Tours
MAIL_ENABLED=true

# Frontend URL for email links
NUXT_PUBLIC_BASE_URL=https://www.northernchile.cl
```

### Important Notes:

- **`MAIL_USERNAME`**: Full email address (e.g., noreply@northernchile.cl)
- **`MAIL_PASSWORD`**: Use the 16-character app password (without spaces)
- **`MAIL_ENABLED`**: Set to `true` to enable email sending, `false` to disable (emails will be logged only)
- **`NUXT_PUBLIC_BASE_URL`**: Your frontend URL for verification and reset links

## Step 3: Verify Configuration

### Test in Development

With `MAIL_ENABLED=false`, emails will be logged to console instead of sent. This is useful for development:

```bash
MAIL_ENABLED=false
```

You'll see logs like:
```
WARN  - Email sending is disabled. Would have sent email to: user@example.com with subject: Verify your email
```

### Test with Real Sending

Set `MAIL_ENABLED=true` and register a new user or request password reset. Check:

1. Application logs for "Email sent successfully" messages
2. The recipient's inbox (might be in spam folder initially)

## Step 4: Production Deployment

### Security Checklist

- ✅ Use App-Specific Password (never your main Google password)
- ✅ Store credentials in environment variables (never in code)
- ✅ Use HTTPS for frontend URLs in production
- ✅ Configure SPF, DKIM, and DMARC records for your domain
- ✅ Monitor email sending limits (Gmail: 500 emails/day, Google Workspace: 2000/day)

### Email Sending Limits

- **Gmail Free**: 500 emails per day
- **Google Workspace**: 2000 emails per day
- **Rate Limit**: ~100-150 emails per hour recommended

If you exceed limits, consider:
- Using a dedicated email service (SendGrid, AWS SES, Mailgun)
- Implementing email queue with retry logic
- Adding rate limiting

## Email Templates

Templates are located in `backend/src/main/resources/templates/email/`:

- `verification.html` - Email verification
- `password-reset.html` - Password reset
- `booking-confirmation.html` - Booking confirmation
- `tour-reminder.html` - Pre-tour reminder
- `base.html` - Base template (not used directly)

### Multilingual Support

Email content is internationalized using `messages_{locale}.properties`:

- `messages_es.properties` - Spanish (default)
- `messages_en.properties` - English
- `messages_pt.properties` - Portuguese

Language is determined by:
1. User's `languageCode` field in booking/registration
2. `Accept-Language` header in API requests
3. Fallback to `es-CL`

## Scheduled Tasks

The system automatically sends tour reminders:

- **Schedule**: Every hour at :00 minutes
- **Timing**: 24 hours before tour start (configurable via `mail.reminder.hours-before-tour`)
- **Status**: Only sent for `CONFIRMED` bookings

To change reminder timing, update `application.properties`:

```properties
mail.reminder.hours-before-tour=48  # Send 48 hours before
```

## Troubleshooting

### Emails Not Sending

1. **Check logs** for error messages
2. **Verify credentials** - Test login at https://mail.google.com/
3. **Check port 587** is not blocked by firewall
4. **Verify STARTTLS** is enabled (required by Gmail)

### Emails Going to Spam

1. **Configure SPF record** for your domain:
   ```
   v=spf1 include:_spf.google.com ~all
   ```

2. **Enable DKIM** in Google Workspace Admin Console

3. **Set DMARC policy**:
   ```
   v=DMARC1; p=quarantine; rua=mailto:admin@northernchile.cl
   ```

4. **Warm up the sender** - Start with small volumes and gradually increase

### Rate Limiting Errors

If you see "Daily sending quota exceeded":

1. Wait 24 hours for quota reset
2. Reduce email volume
3. Consider upgrading to Google Workspace or using dedicated email service

### Authentication Errors

If authentication fails:

1. Ensure 2-Step Verification is enabled
2. Regenerate App Password
3. Remove spaces from password in `.env` file
4. Check username is full email address

## API Endpoints

### Email Verification

```bash
# Verify email with token (GET)
GET /api/auth/verify-email?token={token}

# Resend verification email (POST)
POST /api/auth/resend-verification
Content-Type: application/json

{
  "email": "user@example.com"
}
```

### Password Reset

```bash
# Request password reset (POST)
POST /api/auth/password-reset/request
Content-Type: application/json
Accept-Language: es-CL

{
  "email": "user@example.com"
}

# Confirm password reset (POST)
POST /api/auth/password-reset/confirm
Content-Type: application/json

{
  "token": "abc123...",
  "newPassword": "NewSecurePassword123!"
}
```

## Monitoring

Monitor email delivery in production:

1. **Application Logs**: Check for errors in sending
2. **Google Workspace Admin**: Monitor sending quotas
3. **Email Analytics**: Track open rates, bounces, spam complaints

## Alternative Providers

If Gmail/Google Workspace doesn't meet your needs, the system supports any SMTP provider. Update configuration:

```properties
# Example: SendGrid
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=YOUR_SENDGRID_API_KEY

# Example: AWS SES
spring.mail.host=email-smtp.us-east-1.amazonaws.com
spring.mail.port=587
spring.mail.username=YOUR_AWS_SMTP_USERNAME
spring.mail.password=YOUR_AWS_SMTP_PASSWORD
```

---

## Support

For issues or questions:
- Check application logs: `docker logs northernchile-backend`
- Review Spring Boot Mail documentation: https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.email
- Contact: dev@northernchile.cl
