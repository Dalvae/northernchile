<script setup lang="ts">
import type { BookingRes } from '~/lib/api-client/api'

const { locale, t } = useI18n()
const authStore = useAuthStore()
const config = useRuntimeConfig()
const toast = useToast()
const localePath = useLocalePath()
const { getCountryLabel, getCountryFlag } = useCountries()
const { formatPrice } = useCurrency()
const { generateBookingPdf } = useBookingPdf()

// Load bookings from backend
const bookings = ref<BookingRes[]>([])
const loading = ref(true)
const editingBooking = ref<BookingRes | null>(null)
const downloadingPdf = ref<string | null>(null)

async function fetchBookings() {
  if (!authStore.isAuthenticated) {
    return
  }

  loading.value = true
  try {
    const response = await $fetch<BookingRes[]>(`${config.public.apiBase}/api/bookings`, {
      credentials: 'include'
    })
    bookings.value = response
  } catch (error: unknown) {
    console.error('Error fetching bookings:', error)
    toast.add({
      color: 'error',
      title: t('common.error'),
      description: t('profile.booking_load_error')
    })
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchBookings()
})

// Format date and time
function formatDateTime(dateString: string, timeString: string) {
  if (!dateString) return '-'

  // Parse date
  const date = new Date(dateString)
  const dateFormatted = new Intl.DateTimeFormat(locale.value, {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  }).format(date)

  // If we have time, append it
  if (timeString) {
    return `${dateFormatted} - ${timeString}`
  }

  return dateFormatted
}

// Badge color type
type BadgeColor = 'error' | 'info' | 'success' | 'primary' | 'secondary' | 'tertiary' | 'warning' | 'neutral'

// Status colors
function getStatusColor(status: string): BadgeColor {
  const colors: Record<string, BadgeColor> = {
    CONFIRMED: 'success',
    PENDING: 'warning',
    CANCELLED: 'error',
    COMPLETED: 'info'
  }
  return colors[status] || 'neutral'
}

// Status labels
function getStatusLabel(status: string) {
  return t(`booking.status.${status}`)
}

function downloadBooking(booking: BookingRes) {
  downloadingPdf.value = booking.id
  try {
    generateBookingPdf(booking)
    toast.add({
      color: 'success',
      title: t('profile.pdf_download_success'),
      description: t('profile.pdf_download_success_description')
    })
  } catch (error) {
    console.error('Error generating PDF:', error)
    toast.add({
      color: 'error',
      title: t('profile.pdf_download_error'),
      description: t('profile.pdf_download_error_description')
    })
  } finally {
    downloadingPdf.value = null
  }
}

async function cancelBooking(bookingId: string) {
  if (!confirm(t('profile.cancel_booking_confirm'))) {
    return
  }

  loading.value = true
  try {
    await $fetch(`${config.public.apiBase}/api/bookings/${bookingId}`, {
      method: 'DELETE',
      credentials: 'include'
    })

    // Refresh bookings list
    await fetchBookings()

    toast.add({
      color: 'success',
      title: t('profile.cancel_booking'),
      description: t('profile.booking_cancelled_success')
    })
  } catch (error: unknown) {
    console.error('Error cancelling booking:', error)
    toast.add({
      color: 'error',
      title: t('common.error'),
      description: t('profile.booking_cancel_error')
    })
  } finally {
    loading.value = false
  }
}

function openEditModal(booking: BookingRes) {
  editingBooking.value = booking
}

function closeEditModal() {
  editingBooking.value = null
}

async function handleBookingSaved() {
  await fetchBookings()
}
</script>

