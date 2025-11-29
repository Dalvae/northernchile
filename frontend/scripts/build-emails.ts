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
      totalAmount: '${totalAmount}',
    },
  },
  // Add more templates here as needed
]

async function renderTemplate(template: string, props: Record<string, string>): Promise<string> {
  const response = await fetch(`${NUXT_API_URL}/emails/render`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ template, props }),
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
        '<html xmlns:th="http://www.thymeleaf.org"',
      )

      // Write to output
      const outputPath = join(outputDir, template.output)
      writeFileSync(outputPath, processedHtml, 'utf-8')

      console.log(`   ✅ Generated: ${outputPath}\n`)
    }
    catch (error: any) {
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
