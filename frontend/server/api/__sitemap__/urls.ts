export default defineSitemapEventHandler(async () => {
  const apiBaseUrl = process.env.NUXT_PUBLIC_API_BASE_URL || 'http://localhost:8080'
  const siteUrl = process.env.NUXT_PUBLIC_BASE_URL || 'https://www.northernchile.com'
  const locales = ['es', 'en', 'pt']

  try {
    const tours = await $fetch<any[]>(`${apiBaseUrl}/api/tours/published`)

    // Generate URLs for each tour in each locale with proper hreflang alternates
    return tours.flatMap((tour) => {
      return locales.map((locale) => {
        const path = locale === 'es'
          ? `/tours/${tour.slug}`
          : `/${locale}/tours/${tour.slug}`

        // Generate alternate URLs for hreflang
        const alternatives = locales.map((altLocale) => {
          const altPath = altLocale === 'es'
            ? `/tours/${tour.slug}`
            : `/${altLocale}/tours/${tour.slug}`
          return {
            hreflang: altLocale === 'es' ? 'es-CL' : altLocale === 'en' ? 'en-US' : 'pt-BR',
            href: `${siteUrl}${altPath}`
          }
        })

        // Add x-default pointing to Spanish version
        alternatives.push({
          hreflang: 'x-default',
          href: `${siteUrl}/tours/${tour.slug}`
        })

        return {
          loc: path,
          changefreq: 'weekly' as const,
          priority: 0.8,
          lastmod: tour.updatedAt || new Date().toISOString(),
          alternatives
        }
      })
    })
  } catch (error) {
    console.error('Error fetching tours for sitemap:', error)
    return []
  }
})
