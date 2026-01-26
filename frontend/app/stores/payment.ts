import { defineStore } from 'pinia'
import {
  type PaymentSessionRes,
  PaymentStatus
} from '~/types/payment'
import logger from '~/utils/logger'

interface PaymentState {
  currentPayment: PaymentSessionRes | null
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
     * Get payment session status
     */
    async getSessionStatus(sessionId: string): Promise<PaymentSessionRes> {
      const { extractErrorMessage } = useApiError()

      try {
        const response = await $fetch<PaymentSessionRes>(
          `/api/payment-sessions/${sessionId}/status`,
          {
            credentials: 'include'
          }
        )

        // Update current payment status if it matches
        if (this.currentPayment?.sessionId === sessionId) {
          this.currentPayment = {
            ...this.currentPayment,
            status: response.status || PaymentStatus.Pending
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
    startPolling(sessionId: string, intervalMs: number = 5000) {
      // Clear any existing polling
      this.stopPolling()

      this.pollingInterval = setInterval(async () => {
        try {
          const status = await this.getSessionStatus(sessionId)

          // Stop polling if payment is complete or failed
          const terminalStatuses: PaymentStatus[] = [
            PaymentStatus.Completed,
            PaymentStatus.Failed,
            PaymentStatus.Cancelled,
            PaymentStatus.Expired
          ]

          if (status.status && terminalStatuses.includes(status.status)) {
            this.stopPolling()
          }
        } catch (error) {
          logger.error('Error polling payment status:', error)
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
    setCurrentPayment(payment: Partial<PaymentSessionRes>) {
      // Merge with existing state to preserve previously set fields
      this.currentPayment = {
        ...this.currentPayment,
        ...payment
      } as PaymentSessionRes
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
