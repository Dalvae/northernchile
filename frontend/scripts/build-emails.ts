import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'
import { writeFileSync, mkdirSync } from 'node:fs'

const __dirname = dirname(fileURLToPath(import.meta.url))
const outputDir = join(__dirname, '../../backend/src/main/resources/templates/email')
const NUXT_API_URL = process.env.NUXT_DEV_URL || 'http://localhost:3001'

// Ensure output directory exists
mkdirSync(outputDir, { recursive: true })

interface EmailTemplate {
  name: string
  template: string
  output: string
  props: Record<string, string>
}

const templates: EmailTemplate[] = [
  {
    name: 'Admin Contact',
    template: 'admin-contact',
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
    template: 'admin-new-booking',
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
    template: 'admin-private-request',
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
    template: 'booking-cancelled',
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
    template: 'booking-confirmation',
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
    template: 'password-reset',
    output: 'password-reset.html',
    props: {
      userName: '${userName}',
      resetUrl: '${resetUrl}'
    }
  },
  {
    name: 'Pickup Reminder',
    template: 'pickup-reminder',
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
    template: 'private-tour-quote',
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
    template: 'refund-confirmation',
    output: 'refund-confirmation.html',
    props: {
      customerName: '${customerName}',
      refundAmount: '${refundAmount}',
      paymentMethod: '${paymentMethod}'
    }
  },
  {
    name: 'Tour Manifest',
    template: 'tour-manifest',
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
    template: 'tour-reminder',
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
    template: 'verification',
    output: 'verification.html',
    props: {
      userName: '${userName}',
      verificationUrl: '${verificationUrl}'
    }
  }
]

async function renderTemplate(template: string, props: Record<string, string>): Promise<string> {
  const response = await fetch(`${NUXT_API_URL}/emails/render`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ template, props })
  })

  if (!response.ok) {
    const error = await response.text()
    throw new Error(`Failed to render template: ${error}`)
  }

  const data = await response.json()
  return data.html
}

async function buildEmails() {
  console.log('🚀 Building email templates...\n')
  console.log(`📡 Using Nuxt dev server at: ${NUXT_API_URL}\n`)

  for (const template of templates) {
    try {
      console.log(`📧 Processing ${template.name}...`)

      // Render via Nuxt API endpoint
      const html = await renderTemplate(template.template, template.props)

      // Post-process the HTML to add Thymeleaf namespace
      let processedHtml = html

      // Add Thymeleaf namespace to html tag
      processedHtml = processedHtml.replace(
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