<template>
  <div>
    <!-- Loading State -->
    <div
      v-if="loading"
      class="flex justify-center py-12"
    >
      <UIcon
        name="i-lucide-loader-2"
        class="w-8 h-8 animate-spin text-primary"
      />
    </div>

    <!-- Empty State -->
    <UCard v-else-if="!bookings || bookings.length === 0">
      <div class="text-center py-12">
        <div
          class="w-20 h-20 bg-neutral-100 dark:bg-neutral-800 rounded-full flex items-center justify-center mx-auto mb-4"
        >
          <UIcon
            name="i-lucide-calendar-x"
            class="w-12 h-12 text-neutral-300"
          />
        </div>
        <h3 class="text-xl font-semibold text-neutral-900 dark:text-white mb-2">
          {{ t('profile.no_bookings') }}
        </h3>
        <p class="text-neutral-600 dark:text-neutral-300 mb-6">
          {{ t('profile.no_bookings_description') }}
        </p>
        <UButton
          color="primary"
          size="lg"
          icon="i-lucide-telescope"
          :to="localePath('/tours')"
        >
          {{ t('profile.browse_tours') }}
        </UButton>
      </div>
    </UCard>

    <!-- Bookings List -->
    <div
      v-else
      class="space-y-6"
    >
      <UCard
        v-for="booking in bookings"
        :key="booking.id"
      >
        <template #header>
          <div class="flex justify-between items-start">
            <div>
              <p class="text-sm text-neutral-500 dark:text-neutral-300">
                {{ t('profile.booking_reference') }} #{{ booking.id.substring(0, 8) }}
              </p>
              <p
                class="text-lg font-semibold text-neutral-900 dark:text-white mt-1"
              >
                {{ booking.tourName }}
              </p>
            </div>
            <UBadge
              :color="getStatusColor(booking.status)"
              size="lg"
            >
              {{ getStatusLabel(booking.status) }}
            </UBadge>
          </div>
        </template>

        <div class="space-y-4">
          <!-- Tour Info -->
          <div class="p-3 bg-neutral-50 dark:bg-neutral-800 rounded-lg">
            <div class="space-y-2 text-sm text-neutral-600 dark:text-neutral-300">
              <p class="flex items-center gap-2">
                <UIcon
                  name="i-lucide-calendar"
                  class="w-4 h-4"
                />
                {{ formatDateTime(booking.tourDate, booking.tourStartTime) }}
              </p>
              <p class="flex items-center gap-2">
                <UIcon
                  name="i-lucide-users"
                  class="w-4 h-4"
                />
                {{ booking.participants?.length || 0 }}
                {{ booking.participants?.length === 1 ? t('profile.participant') : t('profile.participants') }}
              </p>
            </div>
          </div>

          <!-- Participants -->
          <div v-if="booking.participants && booking.participants.length > 0">
            <h4
              class="text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-2"
            >
              {{ t('booking.participant_details') }}
            </h4>
            <div class="space-y-3">
              <div
                v-for="participant in booking.participants"
                :key="participant.id"
                class="p-3 bg-neutral-100/50 dark:bg-neutral-900/50 rounded-lg"
              >
                <div class="flex items-start gap-2 text-sm">
                  <UIcon
                    name="i-lucide-user"
                    class="w-4 h-4 mt-0.5 flex-shrink-0 text-neutral-500 dark:text-neutral-300"
                  />
                  <div class="flex-1">
                    <p class="font-medium text-neutral-900 dark:text-white">
                      {{ participant.fullName }}
                    </p>
                    <p class="text-neutral-600 dark:text-neutral-300 text-xs mt-0.5 flex items-center gap-1">
                      <span>{{ participant.documentId }}</span>
                      <span
                        v-if="participant.nationality"
                        class="flex items-center gap-1"
                      >
                        <span>•</span>
                        <span class="text-base">{{ getCountryFlag(participant.nationality) }}</span>
                        <span>{{ getCountryLabel(participant.nationality) }}</span>
                      </span>
                    </p>
                    <div
                      v-if="participant.pickupAddress"
                      class="flex items-start gap-1.5 mt-2 text-xs text-neutral-600 dark:text-neutral-300"
                    >
                      <UIcon
                        name="i-lucide-map-pin"
                        class="w-3.5 h-3.5 mt-0.5 flex-shrink-0"
                      />
                      <span>{{ participant.pickupAddress }}</span>
                    </div>
                    <div
                      v-if="participant.specialRequirements"
                      class="flex items-start gap-1.5 mt-2 text-xs text-neutral-600 dark:text-neutral-300"
                    >
                      <UIcon
                        name="i-lucide-info"
                        class="w-3.5 h-3.5 mt-0.5 flex-shrink-0"
                      />
                      <span>{{ participant.specialRequirements }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Total -->
          <UDivider />
          <div class="space-y-2">
            <div
              class="flex justify-between text-sm text-neutral-600 dark:text-neutral-300"
            >
              <span>{{ t('common.subtotal') }}</span>
              <span>{{ formatPrice(booking.subtotal) }}</span>
            </div>
            <div
              class="flex justify-between text-sm text-neutral-600 dark:text-neutral-300"
            >
              <span>{{ t('common.tax') }} (19%)</span>
              <span>{{ formatPrice(booking.taxAmount) }}</span>
            </div>
            <div
              class="flex justify-between text-lg font-bold text-neutral-900 dark:text-white pt-2 border-t border-neutral-200 dark:border-neutral-700"
            >
              <span>{{ t('common.total') }}</span>
              <span>{{ formatPrice(booking.totalAmount) }}</span>
            </div>
          </div>
        </div>

        <template #footer>
          <div class="flex justify-between items-center">
            <div
              class="flex items-center gap-2 text-sm text-neutral-600 dark:text-neutral-300"
            >
              <UIcon
                name="i-lucide-info"
                class="w-4 h-4"
              />
              <span>{{ t('profile.free_cancellation') }}</span>
            </div>
            <div class="flex gap-2">
              <UButton
                color="neutral"
                variant="outline"
                size="sm"
                :icon="downloadingPdf === booking.id ? 'i-lucide-loader-2' : 'i-lucide-download'"
                :loading="downloadingPdf === booking.id"
                @click="downloadBooking(booking)"
              >
                {{ t('profile.download') }}
              </UButton>
              <UButton
                v-if="booking.status === 'CONFIRMED' || booking.status === 'PENDING'"
                color="primary"
                variant="outline"
                size="sm"
                icon="i-lucide-pencil"
                @click="openEditModal(booking)"
              >
                {{ t('profile.edit') }}
              </UButton>
              <UButton
                v-if="booking.status === 'CONFIRMED' || booking.status === 'PENDING'"
                color="error"
                variant="soft"
                size="sm"
                icon="i-lucide-x"
                @click="cancelBooking(booking.id)"
              >
                {{ t('common.cancel') }}
              </UButton>
            </div>
          </div>
        </template>
      </UCard>
    </div>

    <!-- Edit Booking Modal -->
    <ProfileEditBookingModal
      v-if="editingBooking"
      :booking="editingBooking"
      @close="closeEditModal"
      @saved="handleBookingSaved"
    />
  </div>
</template>
