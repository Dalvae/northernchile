import { defineStore } from 'pinia'
import type { TourRes } from 'api-client'
import logger from '~/utils/logger'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    tours: [] as TourRes[],
    loading: false,
    initialized: false,
    lastFetched: 0,
    fetchPromise: null as Promise<TourRes[]> | null
  }),

  getters: {
    getTourBySlug: state => (slug: string) => {
      return state.tours.find(t => t.slug === slug)
    },
    getTourById: state => (id: string) => {
      return state.tours.find(t => t.id === id)
    }
  },

  actions: {
    async fetchTours(force = false) {
      // If already fetching, return the existing promise
      if (this.fetchPromise) {
        return this.fetchPromise
      }

      // Cache for 5 minutes unless forced
      const now = Date.now()
      if (this.initialized && !force && (now - this.lastFetched < 5 * 60 * 1000)) {
        return this.tours
      }

      const { fetchAdminTours } = useAdminData()
      this.loading = true

      this.fetchPromise = (async () => {
        try {
          const data = await fetchAdminTours()
          this.tours = data
          this.initialized = true
          this.lastFetched = Date.now()
          return data
        } catch (error) {
          logger.error('[AdminStore] Error fetching tours:', error)
          throw error
        } finally {
          this.loading = false
          this.fetchPromise = null
        }
      })()

      return this.fetchPromise
    },

    async refreshTours() {
      return this.fetchTours(true)
    }
  }
})
