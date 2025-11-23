import type { TourRes } from 'api-client'

export default defineEventHandler<TourRes>(async (event) => {
  const config = useRuntimeConfig()
  const slugParam = event.context.params?.slug

  if (!slugParam || typeof slugParam !== 'string') {
    throw createError({ statusCode: 400, statusMessage: 'Missing slug' })
  }

  const apiBase = config.public.apiBase || 'http://localhost:8080'

  try {
    return await $fetch<TourRes>(
      `${apiBase}/api/tours/slug/${encodeURIComponent(slugParam)}`
    )
  } catch (error: any) {
    if (error?.status === 404) {
      throw createError({ statusCode: 404, statusMessage: 'Tour not found' })
    }

    throw createError({
      statusCode: 500,
      statusMessage: 'Failed to fetch tour'
    })
  }
})
