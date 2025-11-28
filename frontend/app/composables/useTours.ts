import type { TourRes } from 'api-client'

export const useTours = () => {
  /**
   * Obtiene la lista de todos los tours publicados.
   * Llama a nuestro propio endpoint de Nuxt: /api/tours
   */
  const fetchAll = () => {
    // useFetch ya sabe que tiene que llamar a nuestro servidor Nuxt.
    // La clave 'tours' es para caching y refresco de datos.
    return useFetch<TourRes[]>('/api/tours', { key: 'public-tours' })
  }

  /**
   * Obtiene un tour específico por su ID.
   * @param tourId El UUID del tour.
   */
  const fetchById = (tourId: string) => {
    // Llama a nuestro endpoint dinámico: /api/tours/{id}
    return useFetch<TourRes>(`/api/tours/${tourId}`, { key: `public-tour-${tourId}` })
  }

  /**
   * Obtiene un tour específico por su slug.
   * @param slug El slug del tour.
   */
  const fetchBySlug = (slug: string) => {
    return useFetch<TourRes>(`/api/tours/slug/${slug}`, { key: `public-tour-slug-${slug}` })
  }

  /**
   * Invalida el caché de tours públicos.
   * Llamar después de actualizar tours desde el admin.
   */
  const invalidateCache = async () => {
    await clearNuxtData('public-tours')
    // También limpiar tours individuales
    const nuxtApp = useNuxtApp()
    const keys = Object.keys(nuxtApp.payload.data || {})
    for (const key of keys) {
      if (key.startsWith('public-tour-')) {
        await clearNuxtData(key)
      }
    }
  }

  return {
    fetchAll,
    fetchById,
    fetchBySlug,
    invalidateCache
  }
}
