# Email Configuration with Amazon SES

This guide explains how to configure the email system for Northern Chile Tours using Amazon SES (Simple Email Service).

## Overview

The application uses Spring Boot Mail with JavaMailSender and Thymeleaf templates to send:

1. **Email Verification** - Sent when users register (token valid for 24 hours)
2. **Password Reset** - Sent when users request password reset (token valid for 2 hours)
3. **Booking Confirmation** - Sent immediately after successful booking
4. **Tour Reminders** - Sent 24 hours before the tour starts (automated)

## Prerequisites

- AWS account with Amazon SES enabled
- Verified domain (northernchile.com) or verified email addresses in SES
- SMTP credentials generated in SES console

## Step 1: Set Up Amazon SES

### 1.1 Verify Your Domain

1. Go to [AWS Console](https://console.aws.amazon.com/) → **Amazon SES**
2. Navigate to **Verified identities** → **Create identity**
3. Choose **Domain** and enter `northernchile.com`
4. Follow the DNS verification steps (add CNAME records to your domain)
5. Wait for verification (usually 24-72 hours)

### 1.2 Create SMTP Credentials

1. In SES Console → **SMTP Settings**
2. Click **Create SMTP credentials**
3. Enter a name (e.g., "northernchile-backend")
4. Click **Create user**
5. **Save the credentials immediately** - the password is only shown once!

### 1.3 Request Production Access (Important!)

SES starts in **Sandbox Mode** where you can only send to verified emails. For production:

1. Go to SES Console → **Account dashboard**
2. Click **Request production access**
3. Fill in the form explaining your use case
4. Wait for AWS approval (usually 24 hours)

## Step 2: Configure Environment Variables

Add the following variables to your `.env` file:

```bash
# Email Configuration (Amazon SES SMTP)
MAIL_HOST=email-smtp.us-east-1.amazonaws.com
MAIL_PORT=587
MAIL_USERNAME=YOUR_AWS_SMTP_USERNAME
MAIL_PASSWORD=YOUR_AWS_SMTP_PASSWORD
MAIL_FROM_EMAIL=noreply@northernchile.com
MAIL_FROM_NAME=Northern Chile Tours
MAIL_ENABLED=true

# Frontend URL for email links
NUXT_PUBLIC_BASE_URL=https://www.northernchile.com
```

### SES SMTP Endpoints by Region

| Region | SMTP Endpoint |
|--------|---------------|
| US East (N. Virginia) | `email-smtp.us-east-1.amazonaws.com` |
| US West (Oregon) | `email-smtp.us-west-2.amazonaws.com` |
| Europe (Ireland) | `email-smtp.eu-west-1.amazonaws.com` |
| South America (São Paulo) | `email-smtp.sa-east-1.amazonaws.com` |

### Important Notes:

- **`MAIL_HOST`**: Use the SES SMTP endpoint for your region
- **`MAIL_USERNAME`**: The SMTP username from Step 1.2 (NOT your AWS Access Key)
- **`MAIL_PASSWORD`**: The SMTP password from Step 1.2 (NOT your AWS Secret Key)
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

- ✅ Use dedicated SMTP credentials (not root AWS credentials)
- ✅ Store credentials in environment variables (never in code)
- ✅ Use HTTPS for frontend URLs in production
- ✅ Configure SPF, DKIM, and DMARC records for your domain
- ✅ Request production access to exit SES sandbox mode

### Email Sending Limits

Amazon SES limits depend on your account status:

- **Sandbox Mode**: 200 emails/day, only to verified addresses
- **Production Mode**: Starts at 50,000 emails/day (can request increase)

### DNS Records for Best Deliverability

1. **SPF Record** - SES configures this automatically when you verify your domain

2. **DKIM** - Enable in SES Console → Verified identities → Your domain → DKIM

3. **DMARC** - Add this TXT record to your domain:
   ```
   _dmarc.northernchile.com  TXT  "v=DMARC1; p=quarantine; rua=mailto:admin@northernchile.com"
   ```

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
2. **Verify SMTP credentials** - Ensure you're using SES SMTP credentials (not AWS access keys)
3. **Check region** - Make sure MAIL_HOST matches the region where you set up SES
4. **Verify identity** - Domain or sender email must be verified in SES

### Emails Going to Spam

1. **Verify DKIM is enabled** in SES Console
2. **Check SPF record** is correctly configured
3. **Set up DMARC** policy for your domain
4. **Warm up the sender** - Start with small volumes and gradually increase

### Sandbox Mode Limitations

If you see "Email address not verified" errors:
1. You're still in sandbox mode
2. Request production access in SES Console
3. While waiting, verify recipient addresses in SES for testing

### Authentication Errors

If authentication fails:
1. Regenerate SMTP credentials in SES Console
2. Ensure you're using the SMTP username/password (not IAM credentials)
3. Check the SMTP endpoint matches your SES region

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

If Amazon SES doesn't meet your needs, the system supports any SMTP provider. Update configuration:

```properties
# Example: SendGrid
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=YOUR_SENDGRID_API_KEY

# Example: Google Workspace / Gmail
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=noreply@yourdomain.com
spring.mail.password=YOUR_APP_SPECIFIC_PASSWORD
```

---

## Support

For issues or questions:
- Check application logs: `docker logs northernchile-backend-1`
- Review Spring Boot Mail documentation: https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.email
- AWS SES documentation: https://docs.aws.amazon.com/ses/latest/dg/send-email-smtp.html
- Contact: dev@northernchile.com
