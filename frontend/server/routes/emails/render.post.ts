import { templateRender } from '@vue-email/compiler'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'

export default defineEventHandler(async (event) => {
  const body = await readBody(event)
  const { template, props = {} } = body

  // Map template names to file paths
  const templates: Record<string, string> = {
    'booking-confirmation': 'app/emails/BookingConfirmation.vue',
  }

  const templatePath = templates[template]

  if (!templatePath) {
    throw createError({
      statusCode: 404,
      message: `Template "${template}" not found`,
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
        components: [], // No external components for now
      },
      {
        props,
      },
    )

    return {
      success: true,
      html: result.html,
    }
  }
  catch (error: any) {
    throw createError({
      statusCode: 500,
      message: `Failed to render template: ${error.message}`,
    })
  }
})
