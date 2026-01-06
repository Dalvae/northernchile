export default defineSitemapEventHandler(async () => {
  const apiBaseUrl = process.env.NUXT_PUBLIC_API_BASE_URL || 'http://localhost:8080'
  const siteUrl = process.env.NUXT_PUBLIC_BASE_URL || 'https://www.northernchile.com'
  const locales = ['es', 'en', 'pt']

  try {
    const tours = await $fetch<Array<{ slug: string, updatedAt?: string }>>(`${apiBaseUrl}/api/tours`)

    const staticPages = [
      '/',
      '/about',
      '/contact',
      '/tours',
      '/private-tours',
      '/moon-calendar'
    ]

    const staticUrls = staticPages.flatMap((page) => {
      return locales.map((locale) => {
        const path = locale === 'es'
          ? page
          : `/${locale}${page === '/' ? '' : page}`

        const alternatives = locales.map((altLocale) => {
          const altPath = altLocale === 'es'
            ? page
            : `/${altLocale}${page === '/' ? '' : page}`
          return {
            hreflang: altLocale === 'es' ? 'es-CL' : altLocale === 'en' ? 'en-US' : 'pt-BR',
            href: `${siteUrl}${altPath}`
          }
        })

        alternatives.push({
          hreflang: 'x-default',
          href: `${siteUrl}${page}`
        })

        return {
          loc: path,
          changefreq: 'weekly' as const,
          priority: (page === '/' ? 1.0 : 0.8) as 0.8 | 1.0,
          lastmod: new Date().toISOString(),
          alternatives
        }
      })
    })

    const tourUrls = tours.flatMap((tour) => {
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
          priority: 0.8 as const,
          lastmod: tour.updatedAt || new Date().toISOString(),
          alternatives
        }
      })
    })

    return [...staticUrls, ...tourUrls]
  } catch (error) {
    console.error('Error fetching tours for sitemap:', error)
    return []
  }
})
