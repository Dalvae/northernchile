import { templateRender } from '@vue-email/compiler'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'

export default defineEventHandler(async (event) => {
  const body = await readBody(event)
  const { template, props = {} } = body

  // Map template names to file paths
  const templates: Record<string, string> = {
    'admin-contact': 'app/emails/AdminContact.vue',
    'admin-new-booking': 'app/emails/AdminNewBooking.vue',
    'admin-private-request': 'app/emails/AdminPrivateRequest.vue',
    'booking-cancelled': 'app/emails/BookingCancelled.vue',
    'booking-confirmation': 'app/emails/BookingConfirmation.vue',
    'password-reset': 'app/emails/PasswordReset.vue',
    'pickup-reminder': 'app/emails/PickupReminder.vue',
    'private-tour-quote': 'app/emails/PrivateTourQuote.vue',
    'refund-confirmation': 'app/emails/RefundConfirmation.vue',
    'tour-manifest': 'app/emails/TourManifest.vue',
    'tour-reminder': 'app/emails/TourReminder.vue',
    'verification': 'app/emails/Verification.vue'
  }

  const templatePath = templates[template]

  if (!templatePath) {
    throw createError({
      statusCode: 404,
      message: `Template "${template}" not found`
    })
  }

  try {
    // Read the .vue file as source code
    const fullPath = join(process.cwd(), templatePath)
    const source = readFileSync(fullPath, 'utf-8')

    // Render using vue-email compiler
    const result = await templateRender(
      template,
      {
        source,
        components: [] // No external components for now
      },
      {
        props
      }
    )

    return {
      success: true,
      html: result.html
    }
  } catch (error: any) {
    throw createError({
      statusCode: 500,
      message: `Failed to render template: ${error.message}`
    })
  }
})
