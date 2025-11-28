import { defineStore } from 'pinia'
import type {
  PaymentInitReq,
  PaymentInitRes,
  PaymentStatusRes
} from '~/types/payment'

interface PaymentState {
  currentPayment: PaymentInitRes | null
  isProcessing: boolean
  error: string | null
  pollingInterval: ReturnType<typeof setInterval> | null
}

export const usePaymentStore = defineStore('payment', {
  state: (): PaymentState => ({
    currentPayment: null,
    isProcessing: false,
    error: null,
    pollingInterval: null
  }),

  getters: {
    hasActivePayment: state => state.currentPayment !== null,
    isPIX: state => state.currentPayment?.qrCode !== undefined,
    isTransbank: state => state.currentPayment?.paymentUrl !== undefined && !state.currentPayment?.qrCode,
    paymentExpired: (state) => {
      if (!state.currentPayment?.expiresAt) return false
      return new Date(state.currentPayment.expiresAt) < new Date()
    }
  },

  actions: {
    /**
     * Initialize a new payment
     */
    async initializePayment(request: PaymentInitReq): Promise<PaymentInitRes> {
      this.isProcessing = true
      this.error = null

      const config = useRuntimeConfig()
      const { extractErrorMessage, showErrorToast } = useApiError()

      try {
        const response = await $fetch<PaymentInitRes>(
          `${config.public.apiBase}/api/payments/init`,
          {
            method: 'POST',
            body: request,
            credentials: 'include',
            headers: {
              'Content-Type': 'application/json'
            }
          }
        )

        this.currentPayment = response
        return response
      } catch (error) {
        const errorMessage = extractErrorMessage(error)
        this.error = errorMessage
        showErrorToast(error)
        throw new Error(errorMessage)
      } finally {
        this.isProcessing = false
      }
    },

    /**
     * Get payment status
     */
    async getPaymentStatus(paymentId: string): Promise<PaymentStatusRes> {
      const config = useRuntimeConfig()
      const { extractErrorMessage } = useApiError()

      try {
        const response = await $fetch<PaymentStatusRes>(
          `${config.public.apiBase}/api/payments/${paymentId}/status`,
          {
            method: 'GET',
            credentials: 'include'
          }
        )

        // Update current payment status if it matches
        if (this.currentPayment?.paymentId === paymentId) {
          this.currentPayment = {
            ...this.currentPayment,
            status: response.status
          }
        }

        return response
      } catch (error) {
        const errorMessage = extractErrorMessage(error)
        this.error = errorMessage
        throw new Error(errorMessage)
      }
    },

    /**
     * Start polling for payment status (for PIX)
     */
    startPolling(paymentId: string, intervalMs: number = 5000) {
      // Clear any existing polling
      this.stopPolling()

      this.pollingInterval = setInterval(async () => {
        try {
          const status = await this.getPaymentStatus(paymentId)

          // Stop polling if payment is complete or failed
          if (['COMPLETED', 'FAILED', 'CANCELLED', 'EXPIRED'].includes(status.status)) {
            this.stopPolling()
          }
        } catch (error) {
          console.error('Error polling payment status:', error)
        }
      }, intervalMs)
    },

    /**
     * Stop polling for payment status
     */
    stopPolling() {
      if (this.pollingInterval) {
        clearInterval(this.pollingInterval)
        this.pollingInterval = null
      }
    },

    /**
     * Confirm payment after redirect (for Transbank)
     */
    async confirmPayment(token: string): Promise<PaymentStatusRes> {
      const config = useRuntimeConfig()
      const { extractErrorMessage, showErrorToast } = useApiError()

      try {
        const response = await $fetch<PaymentStatusRes>(
          `${config.public.apiBase}/api/payments/confirm`,
          {
            method: 'GET',
            query: { token_ws: token }
          }
        )

        return response
      } catch (error) {
        const errorMessage = extractErrorMessage(error)
        this.error = errorMessage
        showErrorToast(error)
        throw new Error(errorMessage)
      }
    },

    /**
     * Clear current payment state
     */
    clearPayment() {
      this.stopPolling()
      this.currentPayment = null
      this.error = null
      this.isProcessing = false
    }
  }
})
