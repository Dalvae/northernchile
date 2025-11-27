export default defineSitemapEventHandler(async () => {
  const apiBaseUrl = process.env.NUXT_PUBLIC_API_BASE_URL || 'http://localhost:8080'

  try {
    const tours = await $fetch<any[]>(`${apiBaseUrl}/api/tours/published`)
    const locales = ['es', 'en', 'pt']

    return tours.flatMap((tour) => {
      return locales.map((locale) => {
        const path = locale === 'es'
          ? `/tours/${tour.slug}`
          : `/${locale}/tours/${tour.slug}`

        return {
          loc: path,
          changefreq: 'weekly' as const,
          priority: 0.8,
          lastmod: tour.updatedAt || new Date().toISOString()
        }
      })
    })
  } catch (error) {
    console.error('Error fetching tours for sitemap:', error)
    return []
  }
})
