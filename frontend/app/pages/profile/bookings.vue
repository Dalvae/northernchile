<template>
  <div class="min-h-screen bg-neutral-50 dark:bg-neutral-900 py-12">
    <UContainer>
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-neutral-900 dark:text-white mb-2">
          {{ t('user.my_bookings') }}
        </h1>
        <p class="text-neutral-600 dark:text-neutral-400">
          {{ t('profile.bookings_description') }}
        </p>
      </div>

      <!-- Loading State -->
      <div v-if="pending" class="flex justify-center py-12">
        <UIcon name="i-lucide-loader-2" class="w-8 h-8 animate-spin text-primary" />
      </div>

      <!-- Error State -->
      <UAlert
        v-else-if="error"
        color="error"
        icon="i-lucide-alert-circle"
        :title="t('common.error')"
        :description="error.message"
      />

      <!-- Empty State -->
      <div v-else-if="!bookings || bookings.length === 0" class="text-center py-12">
        <UIcon name="i-lucide-calendar-x" class="w-16 h-16 mx-auto text-neutral-400 mb-4" />
        <h3 class="text-xl font-semibold text-neutral-900 dark:text-white mb-2">
          {{ t('profile.no_bookings') }}
        </h3>
        <p class="text-neutral-600 dark:text-neutral-400 mb-6">
          {{ t('profile.no_bookings_description') }}
        </p>
        <UButton
          color="primary"
          size="lg"
          icon="i-lucide-search"
          to="/tours"
        >
          {{ t('profile.browse_tours') }}
        </UButton>
      </div>

      <!-- Bookings List -->
      <div v-else class="grid gap-6">
        <UCard
          v-for="booking in bookings"
          :key="booking.id"
          class="hover:shadow-lg transition-shadow"
        >
          <template #content>
            <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
              <!-- Booking Info -->
              <div class="flex-1">
                <div class="flex items-center gap-3 mb-2">
                  <h3 class="text-lg font-semibold text-neutral-900 dark:text-white">
                    {{ booking.tourName }}
                  </h3>
                  <UBadge
                    :color="getStatusColor(booking.status)"
                    :label="t(`booking.status.${booking.status}`)"
                  />
                </div>

                <div class="grid md:grid-cols-2 gap-2 text-sm text-neutral-600 dark:text-neutral-400">
                  <div class="flex items-center gap-2">
                    <UIcon name="i-lucide-calendar" class="w-4 h-4" />
                    <span>{{ formatDate(booking.tourDate) }}</span>
                  </div>

                  <div class="flex items-center gap-2">
                    <UIcon name="i-lucide-users" class="w-4 h-4" />
                    <span>{{ booking.participants.length }} {{ t('booking.participants') }}</span>
                  </div>

                  <div class="flex items-center gap-2">
                    <UIcon name="i-lucide-receipt" class="w-4 h-4" />
                    <span>${{ formatPrice(booking.totalAmount) }}</span>
                  </div>

                  <div class="flex items-center gap-2">
                    <UIcon name="i-lucide-clock" class="w-4 h-4" />
                    <span>{{ t('profile.booked_on') }} {{ formatDate(booking.createdAt) }}</span>
                  </div>
                </div>
              </div>

              <!-- Actions -->
              <div class="flex flex-col gap-2">
                <UButton
                  color="primary"
                  variant="outline"
                  size="sm"
                  icon="i-lucide-eye"
                  @click="viewBookingDetails(booking.id)"
                >
                  {{ t('common.view') }}
                </UButton>

                <!-- Booking cancellation feature temporarily disabled -->
                <!-- Will be implemented after payment gateway integration -->
              </div>
            </div>

            <!-- Expandable Details -->
            <UDivider v-if="expandedBooking === booking.id" class="my-4" />

            <div v-if="expandedBooking === booking.id" class="mt-4">
              <h4 class="font-semibold text-neutral-900 dark:text-white mb-3">
                {{ t('booking.participant_details') }}
              </h4>

              <div class="grid gap-3">
                <div
                  v-for="(participant, index) in booking.participants"
                  :key="participant.id"
                  class="p-3 bg-neutral-100 dark:bg-neutral-800 rounded-lg"
                >
                  <div class="font-medium text-neutral-900 dark:text-white mb-1">
                    {{ index + 1 }}. {{ participant.fullName }}
                  </div>
                  <div class="text-sm text-neutral-600 dark:text-neutral-400 space-y-1">
                    <div>{{ t('booking.document_id') }}: {{ participant.documentId }}</div>
                    <div>{{ t('booking.nationality') }}: {{ participant.nationality }}</div>
                    <div>{{ t('booking.age') }}: {{ participant.age }}</div>
                    <div>{{ t('booking.pickup_address') }}: {{ participant.pickupAddress }}</div>
                    <div v-if="participant.specialRequirements">
                      {{ t('booking.special_requirements') }}: {{ participant.specialRequirements }}
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="booking.specialRequests" class="mt-4">
                <h4 class="font-semibold text-neutral-900 dark:text-white mb-2">
                  {{ t('booking.special_requirements') }}
                </h4>
                <p class="text-sm text-neutral-600 dark:text-neutral-400">
                  {{ booking.specialRequests }}
                </p>
              </div>
            </div>
          </template>
        </UCard>
      </div>
    </UContainer>
  </div>
</template>

<script setup lang="ts">
const { t, locale } = useI18n()
const toast = useToast()
const { formatPrice } = useCurrency()

definePageMeta({
  layout: 'default',
})

// Fetch bookings
const { data: bookings, pending, error, refresh } = await useFetch('/api/bookings', {
  credentials: 'include'
})

// State
const expandedBooking = ref<string | null>(null)

// View booking details (toggle expand)
function viewBookingDetails(bookingId: string) {
  expandedBooking.value = expandedBooking.value === bookingId ? null : bookingId
}

// Cancel booking
async function cancelBooking(bookingId: string) {
  const confirmed = confirm(t('profile.cancel_booking_confirm'))
  if (!confirmed) return

  try {
    await $fetch(`/api/bookings/${bookingId}/cancel`, {
      method: 'PUT',
      credentials: 'include'
    })

    toast.add({
      title: t('common.success'),
      description: t('profile.booking_cancelled_success'),
      color: 'success'
    })

    refresh()
  } catch (error: any) {
    toast.add({
      title: t('common.error'),
      description: error.data?.message || t('profile.booking_cancel_error'),
      color: 'error'
    })
  }
}

// Check if booking can be cancelled (24h before tour)
function canCancelBooking(booking: any): boolean {
  if (booking.status !== 'CONFIRMED') return false

  const tourDate = new Date(booking.tourDate)
  const now = new Date()
  const hoursUntilTour = (tourDate.getTime() - now.getTime()) / (1000 * 60 * 60)

  return hoursUntilTour > 24
}

// Get status badge color
function getStatusColor(status: string): string {
  const colors: Record<string, string> = {
    PENDING: 'warning',
    CONFIRMED: 'success',
    CANCELLED: 'error',
    COMPLETED: 'info'
  }
  return colors[status] || 'neutral'
}

// Format date
function formatDate(dateString: string): string {
  const date = new Date(dateString)
  return date.toLocaleDateString(locale.value, {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}
</script>
