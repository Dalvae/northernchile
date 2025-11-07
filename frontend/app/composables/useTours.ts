import type { TourRes } from '~/lib/api-client'

export const useTours = () => {
  /**
   * Obtiene la lista de todos los tours publicados.
   * Llama a nuestro propio endpoint de Nuxt: /api/tours
   */
  const fetchAll = () => {
    // useFetch ya sabe que tiene que llamar a nuestro servidor Nuxt.
    // La clave 'tours' es para caching y refresco de datos.
    return useFetch<TourRes[]>('/api/tours')
  }

  /**
   * Obtiene un tour específico por su ID.
   * @param tourId El UUID del tour.
   */
  const fetchById = (tourId: string) => {
    // Llama a nuestro endpoint dinámico: /api/tours/{id}
    return useFetch<TourRes>(`/api/tours/${tourId}`)
  }

  return {
    fetchAll,
    fetchById
  }
}
