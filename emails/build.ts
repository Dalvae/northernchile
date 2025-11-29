import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'
import { writeFileSync, readFileSync, mkdirSync } from 'node:fs'
import { templateRender } from '@vue-email/compiler'

const __dirname = dirname(fileURLToPath(import.meta.url))
const outputDir = join(__dirname, '../backend/src/main/resources/templates/email')

// Ensure output directory exists
mkdirSync(outputDir, { recursive: true })

interface EmailTemplate {
  name: string
  file: string
  output: string
  props: Record<string, string>
}

const templates: EmailTemplate[] = [
  {
    name: 'Admin Contact',
    file: 'AdminContact.vue',
    output: 'admin-contact.html',
    props: {
      name: '${name}',
      email: '${email}',
      phone: '${phone}',
      message: '${message}',
      createdAt: '${createdAt}',
      messageId: '${messageId}'
    }
  },
  {
    name: 'Admin New Booking',
    file: 'AdminNewBooking.vue',
    output: 'admin-new-booking.html',
    props: {
      bookingId: '${bookingId}',
      status: '${status}',
      tourName: '${tourName}',
      tourDate: '${tourDate}',
      customerName: '${customerName}',
      customerEmail: '${customerEmail}',
      participantCount: '${participantCount}',
      subtotal: '${subtotal}',
      taxAmount: '${taxAmount}',
      totalAmount: '${totalAmount}',
      specialRequests: '${specialRequests}',
      createdAt: '${createdAt}'
    }
  },
  {
    name: 'Admin Private Request',
    file: 'AdminPrivateRequest.vue',
    output: 'admin-private-request.html',
    props: {
      requestId: '${requestId}',
      status: '${status}',
      tourType: '${tourType}',
      requestedDate: '${requestedDate}',
      participantCount: '${participantCount}',
      customerName: '${customerName}',
      customerEmail: '${customerEmail}',
      customerPhone: '${customerPhone}',
      specialRequests: '${specialRequests}',
      createdAt: '${createdAt}'
    }
  },
  {
    name: 'Booking Cancelled',
    file: 'BookingCancelled.vue',
    output: 'booking-cancelled.html',
    props: {
      customerName: '${customerName}',
      bookingId: '${bookingId}',
      tourName: '${tourName}',
      tourDate: '${tourDate}',
      reason: '${reason}',
      refundAmount: '${refundAmount}'
    }
  },
  {
    name: 'Booking Confirmation',
    file: 'BookingConfirmation.vue',
    output: 'booking-confirmation.html',
    props: {
      customerName: '${customerName}',
      bookingId: '${bookingId}',
      tourName: '${tourName}',
      tourDate: '${tourDate}',
      tourTime: '${tourTime}',
      participantCount: '${participantCount}',
      totalAmount: '${totalAmount}'
    }
  },
  {
    name: 'Password Reset',
    file: 'PasswordReset.vue',
    output: 'password-reset.html',
    props: {
      userName: '${userName}',
      resetUrl: '${resetUrl}'
    }
  },
  {
    name: 'Pickup Reminder',
    file: 'PickupReminder.vue',
    output: 'pickup-reminder.html',
    props: {
      customerName: '${customerName}',
      tourName: '${tourName}',
      tourDate: '${tourDate}',
      tourTime: '${tourTime}',
      pickupLocation: '${pickupLocation}',
      equipmentList: '${equipmentList}',
      emergencyContact: '${emergencyContact}'
    }
  },
  {
    name: 'Private Tour Quote',
    file: 'PrivateTourQuote.vue',
    output: 'private-tour-quote.html',
    props: {
      customerName: '${customerName}',
      tourType: '${tourType}',
      price: '${price}',
      requests: '${requests}',
      notes: '${notes}'
    }
  },
  {
    name: 'Refund Confirmation',
    file: 'RefundConfirmation.vue',
    output: 'refund-confirmation.html',
    props: {
      customerName: '${customerName}',
      refundAmount: '${refundAmount}',
      paymentMethod: '${paymentMethod}'
    }
  },
  {
    name: 'Tour Manifest',
    file: 'TourManifest.vue',
    output: 'manifest.html',
    props: {
      tourName: '${tourName}',
      tourDate: '${tourDate}',
      tourTime: '${tourTime}',
      guideName: '${guideName}',
      totalParticipants: '${totalParticipants}',
      participants: '${participants}'
    }
  },
  {
    name: 'Tour Reminder',
    file: 'TourReminder.vue',
    output: 'tour-reminder.html',
    props: {
      customerName: '${customerName}',
      bookingId: '${bookingId}',
      tourName: '${tourName}',
      tourDate: '${tourDate}',
      tourTime: '${tourTime}'
    }
  },
  {
    name: 'Verification',
    file: 'Verification.vue',
    output: 'verification.html',
    props: {
      userName: '${userName}',
      verificationUrl: '${verificationUrl}'
    }
  }
]

async function buildEmails() {
  console.log('🚀 Building email templates...\n')

  for (const template of templates) {
    try {
      console.log(`📧 Processing ${template.name}...`)

      // Read the Vue template
      const templatePath = join(__dirname, template.file)
      const source = readFileSync(templatePath, 'utf-8')

      // Render using vue-email compiler
      const result = await templateRender(
        template.file,
        {
          source,
          components: []
        },
        {
          props: template.props
        }
      )

      // Add Thymeleaf namespace to html tag
      let processedHtml = result.html.replace(
        /<html/,
        '<html xmlns:th="http://www.thymeleaf.org"'
      )

      // Write to output
      const outputPath = join(outputDir, template.output)
      writeFileSync(outputPath, processedHtml, 'utf-8')

      console.log(`   ✅ Generated: ${outputPath}\n`)
    } catch (error: any) {
      console.error(`   ❌ Failed to build ${template.name}:`, error.message)
      process.exit(1)
    }
  }

  console.log('✨ All email templates built successfully!')
  console.log(`📂 Output directory: ${outputDir}`)
}

buildEmails().catch((error) => {
  console.error('Failed to build emails:', error)
  process.exit(1)
})
