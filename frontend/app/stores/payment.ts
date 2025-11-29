import { defineStore } from 'pinia'
import type {
  PaymentInitRes,
  PaymentStatusRes
} from '~/types/payment'

interface PaymentState {
  currentPayment: PaymentInitRes | null
  isProcessing: boolean
  error: string | null
  pollingInterval: ReturnType<typeof setInterval> | null
}

/**
 * Payment store for managing payment UI state.
 * Note: Actual payment initialization uses PaymentSessionService via checkout.vue.
 * This store manages UI state and polling for PIX payments.
 */
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
     * Get payment status
     */
    async getPaymentStatus(paymentId: string): Promise<PaymentStatusRes> {
      const { extractErrorMessage } = useApiError()

      try {
        const response = await $fetch<PaymentStatusRes>(
          `/api/payments/${paymentId}/status`,
          {
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
     * Set current payment state (for PaymentSession flow)
     */
    setCurrentPayment(payment: Partial<PaymentInitRes>) {
      this.currentPayment = payment as PaymentInitRes
    },

    /**
     * Set processing state
     */
    setProcessing(value: boolean) {
      this.isProcessing = value
    },

    /**
     * Set error message
     */
    setError(error: string | null) {
      this.error = error
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
