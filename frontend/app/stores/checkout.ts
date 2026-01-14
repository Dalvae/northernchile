import { defineStore } from 'pinia'
import type { PaymentProvider, PaymentMethod } from '~/types/payment'

export interface CheckoutParticipant {
  fullName: string
  documentId: string
  nationality: string
  dateOfBirth: string | null
  pickupAddress: string
  specialRequirements: string
  phoneCountryCode: string
  phoneNumber: string
  email: string
}

export interface CheckoutContactForm {
  email: string
  fullName: string
  phone: string
  countryCode: string
  password: string
  confirmPassword: string
}

const CHECKOUT_CONTACT_KEY = 'checkout_contact'
const CHECKOUT_PARTICIPANTS_KEY = 'checkout_participants'
const CHECKOUT_STEP_KEY = 'checkout_step'

/**
 * Checkout store - manages checkout wizard state with localStorage persistence.
 * Consolidates state that was previously scattered across the checkout page.
 */
export const useCheckoutStore = defineStore('checkout', () => {
  // === STATE ===

  const currentStep = ref(1)
  const totalSteps = 3

  const contactForm = ref<CheckoutContactForm>({
    email: '',
    fullName: '',
    phone: '',
    countryCode: '+56',
    password: '',
    confirmPassword: ''
  })

  const participants = ref<CheckoutParticipant[]>([])

  const selectedPaymentMethod = ref<{ provider: PaymentProvider, method: PaymentMethod } | null>(null)

  const isSubmitting = ref(false)
  const lastSubmitTime = ref(0)
  const isLoaded = ref(false)

  // === COMPUTED ===

  const isStep1Valid = computed(() => {
    const authStore = useAuthStore()
    const baseValidation =
      contactForm.value.email &&
      contactForm.value.fullName &&
      contactForm.value.phone.length >= 6

    if (authStore.isAuthenticated) {
      return baseValidation
    }

    const passwordValidation =
      contactForm.value.password.length >= 8 &&
      contactForm.value.password === contactForm.value.confirmPassword

    return baseValidation && passwordValidation
  })

  const isStep2Valid = computed(() => {
    return participants.value.every(
      p => p.fullName && p.documentId && p.nationality
    )
  })

  const canSubmit = computed(() => {
    return !isSubmitting.value && selectedPaymentMethod.value !== null
  })

  // === ACTIONS ===

  /**
   * Load checkout data from localStorage
   */
  function loadFromStorage() {
    if (!import.meta.client || isLoaded.value) return

    try {
      // Load contact form (except passwords)
      const savedContact = localStorage.getItem(CHECKOUT_CONTACT_KEY)
      if (savedContact) {
        const parsed = JSON.parse(savedContact)
        contactForm.value = {
          ...contactForm.value,
          email: parsed.email || contactForm.value.email,
          fullName: parsed.fullName || contactForm.value.fullName,
          phone: parsed.phone || '',
          countryCode: parsed.countryCode || '+56',
          password: '',
          confirmPassword: ''
        }
      }

      // Load participants
      const savedParticipants = localStorage.getItem(CHECKOUT_PARTICIPANTS_KEY)
      if (savedParticipants) {
        participants.value = JSON.parse(savedParticipants)
      }

      // Load step
      const savedStep = localStorage.getItem(CHECKOUT_STEP_KEY)
      if (savedStep) {
        const step = parseInt(savedStep, 10)
        if (step >= 1 && step <= totalSteps) {
          currentStep.value = step
        }
      }

      isLoaded.value = true
    } catch (e) {
      console.error('Error loading checkout data from localStorage:', e)
      isLoaded.value = true
    }
  }

  /**
   * Save contact form to localStorage
   */
  function saveContactForm() {
    if (!import.meta.client) return

    const toSave = {
      email: contactForm.value.email,
      fullName: contactForm.value.fullName,
      phone: contactForm.value.phone,
      countryCode: contactForm.value.countryCode
    }
    localStorage.setItem(CHECKOUT_CONTACT_KEY, JSON.stringify(toSave))
  }

  /**
   * Save participants to localStorage
   */
  function saveParticipants() {
    if (!import.meta.client || participants.value.length === 0) return
    localStorage.setItem(CHECKOUT_PARTICIPANTS_KEY, JSON.stringify(participants.value))
  }

  /**
   * Save current step to localStorage
   */
  function saveStep() {
    if (!import.meta.client) return
    localStorage.setItem(CHECKOUT_STEP_KEY, String(currentStep.value))
  }

  /**
   * Clear all checkout data from localStorage
   */
  function clearStorage() {
    if (!import.meta.client) return
    localStorage.removeItem(CHECKOUT_CONTACT_KEY)
    localStorage.removeItem(CHECKOUT_PARTICIPANTS_KEY)
    localStorage.removeItem(CHECKOUT_STEP_KEY)
  }

  /**
   * Initialize participants based on cart total
   */
  function initializeParticipants(totalParticipants: number) {
    if (participants.value.length === totalParticipants) {
      return
    }

    participants.value = Array.from(
      { length: totalParticipants },
      (_, i) => ({
        fullName: i === 0 ? contactForm.value.fullName : '',
        documentId: '',
        nationality: 'CL',
        dateOfBirth: null,
        pickupAddress: '',
        specialRequirements: '',
        phoneCountryCode: contactForm.value.countryCode,
        phoneNumber: i === 0 ? contactForm.value.phone : '',
        email: i === 0 ? contactForm.value.email : ''
      })
    )
  }

  /**
   * Update a specific participant
   */
  function updateParticipant(index: number, data: Partial<CheckoutParticipant>) {
    const current = participants.value[index]
    if (!current) return

    participants.value[index] = {
      ...current,
      ...data
    }
    saveParticipants()
  }

  /**
   * Copy pickup info from first participant to another
   */
  function copyFromFirstParticipant(index: number) {
    const first = participants.value[0]
    if (!first || index === 0) return

    const current = participants.value[index]
    if (!current) return

    participants.value[index] = {
      ...current,
      pickupAddress: first.pickupAddress,
      specialRequirements: first.specialRequirements,
      phoneCountryCode: first.phoneCountryCode
    }
    saveParticipants()
  }

  /**
   * Navigate to next step
   */
  function nextStep(totalParticipants?: number) {
    if (currentStep.value === 1 && totalParticipants) {
      initializeParticipants(totalParticipants)
    }

    if (currentStep.value < totalSteps) {
      currentStep.value++
      saveStep()
    }
  }

  /**
   * Navigate to previous step
   */
  function prevStep() {
    if (currentStep.value > 1) {
      currentStep.value--
      saveStep()
    }
  }

  /**
   * Check if rapid double-submit
   */
  function canSubmitNow(): boolean {
    const now = Date.now()
    if (now - lastSubmitTime.value < 3000) {
      console.warn('Submit too fast, ignoring')
      return false
    }
    lastSubmitTime.value = now
    return true
  }

  /**
   * Reset all checkout state
   */
  function resetState() {
    currentStep.value = 1
    contactForm.value = {
      email: '',
      fullName: '',
      phone: '',
      countryCode: '+56',
      password: '',
      confirmPassword: ''
    }
    participants.value = []
    selectedPaymentMethod.value = null
    isSubmitting.value = false
    lastSubmitTime.value = 0
    isLoaded.value = false
    clearStorage()
  }

  /**
   * Initialize with user data if authenticated
   */
  function initWithUser(user: { email?: string, fullName?: string } | null) {
    if (user) {
      contactForm.value.email = user.email || contactForm.value.email
      contactForm.value.fullName = user.fullName || contactForm.value.fullName
    }
  }

  return {
    // State
    currentStep,
    totalSteps,
    contactForm,
    participants,
    selectedPaymentMethod,
    isSubmitting,
    isLoaded,

    // Computed
    isStep1Valid,
    isStep2Valid,
    canSubmit,

    // Actions
    loadFromStorage,
    saveContactForm,
    saveParticipants,
    saveStep,
    clearStorage,
    initializeParticipants,
    updateParticipant,
    copyFromFirstParticipant,
    nextStep,
    prevStep,
    canSubmitNow,
    resetState,
    initWithUser
  }
})
